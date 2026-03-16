package com.jiedan.repository;

import com.jiedan.entity.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 项目状态Repository
 */
@Repository
public interface ProjectStatusRepository extends JpaRepository<ProjectStatus, String> {

    /**
     * 根据项目ID查询
     */
    Optional<ProjectStatus> findByProjectId(String projectId);
}
