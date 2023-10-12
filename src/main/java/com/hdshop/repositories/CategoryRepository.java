package com.hdshop.repositories;

import com.hdshop.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Boolean existsCategoryByName(String name);

    Boolean existsCategoryBySlug(String slug);

    Boolean existsCategoryByParentId(Long parentId);

    Boolean existsCategoryById(Long id);

    Optional<Category> findBySlug(String slug);
}
