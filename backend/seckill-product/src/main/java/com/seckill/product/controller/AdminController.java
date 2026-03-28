package com.seckill.product.controller;

import com.seckill.product.config.JwtConfig;
import com.seckill.product.entity.SeckillActivity;
import com.seckill.product.entity.SeckillProduct;
import com.seckill.product.repository.SeckillActivityRepository;
import com.seckill.product.repository.SeckillProductRepository;
import com.seckill.product.service.SeckillService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/product/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SeckillProductRepository productRepository;
    private final SeckillActivityRepository activityRepository;
    private final SeckillService seckillService;
    private final JwtConfig jwtConfig;

    private void requireAdmin(String auth) {
        if (auth == null || !auth.startsWith("Bearer ") || !jwtConfig.isAdmin(auth)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "需要管理员权限");
        }
    }

    @PostMapping("/product")
    public ProductVO createProduct(@RequestHeader(value = "Authorization", required = false) String auth,
                                   @Valid @RequestBody ProductCreateRequest req) {
        requireAdmin(auth);
        SeckillProduct p = new SeckillProduct();
        p.setName(req.getName());
        p.setImageUrl(req.getImageUrl());
        p.setOriginalPrice(new BigDecimal(req.getOriginalPrice()));
        p.setSeckillPrice(new BigDecimal(req.getSeckillPrice()));
        SeckillProduct saved = productRepository.save(p);
        return new ProductVO(saved.getId(), saved.getName(), saved.getImageUrl(),
            saved.getOriginalPrice().toString(), saved.getSeckillPrice().toString());
    }

    @PostMapping("/activity")
    public ActivityVO createActivity(@RequestHeader(value = "Authorization", required = false) String auth,
                                     @Valid @RequestBody ActivityCreateRequest req) {
        requireAdmin(auth);
        if (!productRepository.existsById(req.getProductId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品不存在");
        }
        SeckillActivity a = new SeckillActivity();
        a.setProductId(req.getProductId());
        a.setTotalStock(req.getTotalStock());
        a.setStartTime(Instant.parse(req.getStartTime()));
        a.setEndTime(Instant.parse(req.getEndTime()));
        a.setOnline(false);
        SeckillActivity saved = activityRepository.save(a);
        return toVO(saved);
    }

    @PutMapping("/activity/{id}/online")
    public void online(@RequestHeader(value = "Authorization", required = false) String auth,
                       @PathVariable Long id) {
        requireAdmin(auth);
        SeckillActivity a = activityRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));
        a.setOnline(true);
        activityRepository.save(a);
        seckillService.warmCache(id);
    }

    @GetMapping("/activities")
    public List<ActivityVO> listAllActivities(@RequestHeader(value = "Authorization", required = false) String auth) {
        requireAdmin(auth);
        return activityRepository.findAll().stream().map(this::toVO).toList();
    }

    private ActivityVO toVO(SeckillActivity a) {
        return new ActivityVO(a.getId(), a.getProductId(), a.getTotalStock(),
            a.getStartTime().toString(), a.getEndTime().toString(), a.getOnline());
    }

    public record ProductVO(Long id, String name, String imageUrl, String originalPrice, String seckillPrice) {}
    public record ActivityVO(Long id, Long productId, Integer totalStock, String startTime, String endTime, Boolean online) {}

    @Data
    public static class ProductCreateRequest {
        @NotNull
        private String name;
        private String imageUrl;
        @NotNull
        private String originalPrice;
        @NotNull
        private String seckillPrice;
    }

    @Data
    public static class ActivityCreateRequest {
        @NotNull
        private Long productId;
        @NotNull
        private Integer totalStock;
        @NotNull
        private String startTime;
        @NotNull
        private String endTime;
    }
}
