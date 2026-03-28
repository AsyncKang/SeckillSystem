package com.seckill.product.repository;

import com.seckill.product.entity.SeckillActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeckillActivityRepository extends JpaRepository<SeckillActivity, Long> {

    List<SeckillActivity> findByOnlineTrueOrderByStartTimeAsc();
}
