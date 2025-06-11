<template>
  <div style="padding: 20px;">
    <!-- 审批记录 -->
    <el-card shadow="never" class="usr_card_override top">
      <div class="approval-tabs">
        <el-button 
          :type="approvalType === 0 ? 'primary' : 'default'" 
          @click="switchApprovalType(0)">{{ langText.pendingApprovals }}</el-button>
        <el-button 
          :type="approvalType === 1 ? 'primary' : 'default'" 
          @click="switchApprovalType(1)">{{ langText.approvalHistory }}</el-button>
        
        <!-- 添加审批人选择下拉框 -->
        <div class="approver-selector">
          <el-select 
            v-model="selectedApproverId" 
            :placeholder="langText.selectApproverIdentity" 
            @change="handleApproverChange"
            size="default">
            <el-option 
              v-for="approver in approverList" 
              :key="approver.id" 
              :label="`${approver.costCenterName || langText.costCenter}`" 
              :value="approver.id">
            </el-option>
          </el-select>
        </div>
      </div>
      
      <h3>{{ langText.deviceApprovalRecords }}</h3>
      <div class="table-container">
        <el-table 
          :data="approvalList" 
          :loading="loading" 
          style="width: 100%;" 
          :cell-style="{ padding: '12px 0' }">
          <el-table-column :label="langText.applicationTime" prop="createdAt" width="180"></el-table-column>
          <el-table-column :label="langText.updateTime" prop="updatedAt" width="180"></el-table-column>
          <el-table-column :label="langText.applicant" prop="userName" width="100"></el-table-column>
          <el-table-column :label="langText.responsible" prop="responsibilityName" width="100"></el-table-column>
          <el-table-column :label="langText.computerType" prop="deviceType" width="180">
            <template #default="{ row }">
              {{ getDeviceTypeName(row.deviceType) }}
            </template>
          </el-table-column>
          <el-table-column :label="langText.computerToReplace" prop="ciName" width="160">
            <template #default="{ row }">
              {{ row.ciName || langText.applyForNewComputer }}
            </template>
          </el-table-column>
          <el-table-column :label="langText.applicationReason" prop="reason" show-overflow-tooltip min-width="180"></el-table-column>
          <el-table-column :label="langText.approvalReason" prop="approvalReason" show-overflow-tooltip min-width="180" v-if="approvalType === 1">
            <template #default="{ row }">
              {{ row.approvalReason || '' }}
            </template>
          </el-table-column>
          <el-table-column :label="langText.status" prop="status" width="140" align="center">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)">
                {{ getLocalizedStatus(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="langText.actions" width="100" fixed="right" align="center">
            <template #default="{ row }">
              <el-button 
                type="primary" 
                text 
                @click="viewApprovalDetails(row)"
                :class="{ 'flow-not-reached': row.status === '审批中' && row.status1Signal === 0 }">
                {{ getActionButtonText(row) }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageData.pageNum"
          :page-size="pageData.pageSize"
          :total="total"
          @current-change="handleCurrentChange"
          layout="total, prev, pager, next, jumper"
          background>
        </el-pagination>
      </div>
    </el-card>

    <!-- 审批详情弹窗 -->
    <el-dialog 
      v-model="approvalDialogVisible" 
      title=""
      width="700px"
      class="application-detail-dialog"
      destroy-on-close>
      <div v-if="selectedApproval" class="application-detail-container">
        <!-- 顶部概要信息 -->
        <div class="detail-header">
          <div class="application-title">
            <span class="computer-name">{{ selectedApproval.ciName || langText.applyForNewComputer }}</span>
            <el-tag class="status-tag" :type="statusTagType(selectedApproval.status)">
              {{ getLocalizedStatus(selectedApproval.status) }}
            </el-tag>
          </div>
          <div class="application-info">
            <span class="info-item">
              <i class="el-icon-user"></i> {{ langText.applicant }}: {{ selectedApproval.userName }}
            </span>
            <span class="info-item">
              <i class="el-icon-date"></i> {{ langText.applicationTime }}: {{ selectedApproval.createdAt }}
            </span>
          </div>
        </div>

        <!-- 详细信息 -->
        <div class="detail-content">
          <el-descriptions :column="2" border size="default" class="detail-descriptions">
            <el-descriptions-item :label="langText.applicationType" label-class-name="item-label" content-class-name="item-content">
              {{ selectedApproval.deviceCategory }}
            </el-descriptions-item>
            <el-descriptions-item :label="langText.computerType" label-class-name="item-label" content-class-name="item-content">
              {{ getDeviceTypeName(selectedApproval.deviceType) }}
            </el-descriptions-item>
            <el-descriptions-item :label="langText.costCenter" label-class-name="item-label" content-class-name="item-content">
              {{ selectedApproval.costCenter }}
            </el-descriptions-item>
            <el-descriptions-item :label="langText.company" label-class-name="item-label" content-class-name="item-content">
              {{ selectedApproval.company }}
            </el-descriptions-item>
            <el-descriptions-item :label="langText.responsible" label-class-name="item-label" content-class-name="item-content">
              {{ selectedApproval.responsibilityName }}
            </el-descriptions-item>
            <el-descriptions-item :label="langText.computerSituation" label-class-name="item-label" content-class-name="item-content">
              {{ selectedApproval.deviceSituation }}
            </el-descriptions-item>
            <el-descriptions-item :label="langText.companySystem" label-class-name="item-label" content-class-name="item-content">
              {{ selectedApproval.companySystem }}
            </el-descriptions-item>
            <el-descriptions-item :label="langText.updateTime" label-class-name="item-label" content-class-name="item-content">
              {{ selectedApproval.updatedAt }}
            </el-descriptions-item>
          </el-descriptions>
          
          <!-- 申请理由区域 -->
          <div class="reason-section">
            <div class="reason-title">{{ langText.applicationReason }}</div>
            <div class="reason-content">{{ selectedApproval.reason }}</div>
          </div>
          
          <!-- 审批理由区域 - 仅当状态不是"审批中"时显示 -->
          <div v-if="selectedApproval.status !== '审批中' && selectedApproval.approvalReason" class="reason-section approval-reason-section">
            <div class="reason-title">{{ langText.approvalReason }}</div>
            <div class="reason-content">{{ selectedApproval.approvalReason }}</div>
          </div>
        </div>
        
        <!-- 根据状态显示不同内容 -->
        <div class="approval-actions">
          <!-- 如果状态是"审批中"且status1Signal不为0，显示审批按钮 -->
          <template v-if="selectedApproval.status === '审批中' && selectedApproval.status1Signal !== 0">
            <div class="action-buttons">
              <el-button type="success" size="large" @click="approveApplication(true)">
                <i class="el-icon-check"></i> {{ langText.approve }}
              </el-button>
              <el-button type="danger" size="large" @click="approveApplication(false)">
                <i class="el-icon-close"></i> {{ langText.reject }}
              </el-button>
            </div>
          </template>
          
          <!-- 如果状态是"审批中"但status1Signal为0，显示流程未到达 -->
          <template v-else-if="selectedApproval.status === '审批中' && selectedApproval.status1Signal === 0">
            <div class="approval-waiting">
              <i class="el-icon-warning"></i>
              {{ langText.approvalNotReached }}
            </div>
          </template>
          
          <!-- 如果状态不是"审批中"和"Pending"，显示审批历史 -->
          <template v-else-if="selectedApproval.status !== 'Pending'">
            <div :class="['approval-history', getApprovalHistoryClass(selectedApproval.status)]">
              <i :class="selectedApproval.status.includes('通过') ? 'el-icon-check' : 'el-icon-close'"></i>
              {{ langText.approvalHistoryMessage }}: {{ getLocalizedStatus(selectedApproval.status) }}
            </div>
          </template>
          
          <!-- 保留原有的Pending状态处理 -->
          <template v-else-if="selectedApproval.status === 'Pending'">
            <div class="action-buttons">
              <el-button type="success" size="large" @click="approveApplication(true)" :disabled="selectedApproval.status1Signal === 0">
                <i class="el-icon-check"></i> {{ langText.agree }}
              </el-button>
              <el-button type="danger" size="large" @click="approveApplication(false)" :disabled="selectedApproval.status1Signal === 0">
                <i class="el-icon-close"></i> {{ langText.disagree }}
              </el-button>
            </div>
          </template>
        </div>
      </div>
    </el-dialog>
    
    <!-- 审批不通过理由弹窗 -->
    <el-dialog
      v-model="rejectDialogVisible"
      :title="langText.enterRejectReason"
      width="500px"
      :close-on-click-modal="false"
      :show-close="true"
      destroy-on-close>
      <div class="reject-reason-container">
        <el-input
          v-model="approvalReason"
          type="textarea"
          :rows="4"
          :placeholder="langText.enterRejectReasonPlaceholder"
          maxlength="200"
          show-word-limit>
        </el-input>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelReject">{{ langText.cancel }}</el-button>
          <el-button type="primary" @click="confirmReject">{{ langText.confirm }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from "vue";
import httpUtil from "@/utils/HttpUtil";
import { ElMessage } from "element-plus";
import { useLanguageStore } from '@/stores/_frame/languageStore';

export default {
  setup() {
    const approvalList = ref([]);
    const loading = ref(false);
    const approvalDialogVisible = ref(false);
    const selectedApproval = ref({});
    const total = ref(0);
    const approvalType = ref(0); // 默认显示待办审批
    const approverList = ref([]); // 存储审批人列表（包含ID和成本中心名称）
    const selectedApproverId = ref(null); // 当前选中的审批人ID
    const approvalReason = ref('');
    const rejectDialogVisible = ref(false);

    // 引入语言状态管理
    const languageStore = useLanguageStore();
    const currentLang = computed(() => languageStore.currentLang);

    // 多语言文本
    const langText = computed(() => {
      return {
        // 顶部按钮和标题
        pendingApprovals: currentLang.value === 'zh' ? '待办审批' : 'Pending Approvals',
        approvalHistory: currentLang.value === 'zh' ? '审批历史' : 'Approval History',
        selectApproverIdentity: currentLang.value === 'zh' ? '选择审批身份' : 'Select Approver Identity',
        deviceApprovalRecords: currentLang.value === 'zh' ? '设备审批记录' : 'Device Approval Records',
        costCenter: currentLang.value === 'zh' ? '成本中心' : 'Cost Center',
        
        // 表格列标题
        applicationTime: currentLang.value === 'zh' ? '申请时间' : 'Application Time',
        updateTime: currentLang.value === 'zh' ? '更新时间' : 'Update Time',
        applicant: currentLang.value === 'zh' ? '申请人' : 'Applicant',
        responsible: currentLang.value === 'zh' ? '责任人' : 'Responsible Person',
        computerType: currentLang.value === 'zh' ? '电脑类型' : 'Computer Type',
        computerToReplace: currentLang.value === 'zh' ? '需要更换的电脑' : 'Computer to Replace',
        applicationReason: currentLang.value === 'zh' ? '申请理由' : 'Application Reason',
        approvalReason: currentLang.value === 'zh' ? '审批理由' : 'Approval Reason',
        status: currentLang.value === 'zh' ? '状态' : 'Status',
        actions: currentLang.value === 'zh' ? '操作' : 'Actions',
        
        // 操作按钮
        view: currentLang.value === 'zh' ? '查看' : 'View',
        approve: currentLang.value === 'zh' ? '审批通过' : 'Approve',
        reject: currentLang.value === 'zh' ? '审批不通过' : 'Reject',
        agree: currentLang.value === 'zh' ? '同意' : 'Agree',
        disagree: currentLang.value === 'zh' ? '驳回' : 'Reject',
        flowNotReached: currentLang.value === 'zh' ? '流程未到达' : 'Process Not Reached',
        approvalAction: currentLang.value === 'zh' ? '审批' : 'Approve',
        
        // 详情页
        applyForNewComputer: currentLang.value === 'zh' ? '申请新电脑' : 'Apply for new computer',
        applicationType: currentLang.value === 'zh' ? '申请类别' : 'Application Type',
        company: currentLang.value === 'zh' ? '所属公司' : 'Company',
        computerSituation: currentLang.value === 'zh' ? '电脑情形' : 'Computer Situation',
        companySystem: currentLang.value === 'zh' ? '公司系统' : 'Company System',
        approvalNotReached: currentLang.value === 'zh' ? '上一级审批尚未完成，流程未到达' : 'Previous approval not complete, process not reached',
        approvalHistoryMessage: currentLang.value === 'zh' ? '您已进行审批，审批历史为' : 'You have already approved, history is',
        
        // 拒绝理由弹窗
        enterRejectReason: currentLang.value === 'zh' ? '请输入审批不通过理由' : 'Please Enter Rejection Reason',
        enterRejectReasonPlaceholder: currentLang.value === 'zh' ? '请输入审批不通过的理由' : 'Please enter reason for rejection',
        cancel: currentLang.value === 'zh' ? '取消' : 'Cancel',
        confirm: currentLang.value === 'zh' ? '确认' : 'Confirm',
        
        // 状态文本
        pending: currentLang.value === 'zh' ? '待审批' : 'Pending',
        approving: currentLang.value === 'zh' ? '审批中' : 'In Approval',
        approved: currentLang.value === 'zh' ? '审批通过' : 'Approved',
        rejected: currentLang.value === 'zh' ? '审批不通过' : 'Rejected',
        pendingOld: 'Pending',
        approvedOld: '已通过',
        rejectedOld: '已驳回',
        
        // 消息提示
        approverNotFound: currentLang.value === 'zh' ? '未找到您的审批身份信息' : 'Your approver identity not found',
        approvalSuccess: currentLang.value === 'zh' ? '审批操作成功' : 'Approval successful',
        approvalFailed: currentLang.value === 'zh' ? '审批操作失败' : 'Approval failed',
        approvalProcessFailed: currentLang.value === 'zh' ? '审批操作失败' : 'Approval process failed',
        pleaseEnterReason: currentLang.value === 'zh' ? '请输入审批不通过的理由' : 'Please enter a reason for rejection'
      }
    });

    // 传入分页数据
    const pageData = reactive({
      pageNum: 1,
      pageSize: 10,
      approvalType: 0, // 默认为待办审批
      approverId: null // 添加审批人ID参数
    });

    // 获取审批人列表
    const fetchApproverList = () => {
      httpUtil.post("/sysApply/getApproverList").then((res) => {
        if (res.code === 200 && res.data && res.data.list && res.data.list.length > 0) {
          approverList.value = res.data.list;
          // 默认选择第一个审批人ID
          selectedApproverId.value = res.data.list[0].id;
          pageData.approverId = res.data.list[0].id;
          // 获取审批列表
          fetchApprovalList();
        } else {
          ElMessage.warning(langText.value.approverNotFound);
        }
      }).catch(err => {
        console.error("获取审批人列表失败", err);
        ElMessage.error(currentLang.value === 'zh' ? "获取审批人身份失败" : "Failed to get approver identity");
      });
    };

    // 获取本地化的状态文本
    const getLocalizedStatus = (status) => {
      if (status === '审批中') return langText.value.approving;
      if (status === '审批通过') return langText.value.approved;
      if (status === '审批不通过') return langText.value.rejected;
      if (status === '待审批' || status === 'Pending') return langText.value.pending;
      if (status === '已通过') return langText.value.approved;
      if (status === '已驳回') return langText.value.rejected;
      return status; // 如果没有匹配项，返回原始状态
    };

    // 处理审批人变更
    const handleApproverChange = (value) => {
      pageData.approverId = value;
      pageData.pageNum = 1; // 切换审批人时重置页码
      fetchApprovalList();
    };

    // 切换审批类型
    const switchApprovalType = (type) => {
      approvalType.value = type;
      pageData.approvalType = type;
      pageData.pageNum = 1; // 切换类型时重置页码
      fetchApprovalList();
    };

    // 获取审批记录
    const fetchApprovalList = () => {
      if (!pageData.approverId) {
        return; // 如果没有审批人ID，不进行请求
      }
      loading.value = true;
      const pageData1 = { ...pageData};
      httpUtil.post("/sysApply/getApprovalListById", pageData1).then((res) => {
        approvalList.value = res.data.list || [];
        total.value = res.data.total || 0;
      }).catch(err => {
        console.error("获取审批记录失败", err);
      }).finally(() => {
        loading.value = false;
      });
    };

    // 处理页码变化
    const handleCurrentChange = (val) => {
      pageData.pageNum = val;
      fetchApprovalList();
    };

    // 获取操作按钮文本
    const getActionButtonText = (row) => {
      if (row.status === '审批中') {
        return row.status1Signal === 0 ? langText.value.flowNotReached : langText.value.approvalAction;
      }
      return langText.value.view;
    };

    // 查看审批详情
    const viewApprovalDetails = (row) => {
      selectedApproval.value = { ...row };
      approvalDialogVisible.value = true;
    };

    // 处理审批（同意 / 驳回）
    const approveApplication = (isApproved) => {
      // 如果流程未到达，不允许审批
      if (selectedApproval.value.status1Signal === 0) {
        ElMessage.warning(langText.value.approvalNotComplete);
        return;
      }
      
      // 如果是审批通过，直接提交，不设置理由
      if (isApproved) {
        submitApproval(true, null);
      } else {
        // 如果是审批不通过，显示弹框让用户输入理由
        approvalDialogVisible.value = false;
        rejectDialogVisible.value = true;
      }
    };
    
    // 提交审批
    const submitApproval = (isApproved, reason) => {
      // 更新状态文本，兼容新旧两种状态命名
      const status = isApproved ? 
        (selectedApproval.value.status === "审批中" ? "审批通过" : "已通过") : 
        (selectedApproval.value.status === "审批中" ? "审批不通过" : "已驳回");
      
      const data = {
        id: selectedApproval.value.approvalId,
        flowId: selectedApproval.value.flowId,
        status: status,
        approvedAt: new Date().toISOString(),
        reason: reason,
      };
      
      httpUtil.post("/sysApply/processApproval", data, {
        headers: {
          'Content-Type': 'application/json'
        }
      }).then((res) => {
        if (res.code === 200) {
          ElMessage.success(res.msg || langText.value.approvalSuccess);
          fetchApprovalList();
          approvalDialogVisible.value = false;
          rejectDialogVisible.value = false;
          // 清空审批理由
          approvalReason.value = '';
        } else {
          ElMessage.error(res.msg || langText.value.approvalFailed);
        }
      }).catch(err => {
        console.error("审批操作失败", err);
        ElMessage.error(langText.value.approvalProcessFailed + ': ' + (err.message || '未知错误'));
      });
    };
    
    // 确认提交拒绝理由
    const confirmReject = () => {
      if (!approvalReason.value.trim()) {
        ElMessage.warning(langText.value.pleaseEnterReason);
        return;
      }
      submitApproval(false, approvalReason.value);
    };
    
    // 取消拒绝
    const cancelReject = () => {
      rejectDialogVisible.value = false;
      approvalReason.value = '';
    };

    // 状态标签颜色
    const statusTagType = (status) => {
      if (status === "待审批" || status === "审批中" || status === "Pending") return "primary"; // 蓝色
      if (status === "已通过" || status === "审批通过") return "success"; // 绿色
      if (status === "已驳回" || status === "审批不通过") return "danger"; // 红色
      return "";
    };

    // 获取审批历史消息的样式类
    const getApprovalHistoryClass = (status) => {
      if (status === "已通过" || status === "审批通过") return "approval-success";
      if (status === "已驳回" || status === "审批不通过") return "approval-danger";
      return "";
    };

    // 获取设备类型名称
    const getDeviceTypeName = (type) => {
      const types = {
        'Standard Notebook': 'Standard Notebook',
        'Performance Notebook': 'Performance Notebook',
        'Standard Desktop': 'Standard Desktop',
        'Performance Desktop': 'Performance Desktop',
        // 保留旧映射以兼容已有数据
        'normalLaptop': 'Standard Notebook',
        'workstationLaptop': 'Performance Notebook',
        'normalDesktop': 'Standard Desktop',
        'workstationDesktop': 'Performance Desktop'
      };
      return types[type] || type;
    };

    onMounted(() => {
      // 首先获取审批人列表，然后获取审批列表
      fetchApproverList();
    });

    return {
      approvalList,
      loading,
      approvalDialogVisible,
      selectedApproval,
      viewApprovalDetails,
      approveApplication,
      statusTagType,
      getDeviceTypeName,
      pageData,
      total,
      handleCurrentChange,
      approvalType,
      switchApprovalType,
      getApprovalHistoryClass,
      getActionButtonText,
      approverList,
      selectedApproverId,
      handleApproverChange,
      approvalReason,
      rejectDialogVisible,
      confirmReject,
      cancelReject,
      // 新增变量
      currentLang,
      langText,
      getLocalizedStatus
    };
  },
};
</script>

<style scoped>
/* 科技风格卡片基础样式 */
.usr_card_override {
  margin-bottom: 20px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid rgba(0, 83, 137, 0.2);
  box-shadow: 0 4px 20px rgba(0, 83, 137, 0.08);
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.usr_card_override::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #005389, #029165, #005389);
  background-size: 200% auto;
  animation: gradientShift 3s ease infinite;
}

.usr_card_override:hover {
  border-color: rgba(0, 83, 137, 0.4);
  box-shadow: 0 8px 30px rgba(0, 83, 137, 0.15);
}

@keyframes gradientShift {
  0% { background-position: 0% center; }
  50% { background-position: 100% center; }
  100% { background-position: 0% center; }
}

/* 卡片标题样式 */
.usr_card_override h3 {
  font-weight: 600 !important;
  font-size: 16px;
  margin-bottom: 16px;
  position: relative;
}

/* 卡片内容区域 */
.usr_card_override :deep(.el-card__body) {
  padding: 24px;
  position: relative;
}

.top {
  margin-top: 10px;
}

/* 审批切换按钮 */
.approval-tabs {
  margin-bottom: 20px;
  display: flex;
  gap: 15px;
  align-items: center;
}

.approval-tabs .el-button--primary {
  background: linear-gradient(135deg, #005389, #029165);
  border: none;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 83, 137, 0.2);
  transition: all 0.3s ease;
}

.approval-tabs .el-button--primary:hover {
  background: linear-gradient(135deg, #0068ab, #02a674);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 83, 137, 0.3);
}

.approval-tabs .el-button--default {
  border: 1px solid rgba(0, 83, 137, 0.3);
  color: #005389;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.approval-tabs .el-button--default:hover {
  border-color: #005389;
  color: #005389;
  background-color: rgba(0, 83, 137, 0.05);
}

/* 审批人选择下拉框样式 */
.approver-selector {
  margin-left: auto;
  min-width: 150px;
}

.approver-selector :deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid rgba(0, 83, 137, 0.2);
  transition: all 0.3s ease;
}

.approver-selector :deep(.el-select .el-input__wrapper:hover) {
  border-color: rgba(0, 83, 137, 0.4);
  box-shadow: 0 2px 8px rgba(0, 83, 137, 0.1);
}

.approver-selector :deep(.el-select .el-input__wrapper.is-focus) {
  border-color: #005389;
  box-shadow: 0 0 0 2px rgba(0, 83, 137, 0.2);
}

/* 表格容器 */
.table-container {
  min-height: auto;
  position: relative;
}

/* 科技风格表格 */
.el-table {
  margin-top: 20px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(0, 83, 137, 0.1);
}

/* 调整表格内容间距 */
:deep(.el-table__header) {
  background: linear-gradient(135deg, rgba(0, 83, 137, 0.05), rgba(2, 145, 101, 0.05));
}

:deep(.el-table__header) th {
  padding: 12px 0;
  font-weight: bold;
  background: transparent;
  border-bottom: 2px solid rgba(0, 83, 137, 0.1);
}

:deep(.el-table__row) {
  height: 47px;
  transition: all 0.3s ease;
}

:deep(.el-table__row:hover) {
  background-color: rgba(0, 83, 137, 0.02) !important;
}

/* 确保表格即使没有足够数据也保持固定高度 */
:deep(.el-table__empty-block) {
  min-height: 470px;
}

/* 状态标签恢复默认样式 */
:deep(.el-tag) {
  border-radius: 4px;
  font-weight: normal;
  padding: 0 10px;
}

/* 操作按钮恢复默认样式 */
:deep(.el-button--primary.is-text) {
  color: #409EFF;
  transition: all 0.3s ease;
}

:deep(.el-button--primary.is-text:hover) {
  color: #66b1ff;
  background-color: #ecf5ff;
}

/* 流程未到达按钮保持现有样式 */
:deep(.flow-not-reached) {
  color: #909399 !important;
  background-color: #f4f4f5 !important;
  padding: 3px 6px !important;
  border-radius: 4px !important;
}

/* 分页组件蓝绿色主题 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 10px 0;
  border-top: 1px solid rgba(0, 83, 137, 0.1);
  padding-top: 15px;
}

.pagination-container :deep(.el-pagination .el-pager li.is-active) {
  background: linear-gradient(135deg, #005389, #029165) !important;
  color: #ffffff !important;
  font-weight: normal !important;
  border-color: #005389 !important;
}

.pagination-container :deep(.el-pagination .el-pager li.is-active:hover) {
  background: linear-gradient(135deg, #005389, #029165) !important;
  color: #ffffff !important;
}

.pagination-container :deep(.el-pagination .btn-prev:hover),
.pagination-container :deep(.el-pagination .btn-next:hover) {
  color: #005389;
}

.pagination-container :deep(.el-pagination .el-pager li:hover) {
  color: #005389;
}

.pagination-container :deep(.el-pagination .el-select .el-input.is-focus .el-input__wrapper) {
  border-color: #005389;
  box-shadow: 0 0 0 1px rgba(0, 83, 137, 0.2);
}

.pagination-container :deep(.el-pagination .el-input__wrapper:hover) {
  border-color: rgba(0, 83, 137, 0.4);
}

/* 申请详情弹窗样式 */
.application-detail-dialog {
  :deep(.el-dialog__header) {
    padding: 0;
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }

  :deep(.el-dialog__headerbtn) {
    z-index: 10;
    top: 10px;
    right: 10px;
  }

  :deep(.el-dialog__headerbtn .el-dialog__close) {
    color: #fff;
    font-size: 18px;
    transition: transform 0.3s ease;
  }
  
  :deep(.el-dialog__headerbtn:hover .el-dialog__close) {
    transform: rotate(90deg);
  }
  
  /* Dialog animation */
  :deep(.el-dialog) {
    transform: translateY(0);
    opacity: 1;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
    animation: dialogFadeIn 0.3s ease-out;
  }
  
  :deep(.el-dialog__wrapper) {
    &:not(.is-visible) {
      .el-dialog {
        transform: translateY(-20px);
        opacity: 0;
      }
    }
  }
}

@keyframes dialogFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.application-detail-container {
  position: relative;
  border-radius: 4px;
  overflow: hidden;
}

.detail-header {
  background: linear-gradient(135deg, #2580bf 0%, #20b2aa 100%);
  color: #fff;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
  
  /* Add shimmer animation */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -150%;
    width: 150%;
    height: 100%;
    background: linear-gradient(to right, 
      rgba(255, 255, 255, 0) 0%,
      rgba(255, 255, 255, 0.2) 50%,
      rgba(255, 255, 255, 0) 100%
    );
    transform: skewX(-25deg);
    animation: shimmer 5s infinite;
  }
}

.application-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  position: relative;
}

.computer-name {
  font-size: 20px;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  animation: fadeInLeft 0.5s ease-out;
}

.status-tag {
  font-weight: 500;
  transition: all 0.3s ease;
  transform-origin: right;
  animation: scaleIn 0.4s ease-out 0.2s both;
}

.application-info {
  display: flex;
  gap: 20px;
  font-size: 14px;
  position: relative;
  animation: fadeInUp 0.5s ease-out 0.1s both;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 5px;
  transition: transform 0.2s ease;
  
  &:hover {
    transform: translateY(-2px);
  }
}

.detail-content {
  padding: 20px;
  background-color: #f8f9fa;
  animation: fadeIn 0.5s ease-out 0.1s both;
  position: relative;
}

.detail-descriptions {
  margin-bottom: 20px;
  
  :deep(.item-label) {
    background-color: rgba(100, 180, 255, 0.1);
    width: 120px;
    text-align: right;
    font-weight: 500;
    transition: background-color 0.3s ease;
  }
  
  :deep(.item-content) {
    padding: 8px 12px;
    transition: background-color 0.3s ease;
  }
  
  :deep(.el-descriptions__table) {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    border-radius: 4px;
    overflow: hidden;
    animation: fadeInUp 0.5s ease-out 0.2s both;
  }
  
  :deep(.el-descriptions__cell) {
    &:hover {
      .item-label {
        background-color: rgba(100, 180, 255, 0.2);
      }
      .item-content {
        background-color: rgba(240, 247, 255, 0.5);
      }
    }
  }
}

.reason-section {
  background-color: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  margin-top: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
  animation: fadeInUp 0.5s ease-out 0.3s both;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(37, 128, 191, 0.1);
  }
}

.reason-title {
  font-weight: 600;
  margin-bottom: 8px;
  color: #2580bf;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 5px;
  
  /* Add glowing dot before the title */
  &::before {
    content: '';
    display: inline-block;
    width: 8px;
    height: 8px;
    background-color: #2580bf;
    border-radius: 50%;
    margin-right: 8px;
    vertical-align: middle;
    animation: pulse 2s infinite;
  }
}

.reason-content {
  white-space: pre-wrap;
  line-height: 1.6;
  padding: 5px;
  border-radius: 3px;
  transition: background-color 0.3s ease;
  min-height: 60px;
  word-break: break-word;
  
  &:hover {
    background-color: rgba(240, 247, 255, 0.5);
  }
}

/* 审批理由区域样式 */
.approval-reason-section {
  margin-top: 15px;
  border-top: 1px dashed #e8e8e8;
  padding-top: 15px;
}

.approval-reason-section .reason-title {
  color: #2580bf;
}

.approval-reason-section .reason-title::before {
  background-color: #2580bf;
}

.approval-actions {
  padding: 15px 20px;
  background-color: #fff;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: center;
  animation: fadeInUp 0.5s ease-out 0.4s both;
}

.action-buttons {
  display: flex;
  gap: 25px;
}

.action-buttons .el-button {
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  
  &::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 5px;
    height: 5px;
    background: rgba(255, 255, 255, 0.5);
    opacity: 0;
    border-radius: 100%;
    transform: scale(1, 1) translate(-50%);
    transform-origin: 50% 50%;
  }
  
  &:hover::after {
    animation: ripple 0.6s ease-out;
  }
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  }
  
  &:active {
    transform: translateY(0);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}

.action-buttons .el-button--success {
  background: linear-gradient(135deg, #005389, #029165);
  border: none;
}

.action-buttons .el-button--danger {
  background: linear-gradient(135deg, #e54b6b, #ff7676);
  border: none;
}

/* 审批历史消息样式 - 技术风格 */
.approval-history {
  padding: 16px 20px;
  border-radius: 8px;
  color: #606266;
  font-size: 14px;
  background-color: #f8fafc;
  display: flex;
  align-items: center;
  gap: 15px;
  width: 100%;
  max-width: 500px;
  justify-content: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  }
  
  /* Add subtle shimmer */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(to right, 
      rgba(255, 255, 255, 0) 0%,
      rgba(255, 255, 255, 0.3) 50%,
      rgba(255, 255, 255, 0) 100%
    );
    animation: subtleShimmer 3s infinite;
  }
}

.approval-success {
  background-color: rgba(32, 178, 170, 0.05);
  color: #20b2aa;
  border: none;
  
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    height: 100%;
    background-color: #20b2aa;
  }
}

.approval-danger {
  background-color: rgba(245, 88, 123, 0.05);
  color: #f5587b;
  border: none;
  
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    height: 100%;
    background-color: #f5587b;
  }
}

