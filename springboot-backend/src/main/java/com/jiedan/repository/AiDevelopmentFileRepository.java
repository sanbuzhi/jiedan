package com.jiedan.repository;

import com.jiedan.entity.AiDevelopmentFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiDevelopmentFileRepository extends JpaRepository<AiDevelopmentFile, Long> {

    List<AiDevelopmentFile> findByProjectIdAndPhaseOrderByRoundNumberAsc(String projectId, Integer phase);

    List<AiDevelopmentFile> findByProjectIdAndPhaseAndRoundNumber(String projectId, Integer phase, Integer roundNumber);

    void deleteByProjectId(String projectId);
}
