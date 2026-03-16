package com.jiedan.repository;

import com.jiedan.entity.PointRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointRecordRepository extends JpaRepository<PointRecord, Long> {

    List<PointRecord> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<PointRecord> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