/* 等待上一级审批样式 - 技术风格 */
.approval-waiting {
  padding: 16px 20px;
  border-radius: 8px;
  background-color: rgba(37, 128, 191, 0.05);
  color: #2580bf;
  border: none;
  display: flex;
  align-items: center;
  gap: 15px;
  width: 100%;
  max-width: 500px;
  justify-content: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  }
  
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    height: 100%;
    background-color: #2580bf;
  }
  
  /* Add subtle shimmer */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(to right, 
      rgba(255, 255, 255, 0) 0%,
      rgba(255, 255, 255, 0.3) 50%,
      rgba(255, 255, 255, 0) 100%
    );
    animation: subtleShimmer 3s infinite;
  }
}

.approval-waiting i, 
.approval-history i {
  font-size: 22px;
  animation: pulse 2s infinite;
}

/* 拒绝理由弹框样式 */
.reject-reason-container {
  margin: 10px 0;
}

.reject-reason-container :deep(.el-textarea__inner) {
  border-radius: 8px;
  border: 1px solid rgba(0, 83, 137, 0.2);
  transition: all 0.3s ease;
}

.reject-reason-container :deep(.el-textarea__inner:hover) {
  border-color: rgba(0, 83, 137, 0.4);
  box-shadow: 0 2px 8px rgba(0, 83, 137, 0.1);
}

