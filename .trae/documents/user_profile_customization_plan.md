# 用户头像/昵称自定义功能实现计划

## 需求概述
1. 不再调用微信接口获取用户信息
2. 后端使用默认头像保存到本地静态文件
3. 默认昵称格式："智搭小掌柜-{推荐码}"
4. 默认头像旁显示提示："点击修改专属头像"
5. 点击头像弹出编辑弹窗，可修改头像和昵称
6. UI设计风格：现代简约、科技感、暖色调

## 主题色彩方案

### 主色调
- **主色**: `#6366F1` (靛蓝紫) - 代表智能、科技
- **辅助色**: `#F59E0B` (琥珀金) - 代表活力、积分
- **强调色**: `#10B981` (翠绿) - 代表成功、推荐

### 中性色
- **背景**: `#F8FAFC` (浅灰蓝)
- **卡片背景**: `#FFFFFF` (纯白)
- **文字主色**: `#1E293B` (深 slate)
- **文字次要**: `#64748B` (灰 slate)
- **边框**: `#E2E8F0` (浅灰)

### 渐变效果
- **用户卡片**: `linear-gradient(135deg, #6366F1 0%, #8B5CF6 100%)`
- **按钮主色**: `linear-gradient(135deg, #6366F1 0%, #4F46E5 100%)`

---

## 后端实现

### 1. 配置文件修改 (application.yml)
```yaml
app:
  upload:
    avatar-path: ${user.dir}/uploads/avatars/  # 头像存储路径
    avatar-url-prefix: /uploads/avatars/       # 头像访问URL前缀
    default-avatar: default-avatar.png         # 默认头像文件名
```

### 2. 新增文件存储配置类
**文件**: `com.jiedan.config.FileStorageConfig.java`
- 配置头像存储路径
- 初始化存储目录
- 提供路径获取方法

### 3. 修改 WechatService.java
**修改点**:
- 移除从微信获取用户信息的逻辑
- 新用户注册时使用默认头像
- 设置默认昵称格式为 "智搭小掌柜-{推荐码}"

### 4. 新增 UserProfileService.java
**功能**:
- `updateAvatar(Long userId, MultipartFile file)` - 更新用户头像
- `updateNickname(Long userId, String nickname)` - 更新用户昵称
- `getDefaultAvatarUrl()` - 获取默认头像URL
- `isDefaultAvatar(String avatarUrl)` - 判断是否为默认头像

### 5. 新增 UserController 接口
**新增端点**:
- `POST /api/users/me/avatar` - 上传头像
- `PUT /api/users/me/nickname` - 修改昵称
- `GET /api/users/me` - 获取当前用户信息（包含是否为默认头像标识）

### 6. 新增 WebConfig 静态资源映射
```java
registry.addResourceHandler("/uploads/**")
        .addResourceLocations("file:" + uploadPath);
```

### 7. 数据库无需修改
- User 实体已有 nickname 和 avatar 字段

---

## 前端实现

### 1. 修改登录页 (login.js)
**修改点**:
- 移除 `wx.getUserProfile` 调用
- 登录时不再传递用户信息
- 仅传递微信 code 给后端

### 2. 修改个人中心页 (profile.wxml)
**修改点**:
- 头像区域添加点击事件
- 默认头像时显示提示文字
- 添加编辑弹窗

**UI结构**:
```
用户卡片区域:
├── 头像 (可点击)
│   ├── 默认头像 + 相机图标遮罩
│   └── 提示文字: "点击修改专属头像"
├── 昵称显示
│   └── 默认昵称: "智搭小掌柜-XXXX"
└── ID显示
```

### 3. 修改个人中心样式 (profile.wxss)
**新增样式**:
- 头像容器样式（带悬浮效果）
- 编辑图标样式
- 提示文字样式（动画效果）
- 编辑弹窗样式

### 4. 修改个人中心逻辑 (profile.js)
**新增方法**:
- `onAvatarTap()` - 点击头像
- `showEditModal()` - 显示编辑弹窗
- `hideEditModal()` - 隐藏编辑弹窗
- `chooseAvatar()` - 选择头像
- `updateNickname()` - 更新昵称
- `saveProfile()` - 保存修改

### 5. 新增编辑弹窗组件
**弹窗内容**:
- 头像预览区（圆形）
- 更换头像按钮
- 昵称输入框
- 保存按钮

