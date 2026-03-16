package com.jiedan.repository;

import com.jiedan.entity.ExchangeItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangeItemRepository extends JpaRepository<ExchangeItem, Long> {

    List<ExchangeItem> findByIsActiveTrue();

    Page<ExchangeItem> findByIsActiveTrue(Pageable pageable);

    List<ExchangeItem> findByIsActiveTrueAndStockGreaterThan(Integer stock);
}
