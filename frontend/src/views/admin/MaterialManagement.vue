<template>
  <div class="material-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>素材管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleRefresh">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-empty description="素材管理功能开发中" />
    </el-card>
  </div>
</template>

<script setup>
import { Refresh } from '@element-plus/icons-vue'

function handleRefresh() {
  // 刷新数据
}
</script>

<style scoped>
.material-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}
</style>
