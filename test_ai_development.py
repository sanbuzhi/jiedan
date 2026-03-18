#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
AI开发机制完整测试脚本
使用项目54的真实需求文档和任务书进行测试
"""

import requests
import json
import time
import os
from datetime import datetime

# API配置
# 注意: application.yml中设置了 context-path: /api
# Controller的RequestMapping是 /api/ai/code
# 所以完整路径是 /api/api/ai/code/...
BASE_URL = "http://localhost:8081"
HEADERS = {
    "Content-Type": "application/json"
}

# 项目文件路径
PROJECT_54_REQ = "d:\\jiedan_auto_get_clients\\springboot-backend\\projects\\54\\req\\REQUIREMENT.md"
PROJECT_54_TASK = "d:\\jiedan_auto_get_clients\\springboot-backend\\projects\\54\\task\\TASKS.md"

def load_file(filepath):
    """加载文件内容"""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            return f.read()
    except Exception as e:
        print(f"❌ 加载文件失败 {filepath}: {e}")
        return None

def test_parse_task_document():
    """测试1: 任务书解析接口"""
    print("\n" + "="*60)
    print("测试1: 任务书解析接口")
    print("="*60)
    
    task_doc = load_file(PROJECT_54_TASK)
    if not task_doc:
        return False
    
    # 只取前3000字符作为测试（避免token过多）
    task_doc_short = task_doc[:3000]
    
    url = f"{BASE_URL}/api/api/ai/code/parse-task"
    payload = {
        "projectId": "test-project-001",
        "taskDoc": task_doc_short
    }
    
    try:
        response = requests.post(url, json=payload, headers=HEADERS, timeout=30)
        result = response.json()
        
        if response.status_code == 200 and result.get("code") == 0:
            print("✅ 任务书解析成功")
            data = result.get("data", {})
            modules = data.get("modules", [])
            print(f"   识别到 {len(modules)} 个模块")
            for i, module in enumerate(modules[:5]):  # 只显示前5个
                print(f"   - {module.get('name', 'N/A')} ({module.get('type', 'N/A')})")
            return True
        else:
            print(f"❌ 任务书解析失败: {result.get('message', 'Unknown error')}")
            return False
    except Exception as e:
        print(f"❌ 请求异常: {e}")
        return False

def test_analyze_dependencies():
    """测试2: 依赖分析接口"""
    print("\n" + "="*60)
    print("测试2: 依赖分析接口")
    print("="*60)
    
    # 使用当前项目路径作为测试
    project_path = "d:\\jiedan_auto_get_clients"
    
    url = f"{BASE_URL}/api/api/ai/code/analyze-dependencies/test-deps-001"
    payload = {
        "projectPath": project_path
    }
    
    try:
        response = requests.post(url, json=payload, headers=HEADERS, timeout=60)
        result = response.json()
        
        if response.status_code == 200 and result.get("code") == 0:
            print("✅ 依赖分析成功")
            data = result.get("data", {})
            modules = data.get("modules", {})
            dependencies = data.get("dependencies", [])
            print(f"   发现 {len(modules)} 个模块")
            print(f"   发现 {len(dependencies)} 条依赖关系")
            for name, module in modules.items():
                print(f"   - {name}: {len(module.get('files', []))} 个文件")
            return True
        else:
            print(f"❌ 依赖分析失败: {result.get('message', 'Unknown error')}")
            return False
    except Exception as e:
        print(f"❌ 请求异常: {e}")
        return False

def test_analyze_impact():
    """测试3: 变更影响分析接口"""
    print("\n" + "="*60)
    print("测试3: 变更影响分析接口")
    print("="*60)
    
    url = f"{BASE_URL}/api/api/ai/code/analyze-impact/test-impact-001"
    payload = {
        "changedModule": "backend",
        "changedClasses": ["UserService", "OrderService"]
    }
    
    try:
        response = requests.post(url, json=payload, headers=HEADERS, timeout=30)
        result = response.json()
        
        if response.status_code == 200 and result.get("code") == 0:
            print("✅ 影响分析成功")
            data = result.get("data", {})
            changed_module = data.get("changedModule", "N/A")
            impacted = data.get("impactedModules", [])
            print(f"   变更模块: {changed_module}")
            print(f"   受影响模块数: {len(impacted)}")
            for mod in impacted:
                print(f"   - {mod.get('moduleName', 'N/A')} (严重程度: {mod.get('severity', 0)}/5)")
            return True
        else:
            print(f"❌ 影响分析失败: {result.get('message', 'Unknown error')}")
            return False
    except Exception as e:
        print(f"❌ 请求异常: {e}")
        return False

def test_large_project_start():
    """测试4: 大项目分批次生成 - 启动"""
    print("\n" + "="*60)
    print("测试4: 大项目分批次生成 - 启动")
    print("="*60)
    
    req_doc = load_file(PROJECT_54_REQ)
    task_doc = load_file(PROJECT_54_TASK)
    
    if not req_doc or not task_doc:
        print("❌ 无法加载需求文档或任务书")
        return False
    
    # 只取部分内容进行测试
    req_doc_short = req_doc[:2000]
    task_doc_short = task_doc[:2000]
    
    url = f"{BASE_URL}/api/api/ai/code/large-project/start/test-large-001"
    payload = {
        "requirementDoc": req_doc_short,
        "taskDoc": task_doc_short
    }
    
    try:
        response = requests.post(url, json=payload, headers=HEADERS, timeout=60)
        result = response.json()
        
        if response.status_code == 200 and result.get("code") == 0:
            print("✅ 大项目生成启动成功")
            data = result.get("data", {})
            print(f"   项目ID: {data.get('projectId', 'N/A')}")
            print(f"   总批次数: {data.get('totalBatches', 0)}")
            print(f"   当前批次: {data.get('currentBatchIndex', 0)}")
            print(f"   进度: {data.get('progress', 0)}%")
            return True
        else:
            print(f"❌ 启动失败: {result.get('message', 'Unknown error')}")
            return False
    except Exception as e:
        print(f"❌ 请求异常: {e}")
        return False

def test_large_project_status():
    """测试5: 大项目分批次生成 - 获取状态"""
    print("\n" + "="*60)
    print("测试5: 大项目分批次生成 - 获取状态")
    print("="*60)
    
    url = f"{BASE_URL}/api/api/ai/code/large-project/status/test-large-001"
    
    try:
        response = requests.get(url, headers=HEADERS, timeout=30)
        result = response.json()
        
        if response.status_code == 200 and result.get("code") == 0:
            print("✅ 获取状态成功")
            data = result.get("data", {})
            print(f"   项目ID: {data.get('projectId', 'N/A')}")
            print(f"   是否完成: {data.get('completed', False)}")
            print(f"   进度: {data.get('progress', 0)}%")
            completed_modules = data.get('completedModules', [])
            print(f"   已完成模块: {completed_modules}")
            return True
        else:
            print(f"❌ 获取状态失败: {result.get('message', 'Unknown error')}")
            return False
    except Exception as e:
        print(f"❌ 请求异常: {e}")
        return False

def test_auto_update_create():
    """测试6: 自动更新任务创建"""
    print("\n" + "="*60)
    print("测试6: 自动更新任务创建")
    print("="*60)
    
    url = f"{BASE_URL}/api/api/ai/code/auto-update/create/test-update-001"
    payload = {
        "moduleName": "backend",
        "projectPath": "d:\\jiedan_auto_get_clients"
    }
    
    try:
        response = requests.post(url, json=payload, headers=HEADERS, timeout=30)
        result = response.json()
        
        if response.status_code == 200:
            print("✅ 自动更新任务创建成功")
            data = result.get("data")
            if data:
                print(f"   任务ID: {data.get('taskId', 'N/A')}")
                print(f"   触发模块: {data.get('triggerModule', 'N/A')}")
                print(f"   状态: {data.get('status', 'N/A')}")
            else:
                print("   未检测到变更")
            return True
        else:
            print(f"❌ 创建失败: {result.get('message', 'Unknown error')}")
            return False
    except Exception as e:
        print(f"❌ 请求异常: {e}")
        return False

def run_all_tests():
    """运行所有测试"""
    print("\n" + "="*60)
    print("AI开发机制完整测试")
    print(f"测试时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("="*60)
    
    results = []
    
    # 测试1: 任务书解析
    results.append(("任务书解析", test_parse_task_document()))
    
    # 测试2: 依赖分析
    results.append(("依赖分析", test_analyze_dependencies()))
    
    # 测试3: 变更影响分析
    results.append(("变更影响分析", test_analyze_impact()))
    
    # 测试4: 大项目生成启动
    results.append(("大项目生成启动", test_large_project_start()))
    
    # 测试5: 大项目状态查询
    results.append(("大项目状态查询", test_large_project_status()))
    
    # 测试6: 自动更新任务创建
    results.append(("自动更新任务创建", test_auto_update_create()))
    
    # 打印测试总结
    print("\n" + "="*60)
    print("测试总结")
    print("="*60)
    
    passed = sum(1 for _, result in results if result)
    total = len(results)
    
    for name, result in results:
        status = "✅ 通过" if result else "❌ 失败"
        print(f"{status}: {name}")
    
    print(f"\n总计: {passed}/{total} 通过 ({passed/total*100:.1f}%)")
    
    if passed == total:
        print("\n🎉 所有测试通过！AI开发机制工作正常。")
    else:
        print(f"\n⚠️ 有 {total - passed} 个测试失败，请检查日志。")

if __name__ == "__main__":
    # 检查服务是否运行
    try:
        response = requests.get("http://localhost:8081/api/api/health", timeout=5)
        print("✅ 后端服务已连接")
    except:
        print("⚠️ 无法连接后端服务，请确保服务已启动在端口8081")
        print("   启动命令: java -jar target/jiedan-backend-1.0.0.jar --server.port=8081")
        exit(1)
    
    run_all_tests()
