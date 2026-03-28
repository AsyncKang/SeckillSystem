package com.seckill.order.listener;

import com.seckill.order.config.RocketMqConstants;
import com.seckill.order.dto.OrderMessage;
import com.seckill.order.entity.SeckillOrder;
import com.seckill.order.repository.SeckillOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
@RocketMQMessageListener(
    topic = RocketMqConstants.ORDER_TOPIC,
    consumerGroup = RocketMqConstants.ORDER_CONSUMER_GROUP
)
public class OrderMessageListener implements RocketMQListener<OrderMessage> {

    private final SeckillOrderRepository orderRepository;

    @Transactional
    @Override
    public void onMessage(OrderMessage msg) {
        try {
            SeckillOrder order = new SeckillOrder();
            order.setUserId(msg.getUserId());
            order.setActivityId(msg.getActivityId());
            order.setProductId(msg.getProductId());
            order.setProductName(msg.getProductName());
            order.setPrice(new BigDecimal(msg.getPrice()));
            order.setStatus(0);
            orderRepository.save(order);
            log.info("订单已生成: userId={}, activityId={}, productName={}", msg.getUserId(), msg.getActivityId(), msg.getProductName());
        } catch (Exception e) {
            log.error("订单处理失败（RocketMQ 将按配置自动重试/进入死信）: {}", msg, e);
            throw new RuntimeException(e);
        }
    }
}
