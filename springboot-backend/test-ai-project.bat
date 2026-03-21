@echo off
REM AI异步阶段式代码生成系统 - 测试脚本
REM 测试项目：项目67

set BASE_URL=http://localhost:8080

echo ========================================
echo AI异步阶段式代码生成系统 - 测试脚本
echo ========================================
echo.

echo [测试1] 启动项目开发 (项目67)
echo 请求: POST /api/ai/code/project/start
echo.
curl -X POST "%BASE_URL%/api/ai/code/project/start" ^
  -H "Content-Type: application/json" ^
  -d "{\"requirementId\": 67}" ^
  -w "\nHTTP Status: %%{http_code}\n" ^
  -s

echo.
echo [测试2] 查询项目状态
echo 请求: GET /api/ai/code/project/67/status
echo.
curl -X GET "%BASE_URL%/api/ai/code/project/67/status" ^
  -w "\nHTTP Status: %%{http_code}\n" ^
  -s

echo.
echo [测试3] 查询阶段1状态
echo 请求: GET /api/ai/code/project/67/phase/1/status
echo.
curl -X GET "%BASE_URL%/api/ai/code/project/67/phase/1/status" ^
  -w "\nHTTP Status: %%{http_code}\n" ^
  -s

echo.
echo [测试4] 查询阶段1进度文档
echo 请求: GET /api/ai/code/project/67/phase/1/progress
echo.
curl -X GET "%BASE_URL%/api/ai/code/project/67/phase/1/progress" ^
  -w "\nHTTP Status: %%{http_code}\n" ^
  -s

echo.
echo ========================================
echo 测试完成
echo ========================================
