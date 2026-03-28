package com.seckill.order.repository;

import com.seckill.order.entity.SeckillOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeckillOrderRepository extends JpaRepository<SeckillOrder, Long> {

    List<SeckillOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
}
