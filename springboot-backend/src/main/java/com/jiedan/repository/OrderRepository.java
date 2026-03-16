package com.jiedan.repository;

import com.jiedan.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Order> findByRequirementId(Long requirementId);

    List<Order> findByUserIdAndStatusNotIn(Long userId, List<String> statuses);

    // 统计指定时间后创建的订单数
    long countByCreatedAtAfter(LocalDateTime dateTime);

    // 获取最近的5条订单
    List<Order> findTop5ByOrderByCreatedAtDesc();
}
