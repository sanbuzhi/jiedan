#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
调试任务书解析
"""

import requests
import json

BASE_URL = "http://localhost:8081/api"
HEADERS = {"Content-Type": "application/json"}

# 加载任务书
with open("d:\\jiedan_auto_get_clients\\springboot-backend\\projects\\54\\task\\TASKS.md", 'r', encoding='utf-8') as f:
    task_doc = f.read()[:3000]

url = f"{BASE_URL}/api/ai/code/parse-task"
payload = {
    "projectId": "test-project-001",
    "taskDoc": task_doc
}

print("请求URL:", url)
print("请求体:", json.dumps(payload, ensure_ascii=False)[:500])

try:
    response = requests.post(url, json=payload, headers=HEADERS, timeout=30)
    print("\n响应状态:", response.status_code)
    print("响应内容:", response.text[:2000])
    
    result = response.json()
    print("\n解析结果:")
    print("  code:", result.get("code"))
    print("  message:", result.get("message"))
    print("  data:", result.get("data"))
except Exception as e:
    print(f"错误: {e}")