### 6. 修改 API 工具类 (api.js)
**新增接口**:
- `uploadAvatar(filePath)` - 上传头像
- `updateNickname(nickname)` - 更新昵称

---

## 文件清单

### 后端文件
1. `springboot-backend/src/main/java/com/jiedan/config/FileStorageConfig.java` (新增)
2. `springboot-backend/src/main/java/com/jiedan/service/UserProfileService.java` (新增)
3. `springboot-backend/src/main/java/com/jiedan/controller/UserController.java` (修改)
4. `springboot-backend/src/main/java/com/jiedan/service/WechatService.java` (修改)
5. `springboot-backend/src/main/java/com/jiedan/config/WebConfig.java` (修改)
6. `springboot-backend/src/main/resources/application.yml` (修改)

### 前端文件
1. `miniprogram/pages/login/login.js` (修改)
2. `miniprogram/pages/profile/profile.wxml` (修改)
3. `miniprogram/pages/profile/profile.wxss` (修改)
4. `miniprogram/pages/profile/profile.js` (修改)
5. `miniprogram/utils/api.js` (修改)

### 静态资源
1. `miniprogram/images/default-avatar.png` (已存在)
2. `springboot-backend/uploads/avatars/default-avatar.png` (复制)

---

## 实现步骤

### 阶段1: 后端基础设置
1. 创建头像存储目录配置
2. 复制默认头像到后端静态目录
3. 配置静态资源映射

### 阶段2: 后端业务逻辑
1. 创建 UserProfileService
2. 修改 WechatService 使用默认头像和昵称
3. 在 UserController 添加头像/昵称更新接口

### 阶段3: 前端登录逻辑
1. 修改 login.js 移除微信用户信息获取
2. 测试登录流程

### 阶段4: 前端个人中心UI
1. 修改 profile.wxml 添加头像点击区域和提示
2. 修改 profile.wxss 添加新样式
3. 添加编辑弹窗

### 阶段5: 前端业务逻辑
1. 修改 profile.js 添加编辑功能
2. 修改 api.js 添加新接口
3. 联调测试

---

## UI设计细节

### 头像区域设计
```
┌─────────────────────────────┐
│  ┌──────┐                   │
│  │  👤  │ 智搭小掌柜-ABC12  │
│  │ 📷   │ ID: 12345         │
│  └──────┘                   │
│  点击修改专属头像            │
└─────────────────────────────┘
```

### 编辑弹窗设计
```
┌─────────────────────────────┐
│      编辑个人资料      ✕    │
├─────────────────────────────┤
│                             │
│         ┌──────┐            │
│         │  👤  │            │
│         └──────┘            │
│      [点击更换头像]          │
│                             │
│   昵称                      │
│   ┌──────────────────┐      │
│   │ 智搭小掌柜-ABC12 │      │
│   └──────────────────┘      │
│                             │
│   [    保 存    ]           │
│                             │
└─────────────────────────────┘
```

### 颜色应用
- 头像边框: 2px solid #6366F1
- 编辑图标背景: rgba(99, 102, 241, 0.9)
- 提示文字: #F59E0B (琥珀金)
- 弹窗标题栏: linear-gradient(135deg, #6366F1 0%, #8B5CF6 100%)
- 保存按钮: #6366F1

---

## API接口定义

### 上传头像
```
POST /api/users/me/avatar
Content-Type: multipart/form-data

Request:
- file: 图片文件 (jpg/png, max 2MB)

Response:
{
  "avatar": "/uploads/avatars/user_123_1678888888.jpg"
}
```

### 更新昵称
```
PUT /api/users/me/nickname
Content-Type: application/json

Request:
{
  "nickname": "新昵称"
}

Response:
{
  "nickname": "新昵称"
}
```

### 获取用户信息
```
GET /api/users/me

Response:
{
  "id": 123,
  "nickname": "智搭小掌柜-ABC12",
  "avatar": "/uploads/avatars/default-avatar.png",
  "isDefaultAvatar": true,
  "referralCode": "ABC12",
  "totalPoints": 100
}
```

---

## 注意事项
1. 头像文件大小限制: 2MB
2. 头像格式: jpg, jpeg, png
3. 昵称长度限制: 2-20个字符
4. 默认头像路径需要可访问
5. 上传的头像按用户ID存储，便于替换
