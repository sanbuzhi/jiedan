# 智能获客系统

智能获客系统是一套自动化、智能演化的获客平台，专为"自动写代码"产品获取商家、个人经营者和学生用户。系统具备自动获客、自动裂变、自动发布物料、多平台推广、智能成长等核心能力。

## 功能特性

### 1. 多平台发布引擎
- 支持10+主流平台：微信、抖音、小红书、知乎、B站、微博、闲鱼、58同城、贴吧、豆瓣
- 可插拔的适配器架构
- 定时发布和批量发布
- 失败重试机制

### 2. 客户裂变体系
- 多级推荐关系追踪
- 积分奖励规则引擎
- 推荐码生成与管理

### 3. 智能物料生成器
- AI内容生成集成
- 平台适配的文案模板
- 图片和视频脚本生成

### 4. 数据驱动优化
- 数据采集与埋点
- A/B测试框架
- 转化率分析
- 策略自动优化

### 5. 管理后台
- 数据仪表盘（ECharts可视化）
- 用户管理
- 物料管理
- 发布任务管理
- A/B测试管理
- 策略优化
- 系统配置

## 技术栈

### 后端
- FastAPI - Web框架
- SQLAlchemy - ORM
- PostgreSQL - 数据库
- APScheduler - 任务调度
- Pydantic - 数据验证

### 前端
- Vue 3 - 前端框架
- Element Plus - UI组件库
- Vue Router - 路由管理
- Pinia - 状态管理
- ECharts - 数据可视化
- Axios - HTTP客户端

### 部署
- Docker - 容器化
- Docker Compose - 服务编排

## 快速开始

### 环境要求
- Python 3.9+
- Node.js 18+
- PostgreSQL 14+
- Redis 7+

### 使用 Docker Compose 部署

1. 克隆项目
```bash
git clone <repository-url>
cd jiedan_auto_get_clients
```

2. 配置环境变量
```bash
cd backend
cp .env.example .env
# 编辑 .env 文件，配置数据库和其他参数
```

3. 启动服务
```bash
cd ..
docker-compose up -d
```

4. 访问应用
- 前端: http://localhost:5173
- 后端API文档: http://localhost:8000/docs
- 管理后台: http://localhost:5173/admin/dashboard

### 本地开发

#### 后端开发
```bash
cd backend
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

#### 前端开发
```bash
cd frontend
npm install
npm run dev
```

## 项目结构

```
jiedan_auto_get_clients/
├── backend/
│   ├── app/
│   │   ├── api/              # API路由
│   │   │   ├── analytics.py   # 数据分析API
│   │   │   ├── auth.py        # 认证API
│   │   │   ├── health.py      # 健康检查
│   │   │   ├── materials.py   # 物料API
│   │   │   ├── publish.py     # 发布API
│   │   │   ├── users.py       # 用户API
│   │   │   └── admin.py       # 管理后台API
│   │   ├── core/             # 核心配置
│   │   ├── models/           # 数据库模型
│   │   ├── schemas/          # Pydantic模式
│   │   └── services/         # 业务逻辑
│   │       └── platform_adapters/  # 平台适配器
│   ├── .env.example
│   ├── Dockerfile
│   └── requirements.txt
├── frontend/
│   ├── src/
│   │   ├── router/           # 路由配置
│   │   ├── stores/           # Pinia状态
│   │   ├── utils/            # 工具函数
│   │   ├── views/            # 页面组件
│   │   │   └── admin/       # 管理后台页面
│   │   ├── App.vue
│   │   └── main.js
│   ├── Dockerfile
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
├── docker-compose.yml
└── README.md
```

## API文档

启动后端服务后，访问以下地址查看完整的API文档：
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

### 主要API端点

#### 健康检查
- `GET /api/v1/health` - 健康检查
- `GET /api/v1/ready` - 就绪检查
- `GET /api/v1/metrics` - 系统指标

#### 认证
- `POST /api/v1/auth/login` - 用户登录
- `POST /api/v1/auth/register` - 用户注册

#### 用户
- `GET /api/v1/users/me` - 获取当前用户信息
- `PUT /api/v1/users/me` - 更新用户信息

#### 物料
- `GET /api/v1/materials` - 获取物料列表
- `POST /api/v1/materials` - 创建物料
- `POST /api/v1/materials/generate` - AI生成物料

#### 发布
- `GET /api/v1/publish/tasks` - 获取发布任务
- `POST /api/v1/publish/tasks` - 创建发布任务
- `GET /api/v1/publish/history` - 发布历史

#### 数据分析
- `GET /api/v1/analytics/dashboard` - 仪表盘数据
- `GET /api/v1/analytics/conversion-stats` - 转化率统计
- `GET /api/v1/analytics/experiments` - A/B实验列表
- `POST /api/v1/analytics/experiments` - 创建A/B实验
- `GET /api/v1/analytics/rules` - 优化规则列表
- `POST /api/v1/analytics/rules` - 创建优化规则
- `POST /api/v1/analytics/optimize` - 执行策略优化

#### 管理后台
- `GET /api/v1/admin/users` - 用户列表
- `PUT /api/v1/admin/users/{id}/toggle-active` - 启用/禁用用户
- `GET /api/v1/admin/system-config` - 获取系统配置
- `PUT /api/v1/admin/system-config` - 更新系统配置

## 配置说明

### 环境变量 (.env)

```env
# 数据库
DATABASE_URL=postgresql://user:pass@host:5432/db

