package com.jiedan.repository;

import com.jiedan.entity.AiDevelopmentProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiDevelopmentProjectRepository extends JpaRepository<AiDevelopmentProject, Long> {

    Optional<AiDevelopmentProject> findByProjectId(String projectId);

    boolean existsByProjectId(String projectId);

    void deleteByProjectId(String projectId);
}
