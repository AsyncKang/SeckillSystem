package com.seckill.product.service;

import com.seckill.product.config.RocketMqConstants;
import com.seckill.product.dto.OrderMessage;
import com.seckill.product.entity.SeckillActivity;
import com.seckill.product.entity.SeckillProduct;
import com.seckill.product.repository.SeckillActivityRepository;
import com.seckill.product.repository.SeckillProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeckillService {

    private static final String STOCK_KEY = "seckill:stock:";
    private static final String USER_ACTIVITY_KEY = "seckill:user:activity:";
    private static final String LOCK_KEY_PREFIX = "seckill:lock:";
    private static final long USER_ACTIVITY_EXPIRE_SECONDS = 7 * 24 * 3600L;

    private final SeckillActivityRepository activityRepository;
    private final SeckillProductRepository productRepository;
    private final StringRedisTemplate redis;
    private final RedissonClient redissonClient;
    private final RocketMQTemplate rocketMQTemplate;

    /**
     * 活动上线时预热：将库存写入 Redis
     */
    @Transactional
    public void warmCache(Long activityId) {
        SeckillActivity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));
        String key = STOCK_KEY + activityId;
        redis.opsForValue().set(key, String.valueOf(activity.getTotalStock()));
        log.info("活动 {} 库存已预热: {}", activityId, activity.getTotalStock());
    }

    /**
     * 秒杀：三层校验 + Redis 扣减 + 发 MQ
     */
    public void seckill(Long activityId, Long userId, String token) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
        }

        SeckillActivity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));
        if (!Boolean.TRUE.equals(activity.getOnline())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动未上线");
        }
        Instant now = Instant.now();
        if (now.isBefore(activity.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动未开始");
        }
        if (now.isAfter(activity.getEndTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动已结束");
        }

        String stockKey = STOCK_KEY + activityId;
        String userActivityKey = USER_ACTIVITY_KEY + activityId + ":" + userId;

        // 一人一单：使用 Redis SETNX + 过期时间
        Boolean first = redis.opsForValue().setIfAbsent(
            userActivityKey,
            "1",
            USER_ACTIVITY_EXPIRE_SECONDS,
            TimeUnit.SECONDS
        );
        if (Boolean.FALSE.equals(first)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "您已参与过该商品抢购");
        }

        RLock lock = redissonClient.getLock(LOCK_KEY_PREFIX + activityId);
        boolean locked = false;
        try {
            // 看门狗模式：只设置等待时间 3 秒，锁持有期间由 Redisson 自动续期
            locked = lock.tryLock(3, TimeUnit.SECONDS);
            if (!locked) {
                // 获取锁失败，视为高并发下的限流
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "抢购人数过多，请稍后再试");
            }

            String stockStr = redis.opsForValue().get(stockKey);
            long stock = stockStr == null ? 0L : Long.parseLong(stockStr);
            if (stock <= 0) {
                // 库存不足，撤销一人一单标记
                redis.delete(userActivityKey);
                throw new ResponseStatusException(HttpStatus.CONFLICT, "库存不足");
            }

            Long remain = redis.opsForValue().decrement(stockKey);
            if (remain != null && remain < 0) {
                // 理论上不会到这里，但防御性处理
                redis.opsForValue().increment(stockKey);
                redis.delete(userActivityKey);
                throw new ResponseStatusException(HttpStatus.CONFLICT, "库存不足");
            }

            SeckillProduct product = productRepository.findById(activity.getProductId())
                .orElseThrow();
            OrderMessage msg = new OrderMessage(userId, activityId, activity.getProductId(),
                product.getName(), product.getSeckillPrice().toString(), token);
            rocketMQTemplate.convertAndSend(RocketMqConstants.ORDER_DESTINATION_CREATE, msg);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "系统繁忙，请稍后再试");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public Integer getStockRemain(Long activityId) {
        String key = STOCK_KEY + activityId;
        String v = redis.opsForValue().get(key);
        return v == null ? null : Integer.parseInt(v);
    }
}
