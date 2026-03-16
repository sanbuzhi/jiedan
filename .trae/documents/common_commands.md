# 开发常用命令速查

> 汇总项目开发中常用的命令，便于快速查阅

---

## 一、端口管理

### 查看端口占用情况
```powershell
# 查看指定端口是否被占用
Get-NetTCPConnection -LocalPort 8080

# 查看端口占用并显示进程信息
Get-NetTCPConnection -LocalPort 8080 | Select-Object LocalPort, OwningProcess, @{Name="ProcessName";Expression={(Get-Process -Id $_.OwningProcess).ProcessName}}
```

### 释放被占用的端口
```powershell
# 强制结束占用指定端口的进程
Get-NetTCPConnection -LocalPort 8080 | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }

# 或者先查找进程ID，再结束
netstat -ano | findstr :8080
taskkill /PID <进程ID> /F
```

---

## 二、Spring Boot 后端

### 编译项目
```powershell
# 清理并编译（跳过测试）
mvn clean compile -DskipTests

# 清理并打包（跳过测试）
mvn clean package -DskipTests

# 完整构建（包含测试）
mvn clean package
```

### 运行项目
```powershell
# 运行打包后的jar
java -jar target/jiedan-backend-1.0.0.jar

# 指定配置文件运行
java -jar target/jiedan-backend-1.0.0.jar --spring.profiles.active=local

# Maven 直接运行（当前目录）
mvn spring-boot:run
```

### Maven 常用命令
```powershell
# 仅编译
mvn compile

# 运行测试
mvn test

# 清理目标目录
mvn clean

# 安装到本地仓库
mvn install
```

---

## 三、微信小程序开发

### 项目初始化
```powershell
# 安装依赖
npm install

# 初始化项目
npm init -y
```

### 代码检查
```powershell
# 查找文件中特定内容
grep -n "pattern" file.js

# 递归查找目录中所有文件
grep -r "pattern" ./pages

# 查找并显示行号
grep -n "bindtap" step_all.wxml
```

---

## 四、Git 操作

### 基础操作
```powershell
# 查看状态
git status

# 添加文件
git add filename
git add .

# 提交更改
git commit -m "提交信息"

# 推送到远程
git push origin main
```

### 分支操作
```powershell
# 查看分支
git branch

# 创建并切换分支
git checkout -b feature-branch

# 切换分支
git checkout main

# 合并分支
git merge feature-branch
```

---

## 五、Docker 操作

### 容器管理
```powershell
# 查看运行中的容器
docker ps

# 查看所有容器
docker ps -a

# 停止容器
docker stop <容器ID>

# 删除容器
docker rm <容器ID>
```

### 镜像管理
```powershell
# 查看镜像列表
docker images

# 删除镜像
docker rmi <镜像ID>

# 构建镜像
docker build -t jiedan-backend .
```

### Docker Compose
```powershell
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 查看日志
docker-compose logs -f

# 重启服务
docker-compose restart
```

---

## 六、数据库操作

### MySQL
```powershell
# 登录MySQL
mysql -u root -p

# 导出数据库
mysqldump -u root -p jiedan > backup.sql

# 导入数据库
mysql -u root -p jiedan < backup.sql
```

---

## 七、网络诊断

### 测试连接
```powershell
# 测试端口连通性
telnet localhost 8080

# 或者使用 PowerShell
Test-NetConnection -ComputerName localhost -Port 8080

# 查看网络连接
netstat -an
```

### HTTP 请求测试
```powershell
# GET 请求
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/health" -Method GET

# POST 请求
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/login" -Method POST -Body '{"username":"admin","password":"123456"}' -ContentType "application/json"
```

---

## 八、文件操作

### 文件查找
```powershell
# 查找文件
Get-ChildItem -Path . -Recurse -Filter "*.js" | Select-Object FullName

# 或者使用简写
gci -r -fi "*.js"
```

### 文件内容搜索
```powershell
# 在文件中搜索内容
Select-String -Path "*.js" -Pattern "getApiBaseUrl"

# 简写形式
sls -Path "*.js" -Pattern "getApiBaseUrl"
```

---

## 九、进程管理

### 查看进程
```powershell
# 查看所有进程
Get-Process

# 查找特定进程
Get-Process -Name java
Get-Process -Name node

# 查看进程详情
Get-Process -Id <进程ID>
```

### 结束进程
```powershell
# 按名称结束
Stop-Process -Name java

# 按ID结束
Stop-Process -Id <进程ID> -Force
```

---

## 十、快速导航

### 项目目录
```powershell
# 后端目录
cd d:\jiedan_auto_get_clients\springboot-backend

# 小程序目录
cd d:\jiedan_auto_get_clients\miniprogram

# 前端目录
cd d:\jiedan_auto_get_clients\frontend
```

---

## 十一、实用组合命令

### 重启后端服务（完整流程）
```powershell
# 1. 进入目录
cd d:\jiedan_auto_get_clients\springboot-backend

# 2. 结束占用端口的进程
Get-NetTCPConnection -LocalPort 8000 | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }

# 3. 重新编译
mvn clean package -DskipTests

# 4. 启动服务
java -jar target/jiedan-backend-1.0.0.jar
```

### 一键查看服务状态
```powershell
# 检查8000端口
Get-NetTCPConnection -LocalPort 8000 | Select-Object LocalPort, OwningProcess, @{Name="ProcessName";Expression={(Get-Process -Id $_.OwningProcess).ProcessName}}, State
```

---

## 十二、快捷键

### PowerShell 快捷键
- `Tab` - 自动补全
- `Ctrl+C` - 复制 / 取消当前命令
- `Ctrl+V` - 粘贴
- `↑` / `↓` - 浏览历史命令
- `Ctrl+L` / `cls` - 清屏

---

## 常用端口速查

| 服务 | 端口 |
|-----|------|
| 后端服务 | 8000 |
| H2 数据库 | 8000/h2-console |
| LiveReload | 35729 |
| MySQL | 3306 |

---

## 快速访问地址

| 服务 | 地址 |
|-----|------|
| 后端API | http://localhost:8000/api |
| H2数据库 | http://localhost:8000/h2-console |
| 仪表盘 | http://localhost:8080/dashboard.html |

---

## 项目结构

```
jiedan_auto_get_clients/
├── springboot-backend/     # Spring Boot 后端
│   └── src/main/java/com/jiedan/
├── miniprogram/           # 微信小程序
│   ├── pages/
│   └── utils/
├── frontend/              # 网页前端
└── .trae/                # 文档
    ├── specs/            # 规格文档
    └── documents/        # 其他文档
```

---

> 提示：PowerShell 中可以使用 `Get-Help <命令>` 查看命令帮助，如 `Get-Help Get-Process`
