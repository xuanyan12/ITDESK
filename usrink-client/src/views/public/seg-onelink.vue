<template>
  <div class="tech-container">
    <!-- Animated background elements -->
    <div class="tech-background">
      <div class="glow-circle top-left"></div>
      <div class="glow-circle bottom-right"></div>
      <div class="particle-network" ref="particleNetwork"></div>
    </div>
    
    <!-- Full screen flower transition overlay -->
    <div class="fullscreen-flower-transition" ref="fullscreenTransition" :style="{ display: 'none' }">
      <div class="flower-rings-container">
        <!-- Large outer ring - clockwise -->
        <div class="flower-ring ring-large">
          <div v-for="n in 24" :key="n" class="flower large-flower"
            :style="`
              transform: rotate(${(n-1) * (360/24)}deg) translateY(-95vh);
              --color: ${getRandomColor(n)};
            `">
            <div class="flower-center"></div>
            <div class="flower-petal" v-for="p in 8" :key="p" 
              :style="`transform: rotate(${(p-1) * 45}deg)`"></div>
          </div>
        </div>
        
        <!-- Medium middle ring - counter-clockwise -->
        <div class="flower-ring ring-medium">
          <div v-for="n in 18" :key="n" class="flower medium-flower"
            :style="`
              transform: rotate(${(n-1) * (360/18)}deg) translateY(-63vh);
              --color: ${getRandomColor(n+24)};
            `">
            <div class="flower-center"></div>
            <div class="flower-petal" v-for="p in 8" :key="p" 
              :style="`transform: rotate(${(p-1) * 45}deg)`"></div>
          </div>
        </div>
        
        <!-- Small inner ring - clockwise -->
        <div class="flower-ring ring-small">
          <div v-for="n in 12" :key="n" class="flower small-flower"
            :style="`
              transform: rotate(${(n-1) * (360/12)}deg) translateY(-32vh);
              --color: ${getRandomColor(n+42)};
            `">
            <div class="flower-center"></div>
            <div class="flower-petal" v-for="p in 8" :key="p" 
              :style="`transform: rotate(${(p-1) * 45}deg)`"></div>
          </div>
        </div>
      </div>
      
      <div class="transition-content">
        <div class="transition-icon">
          <i :class="currentSystem.icon" v-if="currentSystem"></i>
        </div>
        <div class="transition-details" v-if="currentSystem">
          <div class="transition-title">
            <span class="transition-category">{{ currentSystem.category }}</span>
            <span class="transition-divider">|</span>
            <span class="transition-name">{{ currentSystem.name }}</span>
          </div>
          <div class="transition-description">{{ currentSystem.description }}</div>
        </div>
      </div>
    </div>
    
    <!-- Fixed header that stays at top when scrolling -->
    <div class="fixed-header" :class="{ 'scrolled': isScrolled }">
      <div class="fixed-header-content">
        <div class="fixed-header-inner">
          <div class="fixed-logo">
            <img src="/SEG-logo.png" alt="SEG Logo" class="fixed-logo-img">
          </div>
          <div class="fixed-title-container">
            <h1 class="fixed-title">
              <span class="gradient-text">One Link</span>
            </h1>
            <div class="fixed-decoration"></div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Main content -->
    <div class="content-wrapper">
      <!-- SEG Logo in top left corner -->
      <div class="seg-logo-container">
        <img src="/SEG-logo.png" alt="SEG Logo" class="seg-logo">
      </div>
      
      <!-- Search box in top right corner -->
      <div class="search-container">
        <div class="search-box">
          <input 
            type="text" 
            placeholder="连接所有 简化工作" 
            class="search-input"
            v-model="searchQuery"
            @input="filterSystems"
            @focus="showDropdown = true"
            @blur="handleBlur"
          >
          <button class="search-button">
            <i class="fas fa-search"></i>
          </button>
          <!-- Search results dropdown -->
          <div class="search-results" v-if="showDropdown && filteredSystems.length > 0">
            <div 
              class="search-result-item" 
              v-for="(system, index) in filteredSystems" 
              :key="index"
              @mousedown="handleCardClick($event, system.url)"
            >
              <div class="search-result-text">
                <div class="search-result-title">
                  <span class="result-name">{{ system.name }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- AI助手按钮组 -->
        <div class="ai-assistant-group" :class="{ 'hide-ai-assistant': isScrolled }">
          <!-- AI助手弹窗按钮 -->
          <button class="ai-assistant-button" @click="openDifyModal" title="SEG-AIBot">
            <i class="fas fa-robot"></i>
          </button>
        </div>
        
        <!-- Favorites icon button - now next to search box -->
        <div class="favorites-wrapper" ref="favoritesWrapper" :class="{ 'hide-favorites': isScrolled }">
          <button class="my-favorites-button" @click="toggleFavoritesDropdown">
            ★
          </button>
          
          <!-- Favorites dropdown -->
          <div class="favorites-dropdown" v-if="showFavoritesDropdown">
            <div class="favorites-list" v-if="favorites.length > 0">
              <div 
                class="favorite-item" 
                v-for="system in favorites" 
                :key="system.url"
                @click="handleCardClick($event, system.url)"
              >
                <div class="favorite-name">{{ system.name }}</div>
                <div class="favorite-remove" @click.stop="removeFavorite(system)">×</div>
              </div>
            </div>
            <div class="empty-favorites" v-else>
              <p>暂无收藏项目</p>
            </div>
          </div>
        </div>
      </div>
      
      <div class="header-section">
        <div class="title-with-logo" data-aos="fade-right">
          <h1 class="main-title">
            <span class="gradient-text">One Link</span>
          </h1>
        </div>
        <div class="title-decoration" data-aos="fade-up"></div>
      </div>
      
      <!-- Category-based layout -->
      <div class="category-sections">
        <!-- 返回按钮 - 只在详情视图显示 -->
        <div v-if="isDetailView" class="back-button-container" data-aos="fade-right">
          <button class="back-button" @click="backToMainView">
            <i class="fas fa-arrow-left"></i>
            <span>返回所有系统</span>
          </button>
        </div>
        
        <!-- 主视图 - 显示所有分类 -->
        <div v-if="!isDetailView" class="departments-row" v-for="i in Math.ceil(Object.keys(categorySystemMap).length / 4)" :key="i">
          <template v-for="(categoryName, index) in Object.keys(categorySystemMap).slice((i-1)*4, i*4)" :key="categoryName">
            <div class="category-section" data-aos="fade-up">
              <div class="category-header">
                <div class="category-icon">
                  <i :class="getCategoryIcon(categoryName)"></i>
                </div>
                <h2 class="category-title">
                  <span class="category-zh gradient-category">{{ getCategoryChineseName(categoryName) }}</span>
                  <span class="category-en">{{ getCategoryEnglishName(categoryName) }}</span>
                </h2>
              </div>
              <div class="system-list">
                <a
                  v-for="(system, idx) in categorySystemMap[categoryName].slice(0, 5)" 
                  :key="system.name"
                  class="system-item" 
                  @click.prevent="handleCardClick($event, system.url)"
                  href="#"
                >
                  <span class="system-number">{{ (idx + 1).toString().padStart(2, '0') }}</span>
                  <span class="system-name">{{ system.name }}</span>
                  <!-- Only show favorite star if it's a favorite -->
                  <span v-if="isFavorite(system)" class="favorite-star-icon" @click.stop="removeFavorite(system)">★</span>
                  <!-- Add to favorites button appears on hover -->
                  <span v-else class="add-favorite-button" @click.stop="addFavorite(system)">+</span>
                </a>
                
                <!-- 查看更多按钮 - 只在系统数量超过5个时显示 -->
                <div v-if="categorySystemMap[categoryName].length > 5" class="view-more-button" @click="expandCategory(categoryName)">
                  <i class="fas fa-chevron-right"></i>
                  <span>查看更多 ({{ categorySystemMap[categoryName].length - 5 }})</span>
                </div>
              </div>
            </div>
          </template>
        </div>
        
        <!-- 详情视图 - 显示单个分类的所有系统 -->
        <div v-if="isDetailView && expandedCategory" class="expanded-category-view" data-aos="fade-up">
          <div class="expanded-category-header">
            <div class="expanded-category-icon">
              <i :class="getCategoryIcon(expandedCategory)"></i>
            </div>
            <div class="expanded-category-title">
              <h1 class="expanded-category-zh gradient-category">{{ getCategoryChineseName(expandedCategory) }}</h1>
              <p class="expanded-category-en">{{ getCategoryEnglishName(expandedCategory) }}</p>
              <div class="expanded-category-count">共 {{ categorySystemMap[expandedCategory].length }} 个系统</div>
            </div>
          </div>
          
          <div class="expanded-systems-grid">
            <a
              v-for="(system, idx) in categorySystemMap[expandedCategory]" 
              :key="system.name"
              class="expanded-system-card" 
              @click.prevent="handleCardClick($event, system.url)"
              href="#"
            >
              <div class="expanded-system-number">{{ (idx + 1).toString().padStart(2, '0') }}</div>
              <div class="expanded-system-content">
                <div class="expanded-system-name">{{ system.name }}</div>
                <div class="expanded-system-description">{{ system.description }}</div>
              </div>
              <div class="expanded-system-actions">
                <!-- Only show favorite star if it's a favorite -->
                <span v-if="isFavorite(system)" class="expanded-favorite-star" @click.stop="removeFavorite(system)">★</span>
                <!-- Add to favorites button appears on hover -->
                <span v-else class="expanded-add-favorite" @click.stop="addFavorite(system)">+</span>
              </div>
            </a>
          </div>
        </div>
      </div>
      
      <!-- Latest announcements ticker - temporarily hidden -->
      <!-- <div class="announcement-ticker" data-aos="fade-up">
        <div class="ticker-label">最新公告:</div>
        <div class="ticker-wrapper">
          <div class="ticker-content" ref="ticker">
          </div>
        </div>
      </div> -->
      
      <!-- Footer -->
      <div class="tech-footer">
        <div class="footer-logo-container">
          <div class="footer-logo-gradient"></div>
          <span class="footer-title">SEG IT</span>
        </div>
        <div class="copyright">© 2025 SEG IT 部门. 保留所有权利</div>
      </div>
    </div>
    
    <!-- Dify工作流模态框 -->
    <div class="dify-modal" v-if="showDifyModal" @click="closeDifyModal">
      <div class="dify-modal-content" @click.stop>
        <div class="dify-modal-header">
          <div></div>
          <button class="dify-close-btn" @click="closeDifyModal">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="dify-modal-body">
          <iframe
            v-if="currentDifySystem"
            :src="currentDifySystem.url"
            style="width: 100%; height: 100%; border: none;"
            frameborder="0"
            allow="microphone">
          </iframe>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import AOS from 'aos'
import 'aos/dist/aos.css'
import httpUtil from '@/utils/HttpUtil'

export default {
  name: 'SegOnelink',
  data() {
    return {
      tickerInterval: null,
      isScrolled: false,
      searchQuery: '',
      showDropdown: false,
      filteredSystems: [],
      pendingUrl: null,
      currentSystem: null,
      enableFlowerTransition: false, // Set to 'true' to enable the flower animation transition when clicking on system cards
      showFavoritesDropdown: false,
      favorites: [],
      // 分类展开相关
      expandedCategory: null, // 当前展开的分类
      isDetailView: false, // 是否在详情视图
      originalViewport: null, // 保存原来的viewport设置
      // Dify模态框相关
      showDifyModal: false,
      currentDifySystem: null,
      // 数据统计相关
      sessionId: null,
      flowerColors: [
        '#FF6B6B', '#FF85A1', '#FF9A8B', 
        '#FFC75F', '#F9F871', '#90D4F7', 
        '#B39DDB', '#66BB6A', '#4DD0E1', 
        '#A5D6A7', '#FFD54F', '#E57373', 
        '#BA68C8', '#4DB6AC', '#7986CB',
        '#F06292', '#FFB74D', '#AED581',
        '#4FC3F7', '#81C784', '#9575CD'
      ],
      systems: [
        // HR System (人事系统)
        {
          category: '人事系统 (HR System)',
          name: 'BIPO',
          description: '薪资计算及人事服务管理',
          url: 'https://hrms11.bipocloud.com/SEG/Login',
          icon: 'fas fa-user-tie'
        },
        {
          category: '人事系统 (HR System)',
          name: 'Cornerstone',
          description: '员工培训与课程管理',
          url: 'https://seg.csod.com/',
          icon: 'fas fa-graduation-cap'
        },
        {
          category: '人事系统 (HR System)',
          name: '劳勤',
          description: '排班与考勤管理',
          url: 'https://a6.coho.net.cn:91/',
          icon: 'fas fa-calendar-alt'
        },
        {
          category: '人事系统 (HR System)',
          name: 'SuccessFactors',
          description: '员工主数据与绩效管理（GPS）',
          url: 'https://hcm55.sapsf.eu',
          icon: 'fas fa-id-card'
        },
        // COR System (财务系统)
        {
          category: '财务系统 (COR System)',
          name: 'Concur',
          description: '费用报销管理',
          url: 'https://eu2.concursolutions.com',
          icon: 'fas fa-calculator'
        },
        {
          category: '财务系统 (COR System)',
          name: 'WebCis',
          description: '采购成本管控报表系统',
          url: 'https://webcis.de/seg_sso/webcis40/',
          icon: 'fas fa-file-invoice-dollar'
        },
        {
          category: '财务系统 (COR System)',
          name: 'd/EPM',
          description: '销售预测和项目获取管理系统',
          url: 'https://mingle-portal.eu1.inforcloudsuite.com/SEGAUTOMOTIVE_PRD/fa1e0533-b204-49cd-ae22-7708c9028ddd',
          icon: 'fas fa-chart-line'
        },
        // Procurement System (采购系统)
        {
          category: '采购系统 (Procurement System)',
          name: 'Jaggaer',
          description: '采购流程与供应商管理',
          url: 'https://app11.jaggaer.com/sso/bsg',
          icon: 'fas fa-shopping-cart'
        },
        {
          category: '采购系统 (Procurement System)',
          name: 'Conga',
          description: '采购合同管理系统',
          url: 'https://seg-automotive.lightning.force.com/lightning/o/Apttus__APTS_Agreement__c/list?filterName=__Recent',
          icon: 'fas fa-file-contract'
        },
        // R&D System (研发系统)
        {
          category: '研发系统 (R&D System)',
          name: 'e-Norm',
          description: '研发相关的规范文档管理',
          url: 'https://seg-automotive.beuth-e-norm.net/eNormSAML',
          icon: 'fas fa-flask'
        },
        {
          category: '研发系统 (R&D System)',
          name: 'PTC Windchill',
          description: '研发设计平台',
          url: 'https://prod-seg-automotive.ttpsc.cloud/Windchill/app/',
          icon: 'fas fa-drafting-compass'
        },
        {
          category: '研发系统 (R&D System)',
          name: 'iPoint SaaS',
          description: 'SEGMDS物料数据系统',
          url: 'https://seg.onipoint.cloud/dashboard/#!DashboardView',
          icon: 'fas fa-cubes'
        },
        {
          category: '研发系统 (R&D System)',
          name: 'Altium Designer',
          description: '电子器件设计平台',
          url: 'https://prod-seg-automotive-germany-gmbh.365.altium.com/getstarted',
          icon: 'fas fa-microchip'
        },
        {
          category: '研发系统 (R&D System)',
          name: 'PLM Knowledge Base',
          description: 'PLM培训文档',
          url: 'https://segautomotivecom.sharepoint.com/sites/Wiki-GL-EN-PDMDocumentation/SitePages/Landscape.aspx',
          icon: 'fas fa-book'
        },
        // Logistics System (物流系统)
        {
          category: '物流系统 (Logistics System)',
          name: 'PaGOS',
          description: '物流优化与运输管理',
          url: 'http://mcppagos01.sg.lan:9025/pagosplus/',
          icon: 'fas fa-truck'
        },
        {
          category: '物流系统 (Logistics System)',
          name: 'Supply On',
          description: '供应商EDI管理',
          url: 'https://platform.application.prd.supplyon.com/logon/logonServlet',
          icon: 'fas fa-exchange-alt'
        },
        {
          category: '物流系统 (Logistics System)',
          name: '智慧关务',
          description: '进出口报关系统',
          url: 'https://seg.51haitun.cn/login?redirect=%2Fcs%2FieManage%2FNonBondedDecErpIList',
          icon: 'fas fa-ship'
        },
        // Quality System (质量系统)
        {
          category: '质量系统 (Quality System)',
          name: 'OMS',
          description: '标准流程文档管理',
          url: 'https://segautomotivecom.sharepoint.com/sites/OMS',
          icon: 'fas fa-check-circle'
        },
        // Production System (生产系统)
        {
          category: '生产系统 (Production System)',
          name: 'SEG-TEF-system',
          description: 'TEF业务流程管理',
          url: 'http://10.219.129.71:5055/login',
          icon: 'fas fa-industry'
        },
        {
          category: '生产系统 (Production System)',
          name: 'SES TQS',
          description: 'SES生产员工培训和认证系统',
          url: 'http://10.219.129.75:8089/',
          icon: 'fas fa-certificate'
        },
        {
          category: '生产系统 (Production System)',
          name: 'SEG TQS',
          description: 'SEG生产员工培训和认证系统',
          url: 'http://10.219.129.33:8089',
          icon: 'fas fa-certificate'
        },
        {
          category: '生产系统 (Production System)',
          name: 'E-SMC',
          description: '生产数字化报表展示系统',
          url: 'http://10.219.129.59:8094/edashboard/views/index.html',
          icon: 'fas fa-chart-bar'
        },
        // IT System (IT系统)
        {
          category: 'IT系统 (IT System)',
          name: 'Matrix42',
          description: 'IT支持流程及IT资产管理',
          url: 'https://it-portal.seg-automotive.com/',
          icon: 'fas fa-server'
        },
        {
          category: 'IT系统 (IT System)',
          name: 'Infor',
          description: 'ERP系统',
          url: 'https://mingle-portal.eu1.inforcloudsuite.com/SEGAUTOMOTIVE_PRD',
          icon: 'fas fa-database'
        },
        {
          category: 'IT系统 (IT System)',
          name: 'Microsoft Office365',
          description: 'Office应用集合',
          url: 'https://m365.cloud.microsoft/?auth=2&home=1',
          icon: 'fas fa-file-word'
        },
        {
          category: 'IT系统 (IT System)',
          name: 'Salesforce',
          description: '通用流程管理',
          url: 'https://seg-automotive.my.salesforce.com/',
          icon: 'fas fa-tasks'
        },
        {
          category: 'IT系统 (IT System)',
          name: 'IT管理系统',
          description: 'IT系统管理中心',
          url: 'http://cngvwms01:5055',
          icon: 'fas fa-cogs'
        }
      ],
    }
  },
  computed: {
    categorySystemMap() {
      // Group systems by category
      const map = {};
      this.systems.forEach(system => {
        if (!map[system.category]) {
          map[system.category] = [];
        }
        map[system.category].push(system);
      });
      
      // 定义分类的显示顺序
      const categoryOrder = [
        '人事系统 (HR System)',
        '财务系统 (COR System)', 
        '采购系统 (Procurement System)',
        '研发系统 (R&D System)',
        '物流系统 (Logistics System)',
        '质量系统 (Quality System)',
        '生产系统 (Production System)',
        'IT系统 (IT System)'
      ];
      
      // 按照指定顺序重新组织map
      const orderedMap = {};
      categoryOrder.forEach(category => {
        if (map[category]) {
          orderedMap[category] = map[category];
        }
      });
      
      // 添加任何不在预定义顺序中的分类
      Object.keys(map).forEach(category => {
        if (!orderedMap[category]) {
          orderedMap[category] = map[category];
        }
      });
      
      return orderedMap;
    }
  },
  mounted() {
    // 设置页面缩放比例为80%
    this.setPageScale()
    
    AOS.init({
      duration: 1000,
      once: false,
      mirror: false,
      offset: 50,
      easing: 'ease-out-cubic'
    })
    
    // 初始化数据统计
    this.initAnalytics()
    
    this.initParticleNetwork()
    
    // Initialize the ticker (commented out since announcements are hidden)
    // const ticker = this.$refs.ticker
    // if (ticker) {
    //   const span = document.createElement('span')
    //   span.textContent = 'One Link 1.0正式上线'
    //   ticker.appendChild(span)
    // }
    
    // Start the ticker animation after initializing content
    // this.startTicker()
    
    this.adjustTitleDecoration()
    
    this.loadFavorites()
    
    // 加载系统数据
    this.loadSystemsFromDatabase()
    
    window.addEventListener('mousemove', this.handleMouseMove)
    window.addEventListener('resize', this.handleResize)
    window.addEventListener('scroll', this.handleScroll)
    document.addEventListener('click', this.handleOutsideClick)
    document.addEventListener('keydown', this.handleEscKey)
    
    // 不再监听页面离开事件，取消停留时间统计
  },
  beforeUnmount() {
    // 恢复原来的viewport设置
    this.restorePageScale()
    
    window.removeEventListener('mousemove', this.handleMouseMove)
    window.removeEventListener('resize', this.handleResize)
    window.removeEventListener('scroll', this.handleScroll)
    document.removeEventListener('click', this.handleOutsideClick)
    document.removeEventListener('keydown', this.handleEscKey)
    
    // 不再需要移除页面离开事件监听器
    
    // 不在这里记录页面离开事件，因为Vue组件卸载不等于用户真正离开页面
    // 只通过beforeunload事件来记录真正的页面离开
    
    if (this.tickerInterval) {
      clearInterval(this.tickerInterval)
    }
  },
  methods: {
    filterSystems() {
      if (!this.searchQuery.trim()) {
        this.filteredSystems = [];
        return;
      }
      
      const query = this.searchQuery.toLowerCase().trim();
      this.filteredSystems = this.systems.filter(system => {
        return (
          system.category.toLowerCase().includes(query) ||
          system.name.toLowerCase().includes(query) ||
          system.description.toLowerCase().includes(query)
        );
      }).slice(0, 5); // Limit to first 5 results
    },
    handleBlur() {
      // Don't hide dropdown immediately to allow click events to fire
      setTimeout(() => {
        this.showDropdown = false
      }, 200)
    },
    handleOutsideClick(event) {
      // 检查搜索框的点击
      const searchContainer = document.querySelector('.search-container')
      if (searchContainer && !searchContainer.contains(event.target)) {
        this.showDropdown = false
      }
      
      // 检查收藏夹的点击
      if (this.$refs.favoritesWrapper && !this.$refs.favoritesWrapper.contains(event.target)) {
        this.showFavoritesDropdown = false
      }
    },
    handleScroll() {
      // Toggle fixed header visibility based on scroll position
      this.isScrolled = window.scrollY > 150;
    },
    adjustTitleDecoration() {
      // Set timeout to ensure DOM is fully rendered
      setTimeout(() => {
        const titleElement = document.querySelector('.gradient-text')
        const decorationElement = document.querySelector('.title-decoration')
        
        if (titleElement && decorationElement) {
          const titleWidth = titleElement.offsetWidth
          decorationElement.style.width = `${titleWidth}px`
        }
      }, 100)
    },
    initParticleNetwork() {
      const container = this.$refs.particleNetwork
      if (!container) return
      
      const width = window.innerWidth
      const height = window.innerHeight
      const particleCount = Math.floor((width * height) / 10000)
      
      // Create animated particles
      for (let i = 0; i < particleCount; i++) {
        const particle = document.createElement('div')
        particle.className = 'network-particle'
        
        // Random position
        const posX = Math.random() * width
        const posY = Math.random() * height
        
        // Random size
        const size = Math.random() * 3 + 1
        
        // Set styles
        particle.style.left = `${posX}px`
        particle.style.top = `${posY}px`
        particle.style.width = `${size}px`
        particle.style.height = `${size}px`
        
        // Create connections between particles
        if (i > 0 && i % 3 === 0 && i < particleCount - 1) {
          const connection = document.createElement('div')
          connection.className = 'particle-connection'
          
          // Set random connection
          const angle = Math.random() * Math.PI * 2
          const length = Math.random() * 150 + 50
          
          connection.style.width = `${length}px`
          connection.style.left = `${posX}px`
          connection.style.top = `${posY}px`
          connection.style.transform = `rotate(${angle}rad)`
          
          container.appendChild(connection)
        }
        
        // Add to container
        container.appendChild(particle)
      }
    },
    startTicker() {
      const ticker = this.$refs.ticker
      if (!ticker) return
      
      // Clear any existing interval
      if (this.tickerInterval) {
        clearInterval(this.tickerInterval)
      }
      
      let currentPos = 0
      const moveAmount = 1
      
      this.tickerInterval = setInterval(() => {
        const announcements = ticker.querySelectorAll('span')
        if (announcements.length === 0) return
        
        currentPos -= moveAmount
        const firstAnnouncement = announcements[0]
        
        if (Math.abs(currentPos) >= firstAnnouncement.offsetWidth + 20) {
          ticker.appendChild(firstAnnouncement)
          currentPos = 0
        }
        
        ticker.style.transform = `translateX(${currentPos}px)`
      }, 30)
    },
    handleMouseMove(event) {
      const { clientX, clientY } = event
      const centerX = window.innerWidth / 2
      const centerY = window.innerHeight / 2
      
      // Calculate distance from center (normalized)
      const moveX = (clientX - centerX) / centerX
      const moveY = (clientY - centerY) / centerY
      
      // Apply parallax effect to background elements
      const glowCircles = document.querySelectorAll('.glow-circle')
      glowCircles.forEach(circle => {
        const offsetX = moveX * 20
        const offsetY = moveY * 20
        circle.style.transform = `translate(${offsetX}px, ${offsetY}px)`
      })
      
      // Move grid background slightly
      const grid = document.querySelector('.grid-overlay')
      if (grid) {
        grid.style.transform = `translate(${moveX * 10}px, ${moveY * 10}px)`
      }
    },
    handleResize() {
      // Reinitialize particle network on window resize
      const container = this.$refs.particleNetwork
      if (container) {
        container.innerHTML = ''
        this.initParticleNetwork()
      }
      
      // Re-adjust title decoration on resize
      this.adjustTitleDecoration()
    },
    handleCardClick(event, url) {
      event.preventDefault();
      
      // Find the system based on URL
      const system = this.systems.find(s => s.url === url);
      
      // 记录系统点击事件
      if (system) {
        this.recordSystemClick(system);
      }
      
      if (this.enableFlowerTransition && system) {
        this.showFlowerTransition(system);
      } else {
        // Skip flower transition and go directly to URL
        window.open(url, '_blank');
      }
    },
    showFlowerTransition(system) {
      // Skip all animation logic if transitions are disabled
      if (!this.enableFlowerTransition) {
        // If flower transition is disabled, just open the URL directly
        if (system && system.url) {
          window.open(system.url, '_blank');
        }
        return;
      }
      
      this.currentSystem = system;
      this.pendingUrl = system.url;
      
      // Reset search state
      this.searchQuery = '';
      this.filteredSystems = [];
      this.showDropdown = false;
      
      // Show fullscreen transition
      const transition = this.$refs.fullscreenTransition;
      if (transition) {
        // Show the overlay
        transition.style.display = 'flex';
        
        // Start animation
        setTimeout(() => {
          transition.classList.add('active');
          
          // Wait for animation to complete before redirecting
          setTimeout(() => {
            // Navigate to the URL
            if (this.pendingUrl) {
              window.open(this.pendingUrl, '_blank');
              this.pendingUrl = null;
            }
            
            // Reset after a delay
            setTimeout(() => {
              transition.classList.remove('active');
              
              setTimeout(() => {
                transition.style.display = 'none';
                this.currentSystem = null;
                
                // Re-focus the search input
                const searchInput = document.querySelector('.search-input');
                if (searchInput) {
                  searchInput.focus();
                }
              }, 300);
            }, 800);
          }, 3000);
        }, 50);
      } else {
        // Fallback
        window.open(system.url, '_blank');
      }
    },
    getRandomColor(index) {
      return this.flowerColors[index % this.flowerColors.length];
    },
    getCategoryChineseName(category) {
      // Extract the Chinese part of the category name (before the parenthesis)
      const match = category.match(/^(.*?)\s*\(/);
      return match ? match[1].trim() : category;
    },
    getCategoryEnglishName(category) {
      // Extract the English part of the category name (within the parentheses)
      const match = category.match(/\((.*?)\)/);
      return match ? match[1].trim() : '';
    },
    getCategoryIcon(category) {
      // Define icons for each category
      const iconMap = {
        '人事系统 (HR System)': 'fas fa-user-tie',
        '财务系统 (COR System)': 'fas fa-coins',
        '采购系统 (Procurement System)': 'fas fa-shopping-cart',
        '研发系统 (R&D System)': 'fas fa-flask',
        '物流系统 (Logistics System)': 'fas fa-truck',
        '质量系统 (Quality System)': 'fas fa-check-circle',
        '生产系统 (Production System)': 'fas fa-gear',
        'IT系统 (IT System)': 'fas fa-laptop-code'
      };
      
      return iconMap[category] || 'fas fa-folder';
    },
    
    // Dify模态框处理方法
    openDifyModal() {
      // 固定的Dify系统配置
      this.currentDifySystem = {
        name: 'SEG-AIBot',
        icon: 'fas fa-robot',
        url: 'http://csd22602:8089/chat/6e55ev5WVAzUrj3d'
      };
      this.showDifyModal = true;
      // 防止背景滚动
      document.body.style.overflow = 'hidden';
    },
    
    closeDifyModal() {
      this.showDifyModal = false;
      this.currentDifySystem = null;
      // 恢复背景滚动
      document.body.style.overflow = 'auto';
    },
    
    handleEscKey(event) {
      if (event.key === 'Escape' && this.showDifyModal) {
        this.closeDifyModal();
      }
    },
    // Favorites management
    toggleFavoritesDropdown() {
      this.showFavoritesDropdown = !this.showFavoritesDropdown;
    },
    
    addFavorite(system) {
      if (!this.isFavorite(system)) {
        // Clone the system object to avoid reference issues
        const systemToAdd = {
          name: system.name,
          url: system.url,
          category: system.category,
          icon: system.icon,
          description: system.description
        };
        this.favorites.push(systemToAdd);
        this.saveFavorites();
        alert(`已添加"${system.name}"到收藏夹`);
      } else {
        this.removeFavorite(system);
      }
    },
    
    removeFavorite(system) {
      const index = this.favorites.findIndex(fav => fav.url === system.url);
      if (index !== -1) {
        const name = this.favorites[index].name;
        this.favorites.splice(index, 1);
        this.saveFavorites();
        alert(`已从收藏夹移除"${name}"`);
      }
    },
    
    isFavorite(system) {
      return this.favorites.some(fav => fav.url === system.url);
    },
    
    saveFavorites() {
      try {
        localStorage.setItem('seg-onelink-favorites', JSON.stringify(this.favorites));
      } catch (error) {
        console.error('Failed to save favorites:', error);
      }
    },
    
    loadFavorites() {
      try {
        const favorites = localStorage.getItem('seg-onelink-favorites');
        if (favorites) {
          this.favorites = JSON.parse(favorites);
        }
      } catch (error) {
        console.error('Failed to load favorites:', error);
        this.favorites = [];
      }
    },
    
    // 数据统计相关方法
    initAnalytics() {
      // 生成会话ID
      this.sessionId = this.generateSessionId();
      // 记录页面浏览事件
      this.recordPageView();
    },
    
    generateSessionId() {
      const timestamp = Date.now();
      const randomStr = Math.random().toString(36).substr(2, 9);
      const sessionId = 'session_' + timestamp + '_' + randomStr;
      
      // 确保会话ID不超过64个字符
      if (sessionId.length > 64) {
        return sessionId.substring(0, 64);
      }
      
      return sessionId;
    },
    
    async recordAnalyticsEvent(eventType, systemName = null, systemUrl = null) {
      try {
        // 基本验证
        if (!this.sessionId || !eventType) {
          console.warn('会话ID或事件类型为空，跳过记录');
          return;
        }
        
        // 验证事件类型（只保留页面浏览和系统点击）
        const validEventTypes = ['page_view', 'system_click'];
        if (!validEventTypes.includes(eventType)) {
          console.warn('无效的事件类型:', eventType);
          return;
        }
        
        // 对于系统点击事件，系统名称不能为空
        if (eventType === 'system_click' && (!systemName || systemName.trim() === '')) {
          console.warn('系统点击事件的系统名称为空');
          return;
        }
        
        const data = {
          sessionId: this.sessionId,
          eventType: eventType,
          systemName: systemName,
          systemUrl: systemUrl,
          stayDuration: null // 不再记录停留时间
        };
        
        await httpUtil.post('/sysAnalyticsLog/record', data, {
          headers: {
            'Content-Type': 'application/json'
          }
        });
        
        console.log('成功记录事件:', eventType, systemName || '');
      } catch (error) {
        console.error('记录分析事件失败:', error);
      }
    },
    
    recordPageView() {
      this.recordAnalyticsEvent('page_view');
    },
    
    recordSystemClick(system) {
      this.recordAnalyticsEvent('system_click', system.name, system.url);
    },
    
    // 加载系统数据
    async loadSystemsFromDatabase() {
      try {
        const response = await httpUtil.get('/sysOnelinkSystem/active')
        if (response.data && response.data.list) {
          // 将数据库中的系统数据转换为前端需要的格式
          this.systems = response.data.list.map(system => ({
            category: system.categoryName,
            name: system.systemName,
            description: system.systemDescription,
            url: system.systemUrl,
            icon: system.systemIcon
          }))
        }
      } catch (error) {
        console.error('加载系统数据失败:', error)
        // 如果加载失败，保持使用硬编码的数据作为备用
        console.log('使用默认系统数据')
      }
    },
    
    // 设置页面缩放比例
    setPageScale() {
      // 保存原来的viewport设置
      const viewport = document.querySelector('meta[name="viewport"]')
      if (viewport) {
        this.originalViewport = viewport.getAttribute('content')
      }
      
      // 检查是否已经存在viewport meta标签
      if (viewport) {
        // 如果存在，更新其content
        viewport.setAttribute('content', 'width=device-width, initial-scale=0.8, maximum-scale=1.0, user-scalable=yes')
      } else {
        // 如果不存在，创建新的viewport meta标签
        const newViewport = document.createElement('meta')
        newViewport.name = 'viewport'
        newViewport.content = 'width=device-width, initial-scale=0.8, maximum-scale=1.0, user-scalable=yes'
        document.getElementsByTagName('head')[0].appendChild(newViewport)
      }
    },
    
    // 恢复原来的viewport设置
    restorePageScale() {
      const viewport = document.querySelector('meta[name="viewport"]')
      if (viewport && this.originalViewport) {
        viewport.setAttribute('content', this.originalViewport)
      }
    },
    
    expandCategory(category) {
      this.expandedCategory = category;
      this.isDetailView = true;
    },
    backToMainView() {
      this.expandedCategory = null;
      this.isDetailView = false;
    }
  }
}
</script>

<style scoped>
/* Import Montserrat font */
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;700;900&display=swap');

/* Base container */
.tech-container {
  position: relative;
  min-height: 100vh;
  width: 100%;
  overflow: hidden;
  background-color: #f8f9fa;
  color: #333;
  font-family: 'Inter', 'Roboto', sans-serif;
}

/* Animated background elements */
.tech-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  overflow: hidden;
}

.glow-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 0;
  transition: transform 0.5s ease-out;
}

.top-left {
  top: -150px;
  left: -150px;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(0, 83, 137, 0.2) 0%, rgba(0, 83, 137, 0.1) 50%, rgba(0, 83, 137, 0) 80%);
}

.bottom-right {
  bottom: -200px;
  right: -200px;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(2, 145, 101, 0.2) 0%, rgba(2, 145, 101, 0.1) 50%, rgba(2, 145, 101, 0) 80%);
}

.particle-network {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2;
}

.network-particle {
  position: absolute;
  background-color: rgba(0, 83, 137, 0.3);
  border-radius: 50%;
  animation: pulse 3s infinite ease-in-out;
}

.particle-connection {
  position: absolute;
  height: 1px;
  background: linear-gradient(90deg, rgba(0, 83, 137, 0.3), rgba(0, 83, 137, 0.1));
  transform-origin: left center;
  opacity: 0.3;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.3;
  }
  50% {
    transform: scale(1.3);
    opacity: 0.5;
  }
}

/* Content wrapper */
.content-wrapper {
  position: relative;
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 50px 40px;
  z-index: 10;
}

/* SEG Logo container */
.seg-logo-container {
  position: absolute;
  top: 25px;
  left: 40px;
  z-index: 20;
}

.seg-logo {
  width: 160px;
  height: auto;
  object-fit: contain;
}

/* Search container */
.search-container {
  position: absolute;
  top: 30px;
  right: 40px;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 15px;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 30px;
  padding: 8px 15px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 83, 137, 0.1);
  transition: all 0.3s ease;
  height: 38px;
}

.search-box:focus-within {
  box-shadow: 0 5px 20px rgba(0, 83, 137, 0.2);
  border-color: rgba(0, 83, 137, 0.3);
}

.search-input {
  width: 220px;
  padding: 8px 5px;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: #333;
  font-family: 'Inter', 'Roboto', sans-serif;
}

.search-input::placeholder {
  color: #888;
  opacity: 0.8;
}

.search-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 5px;
  margin-left: 5px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.search-button:hover {
  background-color: rgba(0, 83, 137, 0.1);
}

.search-button i {
  font-size: 16px;
  color: #005389;
}

/* Search Results Dropdown */
.search-results {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 5px;
  background-color: white;
  border-radius: 10px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(0, 83, 137, 0.15);
  overflow: hidden;
  z-index: 100;
  max-height: 350px;
  overflow-y: auto;
  backdrop-filter: blur(10px);
}

.search-result-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.search-result-item:last-child {
  border-bottom: none;
}

.search-result-item:hover {
  background-color: rgba(0, 83, 137, 0.05);
}

.search-result-text {
  flex-grow: 1;
  overflow: hidden;
}

.search-result-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.result-name {
  font-size: 16px;
  color: #005389;
}

/* Responsive styles for search */
@media (max-width: 768px) {
  .search-container {
    position: relative;
    top: 0;
    right: 0;
    width: 100%;
    margin-bottom: 20px;
    display: flex;
    justify-content: center;
    gap: 12px;
  }
  
  .search-box {
    width: 100%;
    max-width: 400px;
  }
  
  .search-input {
    width: 100%;
  }
  
  .search-results {
    max-width: 400px;
    margin: 5px auto 0;
    left: 50%;
    transform: translateX(-50%);
    width: calc(100% - 40px);
  }
}

@media (max-width: 480px) {
  .search-container {
    gap: 10px;
  }
}

/* Header section */
.header-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 25px;
  margin-top: 40px;
  text-align: center;
}

.title-with-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 8px;
}

.main-title {
  font-size: 54px;
  font-weight: 900;
  letter-spacing: 2px;
  margin: 0;
  color: #333;
  line-height: 1;
  position: relative;
  overflow: hidden;
  text-align: center;
  font-family: 'Montserrat', sans-serif;
}

.gradient-text {
  background: linear-gradient(90deg, #005389, #029165, #005389);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  background-size: 200% auto;
  animation: textGradient 5s ease infinite;
  font-family: 'Montserrat', sans-serif;
}

@keyframes textGradient {
  0% {
    background-position: 0% center;
  }
  50% {
    background-position: 100% center;
  }
  100% {
    background-position: 0% center;
  }
}

.title-decoration {
  width: auto;
  max-width: 280px;
  height: 3px;
  background: linear-gradient(90deg, #005389, #029165);
  margin: 8px auto 10px;
  position: relative;
  overflow: hidden;
}

.title-decoration::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.8), transparent);
  animation: shine 3s infinite;
}

@keyframes shine {
  0% {
    left: -100%;
  }
  100% {
    left: 100%;
  }
}

/* Announcement ticker */
.announcement-ticker {
  display: flex;
  align-items: center;
  background: linear-gradient(to right, rgba(0, 83, 137, 0.08), rgba(2, 145, 101, 0.08));
  border-radius: 8px;
  padding: 15px 20px;
  margin-bottom: 60px;
  border: 1px solid rgba(0, 83, 137, 0.15);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.ticker-label {
  background: linear-gradient(135deg, #005389, #029165);
  color: white;
  padding: 5px 15px;
  border-radius: 4px;
  margin-right: 20px;
  font-weight: 600;
  font-size: 14px;
  white-space: nowrap;
  flex-shrink: 0;
  box-shadow: 0 2px 5px rgba(0, 83, 137, 0.2);
}

.ticker-wrapper {
  overflow: hidden;
  position: relative;
  flex: 1;
}

.ticker-wrapper::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 70px;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(0, 83, 137, 0.05) 40%, rgba(2, 145, 101, 0.08) 100%);
  z-index: 1;
}

.ticker-content {
  display: flex;
  white-space: nowrap;
  font-size: 15px;
}

.ticker-content span {
  margin-right: 50px;
  color: #005389;
  padding: 4px 0;
  font-weight: 500;
}

/* Footer */
.tech-footer {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 100px;
  padding-bottom: 50px;
  text-align: center;
  position: relative;
}

.footer-logo-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  padding: 8px 20px;
  border-radius: 30px;
  background: linear-gradient(135deg, rgba(0, 83, 137, 0.1), rgba(2, 145, 101, 0.1));
}

.footer-logo-gradient {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 30px;
  background: linear-gradient(90deg, rgba(0, 83, 137, 0.1), rgba(2, 145, 101, 0.1));
  filter: blur(10px);
  opacity: 0.5;
  z-index: -1;
}

.footer-title {
  font-weight: 700;
  font-size: 22px;
  letter-spacing: 1px;
  background: linear-gradient(90deg, #005389, #029165);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  padding: 5px 0;
  font-family: 'Montserrat', sans-serif;
}

.copyright {
  color: #005389;
  font-size: 14px;
  opacity: 0.8;
  font-weight: 500;
}

/* Fixed header */
.fixed-header {
  position: fixed;
  top: -80px;
  left: 0;
  width: 100%;
  background-color: rgba(255, 255, 255, 0.95);
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  transition: top 0.3s ease-in-out;
  opacity: 0;
  padding: 8px 0;
}

.fixed-header.scrolled {
  top: 0;
  opacity: 1;
}

.fixed-header-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  padding: 0 20px;
}

.fixed-header-inner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  width: 100%;
}

.fixed-logo {
  display: flex;
  align-items: center;
}

.fixed-logo-img {
  height: 40px;
  width: auto;
  object-fit: contain;
}

.fixed-title-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.fixed-title {
  font-size: 28px;
  font-weight: 900;
  margin: 0;
  font-family: 'Montserrat', sans-serif;
  line-height: 1;
}

.fixed-decoration {
  width: 120px;
  height: 3px;
  background: linear-gradient(90deg, #005389, #029165);
  margin-top: 5px;
  position: relative;
  overflow: hidden;
}

.fixed-decoration::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.8), transparent);
  animation: shine 3s infinite;
}

/* Departments row */
.departments-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

/* Category sections */
.category-sections {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 40px;
}

.category-section {
  background-color: rgba(255, 255, 255, 0.7);
  border-radius: 12px;
  padding: 15px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 83, 137, 0.1);
  height: 260px; /* Increased height for larger cards */
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  z-index: 1;
}

.category-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 16px;
  padding: 1px;
  background: linear-gradient(145deg, rgba(0, 83, 137, 0.3), rgba(2, 145, 101, 0.3));
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  z-index: -1;
  opacity: 0.6;
}

.category-section::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: repeating-linear-gradient(
    45deg,
    rgba(0, 83, 137, 0.03) 0,
    rgba(0, 83, 137, 0.03) 5px,
    rgba(2, 145, 101, 0.03) 5px,
    rgba(2, 145, 101, 0.03) 10px
  );
  transform: rotate(30deg);
  z-index: -1;
  opacity: 0.5;
}

.category-section:hover {
  box-shadow: 0 15px 35px rgba(0, 83, 137, 0.1);
  border-color: rgba(0, 83, 137, 0.2);
}

.category-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  position: relative;
  gap: 10px;
}

.category-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #005389, #029165);
  box-shadow: 0 4px 10px rgba(0, 83, 137, 0.2);
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.category-icon::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, 
    #005389 0%, 
    transparent 40%, 
    transparent 60%, 
    #029165 100%);
  border-radius: 12px;
  z-index: -1;
  animation: borderRotate 4s linear infinite;
}

@keyframes borderRotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.category-icon i {
  font-size: 16px;
  color: white;
  filter: drop-shadow(0 2px 3px rgba(0, 0, 0, 0.2));
}

.category-header::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, rgba(0, 83, 137, 0.3), rgba(2, 145, 101, 0.3), transparent);
}

.category-title {
  font-size: 16px;
  font-weight: 700;
  margin: 0;
  letter-spacing: 0.5px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.category-zh {
  font-size: 16px;
  margin-bottom: 1px;
}

.gradient-category {
  background: linear-gradient(90deg, #005389, #029165, #005389);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  background-size: 200% auto;
  animation: textGradient 5s ease infinite;
}

.category-en {
  color: #666;
  font-size: 12px;
  font-weight: 500;
}

.system-list {
  display: flex;
  flex-direction: column;
  height: calc(100% - 50px);
  overflow: hidden; /* Remove scrollbars */
}

.system-item {
  position: relative;
  display: flex;
  align-items: center;
  height: 20%; /* Each system takes up exactly 20% of the system-list height */
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  border-bottom: 1px solid rgba(0, 83, 137, 0.1);
  overflow: hidden;
  margin: 0 -5px;
  padding-left: 5px;
  padding-right: 34px; /* Space for favorite star */
  border-radius: 4px;
}

.system-item:last-child {
  border-bottom: none;
}

.system-item:hover {
  background-color: rgba(0, 83, 137, 0.05);
  padding-left: 10px;
  box-shadow: 0 2px 5px rgba(0, 83, 137, 0.05);
}

.system-item::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(
    to right,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.3) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  transform: skewX(-25deg);
  transition: all 0.75s ease;
}

.system-item:hover::after {
  left: 150%;
}

.system-number {
  font-size: 14px;
  font-weight: 700;
  color: #005389;
  margin-right: 8px;
  min-width: 22px;
}

.system-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

/* Fullscreen Flower Transition */
.fullscreen-flower-transition {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 10000;
  background-color: rgba(255, 255, 255, 0.96);
  display: none;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  opacity: 0;
  transition: opacity 0.4s ease;
  overflow: hidden;
}

.fullscreen-flower-transition.active {
  opacity: 1;
}

/* Flower Rings Container */
.flower-rings-container {
  position: absolute;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

/* Flower Rings */
.flower-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: scale(0.2);
}

.fullscreen-flower-transition.active .ring-large {
  animation: ring-appear 1.5s ease forwards, ring-rotate 30s linear infinite;
}

.fullscreen-flower-transition.active .ring-medium {
  animation: ring-appear 1.5s ease forwards, ring-rotate-reverse 25s linear infinite;
}

.fullscreen-flower-transition.active .ring-small {
  animation: ring-appear 1.5s ease forwards, ring-rotate 20s linear infinite;
}

@keyframes ring-appear {
  0% {
    opacity: 0;
    transform: scale(0.2);
  }
  70% {
    opacity: 1;
    transform: scale(1.05);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes ring-rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@keyframes ring-rotate-reverse {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(-360deg);
  }
}

/* Individual Flower */
.flower {
  position: absolute;
  transform-origin: center bottom;
}

.large-flower {
  width: 120px;
  height: 120px;
}

.medium-flower {
  width: 100px;
  height: 100px;
}

.small-flower {
  width: 80px;
  height: 80px;
}

.flower-center {
  position: absolute;
  width: 30%;
  height: 30%;
  background-color: #FFD700; /* Gold center */
  border-radius: 50%;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1;
}

.flower-petal {
  position: absolute;
  width: 45%;
  height: 45%;
  background-color: var(--color);
  border-radius: 50% 50% 50% 50%;
  top: 10%;
  left: 27.5%;
  transform-origin: bottom center;
  z-index: 0;
}

/* Content Styling */
.transition-content {
  position: relative;
  z-index: 10001;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transform: scale(0);
  opacity: 0;
  background-color: rgba(255, 255, 255, 0.9);
  padding: 30px;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.fullscreen-flower-transition.active .transition-content {
  animation: content-appear 1.8s cubic-bezier(0.34, 1.56, 0.64, 1) forwards 0.3s;
}

@keyframes content-appear {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.transition-icon {
  width: 120px;
  height: 120px;
  border-radius: 24px;
  background: linear-gradient(135deg, #005389, #029165);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 25px;
  box-shadow: 0 15px 30px rgba(0, 83, 137, 0.3);
  transform: translateY(20px);
  opacity: 0;
}

.fullscreen-flower-transition.active .transition-icon {
  animation: slide-up-fade 1s ease forwards 0.8s;
}

.transition-details {
  text-align: center;
  transform: translateY(20px);
  opacity: 0;
}

.fullscreen-flower-transition.active .transition-details {
  animation: slide-up-fade 1s ease forwards 1s;
}

@keyframes slide-up-fade {
  0% {
    transform: translateY(20px);
    opacity: 0;
  }
  100% {
    transform: translateY(0);
    opacity: 1;
  }
}

.transition-icon i {
  font-size: 50px;
  color: white;
}

.transition-title {
  font-weight: 700;
  font-size: 28px;
  margin-bottom: 10px;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
}

.transition-category {
  color: #333;
}

.transition-divider {
  margin: 0 10px;
  color: #bbb;
}

.transition-name {
  color: #005389;
  font-weight: 600;
}

.transition-description {
  font-size: 18px;
  color: #666;
  max-width: 500px;
  text-align: center;
}

/* Responsive styles */
@media (max-width: 1400px) {
  .departments-row {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 1200px) {
  .departments-row {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .category-section {
    height: 260px;
  }
}

@media (max-width: 768px) {
  .departments-row {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .category-section {
    height: 260px;
  }
  
  .main-title {
    font-size: 42px;
  }
  
  .content-wrapper {
    padding: 40px 20px;
  }
  
  .seg-logo {
    width: 140px;
  }
  
  .fixed-logo-img {
    height: 30px;
  }
  
  .fixed-title {
    font-size: 24px;
  }
  
  .system-number {
    font-size: 15px;
  }
  
  .system-name {
    font-size: 15px;
  }
  
  .category-zh {
    font-size: 16px;
  }
  
  .category-en {
    font-size: 13px;
  }
  
  .large-flower {
    width: 90px;
    height: 90px;
  }
  
  .medium-flower {
    width: 75px;
    height: 75px;
  }
  
  .small-flower {
    width: 60px;
    height: 60px;
  }

  .transition-icon {
    width: 100px;
    height: 100px;
  }
  
  .transition-icon i {
    font-size: 40px;
  }
  
  .transition-title {
    font-size: 24px;
  }
  
  .transition-description {
    font-size: 16px;
    max-width: 90%;
  }
}

@media (max-width: 480px) {
  .main-title {
    font-size: 36px;
  }
  
  .system-number {
    font-size: 14px;
    min-width: 22px;
  }
  
  .system-name {
    font-size: 14px;
  }
  
  .category-zh {
    font-size: 15px;
  }
  
  .category-en {
    font-size: 12px;
  }
}

/* Prominent Favorites Button */
.favorites-wrapper {
  position: relative;
  margin-left: 10px;
  z-index: 1001;
  transition: opacity 0.3s ease, visibility 0.3s ease;
  visibility: visible;
  opacity: 1;
}

.hide-favorites {
  visibility: hidden;
  opacity: 0;
}

.my-favorites-button {
  position: relative;
  background: linear-gradient(135deg, #005389, #029165);
  color: white;
  border: none;
  border-radius: 50%;
  width: 44px;
  height: 44px;
  padding: 0;
  font-size: 20px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(0, 83, 137, 0.3);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  padding-top: 2px;
}

.my-favorites-button:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(0, 83, 137, 0.4);
}

/* Favorites dropdown */
.favorites-dropdown {
  position: absolute;
  top: 45px;
  right: 0;
  width: 280px;
  background-color: white;
  border-radius: 10px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(0, 83, 137, 0.15);
  overflow: hidden;
  z-index: 1001;
}

.favorites-list {
  max-height: 300px;
  overflow-y: auto;
}

.empty-favorites {
  padding: 20px;
  text-align: center;
  color: #666;
}

.favorite-item {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.favorite-item:last-child {
  border-bottom: none;
}

.favorite-item:hover {
  background-color: rgba(0, 83, 137, 0.05);
}

.favorite-name {
  flex-grow: 1;
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.favorite-remove {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  color: #666;
  font-size: 18px;
}

.favorite-remove:hover {
  background-color: rgba(255, 0, 0, 0.1);
  color: #ff3333;
}

/* Star icon in system items */
.favorite-star-icon {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 22px;
  cursor: pointer;
  text-shadow: 0 0 3px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, #29B6F6, #26A69A);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.add-favorite-button {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  color: #005389;
  background-color: rgba(0, 83, 137, 0.1);
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: none;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
}

.system-item:hover .add-favorite-button {
  display: flex;
}

/* Responsive adjustments for favorites */
@media (max-width: 768px) {
  .my-favorites-button {
    width: 40px;
    height: 40px;
    font-size: 18px;
  }
  
  .favorites-dropdown {
    width: 250px;
  }
}

@media (max-width: 480px) {
  .my-favorites-button {
    width: 36px;
    height: 36px;
    font-size: 16px;
  }
  
  .favorites-dropdown {
    top: 65px;
    right: 45px;
  }
}

/* 查看更多按钮样式 */
.view-more-button {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 20%;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #005389;
  font-weight: 500;
  font-size: 14px;
  border-top: 1px solid rgba(0, 83, 137, 0.1);
  margin: 0 -5px;
  padding: 0 5px;
  border-radius: 0 0 4px 4px;
  background: linear-gradient(135deg, rgba(0, 83, 137, 0.02), rgba(2, 145, 101, 0.02));
}

.view-more-button:hover {
  background: linear-gradient(135deg, rgba(0, 83, 137, 0.08), rgba(2, 145, 101, 0.08));
  color: #029165;
  transform: translateY(-1px);
}

.view-more-button i {
  margin-right: 8px;
  font-size: 12px;
  transition: transform 0.3s ease;
}

.view-more-button:hover i {
  transform: translateX(3px);
}

/* 返回按钮样式 */
.back-button-container {
  margin-bottom: 30px;
  display: flex;
  justify-content: flex-start;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 10px;
  background: linear-gradient(135deg, #005389, #029165);
  color: white;
  border: none;
  border-radius: 25px;
  padding: 12px 24px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 83, 137, 0.2);
}

.back-button:hover {
  background: linear-gradient(135deg, #0068ab, #02a674);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 83, 137, 0.3);
}

.back-button i {
  font-size: 14px;
  transition: transform 0.3s ease;
}

.back-button:hover i {
  transform: translateX(-3px);
}

/* 展开分类视图样式 */
.expanded-category-view {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.expanded-category-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 40px;
  padding: 30px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(248, 249, 250, 0.9));
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 83, 137, 0.1);
  backdrop-filter: blur(10px);
}

.expanded-category-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  border-radius: 20px;
  background: linear-gradient(135deg, #005389, #029165);
  box-shadow: 0 8px 25px rgba(0, 83, 137, 0.3);
  position: relative;
  overflow: hidden;
}

.expanded-category-icon::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, 
    #005389 0%, 
    transparent 40%, 
    transparent 60%, 
    #029165 100%);
  border-radius: 22px;
  z-index: -1;
  animation: borderRotate 4s linear infinite;
}

.expanded-category-icon i {
  font-size: 36px;
  color: white;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.expanded-category-title {
  flex: 1;
}

.expanded-category-zh {
  font-size: 36px;
  font-weight: 900;
  margin: 0 0 8px 0;
  font-family: 'Montserrat', sans-serif;
}

.expanded-category-en {
  font-size: 18px;
  color: #666;
  margin: 0 0 12px 0;
  font-weight: 500;
}

.expanded-category-count {
  font-size: 14px;
  color: #005389;
  background: rgba(0, 83, 137, 0.1);
  padding: 6px 12px;
  border-radius: 15px;
  display: inline-block;
  font-weight: 500;
}

/* 展开系统网格样式 */
.expanded-systems-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.expanded-system-card {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 15px;
  padding: 20px;
  text-decoration: none;
  color: inherit;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 83, 137, 0.1);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
}

.expanded-system-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 15px;
  padding: 1px;
  background: linear-gradient(145deg, rgba(0, 83, 137, 0.2), rgba(2, 145, 101, 0.2));
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  z-index: -1;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.expanded-system-card:hover::before {
  opacity: 1;
}

.expanded-system-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 83, 137, 0.15);
  border-color: rgba(0, 83, 137, 0.2);
}

.expanded-system-number {
  font-size: 24px;
  font-weight: 700;
  color: #005389;
  margin-right: 20px;
  min-width: 40px;
  text-align: center;
  background: linear-gradient(135deg, rgba(0, 83, 137, 0.1), rgba(2, 145, 101, 0.1));
  border-radius: 10px;
  padding: 10px 5px;
}

.expanded-system-content {
  flex: 1;
  margin-right: 15px;
}

.expanded-system-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
  line-height: 1.3;
}

.expanded-system-description {
  font-size: 14px;
  color: #666;
  line-height: 1.4;
}

.expanded-system-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
}

.expanded-favorite-star {
  font-size: 24px;
  cursor: pointer;
  text-shadow: 0 0 3px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, #29B6F6, #26A69A);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  transition: transform 0.2s ease;
}

.expanded-favorite-star:hover {
  transform: scale(1.1);
}

.expanded-add-favorite {
  color: #005389;
  background-color: rgba(0, 83, 137, 0.1);
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s ease;
  opacity: 0;
}

.expanded-system-card:hover .expanded-add-favorite {
  opacity: 1;
}

.expanded-add-favorite:hover {
  background-color: rgba(0, 83, 137, 0.2);
  transform: scale(1.1);
}

/* 响应式样式 */
@media (max-width: 1200px) {
  .expanded-systems-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 15px;
  }
}

@media (max-width: 768px) {
  .expanded-category-header {
    flex-direction: column;
    text-align: center;
    gap: 15px;
    padding: 20px;
  }
  
  .expanded-category-icon {
    width: 60px;
    height: 60px;
    border-radius: 15px;
  }
  
  .expanded-category-icon i {
    font-size: 28px;
  }
  
  .expanded-category-zh {
    font-size: 28px;
  }
  
  .expanded-category-en {
    font-size: 16px;
  }
  
  .expanded-systems-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .expanded-system-card {
    padding: 15px;
  }
  
  .expanded-system-number {
    font-size: 20px;
    margin-right: 15px;
    min-width: 35px;
    padding: 8px 4px;
  }
  
  .expanded-system-name {
    font-size: 16px;
  }
  
  .expanded-system-description {
    font-size: 13px;
  }
  
  .back-button {
    padding: 10px 20px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .expanded-category-zh {
    font-size: 24px;
  }
  
  .expanded-category-en {
    font-size: 14px;
  }
  
  .expanded-system-card {
    padding: 12px;
  }
  
  .expanded-system-number {
    font-size: 18px;
    margin-right: 12px;
    min-width: 30px;
    padding: 6px 3px;
  }
  
  .expanded-system-name {
    font-size: 15px;
  }
  
  .expanded-system-description {
    font-size: 12px;
  }
}

/* Dify模态框样式 */
.dify-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  backdrop-filter: blur(5px);
  animation: modalFadeIn 0.3s ease-out;
}

@keyframes modalFadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.dify-modal-content {
  background: white;
  border-radius: 12px;
  width: 98%;
  height: 95%;
  max-width: 1600px;
  max-height: 1000px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.dify-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 16px;
  border-bottom: 1px solid #e5e7eb;
  background: linear-gradient(135deg, #005389 0%, #029165 100%);
  color: white;
}

.dify-modal-header h3 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 12px;
}

.dify-modal-header h3 i {
  font-size: 20px;
}

.dify-close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
}

.dify-close-btn:hover {
  background-color: rgba(255, 255, 255, 0.2);
  transform: scale(1.1);
}

.dify-modal-body {
  flex: 1;
  padding: 0;
  overflow: hidden;
  position: relative;
}

.dify-modal-body iframe {
  width: 100%;
  height: 100%;
  border: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dify-modal-content {
    width: 99%;
    height: 97%;
    margin: 5px;
  }
  
  .dify-modal-header {
    padding: 10px 16px;
  }
  
  .dify-modal-header h3 {
    font-size: 20px;
  }
}

/* 平板设备 */
@media (max-width: 1024px) and (min-width: 769px) {
  .dify-modal-content {
    width: 96%;
    height: 92%;
  }
}

/* 超小屏幕 */
@media (max-width: 480px) {
  .dify-modal-content {
    width: 100%;
    height: 99%;
    margin: 2px;
  }
  
  .dify-modal-header {
    padding: 8px 12px;
  }
  
  .dify-modal-header h3 {
    font-size: 18px;
    gap: 8px;
  }
  
  .dify-close-btn {
    width: 36px;
    height: 36px;
    font-size: 20px;
  }
}

/* AI助手按钮组样式 */
.ai-assistant-group {
  position: relative;
  display: flex;
  transition: all 0.3s ease;
}

.ai-assistant-group.hide-ai-assistant {
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
}

.ai-assistant-button {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #005389, #029165);
  border: none;
  border-radius: 50%;
  color: white;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(0, 83, 137, 0.3);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.ai-assistant-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: left 0.5s ease;
}

.ai-assistant-button:hover::before {
  left: 100%;
}

.ai-assistant-button:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(0, 83, 137, 0.4);
  background: linear-gradient(135deg, #0277BD, #00ACC1);
}

.ai-assistant-button:active {
  transform: scale(0.95);
}

.ai-assistant-button i {
  animation: robotPulse 2s infinite ease-in-out;
}



@keyframes robotPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

/* 响应式适配 */
@media (max-width: 768px) {
  .ai-assistant-button {
    width: 40px;
    height: 40px;
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .ai-assistant-button {
    width: 36px;
    height: 36px;
    font-size: 14px;
  }
}
</style> 