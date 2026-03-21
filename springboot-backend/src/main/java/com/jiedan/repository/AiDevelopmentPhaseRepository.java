package com.jiedan.repository;

import com.jiedan.entity.AiDevelopmentPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiDevelopmentPhaseRepository extends JpaRepository<AiDevelopmentPhase, Long> {

    List<AiDevelopmentPhase> findByProjectIdOrderByPhaseAsc(String projectId);

    Optional<AiDevelopmentPhase> findByProjectIdAndPhase(String projectId, Integer phase);

    void deleteByProjectId(String projectId);
}
