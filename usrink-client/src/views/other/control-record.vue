<template>
  <div style="padding: 20px;">
    <el-card shadow="never" class="usr_card_override">
      <template #header>
        <div class="card-header">
          <span>{{ langText.computerHistory }}</span>
        </div>
      </template>
      
      <!-- Search Form -->
      <el-form :model="queryForm" :inline="true" class="search-form">
        <el-form-item :label="langText.computerName">
          <el-input v-model="queryForm.ciName" :placeholder="langText.enterComputerName" clearable />
        </el-form-item>
        <el-form-item :label="langText.updateDate">
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
          <el-button type="primary" @click="fetchRecordList">{{ langText.search }}</el-button>
          <el-button @click="resetForm">{{ langText.reset }}</el-button>
        </el-form-item>
      </el-form>
      
      <!-- Data Table -->
      <el-table 
        v-loading="loading" 
        :data="recordList" 
        border 
        style="width: 100%; overflow-x: auto;"
        :table-layout="'fixed'"
      >
        <el-table-column type="index" :label="langText.sequence" width="80" />
        <el-table-column prop="ciName" :label="langText.computerName" width="160" />
        <el-table-column prop="pcStatus" :label="langText.computerStatus" width="120" />
        <el-table-column prop="serialNumber" :label="langText.serialNumber" width="150" />
        <el-table-column prop="deviceClass" :label="langText.deviceType" width="120" />
        <el-table-column prop="manufacture" :label="langText.manufacturer" width="120" />
        <el-table-column prop="modelOrVersion" :label="langText.model" width="120" />
        <el-table-column prop="ntAccount" :label="langText.ntAccount" width="150" />
        <el-table-column prop="pcClass" :label="langText.computerCategory" width="150" />
        <el-table-column prop="comment" :label="langText.comment" width="150" />
        <el-table-column prop="lastName" :label="langText.lastName" width="100" />
        <el-table-column prop="firstName" :label="langText.firstName" width="100" />
        <el-table-column prop="emailAddress" :label="langText.email" width="200" />
        <el-table-column prop="department" :label="langText.department" width="150" />
        <el-table-column prop="telephone" :label="langText.phone" width="150" />
        <el-table-column prop="costCenter" :label="langText.costCenter" width="120" />
        <el-table-column prop="lifeCycleStart" :label="langText.manufactureDate" width="120" />
        <el-table-column prop="yrsToDay" :label="langText.yearsSinceManufacture" width="200" />
        <el-table-column prop="cpu" label="CPU" width="150" />
        <el-table-column prop="memory" :label="langText.memory" width="100" />
        <el-table-column prop="disk" :label="langText.disk" width="100" />
        <el-table-column prop="graphic" :label="langText.graphics" width="100" />
        <el-table-column prop="hardwareStatus" :label="langText.hardwareStatus" width="120" />
        <el-table-column prop="pr" :label="langText.pr" width="120" />
        <el-table-column prop="po" :label="langText.po" width="120" />
        <el-table-column prop="vendor" :label="langText.vendor" width="150" />
        <el-table-column prop="company" :label="langText.company" width="120" />
        <el-table-column prop="wbsNum" :label="langText.wbsNum" width="120" />
        <el-table-column prop="price" :label="langText.price" width="100" />
        <el-table-column prop="temp" :label="langText.tempAssignment" width="120">
          <template #default="scope">
            {{ scope.row.temp === 1 ? langText.yes : langText.no }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" :label="langText.updateTime" width="180" fixed="right" />
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
const recordList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const queryForm = ref({
  ciName: '',
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
    computerHistory: currentLang.value === 'zh' ? '电脑修改记录' : 'Computer History Records',
    
    // 查询表单
    computerName: currentLang.value === 'zh' ? '电脑名' : 'Computer Name',
    enterComputerName: currentLang.value === 'zh' ? '输入电脑名' : 'Enter computer name',
    updateDate: currentLang.value === 'zh' ? '更新日期' : 'Update Date',
    dateTo: currentLang.value === 'zh' ? '至' : 'to',
    startDate: currentLang.value === 'zh' ? '开始日期' : 'Start Date',
    endDate: currentLang.value === 'zh' ? '结束日期' : 'End Date',
    search: currentLang.value === 'zh' ? '搜索' : 'Search',
    reset: currentLang.value === 'zh' ? '重置' : 'Reset',
    
    // 表格列名
    sequence: currentLang.value === 'zh' ? '序号' : 'No.',
    computerStatus: currentLang.value === 'zh' ? '电脑状态' : 'Status',
    serialNumber: currentLang.value === 'zh' ? '序列号' : 'Serial Number',
    deviceType: currentLang.value === 'zh' ? '设备类型' : 'Device Type',
    manufacturer: currentLang.value === 'zh' ? '制造商' : 'Manufacturer',
    model: currentLang.value === 'zh' ? '型号' : 'Model',
    ntAccount: currentLang.value === 'zh' ? 'NT账号' : 'NT Account',
    computerCategory: currentLang.value === 'zh' ? '电脑归属情况' : 'Category',
    comment: currentLang.value === 'zh' ? '备注' : 'Comment',
    lastName: currentLang.value === 'zh' ? '姓' : 'Last Name',
    firstName: currentLang.value === 'zh' ? '名' : 'First Name',
    email: currentLang.value === 'zh' ? '邮箱地址' : 'Email',
    department: currentLang.value === 'zh' ? '所属部门' : 'Department',
    phone: currentLang.value === 'zh' ? '电话号码' : 'Phone',
    costCenter: currentLang.value === 'zh' ? '成本中心' : 'Cost Center',
    manufactureDate: currentLang.value === 'zh' ? '出厂时间' : 'Manufacture Date',
    yearsSinceManufacture: currentLang.value === 'zh' ? '出厂日期到今天的时间' : 'Years Since Manufacture',
    memory: currentLang.value === 'zh' ? '内存' : 'Memory',
    disk: currentLang.value === 'zh' ? '硬盘' : 'Disk',
    graphics: currentLang.value === 'zh' ? '显卡' : 'Graphics',
    hardwareStatus: currentLang.value === 'zh' ? '硬件状态' : 'Hardware Status',
    pr: currentLang.value === 'zh' ? '下单号' : 'PR',
    po: currentLang.value === 'zh' ? '订单号' : 'PO',
    vendor: currentLang.value === 'zh' ? '供应商公司' : 'Vendor',
    company: currentLang.value === 'zh' ? '公司' : 'Company',
    wbsNum: currentLang.value === 'zh' ? '资产号' : 'WBS No.',
    price: currentLang.value === 'zh' ? '价格' : 'Price',
    tempAssignment: currentLang.value === 'zh' ? '临时分配标识' : 'Temp Assignment',
    updateTime: currentLang.value === 'zh' ? '更新时间' : 'Update Time',
    yes: currentLang.value === 'zh' ? '是' : 'Yes',
    no: currentLang.value === 'zh' ? '否' : 'No'
  }
});

