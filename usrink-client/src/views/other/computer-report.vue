<template>
  <div class="computer-report-container">
    

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
          
          <el-form-item label="pcclass:">
            <el-select v-model="filterForm.employeeType" placeholder="请选择pcclass" style="width: 160px">
              <el-option v-for="opt in employeeTypes" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </el-form-item>

          <el-form-item label="SN单价:">
            <el-input-number v-model="filterForm.unitPriceSN" :min="0" :step="100" :precision="0" style="width: 140px" />
          </el-form-item>
          <el-form-item label="PN单价:">
            <el-input-number v-model="filterForm.unitPricePN" :min="0" :step="100" :precision="0" style="width: 140px" />
          </el-form-item>
          <el-form-item label="SD单价:">
            <el-input-number v-model="filterForm.unitPriceSD" :min="0" :step="100" :precision="0" style="width: 140px" />
          </el-form-item>
          <el-form-item label="PD单价:">
            <el-input-number v-model="filterForm.unitPricePD" :min="0" :step="100" :precision="0" style="width: 140px" />
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
              <div class="stats-value">{{ multiDeviceUserCount }}</div>
              <div class="stats-label">一人多台用户数</div>
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
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-value">{{ formatCurrency(replacementCost) }}</div>
              <div class="stats-label">预计换新实时成本</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表展示区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <!-- 各季度超年限电脑数量图 -->
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>各季度超年限电脑数量</span>
              </div>
            </template>
            <div ref="companyChartRef" class="chart-container"></div>
          </el-card>
        </el-col>

        <!-- 按年份在用数量（四类设备） -->
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>按年份在用数量（四类设备）</span>
                <el-button type="primary" size="small" @click="exportYearlyData">导出</el-button>
              </div>
            </template>
            <div ref="yearlyChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 设备类型年限分布 + 详细统计数据 同行展示，仅受统计日期影响 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>设备类型年限分布</span>
              </div>
            </template>
            <div ref="deviceTypeChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="table-card">
            <template #header>
              <div class="card-header">
                <span>详细统计数据</span>
                <div>
                  <el-date-picker
                    v-model="statDate"
                    type="date"
                    value-format="YYYY-MM-DD"
                    placeholder="统计日期"
                    style="margin-right: 8px"
                    @change="handleStatDateChange"
                  />
                  <el-button type="primary" size="small" @click="exportData">导出数据</el-button>
                </div>
              </div>
            </template>
            <el-table :data="reportData.companyDeviceStats" stripe style="width: 100%" :max-height="tableMaxHeight">
              <el-table-column prop="company" label="公司" width="80" />
              <el-table-column prop="deviceClass" label="设备类型" width="150" />
              <el-table-column prop="totalCount" label="总数量" width="70" align="center" />
              <el-table-column prop="age0to1" label="0-1年" width="60" align="center" />
              <el-table-column prop="age1to2" label="1-2年" width="60" align="center" />
              <el-table-column prop="age2to3" label="2-3年" width="60" align="center" />
              <el-table-column prop="age3to4" label="3-4年" width="60" align="center" />
              <el-table-column prop="age4to5" label="4-5年" width="60" align="center" />
              <el-table-column prop="age5to6" label="5-6年" width="60" align="center" />
              <el-table-column prop="age6to7" label="6-7年" width="60" align="center" />
              <el-table-column prop="age7to8" label="7-8年" width="60" align="center" />
              <el-table-column prop="age8plus" label="8年+" width="60" align="center" />
              <el-table-column prop="avgAge" label="平均年限" width="90" align="center">
                <template #default="scope">
                  {{ scope.row.avgAge ? scope.row.avgAge.toFixed(1) + '年' : '-' }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>

      <!-- 各公司四类电脑在用数量按年份统计：已移至与季度图同一行显示 -->
    </div>

    
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import httpUtil from '@/utils/HttpUtil'
import * as XLSX from 'xlsx'

// 响应式数据
const filterForm = reactive({
  company: '',
  employeeType: 'Internal User',
  unitPriceSN: 0,
  unitPricePN: 0,
  unitPriceSD: 0,
  unitPricePD: 0
})

const reportData = ref({
  companyDeviceStats: [],
  ageRangeStats: [],
  quarterScrapStats: [],
  yearlyInUseStats: []
})

const companies = ref([])
const statDate = ref('')
const tableMaxHeight = 320
const employeeTypes = ref([
  { label: 'Internal User', value: 'Internal User' },
  { label: 'Other', value: 'Other' },
  { label: 'All', value: 'All' }
])

// 统计数据
const totalDevices = ref(0)
const oldDevicesCount = ref(0)
const multiDeviceUserCount = ref(0)

// 图表引用
const companyChartRef = ref(null)
// const ageChartRef = ref(null)
const deviceTypeChartRef = ref(null)
const yearlyChartRef = ref(null)

// 图表实例
let companyChart = null
let ageChart = null
let deviceTypeChart = null
let yearlyChart = null
const replacementCost = ref(0)

// 加载基础数据
const loadBasicData = async () => {
  try {
    // 并行加载基础数据
  const [companiesRes] = await Promise.all([
      httpUtil.get('/computerReport/getCompanies')
    ])
  
  companies.value = companiesRes.data || []
    
  } catch (error) {
    console.error('加载基础数据失败:', error)
    ElMessage.error('加载基础数据失败')
  }
}

// 加载报表数据
const loadReportData = async () => {
  try {
    const payload = { ...filterForm, statDate: statDate.value || undefined }
    const response = await httpUtil.post('/computerReport/getReport', payload, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    if (response.code === 200) {
      reportData.value = response.data || { companyDeviceStats: [], ageRangeStats: [], quarterScrapStats: [] }
      multiDeviceUserCount.value = Number((response.data && response.data.multiDeviceUserCount) || 0)
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
  
  oldDevicesCount.value = stats.reduce((sum, item) => sum + (item.age6to7 || 0) + (item.age7to8 || 0) + (item.age8plus || 0), 0)

  // 预计换新成本：按“6年以上设备数量 * 各类型单价”估算
  const priceByClass = {
    'Standard Notebook': Number(filterForm.unitPriceSN) || 0,
    'Performance Notebook': Number(filterForm.unitPricePN) || 0,
    'Standard Desktop': Number(filterForm.unitPriceSD) || 0,
    'Performance Desktop': Number(filterForm.unitPricePD) || 0,
  }
  let total = 0
  stats.forEach(item => {
    const over6 = (item.age6to7 || 0) + (item.age7to8 || 0) + (item.age8plus || 0)
    const p = priceByClass[item.deviceClass] || 0
    total += over6 * p
  })
  replacementCost.value = total
}

// 统计日期变更：仅影响“设备类型年限分布”和“详细统计数据”的计算来源
const handleStatDateChange = async () => {
  await loadReportData()
}

// 渲染图表
const renderCharts = () => {
  renderCompanyChart()
  // renderAgeChart()
  renderDeviceTypeChart()
  renderYearlyChart()
}

// 渲染各季度待报废数量图
const renderCompanyChart = () => {
  if (!companyChartRef.value) return
  
  if (companyChart) {
    companyChart.dispose()
  }
  
  companyChart = echarts.init(companyChartRef.value)

  const quarterStats = reportData.value.quarterScrapStats || []
  const option = {
    title: {
      text: '各季度超年限电脑数量',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'line' }
    },
    xAxis: {
      type: 'category',
      data: quarterStats.map(item => item.quarter)
    },
    yAxis: { type: 'value' },
    series: [{
      name: '超年限数量',
      type: 'line',
      smooth: false,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { color: '#3ba272', width: 2 },
      itemStyle: { color: '#3ba272' },
      areaStyle: { opacity: 0 },
      data: quarterStats.map(item => item.total || 0)
    }]
  }

  companyChart.setOption(option)
}

// 已移除年限分布图

// 渲染设备类型分布图
const renderDeviceTypeChart = () => {
  if (!deviceTypeChartRef.value) return
  
  if (deviceTypeChart) {
    deviceTypeChart.dispose()
  }
  
  deviceTypeChart = echarts.init(deviceTypeChartRef.value)
  
  const ageStats = reportData.value.ageRangeStats || []
  const totals = ageStats.map(item => Number(item.total || 0))
  
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
        name: '总计标签',
        type: 'bar',
        data: totals,
        barGap: '-100%',
        itemStyle: { color: 'transparent' },
        label: {
          show: true,
          position: 'top',
          color: '#333',
          formatter: (p) => String(totals[p.dataIndex] ?? 0)
        },
        z: 10,
        silent: true
      },
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

// 渲染按年份在用数量图（四类设备）
const renderYearlyChart = () => {
  if (!yearlyChartRef.value) return

  if (yearlyChart) {
    yearlyChart.dispose()
  }

  yearlyChart = echarts.init(yearlyChartRef.value)

  const raw = reportData.value.yearlyInUseStats || []
  // 若选择了公司，则只看该公司；否则聚合所有公司
  const company = filterForm.company
  let rows = raw
  if (company) {
    rows = raw.filter(r => r.company === company)
  }

  // 将数据按年份聚合
  const yearMap = new Map()
  rows.forEach(r => {
    const y = r.year
    const prev = yearMap.get(y) || { 
      year: y,
      standardNotebook: 0,
      performanceNotebook: 0,
      standardDesktop: 0,
      performanceDesktop: 0,
      total: 0
    }
    prev.standardNotebook += Number(r.standardNotebook || 0)
    prev.performanceNotebook += Number(r.performanceNotebook || 0)
    prev.standardDesktop += Number(r.standardDesktop || 0)
    prev.performanceDesktop += Number(r.performanceDesktop || 0)
    prev.total += Number(r.total || 0)
    yearMap.set(y, prev)
  })

  const years = Array.from(yearMap.keys()).sort((a,b)=>a-b)
  const dataByYear = years.map(y => yearMap.get(y))
  const totals = dataByYear.map(d => (d ? Number(d.total || 0) : 0))

  const option = {
    title: {
      text: company ? `按年份在用数量（${company}）` : '按年份在用数量（全部公司）',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: {
      data: ['Standard Notebook','Performance Notebook','Standard Desktop','Performance Desktop'],
      top: '10%'
    },
    xAxis: { type: 'category', data: years },
    yAxis: { type: 'value' },
    series: [
      {
        name: '总计标签',
        type: 'bar',
        data: totals,
        barGap: '-100%',
        itemStyle: { color: 'transparent' },
        label: { show: true, position: 'top', color: '#333', formatter: (p)=> String(totals[p.dataIndex] ?? 0) },
        z: 10,
        silent: true
      },
      { name: 'Standard Notebook', type: 'bar', stack: 'total', itemStyle: { color: '#5470c6' }, data: dataByYear.map(d=>d.standardNotebook) },
      { name: 'Performance Notebook', type: 'bar', stack: 'total', itemStyle: { color: '#91cc75' }, data: dataByYear.map(d=>d.performanceNotebook) },
      { name: 'Standard Desktop', type: 'bar', stack: 'total', itemStyle: { color: '#fac858' }, data: dataByYear.map(d=>d.standardDesktop) },
      { name: 'Performance Desktop', type: 'bar', stack: 'total', itemStyle: { color: '#ee6666' }, data: dataByYear.map(d=>d.performanceDesktop) }
    ]
  }

  yearlyChart.setOption(option)
}

// 重置筛选条件
const resetFilter = () => {
  filterForm.company = ''
  filterForm.employeeType = 'Internal User'
  filterForm.unitPriceSN = 0
  filterForm.unitPricePN = 0
  filterForm.unitPriceSD = 0
  filterForm.unitPricePD = 0
  loadReportData()
}

// 导出数据（xlsx）
const exportData = () => {
  const rows = reportData.value.companyDeviceStats || []
  const headers = [
    '公司','设备类型','总数量','0-1年','1-2年','2-3年','3-4年','4-5年','5-6年','6-7年','7-8年','8年+','平均年限','预计换新实时成本(元)'
  ]
  const priceByClass = {
    'Standard Notebook': Number(filterForm.unitPriceSN) || 0,
    'Performance Notebook': Number(filterForm.unitPricePN) || 0,
    'Standard Desktop': Number(filterForm.unitPriceSD) || 0,
    'Performance Desktop': Number(filterForm.unitPricePD) || 0,
  }
  const aoa = [headers]
  rows.forEach(r => {
    const over6 = (r.age6to7||0) + (r.age7to8||0) + (r.age8plus||0)
    const rowPrice = priceByClass[r.deviceClass] || 0
    aoa.push([
      r.company ?? '',
      r.deviceClass ?? '',
      r.totalCount||0,
      r.age0to1||0,
      r.age1to2||0,
      r.age2to3||0,
      r.age3to4||0,
      r.age4to5||0,
      r.age5to6||0,
      r.age6to7||0,
      r.age7to8||0,
      r.age8plus||0,
      r.avgAge != null ? Number(r.avgAge).toFixed(1) : '-',
      over6 * rowPrice
    ])
  })
  const ws = XLSX.utils.aoa_to_sheet(aoa)
  ws['!cols'] = [
    { wch: 14 }, // 公司
    { wch: 18 }, // 设备类型
    { wch: 10 }, // 总数量
    { wch: 8 },{ wch: 8 },{ wch: 8 },{ wch: 8 },{ wch: 8 },{ wch: 8 },{ wch: 8 },{ wch: 8 },{ wch: 8 }, // 年限列
    { wch: 10 }, // 平均年限
    { wch: 16 }  // 预计换新成本
  ]
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '详细统计')

  // Sheet2: 按年份统计
  const yearlyRows = reportData.value.yearlyInUseStats || []
  const yearlyHeaders = ['公司','年份','Standard Notebook','Performance Notebook','Standard Desktop','Performance Desktop','合计']
  const yearlyAoa = [yearlyHeaders]
  yearlyRows.forEach(r => {
    yearlyAoa.push([
      r.company || '',
      r.year || '',
      r.standardNotebook || 0,
      r.performanceNotebook || 0,
      r.standardDesktop || 0,
      r.performanceDesktop || 0,
      r.total || 0
    ])
  })
  const ws2 = XLSX.utils.aoa_to_sheet(yearlyAoa)
  ws2['!cols'] = [
    { wch: 14 }, { wch: 8 }, { wch: 20 }, { wch: 22 }, { wch: 18 }, { wch: 20 }, { wch: 10 }
  ]
  XLSX.utils.book_append_sheet(wb, ws2, '按年份统计')
  const fileName = `电脑报表_${new Date().toISOString().slice(0,10)}.xlsx`
  XLSX.writeFile(wb, fileName)
}

// 导出：按年份在用数量（四类设备）单独导出
const exportYearlyData = () => {
  const company = filterForm.company || '全部公司'
  const rows = reportData.value.yearlyInUseStats || []
  const headers = ['公司','年份','Standard Notebook','Performance Notebook','Standard Desktop','Performance Desktop','合计']
  const aoa = [headers]
  rows
    .filter(r => !filterForm.company || r.company === filterForm.company)
    .sort((a,b) => a.company.localeCompare(b.company) || a.year - b.year)
    .forEach(r => {
      aoa.push([
        r.company || '',
        r.year || '',
        r.standardNotebook || 0,
        r.performanceNotebook || 0,
        r.standardDesktop || 0,
        r.performanceDesktop || 0,
        r.total || 0
      ])
    })
  const ws = XLSX.utils.aoa_to_sheet(aoa)
  ws['!cols'] = [
    { wch: 14 }, { wch: 8 }, { wch: 20 }, { wch: 22 }, { wch: 18 }, { wch: 20 }, { wch: 10 }
  ]
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '按年份统计')
  const fileName = `按年份在用数量_${company}_${new Date().toISOString().slice(0,10)}.xlsx`
  XLSX.writeFile(wb, fileName)
}

const formatCurrency = (num) => {
  const n = Number(num)||0
  return n.toLocaleString('zh-CN', { style: 'currency', currency: 'CNY', maximumFractionDigits: 0 })
}

// 监听窗口大小变化
const handleResize = () => {
  if (companyChart) companyChart.resize()
  // if (ageChart) ageChart.resize()
  if (deviceTypeChart) deviceTypeChart.resize()
  if (yearlyChart) yearlyChart.resize()
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
  min-height: 400px;
  display: flex;
  flex-direction: column;
}

:deep(.el-card__body) {
  padding: 15px;
  display: flex;
  flex: 1;
  overflow: hidden;
}

:deep(.el-table) {
  font-size: 13px;
}
</style>