# Redis
REDIS_URL=redis://host:6379/0

# JWT
SECRET_KEY=your-secret-key
ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=30

# AI服务
AI_SERVICE_PROVIDER=openai
AI_SERVICE_API_KEY=your-api-key
AI_SERVICE_MODEL=gpt-4

# 发布配置
PUBLISH_MAX_RETRIES=3
PUBLISH_RETRY_DELAY=60

# 分析配置
ANALYTICS_RETENTION_DAYS=90
AB_TEST_CONFIDENCE=0.95
```

## 平台支持

系统支持以下平台：

| 平台 | 适配器 | 状态 |
|------|--------|------|
| 微信公众号 | WechatAdapter | ✅ |
| 小红书 | XiaohongshuAdapter | ✅ |
| 知乎 | ZhihuAdapter | ✅ |
| 抖音 | DouyinAdapter | ✅ |
| B站 | BilibiliAdapter | ✅ |
| 微博 | WeiboAdapter | ✅ |
| 闲鱼 | XianyuAdapter | ✅ |
| 58同城 | WubaAdapter | ✅ |
| 贴吧 | TiebaAdapter | ✅ |
| 豆瓣 | DoubanAdapter | ✅ |

## 监控

### 健康检查
```bash
# 健康状态
curl http://localhost:8000/api/v1/health

# 就绪状态
curl http://localhost:8000/api/v1/ready

# 系统指标
curl http://localhost:8000/api/v1/metrics
```

### 日志
- 后端日志: 配置 `LOG_FILE` 环境变量
- Docker日志: `docker-compose logs -f backend`

## 开发指南

### 添加新的平台适配器

1. 在 `backend/app/services/platform_adapters/` 创建新的适配器文件
2. 继承 `PlatformAdapter` 基类
3. 实现 `publish()` 和 `validate_account()` 方法
4. 在 `adapter_factory.py` 中注册适配器

示例：
```python
from app.services.platform_adapters.base_adapter import PlatformAdapter, MockPublishResult

class NewPlatformAdapter(PlatformAdapter):
    def __init__(self):
        super().__init__("new_platform")
    
    async def publish(self, title, content, account_config, **kwargs):
        # 实现发布逻辑
        pass
    
    async def validate_account(self, account_config):
        # 实现账号验证逻辑
        pass
```

### 数据库迁移

使用 Alembic 进行数据库迁移：
```bash
cd backend
alembic revision --autogenerate -m "description"
alembic upgrade head
```

## 部署

### 生产环境部署建议

1. 使用 HTTPS
2. 配置数据库备份
3. 设置监控告警（Prometheus + Grafana）
4. 配置日志收集（ELK Stack）
5. 使用反向代理（Nginx）
6. 配置防火墙规则

### Docker Compose 生产配置

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:14
    volumes:
      - postgres_data:/var/lib/postgresql/data
    environment:
      POSTGRES_PASSWORD: secure-password
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build: ./backend
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    deploy:
      replicas: 2
      restart_policy:
        condition: on-failure

  frontend:
    build: ./frontend
    depends_on:
      - backend

volumes:
  postgres_data:
  redis_data:
```

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 联系方式

如有问题或建议，请提交 Issue 或 Pull Request。

---

**注意**: 这是一个演示项目，实际使用时请：
- 修改默认密码和密钥
- 配置适当的安全措施
- 遵守各平台的API使用条款
