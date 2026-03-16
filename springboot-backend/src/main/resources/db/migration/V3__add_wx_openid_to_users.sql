-- 添加微信openid字段到users表
ALTER TABLE users
    ADD COLUMN wx_openid VARCHAR(100) UNIQUE COMMENT '微信openid',
    MODIFY COLUMN phone VARCHAR(50) UNIQUE COMMENT '手机号';

-- 创建索引
CREATE INDEX idx_users_wx_openid ON users(wx_openid);

-- 将现有数据的phone字段（存储的openid）迁移到wx_openid字段
UPDATE users SET wx_openid = phone WHERE phone LIKE 'mock_openid_%' OR phone LIKE 'o_%';
