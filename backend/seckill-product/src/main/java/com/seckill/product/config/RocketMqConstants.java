package com.seckill.product.config;

public final class RocketMqConstants {

    private RocketMqConstants() {}

    public static final String ORDER_TOPIC = "seckill-order-topic";
    public static final String ORDER_TAG_CREATE = "create";
    public static final String ORDER_DESTINATION_CREATE = ORDER_TOPIC + ":" + ORDER_TAG_CREATE;
}