// 获取记录列表
const fetchRecordList = () => {
  loading.value = true
  recordList.value = []
  
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
    ciName: queryForm.value.ciName ? `%${queryForm.value.ciName}%` : null,
    startTime: queryForm.value.startTime || null,
    endTime: queryForm.value.endTime || null,
    pageNum: pageNum.value,
    pageSize: pageSize.value
  }
  
  httpUtil.post('/sysControl/getControlRecordList', params, {
    headers: {
      'Content-Type': 'application/json'
    }
  }).then(res => {
    console.log('获取记录列表响应:', res)
    if (res.data) {
      recordList.value = res.data.list || []
      total.value = res.data.total || 0
    }
  }).catch(err => {
    console.error('Failed to fetch record list:', err)
    ElMessage.error(currentLang.value === 'zh' ? '获取记录列表失败' : 'Failed to fetch record list')
  }).finally(() => {
    loading.value = false
  })
}

// 重置搜索表单
const resetForm = () => {
  queryForm.value.ciName = ''
  queryForm.value.updateTimeRange = []
  queryForm.value.startTime = ''
  queryForm.value.endTime = ''
  pageNum.value = 1
  queryForm.value.pageNum = 1
  fetchRecordList()
}

// 处理页面大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  queryForm.value.pageSize = val
  fetchRecordList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  pageNum.value = val
  queryForm.value.pageNum = val
  fetchRecordList()
}

// 组件挂载时加载数据
onMounted(() => {
  fetchRecordList()
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

/* 分页组件蓝绿色主题 */
.pagination-container {
  margin-top: 20px;
  text-align: right;
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

/* 添加表格相关样式 */
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
</style>
