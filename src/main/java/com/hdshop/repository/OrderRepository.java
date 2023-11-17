package com.hdshop.repository;

import com.hdshop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> getOrdersByUser_UsernameOrderByCreatedDateDesc(String username);

    List<Order> getOrdersByUserIdOrderByCreatedDateDesc(Long userId);

    List<Order> findAllByUser_UsernameOrderByCreatedDateDesc(String username);

    Optional<Order> findByVnpTxnRef(String vnp_TxnRef);
}
