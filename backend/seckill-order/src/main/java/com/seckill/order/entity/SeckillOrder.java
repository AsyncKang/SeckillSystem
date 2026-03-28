package com.seckill.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "seckill_orders", indexes = @Index(unique = true, columnList = "userId, activityId"))
public class SeckillOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long activityId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, length = 128)
    private String productName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer status = 0; // 0 待支付 1 已支付 2 已取消

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
