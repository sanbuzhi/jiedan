package com.jiedan.repository;

import com.jiedan.entity.CodeContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 代码上下文Repository
 */
@Repository
public interface CodeContextRepository extends JpaRepository<CodeContext, Long> {

    /**
     * 根据项目ID查询所有代码上下文
     */
    List<CodeContext> findByProjectId(String projectId);

    /**
     * 根据项目ID和任务ID查询
     */
    Optional<CodeContext> findByProjectIdAndTaskId(String projectId, String taskId);
}
