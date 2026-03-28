package com.seckill.product.controller;

import com.seckill.product.config.JwtConfig;
import com.seckill.product.entity.SeckillActivity;
import com.seckill.product.entity.SeckillProduct;
import com.seckill.product.repository.SeckillActivityRepository;
import com.seckill.product.repository.SeckillProductRepository;
import com.seckill.product.service.SeckillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillActivityRepository activityRepository;
    private final SeckillProductRepository productRepository;
    private final SeckillService seckillService;
    private final JwtConfig jwtConfig;

    /**
     * 已上线的秒杀活动列表（首页）
     */
    @GetMapping("/activities")
    public List<ActivityVO> listActivities() {
        List<SeckillActivity> activities = activityRepository.findByOnlineTrueOrderByStartTimeAsc();
        List<Long> productIds = activities.stream().map(SeckillActivity::getProductId).distinct().toList();
        Map<Long, SeckillProduct> productMap = productRepository.findAllById(productIds).stream()
            .collect(Collectors.toMap(SeckillProduct::getId, p -> p));
        return activities.stream()
            .map(a -> {
                SeckillProduct p = productMap.get(a.getProductId());
                Integer remain = seckillService.getStockRemain(a.getId());
                return new ActivityVO(
                    a.getId(),
                    a.getProductId(),
                    p != null ? p.getName() : "",
                    p != null ? p.getImageUrl() : null,
                    p != null ? p.getOriginalPrice().toString() : null,
                    p != null ? p.getSeckillPrice().toString() : null,
                    a.getStartTime().toString(),
                    a.getEndTime().toString(),
                    remain != null ? remain : a.getTotalStock()
                );
            })
            .toList();
    }

    /**
     * 立即抢购（用户 ID 从 JWT 解析，防篡改）
     */
    @PostMapping("/seckill/{activityId}")
    public Map<String, String> seckill(@PathVariable Long activityId,
                                       @RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
        }
        Long userId = jwtConfig.getUserIdFromToken(auth);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "登录已过期，请重新登录");
        }
        seckillService.seckill(activityId, userId, auth);
        return Map.of("message", "抢购成功，请到订单列表查看");
    }

    public record ActivityVO(Long activityId, Long productId, String productName, String imageUrl,
                            String originalPrice, String seckillPrice, String startTime, String endTime, Integer stockRemain) {}
}
