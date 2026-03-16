# 微信小程序开发速查手册（AI版）

> 高度提炼，直击要害，避免重复犯错

---

## 一、核心铁律

### 1. 事件绑定铁律（重点！）
**规则**：WXML事件名与JS方法名必须逐字符完全一致

**常见错误**：
- `bindtap="onNext"` ↔ `onNextStep()` ❌
- `bindinput="onInputDesc"` ↔ `onDescInput()` ❌

**做法**：JS定义好方法后，复制粘贴到WXML使用

### 2. 数据字段名一致性铁律（重点！）
**规则**：WXML显示、JS保存、JS验证三处字段名必须统一

**常见错误**：
- WXML用`visualStyle`，JS保存用`designStyle` ❌

**做法**：定义好字段名后，复制粘贴到所有使用的地方

### 3. 数据更新铁律
- ✅ 正确：`this.setData({ key: value })`
- ❌ 错误：`this.data.key = value`（不会触发视图更新）

### 4. 数据初始化铁律
**规则**：WXML中使用的所有字段必须在`data`中初始化

**检查方法**：
1. 检查WXML中所有`{{xxx}}`表达式
2. 确保这些字段在`data`中有初始值

### 5. WXML表达式限制
**规则**：WXML只支持简单运算，不支持方法调用

- ✅ 支持：`{{a + b}}`、`{{condition ? '是' : '否'}}`
- ❌ 不支持：`{{calculate()}}`、`{{items.filter()}}`、`{{new Date()}}`

**做法**：JS中计算结果存入data，WXML直接绑定字段

---

## 二、API配置规范（重要！）

### 统一API基础URL配置
**规则**：
1. **唯一配置源**：只在 `util.js` 中定义 `getApiBaseUrl()` 函数
2. **全局统一**：所有API请求都通过 `util.getApiBaseUrl()` 获取基础URL
3. **自动补全**：`getApiBaseUrl()` 自动确保返回的URL包含 `/api/v1`
4. **调用简化**：使用时直接拼接路径，如 `${util.getApiBaseUrl()}/requirements`

**正确做法**：
- `util.js` 中配置 `getApiBaseUrl()` 函数
- `api.js` 中引用 `util.getApiBaseUrl()`
- 页面中直接使用 `util.getApiBaseUrl()` 拼接API路径

**错误做法**：
- 多处硬编码不同地址（如一处用localhost，一处用IP）
- 重复添加 `/api/v1` 路径

---

## 三、WXML速查

### 列表渲染
```xml
<!-- ✅ 正确 -->
<view wx:for="{{list}}" wx:key="id">{{item.name}}</view>

<!-- ❌ 错误 -->
<view wx:for="{{list}}" wx:key="{{item.id}}">  <!-- 多余大括号 -->
<view wx:for="{{list}}">                      <!-- 缺少wx:key -->
```

### 条件渲染
```xml
<!-- 二选一 -->
<view wx:if="{{condition}}">显示</view>
<view wx:else>隐藏</view>

<!-- 频繁切换用hidden -->
<view hidden="{{!condition}}">内容</view>
```

### 事件传参
```xml
<!-- ✅ 正确：data-* 传参 -->
<view bindtap="onTap" data-id="{{item.id}}" data-index="{{index}}">
```

```javascript
// 接收参数
onTap(e) {
  const { id, index } = e.currentTarget.dataset;
}
```

---

## 四、JS速查

### 页面结构
```javascript
Page({
  data: { key: 'value' },
  
  onLoad(options) { /* 初始化，从options获取页面参数 */ },
  onShow() { /* 显示时 */ },
  
  handleTap() {
    this.setData({ key: 'newValue' });
  }
});
```

### 路由跳转
- `wx.navigateTo({ url: '/pages/detail/detail?id=123' })` - 保留当前页
- `wx.redirectTo({ url: '/pages/login/login' })` - 关闭当前页
- `wx.switchTab({ url: '/pages/index/index' })` - Tab页
- `wx.navigateBack({ delta: 1 })` - 返回

### 存储
- `wx.setStorageSync('key', value)`
- `const value = wx.getStorageSync('key') || defaultValue`

---

## 五、WXSS速查

### 单位
- `rpx`：自适应单位，屏幕宽=750rpx
- `px`：固定单位，用于边框、阴影

### 安全区域
```css
.fixed-bottom {
  position: fixed;
  bottom: 0;
  padding-bottom: env(safe-area-inset-bottom);
}
```

---

## 六、常见错误对照表

| 现象 | 原因 | 解决 |
|------|------|------|
| **点击无反应** | 事件名不匹配 | 逐字符检查WXML与JS方法名 |
| **输入后验证失败** | 输入事件名不匹配 | 检查`bindinput`与JS方法名 |
| **选择后验证失败** | 数据字段名不一致 | 统一WXML/保存/验证的字段名 |
| **`xxx is not a function`** | 工具函数未定义/未导出 | 检查函数是否定义并添加到`module.exports` |
| **API请求参数为null** | 页面参数未获取 | 在`onLoad(options)`中获取并保存参数 |
| **API请求地址不一致** | 多处定义API_BASE_URL | 统一使用`util.getApiBaseUrl()`获取基础URL |
| **WXML调用方法报错** | WXML不支持方法调用 | 在JS中计算结果存入data，WXML直接绑定字段 |
| **字段未定义报错** | data初始化缺少字段 | 检查WXML使用的所有字段是否在data中初始化 |
| 数据变视图不变 | 直接修改data | 使用setData |
| 列表更新错乱 | 缺少wx:key | 添加唯一key |

---

## 七、AI生成代码自检

生成代码后，自动检查：

- [ ] **WXML事件名与JS方法名完全一致**（逐字符！）
- [ ] **数据字段名三处一致**（WXML显示/JS保存/JS验证）
- [ ] **工具函数已定义并导出**（添加到`module.exports`）
- [ ] **页面参数已获取**（`onLoad(options)`中获取`options.id`等参数）
- [ ] **API基础URL统一配置**（只使用`util.getApiBaseUrl()`，不硬编码）
- [ ] **WXML中无方法调用**（如`{{calculate()}}`），应在JS计算后存入data
- [ ] **data初始化包含所有WXML使用的字段**（检查所有`{{xxx}}`表达式）
- [ ] 无`filter`/`map`等复杂表达式在WXML中
- [ ] `wx:key`使用字段名（无大括号）
- [ ] 所有数据修改使用`this.setData()`
- [ ] 数组/对象更新使用新引用（`[...arr, newItem]`）

**口诀**：
> 事件名要对齐（逐字符！），字段名要统一，API配置要集中，复杂逻辑移JS，数据更新用setData，列表记得加key

**黄金法则**：
> 1. WXML中的`bindxxx="methodName"`必须与JS中的`methodName()`逐字符相同
> 2. WXML显示的字段、JS保存的字段、JS验证的字段必须是同一个字段名
> 3. API基础URL只在`util.js`中配置，全局统一使用`util.getApiBaseUrl()`
