package com.jiedan.repository;

import com.jiedan.entity.CodeStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 代码风格Repository
 */
@Repository
public interface CodeStyleRepository extends JpaRepository<CodeStyle, Long> {

    /**
     * 根据项目ID查询代码风格
     */
    Optional<CodeStyle> findByProjectId(String projectId);
}
