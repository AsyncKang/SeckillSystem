package com.seckill.product.repository;

import com.seckill.product.entity.SeckillProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeckillProductRepository extends JpaRepository<SeckillProduct, Long> {
}
