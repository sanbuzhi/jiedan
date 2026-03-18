# 微信小程序开发规范

> 高度提炼的错误总结，避免重复犯错

---

## 1. Require 路径规范

### 错误示例
```javascript
// ❌ 错误：路径计算错误
const api = require('../../utils/api.js');  // 相对于 pages/requirement/step/step.js

// ✅ 正确：需要向上3级
const api = require('../../../utils/api.js');
```

### 路径计算规则
- `pages/requirement/step/step.js` → `utils/api.js`
- 相对路径：`../../../utils/api.js`（3个 `../`）
- 原则：文件在第N层目录，就需要N个 `../`

---

## 2. 变量声明规范

### 错误示例
```javascript
// ❌ 错误：顶部已声明，函数内又重复声明
const api = require('../../utils/api.js');

Page({
  completeRequirement() {
    const api = require('../../utils/api.js'); // 重复声明！
    // ...
  }
});
```

### 正确做法
```javascript
// ✅ 正确：顶部统一声明，函数内直接使用
const api = require('../../../utils/api.js');

Page({
  completeRequirement() {
    api.requirementApi.createRequirement(data); // 直接使用
  }
});
```

---

## 3. 数据安全访问规范

### 错误示例
```javascript
// ❌ 错误：直接访问可能不存在的属性
this.stepConfigs[step]  // step可能为undefined

// ❌ 错误：数组可能为空
req.flowNodes[0].status  // flowNodes可能为空数组
```

### 正确做法
```javascript
// ✅ 正确：先检查再访问
const stepConfig = this.stepConfigs[step] || {};

// ✅ 正确：条件渲染配合安全访问
wx:if="{{req.flowNodes[0] && req.flowNodes[0].status === 'active'}}"
```

---

## 4. Promise 错误处理规范

### 错误示例
```javascript
// ❌ 错误：没有 catch 隐藏 loading
wx.showLoading({ title: '加载中...' });
api.requirementApi.createRequirement(data)
  .then(res => {
    // 成功处理
  });
// 失败时 loading 永远不会隐藏！
```

### 正确做法
```javascript
// ✅ 正确：务必处理成功和失败两种情况
wx.showLoading({ title: '加载中...' });
api.requirementApi.createRequirement(data)
  .then(res => {
    wx.hideLoading();
    // 成功处理
  })
  .catch(err => {
    wx.hideLoading();  // 失败也要隐藏
    wx.showToast({ title: '操作失败', icon: 'none' });
  });
```

---

## 5. API 调用规范

### 错误示例
```javascript
// ❌ 错误：直接使用 api.delete
api.delete(`/requirements/${id}`)  // 可能不存在

// ❌ 错误：API 模块内部使用 del，外部用 delete
```

### 正确做法
```javascript
// ✅ 正确：使用已封装的 API 模块
api.requirementApi.deleteRequirement(id)

// 或直接使用 del
api.del(`/requirements/${id}`)
```

---

## 6. 后端连接规范

### 常见错误
```yaml
# ❌ 错误：MySQL 不允许远程连接
url: jdbc:mysql://192.168.1.8:3306/jiedan

# ✅ 正确：本机使用 localhost
url: jdbc:mysql://localhost:3306/jiedan
```

### 端口占用处理
```bash
# 查找占用端口的进程
netstat -ano | findstr :8000

# 停止进程
taskkill /PID <PID> /F
```

---

## 7. 登录检查规范

### 正确实现
```javascript
const util = require('../../../utils/util.js');

Page({
  onShow() {
    // ✅ 正确：在页面显示时检查
    if (!util.checkLogin()) {
      wx.redirectTo({ url: '/pages/login/login' });
      return;
    }
    // 正常逻辑...
  }
});
```

### 特殊页面处理
```javascript
// 引导页：已登录则跳转到首页
onShow() {
  if (util.checkLogin()) {
    wx.switchTab({ url: '/pages/index/index' });
    return;
  }
}

// 登录页：不需要检查
// 页面不需要检查登录状态
```

---

## 8. 前后端数据校验规范

### 问题场景
前端发送空数组，后端强制要求非空，抛出异常 → Spring Boot `/error` 端点处理时 SecurityContext 丢失 → 返回 403

### 解决方案
```java
// ✅ 后端：允许空值，提供默认值或基础逻辑
private void validateRequest(Request request) {
    if (request == null) {
        throw new IllegalArgumentException("请求不能为空");
    }
    // 允许空选择，不强制要求必须选择
    log.debug("selectedIds: {}, customItems: {}", 
              request.getSelectedIds(), 
              request.getCustomItems());
}
```

### 原则
- 后端校验不应过于严格，允许合理的空值
- 前端提交的数据结构要与后端 DTO 匹配
- 异常处理要考虑 SecurityContext 的保持

---

## 9. 调试技巧

### 清除缓存
- 微信开发者工具：**清缓存** → **清除全部缓存**

### 查看日志
```javascript
console.log('调试信息:', data);
console.error('错误信息:', err);
```

### 后端日志级别
```yaml
# application.yml
logging:
  level:
    com.jiedan.security: DEBUG  # 查看 JWT 认证日志
```

---

## 10. 常见错误排查

| 错误 | 可能原因 |
|------|----------|
| module not found | require 路径错误 |
| is not a function | API 调用方式错误 |
| Cannot read property | 访问 undefined 的属性 |
| Port already in use | 端口被占用 |
| Host not allowed | MySQL 不允许远程连接 |
| 403 Forbidden | JWT 认证失败 / 数据校验失败导致跳转到 /error |
| 500 Internal Error | 后端空指针或数据校验异常 |
