@echo off
chcp 65001 >nul
echo ========================================
echo    智能获客系统 - 快速重启
echo ========================================
echo.

:: 默认重启后端
echo [1/3] 停止后端服务...
docker-compose stop backend

echo [2/3] 重新构建并启动后端...
docker-compose up -d --build backend

echo [3/3] 检查服务状态...
timeout /t 3 /nobreak >nul
docker-compose ps

echo.
echo ========================================
echo    重启完成！
echo ========================================
echo.
echo 查看日志: docker-compose logs -f backend
echo.
