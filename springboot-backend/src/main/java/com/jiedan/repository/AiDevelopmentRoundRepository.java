package com.jiedan.repository;

import com.jiedan.entity.AiDevelopmentRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiDevelopmentRoundRepository extends JpaRepository<AiDevelopmentRound, Long> {

    List<AiDevelopmentRound> findByPhaseIdOrderByRoundNumberAsc(Long phaseId);

    Optional<AiDevelopmentRound> findByPhaseIdAndRoundNumber(Long phaseId, Integer roundNumber);

    void deleteByPhaseId(Long phaseId);
}
