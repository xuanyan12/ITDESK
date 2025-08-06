<template>
  <div style="padding: 20px;">
    <el-card shadow="never" class="usr_card_override">
      <template #header>
        <div class="card-header">
          <span>{{ langText.maintenanceHistory }}</span>
        </div>
      </template>
      
      <!-- Search Form -->
      <el-form :model="queryForm" :inline="true" class="search-form">
        <el-form-item :label="langText.computerName">
          <el-input v-model="queryForm.ciName" :placeholder="langText.enterComputerName" clearable />
        </el-form-item>
        <el-form-item :label="langText.applicant">
          <el-input v-model="queryForm.applicant" :placeholder="langText.enterApplicant" clearable />
        </el-form-item>
        <el-form-item :label="langText.orderNumber">
          <el-input v-model="queryForm.orderNumber" :placeholder="langText.enterOrderNumber" clearable />
        </el-form-item>
        <el-form-item :label="langText.maintenanceCategory">
          <el-select v-model="queryForm.maintenanceCategory" :placeholder="langText.selectCategory" clearable style="width: 180px;">
            <el-option :label="langText.allCategories" value="" />
            <el-option :label="langText.qualityIssue" value="qualityIssueRepair" />
            <el-option :label="langText.damageRepair" value="damageRepair" />
          </el-select>
        </el-form-item>
        <el-form-item :label="langText.maintenanceDate">
          <el-date-picker
            v-model="queryForm.updateTimeRange"
            type="daterange"
            :range-separator="langText.dateTo"
            :start-placeholder="langText.startDate"
            :end-placeholder="langText.endDate"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchMaintenanceList">{{ langText.search }}</el-button>
          <el-button @click="resetForm">{{ langText.reset }}</el-button>
        </el-form-item>
      </el-form>
      
      <!-- Data Table -->
      <el-table 
        v-loading="loading" 
        :data="maintenanceList" 
        border 
        style="width: 100%; overflow-x: auto;"
        :table-layout="'fixed'"
      >
        <el-table-column type="index" :label="langText.sequence" width="80" />
        <el-table-column prop="ciName" :label="langText.computerName" width="160" />
        <el-table-column prop="applicant" :label="langText.applicant" width="120" />
        <el-table-column prop="orderNumber" :label="langText.orderNumber" width="150" />
        <el-table-column prop="maintenanceRecord" :label="langText.maintenanceRecord" width="250" show-overflow-tooltip />
        <el-table-column prop="maintenanceCategory" :label="langText.maintenanceCategory" width="120">
          <template #default="scope">
            <el-tag :type="getMaintenanceCategoryType(scope.row.maintenanceCategory)">
              {{ getMaintenanceCategoryText(scope.row.maintenanceCategory) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="problemDescription" :label="langText.problemDescription" width="200" show-overflow-tooltip />
        <el-table-column prop="maintenanceResult" :label="langText.maintenanceResult" width="200" show-overflow-tooltip />
        <el-table-column prop="maintenanceRemark" :label="langText.maintenanceRemark" width="150" show-overflow-tooltip />
        <el-table-column prop="maintenanceTime" :label="langText.maintenanceTime" width="180" fixed="right" />
      </el-table>
      
      <!-- Pagination -->
      <div class="pagination-container">
        <el-pagination
          :current-page="pageNum"
          :page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import httpUtil from '@/utils/HttpUtil'
import { ElMessage } from 'element-plus'
import { useLanguageStore } from '@/stores/_frame/languageStore'

// 主表数据
const loading = ref(false)
const maintenanceList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const queryForm = ref({
  ciName: '',
  applicant: '',
  orderNumber: '',
  maintenanceCategory: '',
  updateTimeRange: [],
  startTime: '',
  endTime: '',
  pageNum: 1,
  pageSize: 10
})

// 引入语言状态管理
const languageStore = useLanguageStore();
const currentLang = computed(() => languageStore.currentLang);

// 多语言文本
const langText = computed(() => {
  return {
    // 页面标题
    maintenanceHistory: currentLang.value === 'zh' ? '电脑维修记录' : 'Computer Maintenance Records',
    
    // 查询表单
    computerName: currentLang.value === 'zh' ? '电脑名' : 'Computer Name',
    enterComputerName: currentLang.value === 'zh' ? '输入电脑名' : 'Enter computer name',
    applicant: currentLang.value === 'zh' ? '申请人' : 'Applicant',
    enterApplicant: currentLang.value === 'zh' ? '输入申请人' : 'Enter applicant',
    orderNumber: currentLang.value === 'zh' ? '维修订单号' : 'Order Number',
    enterOrderNumber: currentLang.value === 'zh' ? '输入订单号' : 'Enter order number',
    maintenanceCategory: currentLang.value === 'zh' ? '维修类别' : 'Maintenance Category',
    selectCategory: currentLang.value === 'zh' ? '选择维修类别' : 'Select category',
    allCategories: currentLang.value === 'zh' ? '全部类别' : 'All Categories',
    qualityIssue: currentLang.value === 'zh' ? '质量问题维修' : 'Quality Issue Repair',
    damageRepair: currentLang.value === 'zh' ? '人为损坏维修' : 'Damage Repair',
    maintenanceDate: currentLang.value === 'zh' ? '维修日期' : 'Maintenance Date',
    dateTo: currentLang.value === 'zh' ? '至' : 'to',
    startDate: currentLang.value === 'zh' ? '开始日期' : 'Start Date',
    endDate: currentLang.value === 'zh' ? '结束日期' : 'End Date',
    search: currentLang.value === 'zh' ? '搜索' : 'Search',
    reset: currentLang.value === 'zh' ? '重置' : 'Reset',
    
    // 表格列名
    sequence: currentLang.value === 'zh' ? '序号' : 'No.',
    applicant: currentLang.value === 'zh' ? '申请人' : 'Applicant',
    orderNumber: currentLang.value === 'zh' ? '维修订单号' : 'Order Number',
    maintenanceRecord: currentLang.value === 'zh' ? '维修记录' : 'Maintenance Record',
    maintenanceCategory: currentLang.value === 'zh' ? '维修类别' : 'Maintenance Category',
    problemDescription: currentLang.value === 'zh' ? '故障描述' : 'Problem Description',
    maintenanceResult: currentLang.value === 'zh' ? '维修结果' : 'Maintenance Result',
    maintenanceRemark: currentLang.value === 'zh' ? '维修备注' : 'Maintenance Remark',
    maintenanceTime: currentLang.value === 'zh' ? '维修完成时间' : 'Maintenance Time'
  }
});

// 获取维修记录列表
const fetchMaintenanceList = () => {
  loading.value = true
  maintenanceList.value = []
  
  // 处理日期范围
  if (queryForm.value.updateTimeRange && queryForm.value.updateTimeRange.length === 2) {
    queryForm.value.startTime = queryForm.value.updateTimeRange[0]
    queryForm.value.endTime = queryForm.value.updateTimeRange[1]
  } else {
    queryForm.value.startTime = ''
    queryForm.value.endTime = ''
  }
  
  // 构建查询参数对象
  const params = {
    ciName: queryForm.value.ciName || '',
    applicant: queryForm.value.applicant || '',
    orderNumber: queryForm.value.orderNumber || '',
    maintenanceCategory: queryForm.value.maintenanceCategory || '',
    startTime: queryForm.value.startTime || '',
    endTime: queryForm.value.endTime || '',
    pageNum: pageNum.value,
    pageSize: pageSize.value
  }
  
  httpUtil.post('/sysControlMaintenance/getControlMaintenanceList', params, {
    headers: {
      'Content-Type': 'application/json'
    }
  }).then(res => {
    console.log('获取维修记录列表响应:', res)
    if (res.data) {
      maintenanceList.value = res.data.list || []
      total.value = res.data.total || 0
      console.log('维修记录列表数据:', maintenanceList.value)
      console.log('总记录数:', total.value)
    }
  }).catch(err => {
    console.error('Failed to fetch maintenance list:', err)
    ElMessage.error(currentLang.value === 'zh' ? '获取维修记录列表失败' : 'Failed to fetch maintenance list')
  }).finally(() => {
    loading.value = false
  })
}

// 重置搜索表单
const resetForm = () => {
  queryForm.value.ciName = ''
  queryForm.value.applicant = ''
  queryForm.value.orderNumber = ''
  queryForm.value.maintenanceCategory = ''
  queryForm.value.updateTimeRange = []
  queryForm.value.startTime = ''
  queryForm.value.endTime = ''
  pageNum.value = 1
  queryForm.value.pageNum = 1
  fetchMaintenanceList()
}

// 处理页面大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  queryForm.value.pageSize = val
  fetchMaintenanceList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  pageNum.value = val
  queryForm.value.pageNum = val
  fetchMaintenanceList()
}

// 获取维修类别标签类型
const getMaintenanceCategoryType = (category) => {
  if (category === 'qualityIssueRepair') return 'success'
  if (category === 'damageRepair') return 'warning'
  return 'info'
}

// 获取维修类别文本
const getMaintenanceCategoryText = (category) => {
  if (category === 'qualityIssueRepair') return currentLang.value === 'zh' ? '质量问题维修' : 'Quality Issue Repair'
  if (category === 'damageRepair') return currentLang.value === 'zh' ? '人为损坏维修' : 'Damage Repair'
  return category || (currentLang.value === 'zh' ? '未知类别' : 'Unknown Category')
}

// 组件挂载时加载数据
onMounted(() => {
  fetchMaintenanceList()
})
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
  z-index: 1;
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
.usr_card_override :deep(.el-card__header) {
  padding: 24px;
  position: relative;
  background-color: #fff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
}

/* 卡片内容区域 */
.usr_card_override :deep(.el-card__body) {
  padding: 24px;
  position: relative;
}

/* 搜索表单科技风格 */
.search-form {
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: flex-start;
}

.search-form .el-form-item {
  margin-bottom: 15px;
  margin-right: 0;
}

.search-form :deep(.el-form-item__label) {
  font-weight: 500;
}

.search-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid rgba(0, 83, 137, 0.2);
  transition: all 0.3s ease;
}

