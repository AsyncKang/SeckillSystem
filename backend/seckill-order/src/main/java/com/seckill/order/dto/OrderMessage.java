package com.seckill.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessage {

    private Long userId;
    private Long activityId;
    private Long productId;
    private String productName;
    private String price;
    private String token;
}
