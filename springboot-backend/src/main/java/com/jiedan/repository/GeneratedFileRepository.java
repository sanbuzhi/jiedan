package com.jiedan.repository;

import com.jiedan.entity.GeneratedFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 生成的代码文件Repository
 */
@Repository
public interface GeneratedFileRepository extends JpaRepository<GeneratedFileEntity, Long> {

    /**
     * 根据项目ID查询所有文件
     */
    List<GeneratedFileEntity> findByProjectId(String projectId);

    /**
     * 根据项目ID和任务ID查询
     */
    List<GeneratedFileEntity> findByProjectIdAndTaskId(String projectId, String taskId);
}
