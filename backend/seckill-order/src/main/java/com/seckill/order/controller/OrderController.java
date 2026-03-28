package com.seckill.order.controller;

import com.seckill.order.config.JwtConfig;
import com.seckill.order.entity.SeckillOrder;
import com.seckill.order.repository.SeckillOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final SeckillOrderRepository orderRepository;
    private final JwtConfig jwtConfig;

    /**
     * 当前用户订单列表（用户 ID 从 JWT 解析）
     */
    @GetMapping("/my")
    public List<OrderVO> myOrders(@RequestHeader(value = "Authorization", required = false) String auth) {
        Long userId = jwtConfig.getUserIdFromToken(auth);
        List<SeckillOrder> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream()
            .map(o -> new OrderVO(o.getId(), o.getProductName(), o.getPrice().toString(), o.getStatus(), o.getCreatedAt().toString()))
            .toList();
    }

    public record OrderVO(Long id, String productName, String price, Integer status, String createdAt) {}
}
