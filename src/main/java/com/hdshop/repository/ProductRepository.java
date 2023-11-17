package com.hdshop.repository;

import com.hdshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Boolean existsProductByName(String name);

    Boolean existsProductBySlug(String generatedSlug);

    Page<Product> findAllByIsActiveIsTrueaAndOrderByProductIdDesc(Pageable pageable);
}
