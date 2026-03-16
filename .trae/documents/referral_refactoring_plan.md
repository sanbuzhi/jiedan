# 推荐关系重构计划

## 目标
去掉 `referral_relationships` 表，通过 `users` 表的 `referrer_id` 字段构造三级推荐关系。

## 当前架构
- `referral_relationships` 表存储推荐关系（user_id, referrer_id, level）
- `users` 表已有 `referrer_id` 字段
- 查询推荐树时需要 JOIN 两个表

## 新架构
- 仅使用 `users` 表的 `referrer_id` 字段
- 通过递归查询构造三级推荐树
- 删除 `referral_relationships` 表和相关代码

---

## 后端改造

### 1. 修改 UserService（核心逻辑）

#### 1.1 重构 getReferralTree 方法
```java
public ReferralTreeItem getReferralTree(Long userId) {
    // 查询当前用户
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
    
    // 构建三级推荐树
    ReferralTreeItem root = new ReferralTreeItem();
    root.setUser(convertToUserResponse(user));
    root.setLevel(0);
    root.setChildren(new ArrayList<>());
    
    // 查询一级推荐（直接推荐）
    List<User> level1Users = userRepository.findByReferrerId(userId);
    for (User level1User : level1Users) {
        ReferralTreeItem level1Item = new ReferralTreeItem();
        level1Item.setUser(convertToUserResponse(level1User));
        level1Item.setLevel(1);
        level1Item.setChildren(new ArrayList<>());
        
        // 查询二级推荐
        List<User> level2Users = userRepository.findByReferrerId(level1User.getId());
        for (User level2User : level2Users) {
            ReferralTreeItem level2Item = new ReferralTreeItem();
            level2Item.setUser(convertToUserResponse(level2User));
            level2Item.setLevel(2);
            level2Item.setChildren(new ArrayList<>());
            
            // 查询三级推荐
            List<User> level3Users = userRepository.findByReferrerId(level2User.getId());
            for (User level3User : level3Users) {
                ReferralTreeItem level3Item = new ReferralTreeItem();
                level3Item.setUser(convertToUserResponse(level3User));
                level3Item.setLevel(3);
                level3Item.setChildren(new ArrayList<>()); // 三级不再展开
                level2Item.getChildren().add(level3Item);
            }
            
            level1Item.getChildren().add(level2Item);
        }
        
        root.getChildren().add(level1Item);
    }
    
    return root;
}
```

#### 1.2 添加统计方法
```java
public ReferralStats getReferralStats(Long userId) {
    int level1 = userRepository.countByReferrerId(userId);
    
    int level2 = 0;
    int level3 = 0;
    
    // 统计二级和三级
    List<User> level1Users = userRepository.findByReferrerId(userId);
    for (User level1User : level1Users) {
        List<User> level2Users = userRepository.findByReferrerId(level1User.getId());
        level2 += level2Users.size();
        
        for (User level2User : level2Users) {
            level3 += userRepository.countByReferrerId(level2User.getId());
        }
    }
    
    return new ReferralStats(level1, level2, level3);
}
```

### 2. 修改 UserRepository

#### 2.1 添加查询方法
```java
// 根据推荐人ID查询被推荐用户列表
List<User> findByReferrerId(Long referrerId);

// 统计推荐人数
int countByReferrerId(Long referrerId);
```

### 3. 删除冗余代码

#### 3.1 删除 Entity
- `ReferralRelationship.java`

#### 3.2 删除 Repository
- `ReferralRelationshipRepository.java`

#### 3.3 删除 Service 中的依赖
- `UserService` 中移除 `ReferralRelationshipRepository` 依赖

#### 3.4 删除数据库表
```sql
DROP TABLE IF EXISTS referral_relationships;
```

### 4. 修改积分发放逻辑（RuleService）

当新用户注册时，通过 `referrer_id` 查找推荐人并发放积分：

```java
@Transactional
public void processReferralReward(Long newUserId, Long referrerId) {
    if (referrerId == null) return;
    
    // 一级推荐奖励
    addPoints(referrerId, 100, "一级推荐奖励", newUserId);
    
    // 查找二级推荐人
    User referrer = userRepository.findById(referrerId).orElse(null);
    if (referrer != null && referrer.getReferrerId() != null) {
        // 二级推荐奖励
        addPoints(referrer.getReferrerId(), 50, "二级推荐奖励", newUserId);
        
        // 查找三级推荐人
        User secondReferrer = userRepository.findById(referrer.getReferrerId()).orElse(null);
        if (secondReferrer != null && secondReferrer.getReferrerId() != null) {
            // 三级推荐奖励
            addPoints(secondReferrer.getReferrerId(), 30, "三级推荐奖励", newUserId);
        }
    }
}
```

---

## 小程序改造

### 1. 修改 profile.js

