package com.jiedan.repository;

import com.jiedan.entity.AiStrategyConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AI策略配置Repository
 */
@Repository
public interface AiStrategyConfigRepository extends JpaRepository<AiStrategyConfig, Long> {

    /**
     * 根据接口代码查询配置
     *
     * @param apiCode 接口代码
     * @return 配置对象
     */
    Optional<AiStrategyConfig> findByApiCode(String apiCode);

    /**
     * 查询所有启用的配置，按排序顺序
     *
     * @return 配置列表
     */
    List<AiStrategyConfig> findAllByEnabledTrueOrderBySortOrderAsc();

    /**
     * 根据接口代码删除配置
     *
     * @param apiCode 接口代码
     */
    void deleteByApiCode(String apiCode);

    /**
     * 检查接口代码是否存在
     *
     * @param apiCode 接口代码
     * @return 是否存在
     */
    boolean existsByApiCode(String apiCode);
}
