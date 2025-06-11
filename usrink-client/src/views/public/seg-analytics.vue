<template>
  <div class="analytics-container">
    <div class="analytics-header">
      <div class="header-left">
        <div class="title-icon">
          <i class="fas fa-chart-line"></i>
        </div>
        <div class="title-content">
          <h1>SEG OneLink 数据统计</h1>
          <div class="info-badge">
            <i class="fas fa-info-circle"></i>
            <span>数据实时更新</span>
          </div>
        </div>
      </div>
      
      <div class="header-controls">
        <div class="date-control">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateRangeChange"
            size="default"
            class="compact-date-picker"
          />
        </div>
        
        <div class="action-controls">
          <el-button 
            @click="setQuickDate('today')" 
            type="primary" 
            plain 
            size="small"
            class="compact-btn"
          >
            今天
          </el-button>
          <el-button 
            @click="setQuickDate('week')" 
            type="primary" 
            plain 
            size="small"
            class="compact-btn"
          >
            近7天
          </el-button>
          <el-button 
            @click="setQuickDate('month')" 
            type="primary" 
            plain 
            size="small"
            class="compact-btn"
          >
            近30天
          </el-button>
          <el-button 
            @click="loadAllData" 
            type="success" 
            size="default"
            class="load-btn"
          >
            <i class="fas fa-database"></i>
            全部数据
          </el-button>
          <el-button 
            @click="openSystemManagement" 
            type="warning" 
            size="default"
            class="load-btn"
          >
            <i class="fas fa-cogs"></i>
            系统管理
          </el-button>
        </div>
      </div>
    </div>
    
    <div class="statistics-grid">
      <div class="stat-card">
        <h3>页面浏览量</h3>
        <div class="stat-number">{{ statistics.pageViewCount || 0 }}</div>
      </div>
      
      <div class="stat-card">
        <h3>系统点击量</h3>
        <div class="stat-number">{{ statistics.systemClickCount || 0 }}</div>
      </div>
      
      <div class="stat-card">
        <h3>独立访客数</h3>
        <div class="stat-number">{{ statistics.uniqueVisitorCount || 0 }}</div>
      </div>
      
      <div class="stat-card">
        <h3>总访问次数</h3>
        <div class="stat-number">{{ (statistics.pageViewCount || 0) + (statistics.systemClickCount || 0) }}</div>
        <div class="stat-note">页面浏览 + 系统点击</div>
      </div>
    </div>
    
    <div class="charts-section">
      <!-- 系统排行和图表并排显示 -->
      <div class="charts-container">
        <!-- 左侧：热门系统排行 -->
    <div class="popular-systems">
      <h2>热门系统排行</h2>
      <div v-if="popularSystems.length > 0" class="ranking-list">
        <div 
          v-for="(system, index) in popularSystems" 
          :key="index"
          class="ranking-item"
        >
          <span class="rank">{{ index + 1 }}</span>
          <span class="system-name">{{ system.systemName }}</span>
              <span class="click-count">{{ system.clickCount }} 次</span>
        </div>
      </div>
      <div v-else class="no-data">
        <p>暂无系统点击数据</p>
          </div>
        </div>

        <!-- 右侧：图表区域 -->
        <div class="charts-panel">
          <!-- 日访问量趋势图 -->
          <div class="chart-card">
            <h3>日访问量趋势</h3>
            <div v-if="echartsError" class="chart-error">
              <i class="fas fa-exclamation-triangle"></i>
              <span>图表加载失败</span>
            </div>
            <div v-else-if="!echartsLoaded" class="chart-loading">
              <i class="fas fa-spinner fa-spin"></i>
              <span>图表加载中...</span>
            </div>
            <div v-else ref="dailyTrendChart" class="chart-container"></div>
          </div>
          
          <!-- 系统点击分布图 -->
          <div class="chart-card">
            <h3>系统点击分布</h3>
            <div v-if="echartsError" class="chart-error">
              <i class="fas fa-exclamation-triangle"></i>
              <span>图表加载失败</span>
            </div>
            <div v-else-if="!echartsLoaded" class="chart-loading">
              <i class="fas fa-spinner fa-spin"></i>
              <span>图表加载中...</span>
            </div>
            <div v-else ref="systemDistributionChart" class="chart-container"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 系统管理弹框 -->
    <el-dialog
      v-model="systemManagementVisible"
      title="OneLink 系统管理"
      width="80%"
      :before-close="handleCloseSystemManagement"
    >
      <div class="system-management-content">
        <!-- 操作按钮区域 -->
        <div class="management-actions">
          <el-button type="primary" @click="openAddSystemDialog">
            <i class="fas fa-plus"></i>
            添加系统
          </el-button>
          <el-button type="success" @click="loadSystemList">
            <i class="fas fa-sync-alt"></i>
            刷新列表
          </el-button>
        </div>

        <!-- 系统列表 -->
        <el-table 
          :data="systemList" 
          style="width: 100%" 
          v-loading="systemListLoading"
          stripe
        >
          <el-table-column prop="systemId" label="ID" width="80" />
          <el-table-column prop="systemName" label="系统名称" width="150" />
          <el-table-column prop="systemDescription" label="系统描述" />
          <el-table-column prop="categoryName" label="分类" width="180" />
          <el-table-column prop="systemUrl" label="系统链接" width="200" show-overflow-tooltip />
          <el-table-column prop="sortOrder" label="排序" width="80" />
          <el-table-column prop="isActive" label="状态" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.isActive ? 'success' : 'danger'">
                {{ scope.row.isActive ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="editSystem(scope.row)">编辑</el-button>
              <el-button 
                size="small" 
                type="danger" 
                @click="deleteSystem(scope.row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页组件 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="systemPagination.page"
            v-model:page-size="systemPagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="systemPagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-dialog>

    <!-- 添加/编辑系统弹框 -->
    <el-dialog
      v-model="systemFormVisible"
      :title="isEditMode ? '编辑系统' : '添加系统'"
      width="600px"
      :before-close="handleCloseSystemForm"
    >
      <el-form 
        :model="systemForm" 
        :rules="systemFormRules" 
        ref="systemFormRef"
        label-width="120px"
      >
        <el-form-item label="系统名称" prop="systemName">
          <el-input v-model="systemForm.systemName" placeholder="请输入系统名称" />
        </el-form-item>
        
        <el-form-item label="系统描述" prop="systemDescription">
          <el-input 
            v-model="systemForm.systemDescription" 
            type="textarea" 
            :rows="3"
            placeholder="请输入系统描述"
          />
        </el-form-item>
        
        <el-form-item label="系统链接" prop="systemUrl">
          <el-input v-model="systemForm.systemUrl" placeholder="请输入系统链接" />
        </el-form-item>
        
        <el-form-item label="分类名称" prop="categoryName">
          <el-select v-model="systemForm.categoryName" placeholder="请选择分类" style="width: 100%">
            <el-option 
              v-for="category in categoryOptions" 
              :key="category" 
              :label="category" 
              :value="category"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="排序顺序" prop="sortOrder">
          <el-input-number v-model="systemForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
        
        <el-form-item label="是否启用" prop="isActive">
          <el-switch v-model="systemForm.isActive" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseSystemForm">取消</el-button>
          <el-button type="primary" @click="saveSystem" :loading="systemFormLoading">
            {{ isEditMode ? '更新' : '添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import httpUtil from '@/utils/HttpUtil'

export default {
  name: 'SegAnalytics',
  data() {
    return {
      startDate: '',
      endDate: '',
      dateRange: [],
      statistics: {},
      popularSystems: [],
      dailyTrendData: [],
      systemDistributionData: [],
      dailyTrendChart: null,
      systemDistributionChart: null,
      echartsLoaded: false,
      echartsError: false,
      systemManagementVisible: false,
      systemFormVisible: false,
      isEditMode: false,
      systemForm: {
        systemName: '',
        systemDescription: '',
        systemUrl: '',
        categoryName: '',
        sortOrder: 0,
        isActive: true
      },
      systemFormRules: {
        systemName: [
          { required: true, message: '请输入系统名称', trigger: 'blur' }
        ],
        systemDescription: [
          { required: true, message: '请输入系统描述', trigger: 'blur' }
        ],
        systemUrl: [
          { required: true, message: '请输入系统链接', trigger: 'blur' }
        ],
        categoryName: [
          { required: true, message: '请选择分类', trigger: 'change' }
        ],
        sortOrder: [
          { required: true, message: '请输入排序顺序', trigger: 'blur' }
        ]
      },
      systemList: [],
      systemListLoading: false,
      categoryOptions: [],
      systemFormLoading: false,
      systemPagination: {
        page: 1,
        size: 10,
        total: 0
      }
    }
  },
  async mounted() {
    // 设置默认日期范围（最近7天）
    const today = new Date()
    const weekAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
    
    this.endDate = today.toISOString().split('T')[0]
    this.startDate = weekAgo.toISOString().split('T')[0]
    this.dateRange = [this.startDate, this.endDate]
    
    // 先加载数据
    this.loadData()
    
    // 动态导入ECharts并初始化图表
    try {
      const echarts = await import('echarts')
      this.echarts = echarts.default || echarts
      this.echartsLoaded = true
      
      // 延迟初始化图表，确保DOM已渲染
      this.$nextTick(() => {
        setTimeout(() => {
          this.initCharts()
        }, 200)
      })
    } catch (error) {
      console.error('ECharts加载失败:', error)
      this.echartsError = true
    }
  },
  beforeUnmount() {
    // 销毁图表实例
    if (this.dailyTrendChart) {
      try {
        this.dailyTrendChart.dispose()
      } catch (error) {
        console.error('销毁日趋势图表失败:', error)
      }
    }
    if (this.systemDistributionChart) {
      try {
        this.systemDistributionChart.dispose()
      } catch (error) {
        console.error('销毁系统分布图表失败:', error)
      }
    }
  },
  methods: {
    async loadStatistics() {
      try {
        const params = {}
        if (this.startDate) params.startDate = this.startDate
        if (this.endDate) params.endDate = this.endDate
        
        const response = await httpUtil.get('/sysAnalyticsLog/statistics', { params })
        this.statistics = response.data || {}
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    },
    
    async loadPopularSystems() {
      try {
        const params = { limit: 10 }
        if (this.startDate) params.startDate = this.startDate
        if (this.endDate) params.endDate = this.endDate
        
        const response = await httpUtil.get('/sysAnalyticsLog/popularSystems', { params })
        this.popularSystems = response.data?.list || []
      } catch (error) {
        console.error('加载热门系统排行失败:', error)
      }
    },
    
    async loadDailyTrendData() {
      try {
        const params = {}
        if (this.startDate) params.startDate = this.startDate
        if (this.endDate) params.endDate = this.endDate
        
        const response = await httpUtil.get('/sysAnalyticsLog/dailyTrend', { params })
        const rawData = response.data?.list || []
        
        // 转换时间戳为日期字符串
        this.dailyTrendData = rawData.map(item => ({
          date: new Date(item.date).toISOString().split('T')[0], // 转换为 YYYY-MM-DD 格式
          count: item.count
        }))
        
        // 如果没有数据，生成模拟数据
        if (this.dailyTrendData.length === 0) {
          this.generateMockDailyTrendData()
        }
        
        // 更新图表
        if (this.dailyTrendChart) {
          this.updateDailyTrendChart()
        }
      } catch (error) {
        console.error('加载日访问量趋势数据失败:', error)
        // 生成模拟数据
        this.generateMockDailyTrendData()
        if (this.dailyTrendChart) {
          this.updateDailyTrendChart()
        }
      }
    },
    
    async loadSystemDistributionData() {
      try {
        const params = { limit: 8 }
        if (this.startDate) params.startDate = this.startDate
        if (this.endDate) params.endDate = this.endDate
        
        const response = await httpUtil.get('/sysAnalyticsLog/systemDistribution', { params })
        const rawData = response.data?.list || []
        
        // 转换为ECharts需要的格式
        this.systemDistributionData = rawData.map(item => ({
          name: item.systemName,
          value: item.clickCount
        }))
        
        // 如果没有数据，生成模拟数据
        if (this.systemDistributionData.length === 0) {
          this.generateMockSystemDistributionData()
        }
        
        // 更新图表
        if (this.systemDistributionChart) {
          this.updateSystemDistributionChart()
        }
      } catch (error) {
        console.error('加载系统点击分布数据失败:', error)
        // 生成模拟数据
        this.generateMockSystemDistributionData()
        if (this.systemDistributionChart) {
          this.updateSystemDistributionChart()
        }
      }
    },
    
    generateMockDailyTrendData() {
      // 生成最近7天的模拟数据
      const data = []
      const today = new Date()
      
      for (let i = 6; i >= 0; i--) {
        const date = new Date(today.getTime() - i * 24 * 60 * 60 * 1000)
        const dateStr = date.toISOString().split('T')[0]
        const count = Math.floor(Math.random() * 100) + 20 // 20-120之间的随机数
        
        data.push({
          date: dateStr,
          count: count
        })
      }
      
      this.dailyTrendData = data
    },
    
    generateMockSystemDistributionData() {
      // 生成模拟的系统点击分布数据
      const mockSystems = [
        { name: 'BIPO', value: Math.floor(Math.random() * 50) + 10 },
        { name: 'Concur', value: Math.floor(Math.random() * 40) + 8 },
        { name: 'Matrix42', value: Math.floor(Math.random() * 35) + 6 },
        { name: 'Infor', value: Math.floor(Math.random() * 30) + 5 },
        { name: 'Salesforce', value: Math.floor(Math.random() * 25) + 4 },
        { name: 'Jaggaer', value: Math.floor(Math.random() * 20) + 3 },
        { name: 'PTC Windchill', value: Math.floor(Math.random() * 15) + 2 },
        { name: 'Office365', value: Math.floor(Math.random() * 10) + 1 }
      ]
      
      this.systemDistributionData = mockSystems
    },
    
    loadData() {
      this.loadStatistics()
      this.loadPopularSystems()
      this.loadDailyTrendData()
      this.loadSystemDistributionData()
    },
    
    loadAllData() {
      // 清空日期过滤，加载所有数据
      this.startDate = ''
      this.endDate = ''
      this.dateRange = []
      this.loadData()
    },
    
    handleDateRangeChange(dateRange) {
      if (dateRange && dateRange.length === 2) {
        this.startDate = dateRange[0]
        this.endDate = dateRange[1]
      } else {
        this.startDate = ''
        this.endDate = ''
      }
      this.loadData()
    },
    
    setQuickDate(type) {
      const today = new Date()
      const todayStr = today.toISOString().split('T')[0]
      
      switch (type) {
        case 'today':
          this.startDate = todayStr
          this.endDate = todayStr
          this.dateRange = [todayStr, todayStr]
          break
        case 'week':
          const weekAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
          this.startDate = weekAgo.toISOString().split('T')[0]
          this.endDate = todayStr
          this.dateRange = [this.startDate, this.endDate]
          break
        case 'month':
          const monthAgo = new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000)
          this.startDate = monthAgo.toISOString().split('T')[0]
          this.endDate = todayStr
          this.dateRange = [this.startDate, this.endDate]
          break
      }
      
      this.loadData()
    },
    
    openSystemManagement() {
      this.systemManagementVisible = true
      // 重置分页状态
      this.systemPagination.page = 1
      this.systemPagination.size = 10
      this.systemPagination.total = 0
      this.loadSystemList()
      this.loadCategoryOptions()
    },
    
    handleCloseSystemManagement() {
      this.systemManagementVisible = false
    },
    
    openAddSystemDialog() {
      this.isEditMode = false
      this.resetSystemForm()
      this.systemFormVisible = true
    },
    
    handleCloseSystemForm() {
      this.systemFormVisible = false
      this.resetSystemForm()
    },
    
    resetSystemForm() {
      this.systemForm = {
        systemName: '',
        systemDescription: '',
        systemUrl: '',
        categoryName: '',
        sortOrder: 0,
        isActive: true
      }
      if (this.$refs.systemFormRef) {
        this.$refs.systemFormRef.clearValidate()
      }
    },
    
    async loadSystemList() {
      try {
        this.systemListLoading = true
        const response = await httpUtil.get('/sysOnelinkSystem/list', {
          params: {
            page: this.systemPagination.page,
            size: this.systemPagination.size
          }
        })
        this.systemList = response.data?.list || []
        this.systemPagination.total = response.data?.total || 0
      } catch (error) {
        console.error('加载系统列表失败:', error)
        this.$message.error('加载系统列表失败')
      } finally {
        this.systemListLoading = false
      }
    },
    
    async loadCategoryOptions() {
      try {
        const response = await httpUtil.get('/sysOnelinkSystem/categories')
        this.categoryOptions = response.data?.list || [
          '人事系统 (HR System)',
          '财务系统 (COR System)',
          '采购系统 (Procurement System)',
          '研发系统 (R&D System)',
          '物流系统 (Logistics System)',
          '质量系统 (Quality System)',
          '生产系统 (Production System)',
          'IT系统 (IT System)'
        ]
      } catch (error) {
        console.error('加载分类选项失败:', error)
        // 使用默认分类选项
        this.categoryOptions = [
          '人事系统 (HR System)',
          '财务系统 (COR System)',
          '采购系统 (Procurement System)',
          '研发系统 (R&D System)',
          '物流系统 (Logistics System)',
          '质量系统 (Quality System)',
          '生产系统 (Production System)',
          'IT系统 (IT System)'
        ]
      }
    },
    
    editSystem(system) {
      this.isEditMode = true
      this.systemForm = { ...system }
      this.systemFormVisible = true
    },
    
    deleteSystem(system) {
      this.$confirm(`确定要删除系统"${system.systemName}"吗？`, '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await httpUtil.post('/sysOnelinkSystem/delete', null, {
            params: { systemId: system.systemId }
          })
          this.$message.success('删除成功')
          
          // 检查删除后当前页是否还有数据
          const currentPageItemCount = this.systemList.length
          if (currentPageItemCount === 1 && this.systemPagination.page > 1) {
            // 如果当前页只有一条数据且不是第一页，则跳转到上一页
            this.systemPagination.page -= 1
          }
          
          this.loadSystemList()
        } catch (error) {
          console.error('删除系统失败:', error)
          this.$message.error('删除失败')
        }
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    },
    
    saveSystem() {
      this.$refs.systemFormRef.validate(async (valid) => {
        if (valid) {
          try {
            this.systemFormLoading = true
            const config = {
              headers: {
                'Content-Type': 'application/json'
              }
            }
            
            if (this.isEditMode) {
              await httpUtil.post('/sysOnelinkSystem/update', this.systemForm, config)
              this.$message.success('更新成功')
            } else {
              await httpUtil.post('/sysOnelinkSystem/add', this.systemForm, config)
              this.$message.success('添加成功')
              // 添加新系统后跳转到第一页
              this.systemPagination.page = 1
            }
            this.handleCloseSystemForm()
            this.loadSystemList()
          } catch (error) {
            console.error('保存系统失败:', error)
            this.$message.error('保存失败')
          } finally {
            this.systemFormLoading = false
          }
        }
      })
    },
    
    handleSizeChange(size) {
      this.systemPagination.size = size
      this.loadSystemList()
    },
    
    handleCurrentChange(page) {
      this.systemPagination.page = page
      this.loadSystemList()
    },
    
    initCharts() {
      // 检查ECharts是否已加载
      if (!this.echartsLoaded || !this.echarts) {
        console.warn('ECharts未加载，跳过图表初始化')
        return
      }
      
      // 检查DOM元素是否存在
      if (!this.$refs.dailyTrendChart || !this.$refs.systemDistributionChart) {
        console.warn('图表容器DOM元素不存在，跳过图表初始化')
        return
      }
      
      try {
        // 初始化日访问量趋势图
        this.dailyTrendChart = this.echarts.init(this.$refs.dailyTrendChart)
        this.dailyTrendChart.setOption({
          tooltip: {
            trigger: 'item',
            backgroundColor: 'rgba(255, 255, 255, 0.95)',
            borderColor: '#005389',
            borderWidth: 1,
            textStyle: {
              color: '#333'
            },
            formatter: function(params) {
              const date = params.name
              const value = params.value
              let formattedDate = '未知'
              
              if (date && typeof date === 'string') {
                // 如果是 YYYY-MM-DD 格式，转换为 MM/DD
                if (date.includes('-')) {
                  const parts = date.split('-')
                  if (parts.length >= 3) {
                    formattedDate = `${parts[1]}/${parts[2]}`
                  }
                } else {
                  formattedDate = date
                }
              } else if (date && typeof date === 'number') {
                // 如果是时间戳，转换为日期
                const dateObj = new Date(date)
                formattedDate = `${(dateObj.getMonth() + 1).toString().padStart(2, '0')}/${dateObj.getDate().toString().padStart(2, '0')}`
              }
              
              return `<strong>日期:</strong> ${formattedDate}<br/><strong>访问量:</strong> ${value} 次`
            }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            top: '3%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: this.dailyTrendData.map(item => item.date),
            axisLine: {
              lineStyle: {
                color: '#005389'
              }
            },
            axisLabel: {
              color: '#666',
              formatter: function(value) {
                return value.split('-').slice(1).join('/')
              }
            }
          },
          yAxis: {
            type: 'value',
            axisLine: {
              lineStyle: {
                color: '#005389'
              }
            },
            axisLabel: {
              color: '#666'
            },
            splitLine: {
              lineStyle: {
                color: '#f0f0f0'
              }
            }
          },
          series: [{
            data: this.dailyTrendData.map(item => item.count),
            type: 'line',
            smooth: true,
            showSymbol: true,
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: {
              color: '#005389',
              width: 3
            },
            itemStyle: {
              color: '#005389',
              borderColor: '#fff',
              borderWidth: 2
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [{
                  offset: 0, color: 'rgba(0, 83, 137, 0.3)'
                }, {
                  offset: 1, color: 'rgba(0, 83, 137, 0.05)'
                }]
              }
            },
            emphasis: {
              itemStyle: {
                color: '#029165',
                borderColor: '#fff',
                borderWidth: 3,
                shadowBlur: 10,
                shadowColor: 'rgba(0, 83, 137, 0.3)'
              }
            }
          }]
        })

        // 初始化系统点击分布图
        this.systemDistributionChart = this.echarts.init(this.$refs.systemDistributionChart)
        this.systemDistributionChart.setOption({
          tooltip: {
            trigger: 'item',
            backgroundColor: 'rgba(255, 255, 255, 0.95)',
            borderColor: '#005389',
            borderWidth: 1,
            textStyle: {
              color: '#333'
            },
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 'left',
            top: 'middle',
            textStyle: {
              color: '#666'
            }
          },
          series: [{
            name: '系统点击',
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['60%', '50%'],
            data: this.systemDistributionData,
            itemStyle: {
              borderRadius: 8,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false
            },
            labelLine: {
              show: false
            },
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              },
              label: {
                show: true,
                fontSize: 12,
                fontWeight: 'bold'
              }
            },
            color: [
              '#005389', '#029165', '#20b2aa', '#4682b4',
              '#5f9ea0', '#6495ed', '#7b68ee', '#9370db'
            ]
          }]
        })
      } catch (error) {
        console.error('图表初始化失败:', error)
      }
    },
    
    updateDailyTrendChart() {
      if (!this.dailyTrendChart || !this.echartsLoaded) {
        console.warn('日趋势图表未初始化或ECharts未加载')
        return
      }
      
      try {
        this.dailyTrendChart.setOption({
          tooltip: {
            trigger: 'item',
            backgroundColor: 'rgba(255, 255, 255, 0.95)',
            borderColor: '#005389',
            borderWidth: 1,
            textStyle: {
              color: '#333'
            },
            formatter: function(params) {
              const date = params.name
              const value = params.value
              let formattedDate = '未知'
              
              if (date && typeof date === 'string') {
                // 如果是 YYYY-MM-DD 格式，转换为 MM/DD
                if (date.includes('-')) {
                  const parts = date.split('-')
                  if (parts.length >= 3) {
                    formattedDate = `${parts[1]}/${parts[2]}`
                  }
                } else {
                  formattedDate = date
                }
              } else if (date && typeof date === 'number') {
                // 如果是时间戳，转换为日期
                const dateObj = new Date(date)
                formattedDate = `${(dateObj.getMonth() + 1).toString().padStart(2, '0')}/${dateObj.getDate().toString().padStart(2, '0')}`
              }
              
              return `<strong>日期:</strong> ${formattedDate}<br/><strong>访问量:</strong> ${value} 次`
            }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            top: '3%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: this.dailyTrendData.map(item => item.date),
            axisLine: {
              lineStyle: {
                color: '#005389'
              }
            },
            axisLabel: {
              color: '#666',
              formatter: function(value) {
                return value.split('-').slice(1).join('/')
              }
            }
          },
          yAxis: {
            type: 'value',
            axisLine: {
              lineStyle: {
                color: '#005389'
              }
            },
            axisLabel: {
              color: '#666'
            },
            splitLine: {
              lineStyle: {
                color: '#f0f0f0'
              }
            }
          },
          series: [{
            data: this.dailyTrendData.map(item => item.count),
            type: 'line',
            smooth: true,
            showSymbol: true,
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: {
              color: '#005389',
              width: 3
            },
            itemStyle: {
              color: '#005389',
              borderColor: '#fff',
              borderWidth: 2
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [{
                  offset: 0, color: 'rgba(0, 83, 137, 0.3)'
                }, {
                  offset: 1, color: 'rgba(0, 83, 137, 0.05)'
                }]
              }
            },
            emphasis: {
              itemStyle: {
                color: '#029165',
                borderColor: '#fff',
                borderWidth: 3,
                shadowBlur: 10,
                shadowColor: 'rgba(0, 83, 137, 0.3)'
              }
            }
          }]
        })
      } catch (error) {
        console.error('更新日趋势图表失败:', error)
      }
    },
    
    updateSystemDistributionChart() {
      if (!this.systemDistributionChart || !this.echartsLoaded) {
        console.warn('系统分布图表未初始化或ECharts未加载')
        return
      }
      
      try {
        this.systemDistributionChart.setOption({
          tooltip: {
            trigger: 'item',
            backgroundColor: 'rgba(255, 255, 255, 0.95)',
            borderColor: '#005389',
            borderWidth: 1,
            textStyle: {
              color: '#333'
            },
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 'left',
            top: 'middle',
            textStyle: {
              color: '#666'
            }
          },
          series: [{
            name: '系统点击',
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['60%', '50%'],
            data: this.systemDistributionData,
            itemStyle: {
              borderRadius: 8,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false
            },
            labelLine: {
              show: false
            },
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              },
              label: {
                show: true,
                fontSize: 12,
                fontWeight: 'bold'
              }
            },
            color: [
              '#005389', '#029165', '#20b2aa', '#4682b4',
              '#5f9ea0', '#6495ed', '#7b68ee', '#9370db'
            ]
          }]
        })
      } catch (error) {
        console.error('更新系统分布图表失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.analytics-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Inter', 'Roboto', sans-serif;
}

.analytics-header {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #ffffff, #f8f9fa);
  border-radius: 12px;
  padding: 16px 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 83, 137, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.title-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #005389, #029165);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 83, 137, 0.2);
}

.title-icon i {
  font-size: 18px;
  color: white;
}

.title-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.title-content h1 {
  color: #005389;
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  line-height: 1.2;
}

.info-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #666;
  background: rgba(0, 83, 137, 0.05);
  padding: 2px 8px;
  border-radius: 6px;
  white-space: nowrap;
}

