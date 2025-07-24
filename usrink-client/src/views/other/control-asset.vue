<template>
  <div style="padding: 20px;">
    <!-- 搜索表单卡片 -->
    <el-card shadow="never" class="usr_card_override">
      <template #header>
        <div class="card-header">
          <span>{{ t('电脑台账') }}</span>
        </div>
      </template>
      
      <!-- 搜索表单 -->
      <el-form :model="queryForm" :inline="true" class="search-form">
        <el-form-item :label="t('电脑名')">
          <el-input v-model="queryForm.ciName" :placeholder="t('输入电脑名')" clearable />
        </el-form-item>
        <el-form-item :label="t('NT账号')">
          <el-input v-model="queryForm.ntAccount" :placeholder="t('输入NT账号，用逗号分隔')" clearable />
        </el-form-item>
        <el-form-item :label="t('部门')">
          <el-input v-model="queryForm.department" :placeholder="t('输入部门')" clearable />
        </el-form-item>
        <el-form-item :label="t('WBS号')">
          <el-input v-model="queryForm.wbsNum" :placeholder="t('输入WBS号，用逗号分隔')" clearable />
        </el-form-item>
        <el-form-item :label="t('残值状态')">
          <el-select v-model="queryForm.residualValueStatus" :placeholder="t('选择残值状态')" clearable>
            <el-option :label="t('全部')" value="" />
            <el-option :label="t('残值为0')" value="zero" />
            <el-option :label="t('残值不为0')" value="nonzero" />
          </el-select>
        </el-form-item>
        <el-form-item class="form-item">
          <el-button type="primary" @click="fetchAssetList">{{ t('查询') }}</el-button>
          <el-button @click="resetForm">{{ t('重置') }}</el-button>
          <el-button type="primary" @click="handleExport" :loading="exportLoading">
            <el-icon><Download /></el-icon>
            {{ t('导出数据') }}
          </el-button>
          <el-upload
            class="upload-inline"
            :action="BASE_URL + '/sysControlAsset/importIfrs'"
            :show-file-list="false"
            :headers="headers"
            :before-upload="beforeUpload"
            :on-success="handleIfrsSuccess"
            :on-error="handleUploadError"
          >
            <el-button type="success" :loading="ifrsLoading">
              <el-icon><Upload /></el-icon>
              {{ t('导入IFRS') }}
            </el-button>
          </el-upload>
          <el-upload
            class="upload-inline"
            :action="BASE_URL + '/sysControlAsset/importPrc'"
            :show-file-list="false"
            :headers="headers"
            :before-upload="beforeUpload"
            :on-success="handlePrcSuccess"
            :on-error="handleUploadError"
          >
            <el-button type="warning" :loading="prcLoading">
              <el-icon><Upload /></el-icon>
              {{ t('导入PRC') }}
            </el-button>
          </el-upload>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 资产列表表格 -->
    <div class="usr_card_override table-container">
      <el-table 
        :data="assetList" 
        :loading="loading" 
        style="width: 100%;"
        border
        :header-cell-style="{backgroundColor: '#f5f7fa'}"
      >
        <el-table-column prop="ciName" :label="t('电脑名')" min-width="120">
          <template #default="{ row }">
            <el-tooltip :content="row.ciName" placement="top">
              <span class="ellipsis">{{ row.ciName }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="modelOrVersion" :label="t('电脑型号')" min-width="120" />
        <el-table-column prop="ntAccount" :label="t('NT账号')" min-width="120" />
        <el-table-column prop="userName" :label="t('使用人')" min-width="120" />
        <el-table-column prop="department" :label="t('所属部门')" min-width="120" />
        <el-table-column prop="wbsNum" :label="t('WBS号')" min-width="120" />
        <el-table-column prop="price" :label="t('价格')" min-width="100">
          <template #default="{ row }">
            {{ formatNumber(row.price) }}
          </template>
        </el-table-column>
        <el-table-column prop="costCenter" :label="t('成本中心')" min-width="120" />
        <el-table-column prop="ifrsValue" :label="t('IFRS总原值')" min-width="120">
          <template #default="{ row }">
            {{ formatNumber(row.ifrsValue) }}
          </template>
        </el-table-column>
        <el-table-column prop="ifrsAllResidualValue" :label="t('IFRS总残值')" min-width="120">
          <template #default="{ row }">
            {{ formatNumber(row.ifrsAllResidualValue) }}
          </template>
        </el-table-column>
        <el-table-column prop="prcAllResidualValue" :label="t('PRC总残值')" min-width="120">
          <template #default="{ row }">
            {{ formatNumber(row.prcAllResidualValue) }}
          </template>
        </el-table-column>
        <el-table-column prop="wbsPercent" :label="t('价值占比')" min-width="100" />
        <el-table-column prop="prcResidualValue" :label="t('PRC残值')" min-width="120">
          <template #default="{ row }">
            {{ formatNumber(row.prcResidualValue) }}
          </template>
        </el-table-column>
        <el-table-column prop="ifrsResidualValue" :label="t('IFRS残值')" min-width="120">
          <template #default="{ row }">
            {{ formatNumber(row.ifrsResidualValue) }}
          </template>
        </el-table-column>
        <el-table-column prop="tenPercentPrice" :label="t('价格10%')" min-width="100" />
        <el-table-column prop="sellPrice" :label="t('转卖价')" min-width="120">
          <template #default="{ row }">
            {{ formatNumber(row.sellPrice) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Upload } from '@element-plus/icons-vue'
import httpUtil from '@/utils/HttpUtil'
import { useLanguageStore } from '@/stores/_frame/languageStore'
import * as XLSX from 'xlsx'
import loginUtil from '@/utils/LoginUtil'
import { BASE_URL } from '@/utils/Constant' 

// 语言相关
const languageStore = useLanguageStore()
const t = (key) => {
  const translations = {
    '电脑台账': languageStore.currentLang === 'en' ? 'Computer Assets' : '电脑台账',
    '电脑名': languageStore.currentLang === 'en' ? 'Computer Name' : '电脑名',
    'NT账号': languageStore.currentLang === 'en' ? 'NT Account' : 'NT账号',
    '部门': languageStore.currentLang === 'en' ? 'Department' : '部门',
    'WBS号': languageStore.currentLang === 'en' ? 'WBS Number' : 'WBS号',
    '查询': languageStore.currentLang === 'en' ? 'Search' : '查询',
    '重置': languageStore.currentLang === 'en' ? 'Reset' : '重置',
    '导出数据': languageStore.currentLang === 'en' ? 'Export' : '导出数据',
    '导入IFRS': languageStore.currentLang === 'en' ? 'Import IFRS' : '导入IFRS',
    '导入PRC': languageStore.currentLang === 'en' ? 'Import PRC' : '导入PRC',
    '输入电脑名': languageStore.currentLang === 'en' ? 'Enter Computer Name' : '输入电脑名',
    '输入NT账号，用逗号分隔': languageStore.currentLang === 'en' ? 'Enter NT accounts separated by commas' : '输入NT账号，用逗号分隔',
    '输入部门': languageStore.currentLang === 'en' ? 'Enter Department' : '输入部门',
    '输入WBS号，用逗号分隔': languageStore.currentLang === 'en' ? 'Enter WBS numbers separated by commas' : '输入WBS号，用逗号分隔',
    '成本中心': languageStore.currentLang === 'en' ? 'Cost Center' : '成本中心',
    '电脑型号': languageStore.currentLang === 'en' ? 'Computer Model' : '电脑型号',
    '使用人': languageStore.currentLang === 'en' ? 'User' : '使用人',
    '所属部门': languageStore.currentLang === 'en' ? 'Department' : '所属部门',
    '价格': languageStore.currentLang === 'en' ? 'Price' : '价格',
    'IFRS总原值': languageStore.currentLang === 'en' ? 'IFRS Total Original Value' : 'IFRS总原值',
    'IFRS总残值': languageStore.currentLang === 'en' ? 'IFRS Total Residual Value' : 'IFRS总残值',
    'PRC总残值': languageStore.currentLang === 'en' ? 'PRC Total Residual Value' : 'PRC总残值',
    '价值占比': languageStore.currentLang === 'en' ? 'Value Percentage' : '价值占比',
    'PRC残值': languageStore.currentLang === 'en' ? 'PRC Residual Value' : 'PRC残值',
    'IFRS残值': languageStore.currentLang === 'en' ? 'IFRS Residual Value' : 'IFRS残值',
    '价格10%': languageStore.currentLang === 'en' ? 'Price 10%' : '价格10%',
    '转卖价': languageStore.currentLang === 'en' ? 'Resale Price' : '转卖价',
    '残值状态': languageStore.currentLang === 'en' ? 'Residual Value Status' : '残值状态',
    '选择残值状态': languageStore.currentLang === 'en' ? 'Select Residual Value Status' : '选择残值状态',
    '全部': languageStore.currentLang === 'en' ? 'All' : '全部',
    '残值为0': languageStore.currentLang === 'en' ? 'Residual Value is 0' : '残值为0',
    '残值不为0': languageStore.currentLang === 'en' ? 'Residual Value is not 0' : '残值不为0',
    // ... 其他翻译
  }
  return translations[key] || key
}

// 数据相关
const loading = ref(false)
const exportLoading = ref(false)
const assetList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

// 查询表单
const queryForm = ref({
  ciName: '',
  ntAccount: '',
  department: '',
  wbsNum: '',
  residualValueStatus: '',
  pageNum: 1,
  pageSize: 10
})

// 上传相关
const ifrsLoading = ref(false)
const prcLoading = ref(false)
const headers = {
  Authorization: `Bearer ${loginUtil.isAuthenticated()}`
}

// 上传前校验
const beforeUpload = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || 
                  file.type === 'application/vnd.ms-excel'
  if (!isExcel) {
    ElMessage.error('只能上传Excel文件！')
    return false
  }
  return true
}

// 处理IFRS上传成功
const handleIfrsSuccess = (response) => {
  ifrsLoading.value = false
  if (response.code === 200) {
    ElMessage.success('IFRS数据导入成功')
    fetchAssetList() // 刷新列表
  } else {
    ElMessage.error(response.msg || 'IFRS数据导入失败')
  }
}

// 处理PRC上传成功
const handlePrcSuccess = (response) => {
  prcLoading.value = false
  if (response.code === 200) {
    ElMessage.success('PRC数据导入成功')
    fetchAssetList() // 刷新列表
  } else {
    ElMessage.error(response.msg || 'PRC数据导入失败')
  }
}

// 处理上传错误
const handleUploadError = () => {
  ifrsLoading.value = false
  prcLoading.value = false
  ElMessage.error('上传失败，请重试')
}

// 获取资产列表
const fetchAssetList = async () => {
  loading.value = true
  try {
    const res = await httpUtil.post('/sysControlAsset/selectSysControlAssetList', {
      ...queryForm.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      // 确保返回的数据结构正确
      const data = res.data || {}
      assetList.value = data.list || []
      total.value = data.total || 0
      pageNum.value = data.pageNum || 1
      pageSize.value = data.pageSize || 10
    } else {
      console.error('获取资产列表失败:', res)
      ElMessage.error(res.msg || '获取资产列表失败')
    }
  } catch (error) {
    console.error('获取资产列表失败:', error)
    ElMessage.error('获取资产列表失败')
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  queryForm.value = {
    ciName: '',
    ntAccount: '',
    department: '',
    wbsNum: '',
    residualValueStatus: '',
    pageNum: 1,
    pageSize: 10
  }
  pageNum.value = 1
  fetchAssetList()
}

// 处理分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchAssetList()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  fetchAssetList()
}

// 格式化数字
const formatNumber = (num) => {
  if (num === undefined || num === null) return ''
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(num)
}

// 导出数据
const handleExport = async () => {
  exportLoading.value = true
  try {
    // 获取所有数据
    const res = await httpUtil.post('/sysControlAsset/selectSysControlAssetList', {
      ...queryForm.value,
      pageNum: 1,
      pageSize: 999999 // 获取所有数据
    })
    
    if (res.code === 200) {
      // 确保返回的数据结构正确
      const data = res.data || {}
      const list = data.list || []
      
      if (list.length === 0) {
        ElMessage.warning('没有数据可供导出')
        return
      }

      // 准备导出数据
      const exportData = list.map(item => ({
        '电脑名': item.ciName || '',
        '电脑型号': item.modelOrVersion || '',
        'NT账号': item.ntAccount || '',
        '使用人': item.userName || '',
        '所属部门': item.department || '',
        'WBS号': item.wbsNum || '',
        '价格': formatNumber(item.price),
        '成本中心': item.costCenter || '',
        'IFRS总原值': formatNumber(item.ifrsValue),
        'IFRS总残值': formatNumber(item.ifrsAllResidualValue),
        'PRC总残值': formatNumber(item.prcAllResidualValue),
        '价值占比': item.wbsPercent || '',
        'PRC残值': formatNumber(item.prcResidualValue),
        'IFRS残值': formatNumber(item.ifrsResidualValue),
        '价格10%': formatNumber(item.tenPercentPrice),
        '转卖价': formatNumber(item.sellPrice)
      }))

      // 创建工作簿
      const wb = XLSX.utils.book_new()
      const ws = XLSX.utils.json_to_sheet(exportData)
      XLSX.utils.book_append_sheet(wb, ws, '电脑台账')

      // 导出文件
      XLSX.writeFile(wb, '电脑台账.xlsx')
      ElMessage.success('导出成功')
    }
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchAssetList()
})
</script>

<style scoped>
.search-form {
  margin-top: 20px;
}

.search-form :deep(.el-input) {
  width: 200px;
}

.search-form :deep(.el-select) {
  width: 200px;
}

.table-container {
  margin-top: 20px;
  padding: 20px;
  background: #fff;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-start;
}

.ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.el-card__header) {
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}

.upload-inline {
  display: inline-block;
  margin-left: 10px;
}
</style> 