.reject-reason-container :deep(.el-textarea__inner:focus) {
  border-color: #005389;
  box-shadow: 0 0 0 2px rgba(0, 83, 137, 0.2);
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 500;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
  gap: 10px;
}

.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #005389, #029165);
  border: none;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.dialog-footer .el-button--primary:hover {
  background: linear-gradient(135deg, #0068ab, #02a674);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 83, 137, 0.3);
}

.dialog-footer .el-button--default {
  border: 1px solid rgba(0, 83, 137, 0.3);
  color: #005389;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.dialog-footer .el-button--default:hover {
  border-color: #005389;
  color: #005389;
  background-color: rgba(0, 83, 137, 0.05);
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(37, 128, 191, 0.4);
  }
  70% {
    box-shadow: 0 0 0 6px rgba(37, 128, 191, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(37, 128, 191, 0);
  }
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes shimmer {
  0% {
    left: -150%;
  }
  50% {
    left: 150%;
  }
  100% {
    left: 150%;
  }
}

@keyframes subtleShimmer {
  0% {
    left: -100%;
  }
  50% {
    left: 100%;
  }
  100% {
    left: 100%;
  }
}

@keyframes ripple {
  0% {
    transform: scale(0, 0);
    opacity: 0.5;
  }
  20% {
    transform: scale(25, 25);
    opacity: 0.3;
  }
  100% {
    opacity: 0;
    transform: scale(40, 40);
  }
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .application-info {
    flex-direction: column;
    gap: 10px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 15px;
  }
  
  .detail-header {
    padding: 20px;
  }
  
  .detail-content {
    padding: 20px;
  }
  
  .approval-tabs {
    flex-direction: column;
    align-items: stretch;
  }
  
  .approver-selector {
    margin-left: 0;
    margin-top: 10px;
  }
}
</style>
