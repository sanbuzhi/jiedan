package com.jiedan.repository;

import com.jiedan.entity.Requirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RequirementRepository extends JpaRepository<Requirement, Long> {

    List<Requirement> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Requirement> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Requirement> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    Page<Requirement> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status, Pageable pageable);

    // 统计指定时间后创建的需求数
    long countByCreatedAtAfter(LocalDateTime dateTime);

    // 获取最近的5条需求
    List<Requirement> findTop5ByOrderByCreatedAtDesc();
}
