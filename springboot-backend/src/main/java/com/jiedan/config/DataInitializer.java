package com.jiedan.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据初始化器
 * 应用启动时自动执行 data.sql 脚本初始化测试数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始检查并初始化数据库...");
        
        try {
            // 第一步：执行建表脚本
            executeSchemaScript();
            
            // 第二步：检查是否已有数据
            boolean hasRules = checkTableHasData("rules");
            boolean hasExchangeItems = checkTableHasData("exchange_items");
            
            if (!hasRules || !hasExchangeItems) {
                log.info("检测到数据缺失，开始执行数据初始化脚本...");
                executeDataScript();
                log.info("数据初始化完成！");
            } else {
                log.info("数据已存在，跳过初始化");
            }
            
            // 验证用户1的积分数据
            verifyUserData();
            
        } catch (Exception e) {
            log.error("数据初始化失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 执行建表脚本
     */
    private void executeSchemaScript() throws SQLException {
        Resource resource = new ClassPathResource("schema.sql");
        if (!resource.exists()) {
            log.warn("建表脚本 schema.sql 不存在");
            return;
        }
        
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, resource);
            log.info("成功执行建表脚本");
        } catch (Exception e) {
            log.warn("执行建表脚本时出错（表可能已存在）: {}", e.getMessage());
        }
    }
    
    /**
     * 检查表是否有数据
     */
    private boolean checkTableHasData(String tableName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName, 
                Integer.class
            );
            return count != null && count > 0;
        } catch (Exception e) {
            log.warn("检查表 {} 数据时出错: {}", tableName, e.getMessage());
            return false;
        }
    }
    
    /**
     * 执行数据初始化脚本
     */
    private void executeDataScript() throws SQLException {
        // 优先使用完整的初始化脚本
        Resource resource = new ClassPathResource("init-data.sql");
        if (!resource.exists()) {
            // 回退到旧的数据脚本
            resource = new ClassPathResource("data.sql");
        }
        
        if (!resource.exists()) {
            log.warn("数据初始化脚本不存在");
            return;
        }
        
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, resource);
            log.info("成功执行数据初始化脚本: {}", resource.getFilename());
        }
    }
    
    /**
     * 验证用户1的数据
     */
    private void verifyUserData() {
        try {
            // 查询用户1的积分
            Integer totalPoints = jdbcTemplate.queryForObject(
                "SELECT total_points FROM users WHERE id = 1",
                Integer.class
            );
            
            // 查询积分记录数
            Integer recordCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM point_records WHERE user_id = 1",
                Integer.class
            );
            
            // 查询推荐关系数（使用 users.referrer_id 字段）
            Integer level1Count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE referrer_id = 1",
                Integer.class
            );
            
            log.info("用户1数据验证: 总积分={}, 积分记录数={}, 一级推荐数={}", 
                totalPoints, recordCount, level1Count);
            
        } catch (Exception e) {
            log.warn("验证用户1数据时出错: {}", e.getMessage());
        }
    }
}
