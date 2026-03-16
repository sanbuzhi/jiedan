package com.jiedan.repository;

import com.jiedan.entity.AiValidationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AI验证记录Repository
 */
@Repository
public interface AiValidationRecordRepository extends JpaRepository<AiValidationRecord, Long> {

    /**
     * 根据项目ID查询验证记录
     */
    List<AiValidationRecord> findByProjectId(String projectId);

    /**
     * 根据项目ID和任务ID查询验证记录
     */
    List<AiValidationRecord> findByProjectIdAndTaskId(String projectId, String taskId);

    /**
     * 根据决策类型查询验证记录
     */
    List<AiValidationRecord> findByDecision(String decision);
}
