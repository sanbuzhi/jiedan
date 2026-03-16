package com.jiedan.repository;

import com.jiedan.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 任务状态Repository
 */
@Repository
public interface TaskStatusRepository extends JpaRepository<TaskStatus, Long> {

    /**
     * 根据项目ID查询所有任务
     */
    List<TaskStatus> findByProjectId(String projectId);

    /**
     * 根据项目ID和任务ID查询
     */
    Optional<TaskStatus> findByProjectIdAndTaskId(String projectId, String taskId);

    /**
     * 根据项目ID和状态查询
     */
    List<TaskStatus> findByProjectIdAndState(String projectId, String state);

    /**
     * 根据项目ID查询未完成的任务（PENDING或IN_PROGRESS）
     */
    List<TaskStatus> findByProjectIdAndStateIn(String projectId, List<String> states);
}
