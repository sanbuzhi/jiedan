<template>
  <div class="referral-tree-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>推荐关系</span>
        </div>
      </template>
      <div class="tree-content">
        <el-empty v-if="!treeData" description="暂无推荐关系" />
        <el-tree
          v-else
          :data="treeData"
          :props="treeProps"
          node-key="id"
          default-expand-all
          :expand-on-click-node="false"
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <el-icon v-if="data.level === 1" color="#409eff"><UserFilled /></el-icon>
              <el-icon v-else-if="data.level === 2" color="#67c23a"><User /></el-icon>
              <el-icon v-else color="#e6a23c"><User /></el-icon>
              <span class="node-label">
                {{ data.label }}
                <el-tag size="small" style="margin-left: 10px;">
                  {{ data.level === 1 ? '一级' : data.level === 2 ? '二级' : '三级' }}
                </el-tag>
              </span>
            </div>
          </template>
        </el-tree>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/utils/api'
import { User, UserFilled } from '@element-plus/icons-vue'

const treeData = ref(null)
const treeProps = {
  children: 'children',
  label: 'label'
}

function transformTreeData(item) {
  const result = {
    id: item.user.id,
    label: item.user.nickname || item.user.phone,
    level: item.level,
    children: []
  }
  
  if (item.children && item.children.length > 0) {
    result.children = item.children.map(child => transformTreeData(child))
  }
  
  return result
}

onMounted(async () => {
  try {
    const data = await api.getReferralTree()
    treeData.value = [transformTreeData(data)]
  } catch (error) {
    console.error('获取推荐树失败:', error)
  }
})
</script>

<style scoped>
.referral-tree-container {
  max-width: 800px;
}

.card-header {
  font-weight: 500;
  font-size: 16px;
}

.tree-content {
  padding: 20px 0;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 0;
}

.node-label {
  font-size: 14px;
}
</style>