#### 1.1 调整数据结构处理
当前接口返回的是数组，需要适配新的树形结构：

```javascript
// 加载推荐关系树
loadReferralTree: function () {
  api.userApi.getReferralTree()
    .then(res => {
      console.log('推荐树接口返回:', res);
      
      // 新接口返回的是树形对象，直接使用
      const treeData = res || { children: [] };
      
      // 初始化展开状态
      if (treeData.children) {
        treeData.children.forEach(child => {
          child.expanded = false;
          if (child.children) {
            child.children.forEach(grandchild => {
              grandchild.expanded = false;
            });
          }
        });
      }

      this.setData({
        referralTree: treeData
      });

      // 计算统计
      this.calculateTreeStats(treeData);
      
      // 初始化 Canvas
      if (this.data.showCanvasView && treeData.children && treeData.children.length > 0) {
        setTimeout(() => {
          this.initCanvas();
        }, 300);
      }
    })
    .catch(err => {
      console.error('获取推荐树失败:', err);
    });
},

// 计算推荐统计
calculateTreeStats: function (treeData) {
  let level1 = 0, level2 = 0, level3 = 0;
  
  if (treeData.children) {
    level1 = treeData.children.length;
    treeData.children.forEach(child => {
      if (child.children) {
        level2 += child.children.length;
        child.children.forEach(grandchild => {
          if (grandchild.children) {
            level3 += grandchild.children.length;
          }
        });
      }
    });
  }
  
  this.setData({
    'treeStats.level1': level1,
    'treeStats.level2': level2,
    'treeStats.level3': level3
  });
}
```

### 2. 修改 Canvas 绘制逻辑

Canvas 绘制逻辑保持不变，因为数据结构仍然是 `{children: [...]}` 的树形结构。

---

## 数据库数据迁移

### 1. 创建迁移脚本

```sql
-- 确保所有用户的 referrer_id 正确设置
-- 从 referral_relationships 表迁移数据到 users 表（如果 needed）

-- 示例：将一级推荐关系同步到 users 表
UPDATE users u
JOIN referral_relationships r ON u.id = r.user_id AND r.level = 1
SET u.referrer_id = r.referrer_id;

-- 删除旧的推荐关系表
DROP TABLE IF EXISTS referral_relationships;
```

### 2. 更新数据初始化脚本

修改 `init-data.sql`，去掉 `referral_relationships` 表的操作，改为通过 `referrer_id` 建立关系：

```sql
-- 创建测试用户时直接设置 referrer_id
INSERT INTO users (id, phone, nickname, referral_code, referrer_id, total_points, is_active) VALUES
(2, 'oWGaF1-TEST002', '张三', 'A1B2C3', 1, 0, true),  -- 推荐人ID=1
(3, 'oWGaF1-TEST003', '李四', 'D4E5F6', 1, 0, true),  -- 推荐人ID=1
(4, 'oWGaF1-TEST004', '王五', 'G7H8I9', 2, 0, true),  -- 推荐人ID=2（用户1的二级）
(5, 'oWGaF1-TEST005', '赵六', 'J0K1L2', 3, 0, true),  -- 推荐人ID=3（用户1的二级）
(6, 'oWGaF1-TEST006', '孙七', 'M3N4O5', 4, 0, true),  -- 推荐人ID=4（用户1的三级）
(7, 'oWGaF1-TEST007', '周八', 'P6Q7R8', 5, 0, true);  -- 推荐人ID=5（用户1的三级）
```

---

## 实施步骤

### 阶段1: 后端改造
1. 修改 `UserRepository`，添加 `findByReferrerId` 和 `countByReferrerId` 方法
2. 重构 `UserService.getReferralTree()` 方法
3. 修改 `RuleService` 中的积分发放逻辑
4. 删除 `ReferralRelationship` Entity 和 Repository
5. 更新 `DataInitializer`，去掉 `referral_relationships` 相关代码

### 阶段2: 数据库迁移
1. 执行数据迁移脚本（确保 referrer_id 正确）
2. 删除 `referral_relationships` 表
3. 更新 `init-data.sql`

### 阶段3: 小程序适配
1. 修改 `profile.js` 中的数据处理方法
2. 测试 Canvas 绘制是否正常

### 阶段4: 测试验证
1. 测试推荐树查询
2. 测试积分发放
3. 测试新用户注册推荐关系建立

---

## 优势

1. **简化数据模型** - 去掉冗余表，减少 JOIN 查询
2. **提高查询效率** - 直接查询 users 表，无需关联
3. **减少数据冗余** - 避免重复存储关系数据
4. **逻辑更清晰** - 通过 referrer_id 自解释推荐关系

## 注意事项

1. 确保 `users.referrer_id` 字段有索引，提高查询性能
2. 新用户注册时必须正确设置 `referrer_id`
3. 删除表前备份数据，防止误删
