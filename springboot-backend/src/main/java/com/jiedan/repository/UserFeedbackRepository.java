package com.jiedan.repository;

import com.jiedan.entity.UserFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户反馈Repository
 */
@Repository
public interface UserFeedbackRepository extends JpaRepository<UserFeedback, String> {

    /**
     * 根据项目ID查询所有反馈
     */
    List<UserFeedback> findByProjectId(String projectId);

    /**
     * 根据项目ID和任务ID查询反馈
     */
    List<UserFeedback> findByProjectIdAndTaskId(String projectId, String taskId);

    /**
     * 根据项目ID和状态查询反馈
     */
    List<UserFeedback> findByProjectIdAndStatus(String projectId, String status);

    /**
     * 根据状态查询所有待处理反馈
     */
    List<UserFeedback> findByStatusIn(List<String> statuses);
}