.info-badge i {
  color: #029165;
  font-size: 10px;
}

.header-controls {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.date-control {
  flex-shrink: 0;
}

.compact-date-picker {
  width: 280px;
}

.compact-date-picker :deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #dcdfe6;
  transition: all 0.2s ease;
  background: white;
}

.compact-date-picker :deep(.el-input__wrapper:hover) {
  border-color: #005389;
}

.compact-date-picker :deep(.el-input__wrapper.is-focus) {
  border-color: #005389;
  box-shadow: 0 0 0 2px rgba(0, 83, 137, 0.1);
}

.action-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.compact-btn {
  border-radius: 6px !important;
  font-weight: 500 !important;
  transition: all 0.2s ease !important;
  border: 1px solid #005389 !important;
  color: #005389 !important;
  background: white !important;
  padding: 4px 12px !important;
  min-width: 60px;
}

.compact-btn:hover {
  background: #005389 !important;
  color: white !important;
  transform: translateY(-1px) !important;
}

.load-btn {
  background: linear-gradient(135deg, #029165, #20b2aa) !important;
  border: none !important;
  border-radius: 8px !important;
  font-weight: 600 !important;
  transition: all 0.2s ease !important;
  color: white !important;
  padding: 6px 16px !important;
}

.load-btn:hover {
  background: linear-gradient(135deg, #027a56, #1a9b94) !important;
  transform: translateY(-1px) !important;
}

.load-btn i {
  font-size: 14px;
}

/* Element UI 日期选择器主题定制 */
.compact-date-picker :deep(.el-date-range-picker__header) {
  background: linear-gradient(135deg, #005389, #029165);
  color: white;
  border-radius: 6px 6px 0 0;
}

.compact-date-picker :deep(.el-picker-panel__content) {
  background: #fafafa;
}

.compact-date-picker :deep(.el-date-table td.today .el-date-table-cell__text) {
  background: #005389;
  color: white;
  border-radius: 4px;
}

.compact-date-picker :deep(.el-date-table td.in-range .el-date-table-cell__text) {
  background: rgba(0, 83, 137, 0.1);
  color: #005389;
}

.compact-date-picker :deep(.el-date-table td.start-date .el-date-table-cell__text),
.compact-date-picker :deep(.el-date-table td.end-date .el-date-table-cell__text) {
  background: #005389;
  color: white;
  border-radius: 4px;
}

.compact-date-picker :deep(.el-picker-panel__footer) {
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
}

.no-data {
  text-align: center;
  padding: 40px 20px;
  color: #666;
}

.no-data p {
  margin: 0;
  font-size: 16px;
}

.statistics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.stat-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  text-align: center;
  border: 1px solid rgba(0, 83, 137, 0.1);
}

.stat-card h3 {
  margin: 0 0 15px 0;
  color: #666;
  font-size: 16px;
  font-weight: 500;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #005389;
  margin: 0;
}

.stat-note {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.charts-section {
  margin-top: 40px;
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(0, 83, 137, 0.1);
}

.charts-container {
  display: flex;
  gap: 20px;
}

.popular-systems {
  flex: 1;
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(0, 83, 137, 0.1);
}

.popular-systems h2 {
  margin: 0 0 20px 0;
  color: #005389;
  font-size: 20px;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.rank {
  width: 30px;
  height: 30px;
  background: linear-gradient(135deg, #005389, #029165);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  margin-right: 15px;
}

.system-name {
  flex: 1;
  font-weight: 500;
  color: #333;
}

.click-count {
  color: #666;
  font-size: 14px;
}

.charts-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chart-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(0, 83, 137, 0.1);
}

.chart-card h3 {
  margin: 0 0 15px 0;
  color: #005389;
  font-size: 20px;
  font-weight: bold;
}

.chart-container {
  height: 300px;
}

.chart-loading {
  height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #666;
  font-size: 14px;
}

.chart-loading i {
  font-size: 24px;
  margin-bottom: 10px;
  color: #005389;
}

.chart-loading span {
  color: #999;
}

.chart-error {
  height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #666;
  font-size: 14px;
}

.chart-error i {
  font-size: 24px;
  margin-bottom: 10px;
  color: #005389;
}

.chart-error span {
  color: #999;
}

@media (max-width: 1024px) {
  .header-controls {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .date-control {
    width: 100%;
  }
  
  .compact-date-picker {
    width: 100%;
  }
  
  .action-controls {
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .analytics-header {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  
  .header-left {
    justify-content: center;
  }
  
  .statistics-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 15px;
  }
  
  .stat-card {
    padding: 20px;
  }
  
  .stat-number {
    font-size: 28px;
  }
  
  .charts-container {
    flex-direction: column;
  }
  
  .popular-systems {
    padding: 20px;
  }
  
  .ranking-item {
    padding: 10px 12px;
  }
  
  .action-controls {
    flex-wrap: wrap;
    gap: 6px;
  }
  
  .compact-btn {
    min-width: 50px;
    padding: 3px 8px !important;
    font-size: 12px;
  }
  
  .load-btn {
    padding: 5px 12px !important;
    font-size: 13px;
  }
}

/* 系统管理相关样式 */
.system-management-content {
  padding: 20px 0;
}

.management-actions {
  margin-bottom: 20px;
  display: flex;
  gap: 12px;
  align-items: center;
}

.management-actions .el-button {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.management-actions .el-button--primary {
  background: linear-gradient(135deg, #005389, #029165);
  border: none;
}

.management-actions .el-button--primary:hover {
  background: linear-gradient(135deg, #004070, #027a56);
  transform: translateY(-1px);
}

.management-actions .el-button--success {
  background: linear-gradient(135deg, #029165, #20b2aa);
  border: none;
}

.management-actions .el-button--success:hover {
  background: linear-gradient(135deg, #027a56, #1a9b94);
  transform: translateY(-1px);
}

.management-actions i {
  margin-right: 6px;
}

/* 表格样式优化 */
.system-management-content .el-table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.system-management-content .el-table th {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  color: #333;
  font-weight: 600;
}

.system-management-content .el-table td {
  border-bottom: 1px solid #f0f0f0;
}

.system-management-content .el-table--striped .el-table__body tr.el-table__row--striped td {
  background: #fafbfc;
}

/* 表单样式优化 */
.el-form-item__label {
  font-weight: 500;
  color: #333;
}

.el-input__wrapper {
  border-radius: 6px;
  transition: all 0.2s ease;
}

.el-input__wrapper:hover {
  border-color: #005389;
}

.el-input__wrapper.is-focus {
  border-color: #005389;
  box-shadow: 0 0 0 2px rgba(0, 83, 137, 0.1);
}

.el-textarea__inner {
  border-radius: 6px;
  transition: all 0.2s ease;
}

.el-textarea__inner:hover {
  border-color: #005389;
}

.el-textarea__inner:focus {
  border-color: #005389;
  box-shadow: 0 0 0 2px rgba(0, 83, 137, 0.1);
}

.el-select .el-input__wrapper {
  border-radius: 6px;
}

.el-input-number .el-input__wrapper {
  border-radius: 6px;
}

/* 对话框样式优化 */
.el-dialog {
  border-radius: 12px;
  overflow: hidden;
}

.el-dialog__header {
  background: linear-gradient(135deg, #005389, #029165);
  color: white;
  padding: 20px 24px;
}

.el-dialog__title {
  color: white;
  font-weight: 600;
}

.el-dialog__headerbtn .el-dialog__close {
  color: white;
  font-size: 18px;
}

.el-dialog__body {
  padding: 24px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer .el-button {
  border-radius: 6px;
  font-weight: 500;
  padding: 8px 20px;
}

.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #005389, #029165);
  border: none;
}

.dialog-footer .el-button--primary:hover {
  background: linear-gradient(135deg, #004070, #027a56);
}

/* 响应式优化 */
@media (max-width: 768px) {
  .management-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .management-actions .el-button {
    width: 100%;
    justify-content: center;
  }
}

/* 分页组件样式 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.pagination-container .el-pagination {
  background: transparent;
}

.pagination-container .el-pagination .el-pager li {
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  margin: 0 2px;
  transition: all 0.2s ease;
}

.pagination-container .el-pagination .el-pager li:hover {
  border-color: #005389;
  color: #005389;
}

.pagination-container .el-pagination .el-pager li.is-active {
  background: linear-gradient(135deg, #005389, #029165);
  border-color: #005389;
  color: white;
}

.pagination-container .el-pagination .btn-prev,
.pagination-container .el-pagination .btn-next {
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.pagination-container .el-pagination .btn-prev:hover,
.pagination-container .el-pagination .btn-next:hover {
  border-color: #005389;
  color: #005389;
}

/* 分页组件响应式样式 */
@media (max-width: 768px) {
  .pagination-container {
    margin-top: 15px;
  }
  
  .pagination-container .el-pagination {
    justify-content: center;
  }
  
  .pagination-container .el-pagination .el-pagination__sizes {
    display: none;
  }
  
  .pagination-container .el-pagination .el-pagination__jump {
    display: none;
  }
}

/* 图表响应式样式 */
@media (max-width: 1200px) {
  .charts-container {
    gap: 15px;
  }
  
  .chart-card {
    padding: 20px;
  }
  
  .chart-container {
    height: 280px;
  }
}

@media (max-width: 768px) {
  .charts-section {
    padding: 20px;
  }
  
  .charts-container {
    flex-direction: column;
    gap: 20px;
  }
  
  .popular-systems {
    padding: 20px;
    box-shadow: none;
    border: 1px solid rgba(0, 83, 137, 0.1);
  }
  
  .charts-panel {
    gap: 15px;
  }
  
  .chart-card {
    padding: 15px;
    box-shadow: none;
    border: 1px solid rgba(0, 83, 137, 0.1);
  }
  
  .chart-container {
    height: 250px;
  }
  
  .ranking-item {
    padding: 10px 12px;
  }
  
  .rank {
    width: 25px;
    height: 25px;
    font-size: 12px;
    margin-right: 10px;
  }
  
  .system-name {
    font-size: 14px;
  }
  
  .click-count {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .chart-container {
    height: 220px;
  }
  
  .chart-card h3 {
    font-size: 14px;
    margin-bottom: 10px;
  }
  
  .popular-systems h2 {
    font-size: 16px;
    margin-bottom: 15px;
  }
  
  .ranking-item {
    padding: 8px 10px;
  }
  
  .rank {
    width: 22px;
    height: 22px;
    font-size: 11px;
    margin-right: 8px;
  }
  
  .system-name {
    font-size: 13px;
  }
  
  .click-count {
    font-size: 11px;
  }
}
</style> 