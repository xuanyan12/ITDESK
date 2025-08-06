<template>
  <div class="computer-report-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>电脑报表统计</h2>
      <p>展示各公司四种类型电脑内部员工在用年限统计</p>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-card class="filter-card">
        <el-form :model="filterForm" inline>
          <el-form-item label="公司:">
            <el-select v-model="filterForm.company" placeholder="请选择公司" clearable style="width: 150px">
              <el-option label="全部" value=""></el-option>
              <el-option v-for="company in companies" :key="company" :label="company" :value="company"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="设备类型:">
            <el-select v-model="filterForm.deviceClass" placeholder="请选择设备类型" clearable style="width: 200px">
              <el-option label="全部" value=""></el-option>
              <el-option v-for="type in deviceTypes" :key="type" :label="type" :value="type"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="年限范围:">
            <el-select v-model="filterForm.ageRange" placeholder="请选择年限范围" clearable style="width: 150px">
              <el-option label="全部" value=""></el-option>
              <el-option v-for="range in ageRanges" :key="range" :label="range + '年'" :value="range"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="loadReportData">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-value">{{ totalDevices }}</div>
              <div class="stats-label">设备总数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-value">{{ totalCompanies }}</div>
              <div class="stats-label">涉及公司</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-value">{{ avgAge }}</div>
              <div class="stats-label">平均年限(年)</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-value">{{ oldDevicesCount }}</div>
              <div class="stats-label">6年以上设备</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表展示区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <!-- 公司设备分布图 -->
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>公司设备分布</span>
              </div>
            </template>
            <div ref="companyChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        
        <!-- 年限分布图 -->
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>年限分布统计</span>
              </div>
            </template>
            <div ref="ageChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 设备类型统计 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="24">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>设备类型年限分布</span>
              </div>
            </template>
            <div ref="deviceTypeChartRef" class="chart-container-large"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 详细数据表格 -->
    <div class="table-section">
      <el-card class="table-card">
        <template #header>
          <div class="card-header">
            <span>详细统计数据</span>
            <el-button type="primary" size="small" @click="exportData">导出数据</el-button>
          </div>
        </template>
        
        <el-table :data="reportData.companyDeviceStats" stripe style="width: 100%">
          <el-table-column prop="company" label="公司" width="80"></el-table-column>
          <el-table-column prop="deviceClass" label="设备类型" width="150"></el-table-column>
          <el-table-column prop="totalCount" label="总数量" width="70" align="center"></el-table-column>
          <el-table-column prop="age0to1" label="0-1年" width="60" align="center"></el-table-column>
          <el-table-column prop="age1to2" label="1-2年" width="60" align="center"></el-table-column>
          <el-table-column prop="age2to3" label="2-3年" width="60" align="center"></el-table-column>
          <el-table-column prop="age3to4" label="3-4年" width="60" align="center"></el-table-column>
          <el-table-column prop="age4to5" label="4-5年" width="60" align="center"></el-table-column>
          <el-table-column prop="age5to6" label="5-6年" width="60" align="center"></el-table-column>
          <el-table-column prop="age6to7" label="6-7年" width="60" align="center"></el-table-column>
          <el-table-column prop="age7to8" label="7-8年" width="60" align="center"></el-table-column>
          <el-table-column prop="age8plus" label="8年+" width="60" align="center"></el-table-column>
          <el-table-column prop="avgAge" label="平均年限" width="90" align="center">
            <template #default="scope">
              {{ scope.row.avgAge ? scope.row.avgAge.toFixed(1) + '年' : '-' }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import httpUtil from '@/utils/HttpUtil'

// 响应式数据
const filterForm = reactive({
  company: '',
  deviceClass: '',
  ageRange: '',
  internalOnly: true
})

const reportData = ref({
  companyDeviceStats: [],
  ageRangeStats: []
})

const companies = ref([])
const deviceTypes = ref([])
const ageRanges = ref([])

// 统计数据
const totalDevices = ref(0)
const totalCompanies = ref(0)
const avgAge = ref(0)
const oldDevicesCount = ref(0)

// 图表引用
const companyChartRef = ref(null)
const ageChartRef = ref(null)
const deviceTypeChartRef = ref(null)

// 图表实例
let companyChart = null
let ageChart = null
let deviceTypeChart = null

// 加载基础数据
const loadBasicData = async () => {
  try {
    // 并行加载基础数据
    const [companiesRes, deviceTypesRes, ageRangesRes] = await Promise.all([
      httpUtil.get('/computerReport/getCompanies'),
      httpUtil.get('/computerReport/getDeviceTypes'),
      httpUtil.get('/computerReport/getAgeRanges')
    ])
    
    companies.value = companiesRes.data || []
    deviceTypes.value = deviceTypesRes.data || []
    ageRanges.value = ageRangesRes.data || []
    
  } catch (error) {
    console.error('加载基础数据失败:', error)
    ElMessage.error('加载基础数据失败')
  }
}

// 加载报表数据
const loadReportData = async () => {
  try {
    const response = await httpUtil.post('/computerReport/getReport', filterForm, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    if (response.code === 200) {
      reportData.value = response.data || { companyDeviceStats: [], ageRangeStats: [] }
      calculateStats()
      await nextTick()
      renderCharts()
    } else {
      ElMessage.error(response.msg || '获取报表数据失败')
    }
  } catch (error) {
    console.error('加载报表数据失败:', error)
    ElMessage.error('加载报表数据失败')
  }
}

// 计算统计数据
const calculateStats = () => {
  const stats = reportData.value.companyDeviceStats || []
  
  totalDevices.value = stats.reduce((sum, item) => sum + (item.totalCount || 0), 0)
  totalCompanies.value = new Set(stats.map(item => item.company)).size
  
  const totalAge = stats.reduce((sum, item) => sum + (item.avgAge || 0) * (item.totalCount || 0), 0)
  avgAge.value = totalDevices.value > 0 ? (totalAge / totalDevices.value).toFixed(1) : 0
  
  oldDevicesCount.value = stats.reduce((sum, item) => sum + (item.age6to7 || 0) + (item.age7to8 || 0) + (item.age8plus || 0), 0)
}

// 渲染图表
const renderCharts = () => {
  renderCompanyChart()
  renderAgeChart()
  renderDeviceTypeChart()
}

// 渲染公司分布图
const renderCompanyChart = () => {
  if (!companyChartRef.value) return
  
  if (companyChart) {
    companyChart.dispose()
  }
  
  companyChart = echarts.init(companyChartRef.value)
  
  const stats = reportData.value.companyDeviceStats || []
  const companyData = {}
  
  stats.forEach(item => {
    if (!companyData[item.company]) {
      companyData[item.company] = 0
    }
    companyData[item.company] += item.totalCount || 0
  })
  
  const option = {
    title: {
      text: '各公司设备数量分布',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    series: [{
      name: '设备数量',
      type: 'pie',
      radius: '60%',
      center: ['50%', '55%'],
      data: Object.entries(companyData).map(([name, value]) => ({ name, value })),
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  
  companyChart.setOption(option)
}

// 渲染年限分布图
const renderAgeChart = () => {
  if (!ageChartRef.value) return
  
  if (ageChart) {
    ageChart.dispose()
  }
  
  ageChart = echarts.init(ageChartRef.value)
  
  const ageStats = reportData.value.ageRangeStats || []
  
  const option = {
    title: {
      text: '年限分布统计',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    xAxis: {
      type: 'category',
      data: ageStats.map(item => item.ageRange)
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '设备数量',
      type: 'bar',
      data: ageStats.map(item => item.total || 0),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#83bff6' },
          { offset: 0.5, color: '#188df0' },
          { offset: 1, color: '#188df0' }
        ])
      }
    }]
  }
  
  ageChart.setOption(option)
}

// 渲染设备类型分布图
const renderDeviceTypeChart = () => {
  if (!deviceTypeChartRef.value) return
  
  if (deviceTypeChart) {
    deviceTypeChart.dispose()
  }
  
  deviceTypeChart = echarts.init(deviceTypeChartRef.value)
  
  const ageStats = reportData.value.ageRangeStats || []
  
  const option = {
    title: {
      text: '设备类型年限分布',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['Standard Notebook', 'Performance Notebook', 'Standard Desktop', 'Performance Desktop'],
      top: '10%'
    },
    xAxis: {
      type: 'category',
      data: ageStats.map(item => item.ageRange)
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: 'Standard Notebook',
        type: 'bar',
        stack: 'total',
        data: ageStats.map(item => item.standardNotebook || 0),
        itemStyle: { color: '#5470c6' }
      },
      {
        name: 'Performance Notebook',
        type: 'bar',
        stack: 'total',
        data: ageStats.map(item => item.performanceNotebook || 0),
        itemStyle: { color: '#91cc75' }
      },
      {
        name: 'Standard Desktop',
        type: 'bar',
        stack: 'total',
        data: ageStats.map(item => item.standardDesktop || 0),
        itemStyle: { color: '#fac858' }
      },
      {
        name: 'Performance Desktop',
        type: 'bar',
        stack: 'total',
        data: ageStats.map(item => item.performanceDesktop || 0),
        itemStyle: { color: '#ee6666' }
      }
    ]
  }
  
  deviceTypeChart.setOption(option)
}

// 重置筛选条件
const resetFilter = () => {
  filterForm.company = ''
  filterForm.deviceClass = ''
  filterForm.ageRange = ''
  loadReportData()
}

// 导出数据
const exportData = () => {
  // 这里可以实现数据导出功能
  ElMessage.info('导出功能待实现')
}

// 监听窗口大小变化
const handleResize = () => {
  if (companyChart) companyChart.resize()
  if (ageChart) ageChart.resize()
  if (deviceTypeChart) deviceTypeChart.resize()
}

// 组件挂载
onMounted(async () => {
  await loadBasicData()
  await loadReportData()
  
  window.addEventListener('resize', handleResize)
})

// 组件卸载时清理
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (companyChart) companyChart.dispose()
  if (ageChart) ageChart.dispose()
  if (deviceTypeChart) deviceTypeChart.dispose()
})
</script>

<style scoped>
.computer-report-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h2 {
  color: #303133;
  margin-bottom: 10px;
}

.page-header p {
  color: #606266;
  font-size: 14px;
}

.filter-section {
  margin-bottom: 20px;
}

.filter-card {
  padding: 10px;
}

.stats-section {
  margin-bottom: 20px;
}

.stats-card {
  text-align: center;
  height: 100px;
}

.stats-item {
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 100%;
}

.stats-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.stats-label {
  font-size: 14px;
  color: #606266;
}

.charts-section {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 320px;
  width: 100%;
}

.chart-container-large {
  height: 350px;
  width: 100%;
}

.table-section {
  margin-top: 20px;
}

.table-card {
  min-height: 300px;
}

:deep(.el-card__body) {
  padding: 15px;
}

:deep(.el-table) {
  font-size: 13px;
}
</style>