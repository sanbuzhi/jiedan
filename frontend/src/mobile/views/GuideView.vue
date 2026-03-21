<template>
  <div class="guide-container">
    <div class="header-decoration">
      <div class="gradient-bg"></div>
      <div class="circle-decoration circle-1"></div>
      <div class="circle-decoration circle-2"></div>
      <div class="circle-decoration circle-3"></div>
    </div>

    <div class="title-section">
      <div class="logo-wrapper">
        <div class="logo-icon">接</div>
      </div>
      <h1 class="main-title">AI自助开发系统</h1>
      <p class="sub-title">智能驱动，让开发更简单</p>
    </div>

    <div class="content-scroll">
      <div class="section">
        <div class="section-header">
          <span class="section-icon">✨</span>
          <span class="section-title">功能介绍</span>
        </div>
        <div class="section-content">
          <div class="feature-item">
            <div class="feature-icon">🤖</div>
            <div class="feature-text">
              <span class="feature-name">AI智能分析</span>
              <span class="feature-desc">基于大语言模型，智能分析您的需求并生成解决方案</span>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">⚡</div>
            <div class="feature-text">
              <span class="feature-name">快速开发</span>
              <span class="feature-desc">自动化代码生成，大幅缩短项目开发周期</span>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">🎯</div>
            <div class="feature-text">
              <span class="feature-name">精准匹配</span>
              <span class="feature-desc">智能匹配最适合的技术方案和开发资源</span>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">🔒</div>
            <div class="feature-text">
              <span class="feature-name">安全可靠</span>
              <span class="feature-desc">企业级安全保障，保护您的数据和隐私</span>
            </div>
          </div>
        </div>
      </div>

      <div class="section">
        <div class="section-header">
          <span class="section-icon">📋</span>
          <span class="section-title">使用流程</span>
        </div>
        <div class="section-content">
          <div class="flow-container">
            <div class="flow-item">
              <div class="flow-number">1</div>
              <div class="flow-content">
                <span class="flow-title">提交需求</span>
                <span class="flow-desc">描述您的项目需求和期望目标</span>
              </div>
            </div>
            <div class="flow-arrow">↓</div>
            <div class="flow-item">
              <div class="flow-number">2</div>
              <div class="flow-content">
                <span class="flow-title">AI分析</span>
                <span class="flow-desc">系统自动分析并生成技术方案</span>
              </div>
            </div>
            <div class="flow-arrow">↓</div>
            <div class="flow-item">
              <div class="flow-number">3</div>
              <div class="flow-content">
                <span class="flow-title">确认方案</span>
                <span class="flow-desc">查看并确认AI生成的开发方案</span>
              </div>
            </div>
            <div class="flow-arrow">↓</div>
            <div class="flow-item">
              <div class="flow-number">4</div>
              <div class="flow-content">
                <span class="flow-title">开始开发</span>
                <span class="flow-desc">系统自动执行开发任务并交付成果</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="section">
        <div class="section-header">
          <span class="section-icon">📌</span>
          <span class="section-title">用户须知</span>
        </div>
        <div class="section-content">
          <div class="notice-list">
            <div class="notice-item">
              <span class="notice-dot">•</span>
              <span class="notice-text">请确保提供的需求描述清晰、完整，以便AI准确理解您的意图</span>
            </div>
            <div class="notice-item">
              <span class="notice-dot">•</span>
              <span class="notice-text">AI生成的方案仅供参考，最终决策权在于用户</span>
            </div>
            <div class="notice-item">
              <span class="notice-dot">•</span>
              <span class="notice-text">开发过程中可能需要您提供额外的信息或进行确认</span>
            </div>
            <div class="notice-item">
              <span class="notice-dot">•</span>
              <span class="notice-text">建议定期查看项目进度，及时反馈问题</span>
            </div>
            <div class="notice-item">
              <span class="notice-dot">•</span>
              <span class="notice-text">如遇到问题，可通过"我的"页面联系客服支持</span>
            </div>
          </div>
        </div>
      </div>

      <div class="section disclaimer-section">
        <div class="section-header">
          <span class="section-icon">⚠️</span>
          <span class="section-title">免责声明</span>
        </div>
        <div class="section-content">
          <div class="disclaimer-content">
            <span class="disclaimer-text">1. 本系统提供的AI生成内容仅供参考，不构成专业建议。</span>
            <span class="disclaimer-text">2. 用户应对使用本系统产生的结果自行判断和承担责任。</span>
            <span class="disclaimer-text">3. 系统可能会因网络、技术等原因出现延迟或故障，敬请谅解。</span>
            <span class="disclaimer-text">4. 我们保留随时修改、暂停或终止服务的权利。</span>
            <span class="disclaimer-text">5. 使用本系统即表示您同意我们的服务条款和隐私政策。</span>
          </div>
        </div>
      </div>

      <div class="bottom-space"></div>
    </div>

    <div class="button-section">
      <button class="btn-primary" @click="onExperienceTap">体验使用</button>
      <button class="btn-secondary" @click="onLoginTap">登录/注册</button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const onExperienceTap = () => {
  router.push('/client/requirement/step_all')
}

