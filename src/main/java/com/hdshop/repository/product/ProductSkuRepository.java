package com.hdshop.repository.product;

import com.hdshop.entity.product.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSkuRepository extends JpaRepository<ProductSku, Long> {
}
