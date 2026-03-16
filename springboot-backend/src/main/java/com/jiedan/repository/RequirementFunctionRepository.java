package com.jiedan.repository;

import com.jiedan.entity.RequirementFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequirementFunctionRepository extends JpaRepository<RequirementFunction, Long> {

    List<RequirementFunction> findByRequirementId(Long requirementId);

    List<RequirementFunction> findByRequirementIdAndIsCustom(Long requirementId, Boolean isCustom);

    List<RequirementFunction> findByFunctionId(Long functionId);

    void deleteByRequirementId(Long requirementId);
}
