package com.duck.repository;

import com.duck.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {
    Optional<Option> findByOptionNameAndProduct_ProductId(String optionName, Long productId);
}