const onLoginTap = () => {
  router.push('/client/login')
}
</script>

<style scoped>
.guide-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  position: relative;
  overflow: hidden;
  background-color: #f5f7fa;
}

.header-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 280px;
  overflow: hidden;
  z-index: 0;
}

.gradient-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 100%;
  background: linear-gradient(135deg, #07c160 0%, #10b981 50%, #059669 100%);
  border-radius: 0 0 60px 60px;
}

.circle-decoration {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.circle-1 {
  width: 200px;
  height: 200px;
  top: -60px;
  right: -40px;
}

.circle-2 {
  width: 120px;
  height: 120px;
  top: 80px;
  right: 120px;
}

.circle-3 {
  width: 80px;
  height: 80px;
  bottom: 40px;
  left: 60px;
}

.title-section {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 40px 40px;
  margin-bottom: 20px;
}

.logo-wrapper {
  width: 120px;
  height: 120px;
  background: #ffffff;
  border-radius: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(7, 193, 96, 0.2);
}

.logo-icon {
  font-size: 40px;
  font-weight: bold;
  color: #07c160;
}

.main-title {
  font-size: 48px;
  font-weight: bold;
  color: #ffffff;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 12px;
}

.sub-title {
  font-size: 28px;
  color: rgba(255, 255, 255, 0.9);
}

.content-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 0 30px;
  box-sizing: border-box;
  -webkit-overflow-scrolling: touch;
}

.section {
  background: #ffffff;
  border-radius: 24px;
  margin-bottom: 24px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
}

.section-icon {
  font-size: 36px;
  margin-right: 16px;
}

.section-title {
  font-size: 32px;
  font-weight: 600;
  color: #1f2937;
}

.section-content {
  padding: 0 8px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 28px;
}

.feature-item:last-child {
  margin-bottom: 0;
}

.feature-icon {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  margin-right: 20px;
  flex-shrink: 0;
}

.feature-text {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.feature-name {
  font-size: 30px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.feature-desc {
  font-size: 26px;
  color: #6b7280;
  line-height: 1.5;
}

.flow-container {
  display: flex;
  flex-direction: column;
}

.flow-item {
  display: flex;
  align-items: center;
  padding: 20px 0;
}

.flow-number {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #07c160 0%, #10b981 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 28px;
  font-weight: bold;
  margin-right: 24px;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(7, 193, 96, 0.3);
}

.flow-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.flow-title {
  font-size: 30px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 6px;
}

.flow-desc {
  font-size: 26px;
  color: #6b7280;
}

.flow-arrow {
  text-align: center;
  color: #07c160;
  font-size: 32px;
  padding: 8px 0;
  opacity: 0.6;
}

.notice-list {
  display: flex;
  flex-direction: column;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 20px;
}

.notice-item:last-child {
  margin-bottom: 0;
}

.notice-dot {
  color: #07c160;
  font-size: 32px;
  margin-right: 16px;
  line-height: 1.4;
  flex-shrink: 0;
}

.notice-text {
  font-size: 28px;
  color: #4b5563;
  line-height: 1.6;
  flex: 1;
}

.disclaimer-section {
  background: linear-gradient(135deg, #fefce8 0%, #fef9c3 100%);
  border: 2px solid #fde047;
}

.disclaimer-content {
  display: flex;
  flex-direction: column;
}

.disclaimer-text {
  font-size: 26px;
  color: #854d0e;
  line-height: 1.7;
  margin-bottom: 12px;
}

.disclaimer-text:last-child {
  margin-bottom: 0;
}

.bottom-space {
  height: 40px;
}

.button-section {
  position: relative;
  z-index: 1;
  background: #ffffff;
  padding: 24px 40px;
  padding-bottom: calc(24px + env(safe-area-inset-bottom));
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.btn-primary {
  background: linear-gradient(135deg, #07c160 0%, #10b981 100%);
  color: #ffffff;
  font-size: 32px;
  font-weight: 600;
  height: 96px;
  line-height: 96px;
  border-radius: 48px;
  border: none;
  box-shadow: 0 8px 24px rgba(7, 193, 96, 0.35);
}

.btn-primary:active {
  transform: scale(0.98);
  opacity: 0.9;
}

.btn-secondary {
  background: #f3f4f6;
  color: #4b5563;
  font-size: 30px;
  font-weight: 500;
  height: 88px;
  line-height: 88px;
  border-radius: 44px;
  border: 2px solid #e5e7eb;
}

.btn-secondary:active {
  background: #e5e7eb;
}
</style>