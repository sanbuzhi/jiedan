package com.jiedan.repository;

import com.jiedan.entity.SystemFunction;
import com.jiedan.entity.enums.ComplexityLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemFunctionRepository extends JpaRepository<SystemFunction, Long> {

    List<SystemFunction> findByRoleId(Long roleId);

    List<SystemFunction> findByRoleIdOrderByPriorityAsc(Long roleId);

    Optional<SystemFunction> findByCode(String code);

    Optional<SystemFunction> findByRoleIdAndCode(Long roleId, String code);

    List<SystemFunction> findByComplexity(ComplexityLevel complexity);
}
