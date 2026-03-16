package com.jiedan.repository;

import com.jiedan.entity.SystemRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemRoleRepository extends JpaRepository<SystemRole, Long> {

    @Query("SELECT sr FROM SystemRole sr LEFT JOIN FETCH sr.functions WHERE sr.template.id = :templateId ORDER BY sr.sortOrder ASC")
    List<SystemRole> findByTemplateIdWithFunctions(@Param("templateId") Long templateId);

    @Query("SELECT sr FROM SystemRole sr WHERE sr.template.id = :templateId ORDER BY sr.sortOrder ASC")
    List<SystemRole> findByTemplateIdOrderBySortOrderAsc(@Param("templateId") Long templateId);
}
