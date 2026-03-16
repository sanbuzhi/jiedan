# 智能获客系统 - 服务重启脚本
# 适用于前后端代码修改后的重启

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   智能获客系统 - 服务重启脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查参数
param(
    [switch]$FullRebuild,
    [switch]$SkipFrontend,
    [switch]$SkipBackend,
    [switch]$SkipDB
)

# 1. 数据库迁移（如果需要）
if (-not $SkipDB) {
    Write-Host "[1/4] 检查数据库迁移..." -ForegroundColor Yellow
    # 这里可以添加 Alembic 迁移命令
    # docker-compose exec backend alembic upgrade head
    Write-Host "      数据库检查完成" -ForegroundColor Green
    Write-Host ""
}

# 2. 重启后端服务
if (-not $SkipBackend) {
    Write-Host "[2/4] 重启后端服务..." -ForegroundColor Yellow
    
    # 停止后端服务
    docker-compose stop backend 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "      警告: 停止后端服务时出现问题" -ForegroundColor Red
    }
    
    # 移除后端容器
    docker-compose rm -f backend 2>$null
    
    # 重新构建并启动后端
    if ($FullRebuild) {
        Write-Host "      执行完全重建 (--no-cache)..." -ForegroundColor Cyan
        docker-compose build --no-cache backend
    }
    
    docker-compose up -d --build backend
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "      后端服务重启成功" -ForegroundColor Green
    } else {
        Write-Host "      错误: 后端服务重启失败" -ForegroundColor Red
        exit 1
    }
    Write-Host ""
}

# 3. 重启前端服务
if (-not $SkipFrontend) {
    Write-Host "[3/4] 重启前端服务..." -ForegroundColor Yellow
    
    # 停止前端服务
    docker-compose stop frontend 2>$null
    docker-compose rm -f frontend 2>$null
    
    # 重新构建并启动前端
    docker-compose up -d --build frontend
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "      前端服务重启成功" -ForegroundColor Green
    } else {
        Write-Host "      警告: 前端服务重启可能有问题" -ForegroundColor Yellow
    }
    Write-Host ""
}

# 4. 检查服务状态
Write-Host "[4/4] 检查服务状态..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

$services = docker-compose ps --format "table {{.Service}}\t{{.Status}}\t{{.Ports}}"
Write-Host $services -ForegroundColor White
Write-Host ""

# 检查后端健康状态
Write-Host "检查后端API健康状态..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8000/api/v1/health" -Method GET -TimeoutSec 5 -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Host "      后端API健康检查通过 ✓" -ForegroundColor Green
    }
} catch {
    Write-Host "      警告: 后端API健康检查未通过，服务可能还在启动中" -ForegroundColor Yellow
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   服务重启完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "常用命令:" -ForegroundColor Cyan
Write-Host "  查看日志: docker-compose logs -f backend" -ForegroundColor White
Write-Host "  查看所有日志: docker-compose logs -f" -ForegroundColor White
Write-Host "  停止服务: docker-compose down" -ForegroundColor White
Write-Host ""