.search-form :deep(.el-input__wrapper:hover) {
  border-color: rgba(0, 83, 137, 0.4);
  box-shadow: 0 2px 8px rgba(0, 83, 137, 0.1);
}

.search-form :deep(.el-input__wrapper.is-focus) {
  border-color: #005389;
  box-shadow: 0 0 0 2px rgba(0, 83, 137, 0.2);
}

.search-form :deep(.el-date-editor .el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid rgba(0, 83, 137, 0.2);
  transition: all 0.3s ease;
}

.search-form :deep(.el-date-editor .el-input__wrapper:hover) {
  border-color: rgba(0, 83, 137, 0.4);
  box-shadow: 0 2px 8px rgba(0, 83, 137, 0.1);
}

.search-form :deep(.el-date-editor .el-input__wrapper.is-focus) {
  border-color: #005389;
  box-shadow: 0 0 0 2px rgba(0, 83, 137, 0.2);
}

/* 表格样式 */
.el-table {
  width: 100% !important;
  overflow-x: auto !important;
}

.el-table :deep(.el-table__body-wrapper) {
  overflow-x: auto !important;
}

.el-table :deep(.el-table__fixed-right) {
  height: auto !important;
  box-shadow: -2px 0 4px rgba(0, 0, 0, 0.12) !important;
}

.el-table :deep(.el-table__fixed) {
  height: auto !important;
  box-shadow: 2px 0 4px rgba(0, 0, 0, 0.12) !important;
}

/* 分页组件蓝绿色主题 */
.pagination-container {
  margin-top: 20px;
  text-align: left;
  padding: 15px 20px;
  background-color: #fff;
  border-top: 1px solid #eaeaea;
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

/* 科技风格按钮 */
.search-form :deep(.el-button--primary) {
  background: linear-gradient(135deg, #005389, #029165);
  border: none;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 83, 137, 0.2);
  transition: all 0.3s ease;
}

.search-form :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #0068ab, #02a674);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 83, 137, 0.3);
}

.search-form :deep(.el-button--default) {
  border: 1px solid rgba(0, 83, 137, 0.3);
  color: #005389;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.search-form :deep(.el-button--default:hover) {
  border-color: #005389;
  color: #005389;
  background-color: rgba(0, 83, 137, 0.05);
}
</style> 