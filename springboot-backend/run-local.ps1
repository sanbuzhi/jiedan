# Spring Boot 本地运行脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Spring Boot 本地运行脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查 Maven 是否安装
try {
    $mavenVersion = mvn -version 2>&1 | Select-String "Apache Maven"
    if ($mavenVersion) {
        Write-Host "✓ Maven 已安装: $mavenVersion" -ForegroundColor Green
    } else {
        Write-Host "✗ Maven 未安装，请先安装 Maven" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ Maven 未安装或不在 PATH 中" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[1/3] 清理并编译项目..." -ForegroundColor Yellow
mvn clean compile

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ 编译失败" -ForegroundColor Red
    exit 1
}
Write-Host "✓ 编译成功" -ForegroundColor Green

Write-Host ""
Write-Host "[2/3] 运行测试..." -ForegroundColor Yellow
mvn test

if ($LASTEXITCODE -ne 0) {
    Write-Host "⚠ 测试有失败，但仍继续运行" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "[3/3] 打包应用..." -ForegroundColor Yellow
mvn package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ 打包失败" -ForegroundColor Red
    exit 1
}
Write-Host "✓ 打包成功" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   启动 Spring Boot 应用" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "应用将启动在: http://localhost:8080/api" -ForegroundColor White
Write-Host "健康检查: http://localhost:8080/api/v1/health" -ForegroundColor White
Write-Host ""
Write-Host "按 Ctrl+C 停止应用" -ForegroundColor Yellow
Write-Host ""

# 运行应用
java -jar target/*.jar
