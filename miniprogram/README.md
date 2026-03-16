# 接单助手 - 微信小程序

## 项目简介

接单助手小程序是智能获客系统的微信小程序端，提供微信授权登录、个人中心、推荐关系管理和分享功能。

## 功能特性

### 1. 微信授权登录
- 一键微信登录
- 自动获取用户头像和昵称
- 支持推荐码关联

### 2. 个人中心
- 显示用户头像、昵称
- 显示积分余额
- 显示推荐码
- 显示推荐关系树（三级分销）
- 积分记录查询

### 3. 分享功能
- 带推荐码的小程序分享卡片
- 支持分享到微信好友
- 支持分享到朋友圈
- 被分享者打开后自动关联推荐关系

### 4. 产品介绍
- 产品功能介绍
- 支持平台展示
- 使用流程说明
- 推荐奖励说明

## 项目结构

```
miniprogram/
├── app.js              # 小程序入口文件
├── app.json            # 小程序全局配置
├── app.wxss            # 小程序全局样式
├── sitemap.json        # 小程序站点地图
├── project.config.json # 项目配置文件
├── README.md           # 项目说明文档
├── pages/              # 页面目录
│   ├── index/          # 首页（产品介绍）
│   │   ├── index.js
│   │   ├── index.json
│   │   ├── index.wxml
│   │   └── index.wxss
│   ├── login/          # 登录页
│   │   ├── login.js
│   │   ├── login.json
│   │   ├── login.wxml
│   │   └── login.wxss
│   └── profile/        # 个人中心页
│       ├── profile.js
│       ├── profile.json
│       ├── profile.wxml
│       └── profile.wxss
├── utils/              # 工具函数
│   ├── api.js          # API请求封装
│   └── util.js         # 通用工具函数
├── components/         # 组件目录
└── images/             # 图片资源
```

## 配置说明

### 1. 修改后端API地址

编辑 `utils/api.js` 文件，修改 `API_BASE_URL`：

```javascript
const API_BASE_URL = 'https://your-domain.com/api/v1'; // 生产环境
```

### 2. 配置微信小程序

编辑 `project.config.json` 文件，设置正确的 `appid`：

```json
{
  "appid": "wx3da9549afa79161a"
}
```

### 3. 配置request合法域名

在微信公众平台-小程序后台，配置服务器域名：
- request合法域名：`https://your-domain.com`
- uploadFile合法域名：`https://your-domain.com`
- downloadFile合法域名：`https://your-domain.com`

## 开发环境运行

1. 安装微信开发者工具
2. 导入项目，选择 `miniprogram` 目录
3. 在详情设置中勾选「不校验合法域名」
4. 启动后端服务（默认端口8080）
5. 编译运行

## 后端API接口

### 微信登录
```
POST /api/v1/auth/wechat-login
Request:
{
  "code": "wechat_login_code",
  "nickname": "用户昵称",
  "avatar": "头像URL",
  "referral_code": "推荐码（可选）"
}

Response:
{
  "access_token": "jwt_token",
  "token_type": "bearer",
  "is_new_user": true
}
```

### 获取用户信息
```
GET /api/v1/users/me
Header: Authorization: Bearer {token}
```

### 获取推荐关系树
```
GET /api/v1/users/referrals/tree
Header: Authorization: Bearer {token}
```

### 获取积分记录
```
GET /api/v1/users/points
Header: Authorization: Bearer {token}
```

## 推荐关系说明

- 一级推荐：直接推荐的好友，奖励100积分
- 二级推荐：好友推荐的好友，奖励50积分
- 三级推荐：二级好友推荐的好友，奖励30积分

## 注意事项

1. 开发环境下，后端使用模拟openid模式，无需配置真实的小程序appid和secret
2. 生产环境需要配置正确的 `WECHAT_APPID` 和 `WECHAT_SECRET`
3. 小程序要求使用HTTPS协议，生产环境后端需要配置SSL证书
4. 分享功能需要用户点击右上角菜单或按钮触发

## 更新日志

### v1.0.0
- 微信授权登录
- 个人中心页面
- 推荐关系树展示
- 分享功能
- 产品介绍页面
