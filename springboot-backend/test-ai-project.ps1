# AI异步阶段式代码生成系统 - 测试脚本
# 测试项目：项目68
# 使用真正的异步轮询模式，支持长时间运行

$BASE_URL = "http://localhost:8080/v1"
$POLL_INTERVAL = 10  # 轮询间隔（秒）
$MAX_POLL_COUNT = 3600  # 最大轮询次数（可支持1小时）

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "AI异步阶段式代码生成系统 - 测试脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: 启动项目开发
Write-Host "[测试1] 启动项目开发" -ForegroundColor Yellow
Write-Host "请求: POST /v1/ai/code/project/start" -ForegroundColor Gray
Write-Host ""

$body = @{
    requirementId = 70
} | ConvertTo-Json

try {
    $startResponse = Invoke-RestMethod -Uri "$BASE_URL/ai/code/project/start" `
        -Method Post `
        -Body $body `
        -ContentType "application/json" `
        -TimeoutSec 30

    $startResponse | ConvertTo-Json -Depth 10
    Write-Host ""

    if ($startResponse.code -eq 0) {
        $projectId = $startResponse.data.projectId
        Write-Host "项目启动成功, projectId: $projectId" -ForegroundColor Green
        Write-Host ""
    } else {
        Write-Host "项目启动失败: $($startResponse.message)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "启动请求失败: $_" -ForegroundColor Red
    exit 1
}

# Test 2: 异步轮询项目状态
Write-Host "[测试2] 开始轮询项目状态" -ForegroundColor Yellow
Write-Host "轮询间隔: ${POLL_INTERVAL}秒, 最大轮询次数: ${MAX_POLL_COUNT}" -ForegroundColor Gray
Write-Host ""

$pollCount = 0
$lastPhase = 0
$lastRound = 0

while ($pollCount -lt $MAX_POLL_COUNT) {
    $pollCount++

    try {
        $status = Invoke-RestMethod -Uri "$BASE_URL/ai/code/project/$projectId/status" `
            -Method Get `
            -TimeoutSec 30

        if ($status.code -ne 0) {
            Write-Host "[$pollCount] 查询失败: $($status.message)" -ForegroundColor Yellow
            Start-Sleep -Seconds $POLL_INTERVAL
            continue
        }

        $projectStatus = $status.data.status
        $currentPhase = $status.data.currentPhase
        $progress = $status.data.progress
        $totalFiles = $status.data.totalFiles

        # 显示状态变化
        $phaseChanged = $currentPhase -ne $lastPhase
        if ($phaseChanged) {
            Write-Host ""
            Write-Host "========== 阶段 $currentPhase 开始 ==========" -ForegroundColor Cyan
            $lastPhase = $currentPhase
            $lastRound = 0
        }

        # 显示当前状态
        $timestamp = Get-Date -Format "HH:mm:ss"
        Write-Host "[$timestamp] [$pollCount] 阶段:$currentPhase 进度:$progress% 文件数:$totalFiles 状态:$projectStatus" -ForegroundColor White

        # 检查是否完成
        if ($projectStatus -eq "COMPLETED") {
            Write-Host ""
            Write-Host "========== 项目完成！==========" -ForegroundColor Green
            break
        }

        if ($projectStatus -eq "FAILED") {
            Write-Host ""
            Write-Host "========== 项目失败！==========" -ForegroundColor Red
            break
        }

        # 每10次轮询显示详细阶段信息
        if ($pollCount % 10 -eq 0 -and $status.data.phases) {
            Write-Host ""
            Write-Host "--- 阶段详情 ---" -ForegroundColor Gray
            foreach ($phase in $status.data.phases) {
                $phaseInfo = "阶段$($phase.phase) [$($phase.name)]: $($phase.status)"
                if ($phase.recentFiles -and $phase.recentFiles.Count -gt 0) {
                    $phaseInfo += " 最近文件: $($phase.recentFiles[0])"
                }
                Write-Host "  $phaseInfo" -ForegroundColor Gray
            }
            Write-Host ""
        }

    } catch {
        Write-Host "[$pollCount] 查询异常: $_" -ForegroundColor Yellow
    }

    Start-Sleep -Seconds $POLL_INTERVAL
}

if ($pollCount -ge $MAX_POLL_COUNT) {
    Write-Host ""
    Write-Host "达到最大轮询次数，停止轮询" -ForegroundColor Yellow
}

# Test 3: 查询最终项目状态
Write-Host ""
Write-Host "[测试3] 查询最终项目状态" -ForegroundColor Yellow
Write-Host ""

try {
    $finalStatus = Invoke-RestMethod -Uri "$BASE_URL/ai/code/project/$projectId/status" `
        -Method Get `
        -TimeoutSec 30
    $finalStatus | ConvertTo-Json -Depth 10
} catch {
    Write-Host "查询最终状态失败: $_" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "测试完成，总轮询次数: $pollCount" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
