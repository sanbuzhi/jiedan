# AI开发机制验证测试脚本
# 测试步骤10-11的功能（不调用真实AI，只测试框架功能）

$BASE_URL = "http://localhost:8081/api"
$PROJECT_ID = "test-project-54"

Write-Host "=== AI开发机制验证测试 ===" -ForegroundColor Green
Write-Host ""

# 1. 测试任务书解析接口
Write-Host "1. 测试任务书解析接口..." -ForegroundColor Yellow
$taskDoc = @"
# 项目任务书

## 数据库设计
- 用户表(user): id, username, password, email
- 订单表(order): id, user_id, amount, status

## 后端模块
### user模块
- UserController: 用户CRUD接口
- UserService: 用户业务逻辑
- UserRepository: 用户数据访问

### order模块
- OrderController: 订单CRUD接口
- OrderService: 订单业务逻辑
- OrderRepository: 订单数据访问

## 前端页面
### user页面
- user/list: 用户列表页
- user/detail: 用户详情页

### order页面
- order/list: 订单列表页
- order/detail: 订单详情页
"@

try {
    $body = @{
        projectId = $PROJECT_ID
        taskDoc = $taskDoc
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$BASE_URL/ai/code/parse-task" -Method POST -ContentType "application/json" -Body $body -TimeoutSec 30
    Write-Host "   任务书解析结果:" -ForegroundColor Cyan
    Write-Host "   - 成功: $($response.success)"
    Write-Host "   - 模块数量: $($response.data.modules.Count)"
    if ($response.data.modules.Count -gt 0) {
        Write-Host "   - 模块列表:"
        foreach ($module in $response.data.modules) {
            Write-Host "     * $($module.name) [$($module.type)]"
        }
    }
    Write-Host "   [PASS] 任务书解析接口测试通过" -ForegroundColor Green
} catch {
    Write-Host "   [FAIL] 任务书解析接口测试失败: $_" -ForegroundColor Red
}
Write-Host ""

# 2. 测试大项目批次生成启动
Write-Host "2. 测试大项目批次生成启动..." -ForegroundColor Yellow
$requirementDoc = @"
# 轻量级自用酒店管理系统需求文档
## 需求概述
家庭式民宿/1-30间房的单体快捷酒店管理系统

## 功能模块
1. 登录与权限管理
2. 基础信息管理（房型、房间、商品）
3. 房态管理
4. 订单管理
5. 入住退房与消费记账
6. 会员管理
7. 基础报表统计
"@

try {
    $body = @{
        requirementDoc = $requirementDoc
        taskDoc = $taskDoc
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$BASE_URL/ai/code/large-project/start/$PROJECT_ID" -Method POST -ContentType "application/json" -Body $body -TimeoutSec 30
    Write-Host "   大项目生成启动结果:" -ForegroundColor Cyan
    Write-Host "   - 成功: $($response.success)"
    Write-Host "   - 项目ID: $($response.data.projectId)"
    Write-Host "   - 总批次数: $($response.data.totalBatches)"
    Write-Host "   - 当前批次: $($response.data.currentBatchIndex)"
    Write-Host "   - 已完成模块: $($response.data.completedModules.Count)"
    Write-Host "   [PASS] 大项目批次生成启动测试通过" -ForegroundColor Green
} catch {
    Write-Host "   [FAIL] 大项目批次生成启动测试失败: $_" -ForegroundColor Red
}
Write-Host ""

# 3. 测试获取批次状态
Write-Host "3. 测试获取批次状态..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/ai/code/large-project/status/$PROJECT_ID" -Method GET -TimeoutSec 10
    Write-Host "   批次状态结果:" -ForegroundColor Cyan
    Write-Host "   - 成功: $($response.success)"
    if ($response.data) {
        Write-Host "   - 项目ID: $($response.data.projectId)"
        Write-Host "   - 进度: $($response.data.progress)%"
        Write-Host "   - 已完成: $($response.data.completed)"
    }
    Write-Host "   [PASS] 获取批次状态测试通过" -ForegroundColor Green
} catch {
    Write-Host "   [FAIL] 获取批次状态测试失败: $_" -ForegroundColor Red
}
Write-Host ""

# 4. 测试依赖分析
Write-Host "4. 测试依赖分析..." -ForegroundColor Yellow
try {
    $body = @{
        projectPath = "d:\jiedan_auto_get_clients"
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$BASE_URL/ai/code/analyze-dependencies/$PROJECT_ID" -Method POST -ContentType "application/json" -Body $body -TimeoutSec 60
    Write-Host "   依赖分析结果:" -ForegroundColor Cyan
    Write-Host "   - 成功: $($response.success)"
    if ($response.data) {
        Write-Host "   - 模块数量: $($response.data.modules.Count)"
        Write-Host "   - 依赖数量: $($response.data.dependencies.Count)"
        if ($response.data.modules.Count -gt 0) {
            Write-Host "   - 发现的模块:"
            foreach ($module in $response.data.modules.PSObject.Properties) {
                Write-Host "     * $($module.Name): $($module.Value.files.Count) 个文件"
            }
        }
    }
    Write-Host "   [PASS] 依赖分析测试通过" -ForegroundColor Green
} catch {
    Write-Host "   [FAIL] 依赖分析测试失败: $_" -ForegroundColor Red
}
Write-Host ""

# 5. 测试变更影响分析
Write-Host "5. 测试变更影响分析..." -ForegroundColor Yellow
try {
    $body = @{
        changedModule = "backend"
        changedClasses = @("UserService", "OrderService")
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$BASE_URL/ai/code/an