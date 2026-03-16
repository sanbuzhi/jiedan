package com.jiedan.repository;

import com.jiedan.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(String phone);

    Optional<User> findByWxOpenid(String wxOpenid);

    Optional<User> findByReferralCode(String referralCode);

    boolean existsByPhone(String phone);

    boolean existsByReferralCode(String referralCode);

    // 根据推荐人ID查询被推荐用户列表
    List<User> findByReferrerId(Long referrerId);

    // 分页查询被推荐用户列表
    Page<User> findByReferrerId(Long referrerId, Pageable pageable);

    // 统计推荐人数
    int countByReferrerId(Long referrerId);

    // 统计指定时间后注册的用户数
    long countByCreatedAtAfter(LocalDateTime dateTime);
}
