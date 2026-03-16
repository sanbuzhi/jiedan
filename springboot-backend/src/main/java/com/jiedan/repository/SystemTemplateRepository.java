package com.jiedan.repository;

import com.jiedan.entity.SystemTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemTemplateRepository extends JpaRepository<SystemTemplate, Long> {

    @Query("SELECT st FROM SystemTemplate st LEFT JOIN FETCH st.roles WHERE st.id = :id")
    Optional<SystemTemplate> findByIdWithRoles(@Param("id") Long id);

    @Query("SELECT st FROM SystemTemplate st WHERE st.isActive = true AND " +
           "(st.keywords LIKE %:keyword% OR st.name LIKE %:keyword% OR st.description LIKE %:keyword%)")
    List<SystemTemplate> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT st FROM SystemTemplate st WHERE st.isActive = true ORDER BY st.createdAt DESC")
    List<SystemTemplate> findActiveTemplatesOrderByCreatedAtDesc();

    Optional<SystemTemplate> findByCode(String code);
}
