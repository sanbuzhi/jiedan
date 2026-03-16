-- 修改 requirements 表字段约束，允许更多字段为 null
-- 与 DTO 验证规则保持一致

-- 修改 urgency 字段为可空
ALTER TABLE requirements MODIFY COLUMN urgency VARCHAR(50) NULL COMMENT '紧急程度';

-- 修改 delivery_period 字段为可空
ALTER TABLE requirements MODIFY COLUMN delivery_period INT NULL COMMENT '交付周期';

-- 修改 ui_style 字段为可空
ALTER TABLE requirements MODIFY COLUMN ui_style VARCHAR(200) NULL COMMENT 'UI风格';

-- 添加 requirement_description 字段（如果不存在）
SET @exist := (SELECT COUNT(*) FROM information_schema.columns 
               WHERE table_name = 'requirements' 
               AND column_name = 'requirement_description' 
               AND table_schema = DATABASE());
               
SET @sql := IF(@exist = 0, 
    'ALTER TABLE requirements ADD COLUMN requirement_description TEXT NULL COMMENT "需求描述" AFTER ui_style',
    'SELECT "Column requirement_description already exists"');
    
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
