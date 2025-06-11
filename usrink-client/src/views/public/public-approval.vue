<template>
  <div class="public-approval-container">
    <!-- SEG 系统标识 -->
    <div class="system-header">
      <div class="logo-container">
        <img src="/SEG-logo.png" alt="SEG Logo" class="seg-logo">
      </div>
      <div class="system-title">
        <h2>电脑管理系统</h2>
      </div>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="tech-loading">
        <div class="loader-ring"></div>
        <div class="loader-text">正在获取数据...</div>
      </div>
    </div>
    
    <!-- 错误信息 -->
    <div v-else-if="errorMsg" class="error-container">
      <el-result
        icon="error"
        title="无法获取审批数据"
        :sub-title="errorMsg">
        <template #extra>
          <div class="error-actions">
            <p v-if="errorMsg.includes('审批链接已过期')">请联系管理员重新生成审批链接，或关闭此页面。</p>
            <el-button type="primary" class="tech-button" @click="fetchApprovalData">重试</el-button>
          </div>
        </template>
      </el-result>
    </div>
    
    <!-- 审批详情 -->
    <div v-else class="approval-content">
      <div class="tech-card">
        <div class="tech-card-header">
          <div class="tech-indicator"></div>
          <h3>设备申请详情</h3>
        </div>
        
        <div class="tech-card-body">
          <div class="tech-info-grid">
            <div class="tech-info-item">
              <div class="tech-info-label">申请编号</div>
              <div class="tech-info-value">{{ approvalData.approvalId }}</div>
            </div>
            <div class="tech-info-item">
              <div class="tech-info-label">申请人</div>
              <div class="tech-info-value">{{ approvalData.userName }}</div>
            </div>
            <div class="tech-info-item">
              <div class="tech-info-label">申请类别</div>
              <div class="tech-info-value">{{ approvalData.deviceCategory }}</div>
            </div>
            <div class="tech-info-item">
              <div class="tech-info-label">电脑类型</div>
              <div class="tech-info-value">{{ approvalData.deviceType }}</div>
            </div>
            <div class="tech-info-item">
              <div class="tech-info-label">成本中心</div>
              <div class="tech-info-value">{{ approvalData.costCenter }}</div>
            </div>
            <div class="tech-info-item">
              <div class="tech-info-label">所属公司</div>
              <div class="tech-info-value">{{ approvalData.company }}</div>
            </div>
            <div class="tech-info-item">
              <div class="tech-info-label">责任人</div>
              <div class="tech-info-value">{{ approvalData.responsibilityName }}</div>
            </div>
            <div class="tech-info-item">
              <div class="tech-info-label">电脑情形</div>
              <div class="tech-info-value">{{ approvalData.deviceSituation }}</div>
            </div>
            <div class="tech-info-item">
              <div class="tech-info-label">公司系统</div>
              <div class="tech-info-value">{{ approvalData.companySystem }}</div>
            </div>
            <div v-if="approvalData.ciName" class="tech-info-item">
              <div class="tech-info-label">需要更换的电脑</div>
              <div class="tech-info-value">{{ approvalData.ciName }}</div>
            </div>
            <div class="tech-info-item">
              <div class="tech-info-label">申请时间</div>
              <div class="tech-info-value">{{ approvalData.createdAt }}</div>
            </div>
          </div>
          
          <div class="tech-reason-section">
            <div class="tech-info-label">申请理由</div>
            <div class="tech-reason-content">{{ approvalData.reason }}</div>
          </div>
        </div>
        
        <!-- 审批操作区 -->
        <div v-if="approvalData.status === '审批中'" class="tech-action-section">
          <div class="tech-button-group">
            <button class="tech-approve-button" :disabled="submitting" @click="handleApprove(true)">
              <span class="button-icon">✓</span>
              <span class="button-text">审批通过</span>
            </button>
            <button class="tech-reject-button" :disabled="submitting" @click="openRejectDialog()">
              <span class="button-icon">✕</span>
              <span class="button-text">审批不通过</span>
            </button>
          </div>
        </div>
        
        <!-- 已审批状态展示 -->
        <div v-else class="tech-result-section">
          <div class="tech-result-card" :class="approvalData.status === '审批通过' ? 'success' : 'error'">
            <div class="tech-result-icon">
              <i :class="approvalData.status === '审批通过' ? 'el-icon-check' : 'el-icon-close'"></i>
            </div>
            <div class="tech-result-title">
              您的审批已完成，审批历史为："{{ approvalData.status }}"
            </div>
            <div v-if="approvalData.comment" class="tech-result-comment">
              <div class="comment-label">审批意见：</div>
              <div class="comment-content">{{ approvalData.comment }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 成功提示弹窗 -->
    <el-dialog v-model="showSuccessDialog" title="" width="30%" center custom-class="tech-dialog">
      <div class="success-dialog-content">
        <div class="tech-success-animation">
          <div class="success-checkmark">
            <div class="check-icon">
              <span class="icon-line line-tip"></span>
              <span class="icon-line line-long"></span>
            </div>
          </div>
        </div>
        <div class="tech-success-message">
          <h3>审批提交成功</h3>
          <p>您已{{ lastApproveAction === 'approve' ? '通过' : '拒绝' }}此申请</p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="tech-confirm-button" @click="showSuccessDialog = false">确定</button>
        </div>
      </template>
    </el-dialog>

    <!-- 审批不通过理由弹框 -->
    <el-dialog v-model="showRejectDialog" title="请输入审批不通过理由" 
      width="30%" center custom-class="tech-dialog">
      <div class="reject-dialog-content">
        <div class="reject-reason-container">
          <div class="reason-label">审批理由：</div>
          <el-input
            v-model="approvalReason"
            type="textarea"
            :rows="3"
            placeholder="请输入审批不通过的理由"
            maxlength="200"
            show-word-limit
          ></el-input>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="tech-cancel-button" @click="cancelReject">取消</button>
          <button class="tech-confirm-button confirm-reject" @click="confirmReject">确认</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { BASE_URL, RES_STATUS_CODE } from '@/utils/Constant'

// 页面状态
const loading = ref(true)
const errorMsg = ref('')
const approvalData = ref({
  approvalId: '',
  applicant: '',
  userName: '',
  deviceCategory: '',
  deviceType: '',
  costCenter: '',
  company: '',
  responsibilityName: '',
  deviceSituation: '',
  companySystem: '',
  ciName: '',
  reason: '',
  createdAt: '',
  status: '',
  comment: ''
})
const submitting = ref(false)
const showSuccessDialog = ref(false)
// 审批相关状态
const approvalReason = ref('')
const showRejectDialog = ref(false)
const lastApproveAction = ref('approve')

// 从URL获取参数
const getUrlParams = () => {
  const urlParams = new URLSearchParams(window.location.search)
  return {
    flowId: urlParams.get('flowId'),
    token: urlParams.get('token')
  }
}

// 获取审批数据
const fetchApprovalData = async () => {
  loading.value = true
  errorMsg.value = ''
  
  const { flowId, token } = getUrlParams()
  
  if (!flowId || !token) {
    errorMsg.value = '审批参数不完整，请检查链接是否正确'
    loading.value = false
    return
  }
  
  try {
    // 创建一个独立的axios实例，不使用全局拦截器
    const response = await axios.get(`${BASE_URL}/sysApply/tempApproval`, {
      params: { flowId, token }
    })
    
    if (response.data.code === RES_STATUS_CODE.SUCCESS) {
      approvalData.value = {
        ...response.data.data,
        // 确保状态字段存在，如果后端没有返回，则默认为"待审批"
        status: response.data.data.status || '待审批'
      }
      // 调试信息
      console.log('获取到的审批状态:', approvalData.value.status);
    } else if (response.data.code === RES_STATUS_CODE.UNAUTHORIZED) {
      // 处理凭证过期错误
      errorMsg.value = '审批链接已过期或无效，请联系管理员获取新的审批链接'
    } else {
      errorMsg.value = response.data.msg || '获取审批数据失败'
    }
  } catch (error) {
    console.error('获取审批数据出错:', error)
    // 尝试从错误响应中提取信息
    if (error.response && error.response.data) {
      if (error.response.data.code === RES_STATUS_CODE.UNAUTHORIZED) {
        errorMsg.value = '审批链接已过期或无效，请联系管理员获取新的审批链接'
      } else {
        errorMsg.value = error.response.data.msg || '网络错误，请稍后再试'
      }
    } else {
      errorMsg.value = '网络错误，请稍后再试'
    }
  } finally {
    loading.value = false
  }
}

// 提交审批
const handleApprove = async (isApproved) => {
  submitting.value = true
  
  // 如果是审批通过，直接设置理由为"同意"
  if (isApproved) {
    approvalReason.value = "同意";
  } else if (!approvalReason.value.trim()) {
    // 如果是审批不通过但没有填写理由，提示错误
    ElMessage.warning('请输入审批不通过的理由');
    submitting.value = false;
    return;
  }
  
  const { flowId, token } = getUrlParams()
  
  try {
    // 创建一个独立的axios实例，不使用全局拦截器
    const response = await axios.post(`${BASE_URL}/sysApply/submitTempApproval`, {
      flowId,
      token,
      id: approvalData.value.approvalId,
      status: isApproved ? '审批通过' : '审批不通过',
      reason: approvalReason.value
    })
    
    if (response.data.code === RES_STATUS_CODE.SUCCESS) {
      // 调试信息
      console.log('提交审批状态:', isApproved ? '审批通过' : '审批不通过');
      
      // 保存审批理由到结果页面
      approvalData.value.comment = approvalReason.value;
      
      // 记录最后的审批动作
      lastApproveAction.value = isApproved ? 'approve' : 'reject';
      
      showSuccessDialog.value = true
      // 刷新数据
      await fetchApprovalData()
      
      // 清空理由
      approvalReason.value = '';
    } else if (response.data.code === RES_STATUS_CODE.UNAUTHORIZED) {
      // 处理凭证过期错误
      ElMessage.error('审批链接已过期或无效，请联系管理员获取新的审批链接')
      errorMsg.value = '审批链接已过期或无效，请联系管理员获取新的审批链接'
    } else {
      ElMessage.error(response.data.msg || '审批操作失败')
    }
  } catch (error) {
    console.error('提交审批出错:', error)
    // 尝试从错误响应中提取信息
    if (error.response && error.response.data) {
      if (error.response.data.code === RES_STATUS_CODE.UNAUTHORIZED) {
        ElMessage.error('审批链接已过期或无效，请联系管理员获取新的审批链接')
        errorMsg.value = '审批链接已过期或无效，请联系管理员获取新的审批链接'
      } else {
        ElMessage.error(error.response.data.msg || '网络错误，请稍后再试')
      }
    } else {
      ElMessage.error('网络错误，请稍后再试')
    }
  } finally {
    submitting.value = false
  }
}

// 显示拒绝理由弹框
const openRejectDialog = () => {
  showRejectDialog.value = true
}

// 取消拒绝
const cancelReject = () => {
  showRejectDialog.value = false
  approvalReason.value = ''
}

// 确认拒绝
const confirmReject = () => {
  if (!approvalReason.value.trim()) {
    ElMessage.warning('请输入审批不通过的理由');
    return;
  }
  showRejectDialog.value = false;
  // 调用审批处理函数，传递审批类型
  handleApprove(false);
}

// 页面初始化
onMounted(() => {
  fetchApprovalData()
})
</script>

<style scoped>
:root {
  --primary-color: #1890ff;
  --primary-light: #40a9ff;
  --primary-dark: #096dd9;
  --success-color: #52c41a;
  --success-light: #73d13d;
  --success-dark: #389e0d;
  --error-color: #f5222d;
  --error-light: #ff4d4f;
  --error-dark: #cf1322;
  --background-color: #f5f7fa;
  --card-background: #ffffff;
  --text-primary: #262626;
  --text-secondary: #595959;
  --text-tertiary: #8c8c8c;
  --border-color: #eaeaea;
  --glow-color: rgba(24, 144, 255, 0.2);
  --shadow-small: 0 2px 8px rgba(0, 0, 0, 0.08);
  --shadow-medium: 0 4px 16px rgba(0, 0, 0, 0.12);
  --shadow-large: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.public-approval-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 16px;
  background-color: var(--background-color);
  min-height: 100vh;
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

/* 加载动画 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.tech-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.loader-ring {
  display: inline-block;
  width: 80px;
  height: 80px;
  border: 3px solid rgba(24, 144, 255, 0.1);
  border-radius: 50%;
  border-top-color: var(--primary-color);
  animation: spin 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;
  box-shadow: 0 0 20px rgba(24, 144, 255, 0.2);
}

.loader-text {
  margin-top: 24px;
  color: var(--text-secondary);
  font-size: 16px;
  animation: pulse 1.5s infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

/* 错误容器 */
.error-container {
  padding: 30px;
  max-width: 600px;
  margin: 0 auto;
}

.error-actions {
  text-align: center;
  margin-top: 16px;
}

.error-actions p {
  margin-bottom: 16px;
  color: #f5222d;
  font-size: 15px;
}

.tech-button {
  background: linear-gradient(135deg, #1890ff, #096dd9);
  color: white;
  border: none;
  padding: 10px 24px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.tech-button:hover {
  background: linear-gradient(135deg, #40a9ff, #1890ff);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

/* 卡片样式 */
.tech-card {
  background-color: var(--card-background);
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  position: relative;
  transition: all 0.3s ease;
  margin-bottom: 24px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  animation: cardEnter 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.tech-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #1890ff, #13c2c2, #52c41a);
}

.tech-card-header {
  padding: 16px 20px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid var(--border-color);
  position: relative;
  background-color: rgba(0, 0, 0, 0.02);
}

.tech-indicator {
  width: 4px;
  height: 24px;
  background: linear-gradient(to bottom, #1890ff, #13c2c2);
  margin-right: 16px;
  border-radius: 2px;
}

.tech-card-header h3 {
  margin: 0;
  font-size: 22px;
  font-weight: 500;
  color: var(--text-primary);
  letter-spacing: 0.5px;
}

.tech-card-body {
  padding: 20px;
}

/* 信息网格 */
.tech-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  padding: 0;
}

.tech-info-item {
  position: relative;
  padding-left: 12px;
  padding-bottom: 12px;
  border-bottom: 1px dashed rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  animation: fadeInStagger 0.5s ease-out backwards;
}

.tech-info-item:hover {
  background-color: rgba(24, 144, 255, 0.03);
}

.tech-info-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 6px;
  width: 4px;
  height: 16px;
  background: linear-gradient(to bottom, rgba(24, 144, 255, 0.8), rgba(24, 144, 255, 0.4));
  border-radius: 2px;
}

.tech-info-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 6px;
  font-weight: 500;
}

.tech-info-value {
  font-size: 15px;
  color: var(--text-primary);
  font-weight: 500;
  word-break: break-all;
  line-height: 1.5;
}

.tech-info-item:nth-child(1) { animation-delay: 0.1s; }
.tech-info-item:nth-child(2) { animation-delay: 0.15s; }
.tech-info-item:nth-child(3) { animation-delay: 0.2s; }
.tech-info-item:nth-child(4) { animation-delay: 0.25s; }
.tech-info-item:nth-child(5) { animation-delay: 0.3s; }
.tech-info-item:nth-child(6) { animation-delay: 0.35s; }
.tech-info-item:nth-child(7) { animation-delay: 0.4s; }
.tech-info-item:nth-child(8) { animation-delay: 0.45s; }
.tech-info-item:nth-child(9) { animation-delay: 0.5s; }
.tech-info-item:nth-child(10) { animation-delay: 0.55s; }
.tech-info-item:nth-child(11) { animation-delay: 0.6s; }

@keyframes fadeInStagger {
  from {
    opacity: 0;
    transform: translateX(10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 理由部分 */
.tech-reason-section {
  margin-top: 24px;
  padding: 16px;
  background-color: rgba(240, 242, 245, 0.6);
  border-radius: 8px;
  position: relative;
  box-shadow: inset 0 0 8px rgba(0, 0, 0, 0.03);
  animation: fadeIn 0.7s ease-out;
  animation-delay: 0.4s;
  animation-fill-mode: backwards;
}

.tech-reason-section::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 4px;
  background: linear-gradient(to bottom, var(--primary-color), transparent);
  border-radius: 2px 0 0 2px;
}

.tech-reason-content {
  margin-top: 12px;
  white-space: pre-line;
  line-height: 1.6;
  padding: 0 8px;
  font-size: 14px;
  color: #555;
}

/* 按钮组 */
.tech-action-section {
  padding: 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  background: linear-gradient(to bottom, #f3f5f7, #f9fafb);
  text-align: center;
  position: relative;
}

.tech-button-group {
  display: flex;
  justify-content: center;
  gap: 30px;
  padding: 10px 0;
  position: relative;
}

.tech-approve-button, .tech-reject-button {
  position: relative;
  width: 160px; /* 固定宽度以保持一致 */
  padding: 12px 0;
  border: none;
  border-radius: 6px;
  font-size: 15px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: white;
  font-weight: 500;
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.2);
  z-index: 1;
  letter-spacing: 0.5px;
  outline: none;
}

/* 修复按钮样式，确保可见性 */
.tech-approve-button {
  background: #52c41a !important;
}

.tech-reject-button {
  background: #f5222d !important;
}

.tech-approve-button:hover {
  background: #5fd925 !important;
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.25);
}

.tech-reject-button:hover {
  background: #ff4d4f !important;
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.25);
}

.tech-approve-button:active, .tech-reject-button:active {
  transform: translateY(0);
  box-shadow: 0 5px 10px rgba(0, 0, 0, 0.2);
}

.button-icon {
  margin-right: 12px;
  font-size: 20px;
}

.button-text {
  position: relative;
  display: inline-block;
  transition: transform 0.3s;
}

/* 结果卡片 */
.tech-result-section {
  padding: 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  background: linear-gradient(to bottom, #f3f5f7, #f9fafb);
}

.tech-result-card {
  padding: 16px;
  border-radius: 6px;
  text-align: center;
  position: relative;
  overflow: hidden;
  animation: fadeIn 0.5s ease-out;
}

.tech-result-card.success {
  background-color: rgba(82, 196, 26, 0.1);
  border: 1px solid rgba(82, 196, 26, 0.3);
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.08);
}

.tech-result-card.error {
  background-color: rgba(245, 34, 45, 0.1);
  border: 1px solid rgba(245, 34, 45, 0.3);
  box-shadow: 0 4px 12px rgba(245, 34, 45, 0.08);
}

.tech-result-icon {
  margin-bottom: 8px;
  font-size: 28px;
}

.tech-result-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
}

.tech-result-comment {
  margin-top: 12px;
  text-align: left;
  background-color: white;
  padding: 12px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 确保按钮正常显示 */
.tech-approve-button, .tech-reject-button {
  display: inline-flex !important;
  visibility: visible !important;
  opacity: 1 !important;
}

.button-icon {
  margin-right: 8px;
  font-size: 18px;
}

.button-text {
  position: relative;
  display: inline-block;
  transition: transform 0.3s;
}

/* 成功对话框 */
:deep(.tech-dialog) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.tech-dialog .el-dialog__header) {
  background: linear-gradient(to right, #f3f5f7, #f9fafb);
  padding: 20px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

:deep(.tech-dialog .el-dialog__title) {
  font-size: 20px;
  color: #333;
  font-weight: 500;
}

:deep(.tech-dialog .el-dialog__body) {
  padding: 30px 24px;
}

:deep(.tech-dialog .el-dialog__footer) {
  padding: 16px 24px 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.success-dialog-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 0 20px;
}

.tech-success-animation {
  margin-bottom: 24px;
}

.success-checkmark {
  width: 80px;
  height: 80px;
  position: relative;
  animation: scaleCheckmark 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes scaleCheckmark {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

.check-icon {
  width: 80px;
  height: 80px;
  position: relative;
  border-radius: 50%;
  box-sizing: content-box;
  border: 4px solid var(--success-color);
}

.check-icon::before {
  top: 3px;
  left: -2px;
  width: 30px;
  transform-origin: 100% 50%;
  border-radius: 100px 0 0 100px;
}

.check-icon::after {
  top: 0;
  left: 30px;
  width: 60px;
  transform-origin: 0 50%;
  border-radius: 0 100px 100px 0;
  animation: rotate-circle 4.25s ease-in;
}

.check-icon::before, .check-icon::after {
  content: '';
  height: 100px;
  position: absolute;
  background: #ffffff;
  transform: rotate(-45deg);
}

.icon-line {
  height: 5px;
  background-color: var(--success-color);
  display: block;
  border-radius: 2px;
  position: absolute;
  z-index: 10;
}

.icon-line.line-tip {
  top: 46px;
  left: 14px;
  width: 25px;
  transform: rotate(45deg);
  animation: icon-line-tip 0.75s;
}

.icon-line.line-long {
  top: 38px;
  right: 8px;
  width: 47px;
  transform: rotate(-45deg);
  animation: icon-line-long 0.75s;
}

.tech-success-message {
  text-align: center;
}

.tech-success-message h3 {
  font-size: 22px;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.tech-success-message p {
  color: var(--text-secondary);
  margin: 0;
  font-size: 16px;
}

.tech-confirm-button {
  background: linear-gradient(135deg, #1890ff, #096dd9);
  color: white;
  border: none;
  padding: 12px 36px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
  font-weight: 500;
  letter-spacing: 0.5px;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.2);
}

.tech-confirm-button:hover {
  background: linear-gradient(135deg, #40a9ff, #1890ff);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.3);
}

.tech-confirm-button:active {
  transform: translateY(0);
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .tech-info-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .tech-card-body {
    padding: 20px;
  }
  
  .tech-button-group {
    flex-direction: column;
    gap: 16px;
    align-items: center;
  }
  
  .tech-approve-button, .tech-reject-button {
    width: 80%;
    max-width: 200px;
  }
  
  .tech-card-header {
    padding: 20px;
  }
  
  .tech-card-header h3 {
    font-size: 18px;
  }
}

/* 适配暗黑模式 */
@media (prefers-color-scheme: dark) {
  :root {
    --background-color: #131313;
    --card-background: #1f1f1f;
    --text-primary: #ffffff;
    --text-secondary: #b0b0b0;
    --text-tertiary: #8c8c8c;
    --border-color: #303030;
  }
  
  .tech-card {
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.25);
    border: 1px solid rgba(255, 255, 255, 0.05);
  }
  
  .tech-card-header {
    background-color: rgba(255, 255, 255, 0.02);
  }
  
  .tech-card::before {
    background: linear-gradient(90deg, #1890ff, #13c2c2, #52c41a);
  }
  
  .tech-info-item {
    border-bottom: 1px dashed rgba(255, 255, 255, 0.1);
  }
  
  .tech-info-item:hover {
    background-color: rgba(255, 255, 255, 0.03);
  }
  
  .tech-reason-section {
    background-color: rgba(0, 0, 0, 0.2);
    box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.2);
  }
  
  .tech-action-section, .tech-result-section {
    background: linear-gradient(to bottom, #161616, #1a1a1a);
  }
  
  .tech-result-card.success {
    background: linear-gradient(to right, rgba(82, 196, 26, 0.05), rgba(82, 196, 26, 0.1));
    border: 1px solid rgba(82, 196, 26, 0.2);
  }
  
  .tech-result-card.error {
    background: linear-gradient(to right, rgba(245, 34, 45, 0.05), rgba(245, 34, 45, 0.1));
    border: 1px solid rgba(245, 34, 45, 0.2);
  }
  
  .tech-result-comment {
    background-color: rgba(0, 0, 0, 0.3);
  }
  
  :deep(.tech-dialog .el-dialog__header) {
    background: linear-gradient(to right, #1a1a1a, #222222);
  }
  
  :deep(.tech-dialog .el-dialog__title) {
    color: #e0e0e0;
  }
  
  .check-icon::before, .check-icon::after {
    background: #1f1f1f;
  }
  
  .loader-ring {
    border-color: rgba(24, 144, 255, 0.1);
    box-shadow: 0 0 20px rgba(24, 144, 255, 0.1);
  }
}

/* 添加额外动画 */
@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

/* SEG 系统标识样式 */
.system-header {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
  margin-bottom: 0;
  padding: 16px;
  text-align: left;
  position: relative;
  gap: 20px;
}

.logo-container {
  margin-bottom: 0;
}

.seg-logo {
  height: 55px;
  width: auto;
  object-fit: contain;
}

.system-title h2 {
  font-size: 28px;
  font-weight: 500;
  color: var(--text-primary);
  margin: 0;
  position: relative;
  padding-left: 15px;
}

.system-title h2:before {
  content: '';
  position: absolute;
  left: 0;
  top: 6px;
  height: 75%;
  width: 4px;
  background: linear-gradient(to bottom, #1890ff, #13c2c2);
  border-radius: 2px;
}

.system-divider {
  height: 3px;
  width: 120px;
  background: linear-gradient(90deg, #1890ff, #13c2c2, #52c41a);
  border-radius: 2px;
  margin: 0 auto;
}

/* 滚动条美化 */
::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: rgba(24, 144, 255, 0.3);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(24, 144, 255, 0.5);
}

.tech-result-card.success .tech-result-icon {
  color: var(--success-color);
}

.tech-result-card.error .tech-result-icon {
  color: var(--error-color);
}

.comment-label {
  font-weight: 500;
  margin-bottom: 6px;
  color: var(--text-primary);
  font-size: 14px;
}

.comment-content {
  white-space: pre-line;
  color: var(--text-secondary);
  line-height: 1.5;
  font-size: 14px;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .system-header {
    padding: 12px;
    gap: 12px;
  }
  
  .seg-logo {
    height: 42px;
  }
  
  .system-title h2 {
    font-size: 22px;
  }
  
  .system-divider {
    width: 100px;
  }
  
  .tech-info-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .tech-card-body {
    padding: 20px;
  }
  
  .tech-button-group {
    flex-direction: column;
    gap: 16px;
    align-items: center;
  }
  
  .tech-approve-button, .tech-reject-button {
    width: 80%;
    max-width: 200px;
  }
  
  .tech-card-header {
    padding: 20px;
  }
  
  .tech-card-header h3 {
    font-size: 18px;
  }
}

/* 微调按钮样式 */
.tech-button-group {
  margin: 0;
}

/* 删除button-glow相关样式 */
.button-glow {
  display: none;
}

/* 按钮悬停样式 */
.tech-approve-button:hover .button-text, .tech-reject-button:hover .button-text {
  transform: none;
}

.tech-approve-button:hover .button-icon, .tech-reject-button:hover .button-icon {
  transform: none;
}

/* 确认弹窗样式 */
.reject-dialog-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.reject-reason-container {
  margin-top: 20px;
  margin-bottom: 10px;
  text-align: left;
}

.reason-label {
  margin-bottom: 8px;
  font-weight: 500;
  color: var(--text-secondary);
}

/* 添加必要的样式 */
.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.tech-cancel-button {
  background: #f0f0f0;
  color: #666;
  border: none;
  padding: 12px 30px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
  font-weight: 500;
}

.tech-cancel-button:hover {
  background: #e0e0e0;
  transform: translateY(-2px);
}

.tech-confirm-button.confirm-reject {
  background: linear-gradient(135deg, #f5222d, #cf1322);
}
</style> 