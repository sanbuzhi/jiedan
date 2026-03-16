package com.jiedan.repository;

import com.jiedan.entity.ExchangeOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeOrderRepository extends JpaRepository<ExchangeOrder, Long> {

    Page<ExchangeOrder> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<ExchangeOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<ExchangeOrder> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndStatus(Long userId, ExchangeOrder.ExchangeOrderStatus status);
}
