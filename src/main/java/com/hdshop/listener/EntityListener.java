package com.hdshop.listener;

import com.hdshop.entity.BaseEntity;
import com.hdshop.entity.Category;
import com.hdshop.entity.Order;
import com.hdshop.entity.Product;
import com.hdshop.service.redis.RedisService;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@SuppressWarnings("unchecked") // Suppress unchecked cast warning
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public class EntityListener<T extends BaseEntity> {
    private final Logger logger = LoggerFactory.getLogger(EntityListener.class);
    private RedisService<Product> redisProductService;
    private RedisService<Order> redisOrderService;
    private RedisService<Category> redisCateService;

    @Autowired
    public EntityListener(RedisService<Product> redisProductService, RedisService<Order> redisOrderService, RedisService<Category> redisCateService) {
        this.redisProductService = redisProductService;
        this.redisOrderService = redisOrderService;
        this.redisCateService = redisCateService;
    }

    @PrePersist
    public void prePersist(T entity) {
        logger.info("PrePersist for " + entity.getClass().getSimpleName());
    }

    @PostPersist
    public void postPersist(T entity) {
        logger.info("PostPersist for " + entity.getClass().getSimpleName());
        clearCache(entity);
    }

    @PreUpdate
    public void preUpdate(T entity) {
        logger.info("PreUpdate for " + entity.getClass().getSimpleName());
    }

    @PostUpdate
    public void postUpdate(T entity) {
        logger.info("PostUpdate for " + entity.getClass().getSimpleName());
        clearCache(entity);
    }

    @PreRemove
    public void preRemove(T entity) {
        logger.info("PreRemove for " + entity.getClass().getSimpleName());
    }

    @PostRemove
    public void postRemove(T entity) {
        logger.info("PostRemove for " + entity.getClass().getSimpleName());
        clearCache(entity);
    }

    private void clearCache(T entity) {
        if (entity instanceof Product) {
            redisProductService.clearCache((Product) entity);
        } else if (entity instanceof Order) {
            redisOrderService.clearCache((Order) entity);
        } else if (entity instanceof Category) {
            redisCateService.clearCache((Category) entity);
        }
    }
}