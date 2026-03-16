-- ============================================
-- 添加需求详细字段（支持step_all页面）
-- ============================================

-- 添加 selected_functions 字段（JSON类型）
SET @exist := (SELECT COUNT(*) FROM information_schema.columns 
               WHERE table_name = 'requirements' 
               AND column_name = 'selected_functions' 
               AND table_schema = DATABASE());
               
SET @sql := IF(@exist = 0, 
    'ALTER TABLE requirements ADD COLUMN selected_functions JSON NULL COMMENT "选中的功能点ID列表" AFTER requirement_description',
    'SELECT "Column selected_functions already exists"');
    
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 materials 字段（JSON类型）
SET @exist := (SELECT COUNT(*) FROM information_schema.columns 
               WHERE table_name = 'requirements' 
               AND column_name = 'materials' 
               AND table_schema = DATABASE());
               
SET @sql := IF(@exist = 0, 
    'ALTER TABLE requirements ADD COLUMN materials JSON NULL COMMENT "需求材料（参考图片、源码包等）" AFTER selected_functions',
    'SELECT "Column materials already exists"');
    
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 deployment_mode 字段
SET @exist := (SELECT COUNT(*) FROM information_schema.columns 
               WHERE table_name = 'requirements' 
               AND column_name = 'deployment_mode' 
               AND table_schema = DATABASE());
               
SET @sql := IF(@exist = 0, 
    'ALTER TABLE requirements ADD COLUMN deployment_mode VARCHAR(50) NULL COMMENT "部署模式：cloud-云托管, local-本地部署, none-不需要" AFTER materials',
    'SELECT "Column deployment_mode already exists"');
    
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 ai_quotation_result 字段（TEXT类型存储JSON字符串）
SET @exist := (SELECT COUNT(*) FROM information_schema.columns 
               WHERE table_name = 'requirements' 
               AND column_name = 'ai_quotation_result' 
               AND table_schema = DATABASE());
               
SET @sql := IF(@exist = 0, 
    'ALTER TABLE requirements ADD COLUMN ai_quotation_result TEXT NULL COMMENT "AI估价结果JSON" AFTER deployment_mode',
    'SELECT "Column ai_quotation_result already exists"');
    
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 ai_quotation_status 字段
SET @exist := (SELECT COUNT(*) FROM information_schema.columns 
               WHERE table_name = 'requirements' 
               AND column_name = 'ai_quotation_status' 
               AND table_schema = DATABASE());
               
SET @sql := IF(@exist = 0, 
    'ALTER TABLE requirements ADD COLUMN ai_quotation_status VARCHAR(20) NULL COMMENT "AI估价状态：PENDING/COMPLETED/FAILED" AFTER ai_quotation_result',
    'SELECT "Column ai_quotation_status already exists"');
    
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT 'Requirements table fields updated successfully' AS result;
