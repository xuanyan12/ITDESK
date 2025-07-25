<script setup>
import {useUserInfoStore} from "@/stores/_frame/userInfoStore";
import {onMounted, ref, computed} from "vue";
import GenerateUtil from "../../utils/GenerateUtil";
import httpUtil from "@/utils/HttpUtil";
import { Monitor, Refresh, House, Notebook, Collection, Operation, User, Bell, Calendar, Check } from '@element-plus/icons-vue';
import { useLanguageStore } from '@/stores/_frame/languageStore';
import { useRouter } from 'vue-router';

const userInfoStore = useUserInfoStore()
const languageStore = useLanguageStore();
const currentLang = computed(() => languageStore.currentLang);
const router = useRouter();

// Language text definitions
const t = computed(() => ({
  myDevices: currentLang.value === 'en' ? 'My Computers' : '我的电脑',
  managedDevices: currentLang.value === 'en' ? 'Cost Center Computers' : '成本中心电脑',
  costCenterLabel: currentLang.value === 'en' ? 'Cost Center:' : '成本中心：',
  costCenterFilter: currentLang.value === 'en' ? 'Filter by Cost Center' : '按成本中心筛选',
  allCostCenters: currentLang.value === 'en' ? 'All Cost Centers' : '全部成本中心',
  deviceStatus: currentLang.value === 'en' ? 'Status:' : '设备状态:',
  deviceType: currentLang.value === 'en' ? 'Type:' : '设备类型:',
  unknown: currentLang.value === 'en' ? 'Unknown' : '未知',
  noDevices: currentLang.value === 'en' ? 'No computers found' : '暂无电脑信息',
  noManagedDevices: currentLang.value === 'en' ? 'No cost center computers found' : '暂无成本中心电脑',
  
  // Descriptions translations
  model: currentLang.value === 'en' ? 'Model' : '型号',
  cpu: currentLang.value === 'en' ? 'CPU' : 'CPU',
  memory: currentLang.value === 'en' ? 'Memory' : '内存',
  disk: currentLang.value === 'en' ? 'Disk' : '硬盘',
  serialNumber: currentLang.value === 'en' ? 'Serial Number' : '序列号',
  manufacturer: currentLang.value === 'en' ? 'Manufacturer' : '制造商',
  productionDate: currentLang.value === 'en' ? 'Production Date' : '出厂日期',
  hardwareStatus: currentLang.value === 'en' ? 'Hardware Status' : '硬件状态',
  graphic: currentLang.value === 'en' ? 'Graphics Card' : '显卡',
  ntAccount: currentLang.value === 'en' ? 'NT Account' : 'NT账号',
  pcClass: currentLang.value === 'en' ? 'PC Classification' : '电脑归属',
  comment: currentLang.value === 'en' ? 'Comment' : '备注',
  department: currentLang.value === 'en' ? 'Department' : '部门',
  yearsToDay: currentLang.value === 'en' ? 'Years in Use' : '使用年限',
  purchaseInfo: currentLang.value === 'en' ? 'Purchase Info' : '采购信息',
  pr: currentLang.value === 'en' ? 'PR' : '下单号',
  po: currentLang.value === 'en' ? 'PO' : '订单号',
  vendor: currentLang.value === 'en' ? 'Vendor' : '供应商',
  company: currentLang.value === 'en' ? 'Company' : '公司',
  wbsNumber: currentLang.value === 'en' ? 'WBS Number' : 'WBS号',
  price: currentLang.value === 'en' ? 'Price' : '价格',
  tempAssignment: currentLang.value === 'en' ? 'Temporary Assignment' : '临时分配',
  yes: currentLang.value === 'en' ? 'Yes' : '是',
  no: currentLang.value === 'en' ? 'No' : '否',
  basicInfo: currentLang.value === 'en' ? 'Basic Information' : '基本信息',
  hardwareInfo: currentLang.value === 'en' ? 'Hardware Information' : '硬件信息',
  userInfo: currentLang.value === 'en' ? 'User Information' : '用户信息',
  name: currentLang.value === 'en' ? 'Name' : '姓名',
  costCenter: currentLang.value === 'en' ? 'Cost Center' : '成本中心',
  email: currentLang.value === 'en' ? 'Email' : '邮箱',
  phone: currentLang.value === 'en' ? 'Phone' : '联系电话',
  commentTab: currentLang.value === 'en' ? 'Comment' : '备注',
  approvalReminders: currentLang.value === 'en' ? 'Approval Reminders' : '待办提醒',
  pendingApprovals: currentLang.value === 'en' ? 'You have {count} pending approvals' : '您有 {count} 条待处理的审批',
  goToApprove: currentLang.value === 'en' ? 'Go to Approve' : '去审批',
  allApproved: currentLang.value === 'en' ? 'Great! You have processed all approvals, with a total of {count} processed.' : '太棒了，你已经处理了所有审批流程，迄今共处理 {count} 条',
  noPendingTasks: currentLang.value === 'en' ? 'No pending tasks today' : '今日暂无待办事项',
  todayTasks: currentLang.value === 'en' ? 'Today\'s Tasks' : '今日任务'
}));

onMounted(() => {
    // 加载欢迎信息
    loadGreeting()
    // 加载用户电脑信息
    loadUserComputers()
    // 加载审批信息
    loadApprovalCounts()
    // 加载审批人管理的设备信息
    loadApproverManagedComputers()
})

// 欢迎
const greeting = ref('')
const welcomes = ref([])

// 审批信息
const approvalData = ref({
    isApprover: false,
    pendingCount: 0,
    processedCount: 0
});

/**
 * 加载欢迎信息
 */
const loadGreeting = () => {
    greeting.value = GenerateUtil.greeting()
    const allWelcomes = GenerateUtil.getRandomTexts()
    // 只取第一段话
    welcomes.value = allWelcomes.length > 0 ? [allWelcomes[0]] : []
}

/**
 * 加载审批数量信息
 */
const loadApprovalCounts = () => {
    httpUtil.get("/sysApply/getApprovalCounts").then(res => {
        if (res.data) {
            approvalData.value = res.data;
        }
    }).catch(err => {
        console.error("获取审批数量信息失败:", err);
    });
}

/**
 * 跳转到审批页面
 */
const goToApprovalPage = () => {
    router.push('/sys/device/approval');
}

// 用户电脑信息
const computerList = ref([])
const loading = ref(false)

// 审批人管理的设备信息
const approverComputers = ref([])
const isApprover = ref(false)
const approverCostCenters = ref([])
const loadingApproverComputers = ref(false)
const approverDevicesTotal = ref(0)
const approverDevicesPageNum = ref(1)
const approverDevicesPageSize = ref(5)

// 成本中心筛选
const selectedCostCenter = ref('')

/**
 * 格式化日期，移除T00:00部分
 */
const formatDate = (dateString) => {
    if (!dateString) return '';
    // 如果包含T，则去除T及之后的内容
    if (dateString.includes('T')) {
        return dateString.split('T')[0];
    }
    return dateString;
}

/**
 * 加载用户电脑信息
 */
const loadUserComputers = () => {
    loading.value = true
    httpUtil.get("/sysControl/getComputerListByUserName", {
        params: { userName: userInfoStore.userInfo.userName }
    }).then(res => {
        if (res.data && res.data.list) {
            // 从服务器获取的是电脑名称列表，需要获取每台电脑的详细信息
            const computerNames = res.data.list;
            const promises = [];
            
            // 对每台电脑发送请求获取详细信息
            for (const ciName of computerNames) {
                if (typeof ciName === 'string') {
                    const promise = httpUtil.get("/sysControl/getComputerInfoByCiName", {
                        params: { ciName: ciName }
                    });
                    promises.push(promise);
                }
            }
            
            // 等待所有请求完成
            Promise.all(promises)
                .then(responses => {
                    const computers = responses
                        .filter(res => res && res.data)
                        .map(res => {
                            const computer = res.data;
                            // 格式化日期
                            if (computer.lifeCycleStart) {
                                computer.lifeCycleStart = formatDate(computer.lifeCycleStart);
                            }
                            return computer;
                        });
                    
                    computerList.value = computers;
                })
                .catch(err => {
                    console.error("获取电脑详细信息失败:", err);
                })
                .finally(() => {
                    loading.value = false;
                });
        } else {
            computerList.value = [];
            loading.value = false;
        }
    }).catch(err => {
        console.error("获取用户电脑信息失败:", err);
        loading.value = false;
    })
}

/**
 * 加载审批人管理的设备信息
 */
const loadApproverManagedComputers = () => {
    loadingApproverComputers.value = true
    const params = { 
        userName: userInfoStore.userInfo.userName,
        pageNum: approverDevicesPageNum.value,
        pageSize: approverDevicesPageSize.value
    };
    
    // 添加成本中心筛选参数
    if (selectedCostCenter.value && selectedCostCenter.value !== 'all') {
        params.costCenterFilter = selectedCostCenter.value;
    }
    
    httpUtil.get("/sysControl/getApproverManagedComputers", {
        params: params
    }).then(res => {
        if (res.data) {
            isApprover.value = res.data.isApprover || false;
            approverCostCenters.value = res.data.costCenters || [];
            approverDevicesTotal.value = res.data.total || 0;
            
            if (res.data.managedComputers && res.data.managedComputers.length > 0) {
                // 获取每台设备的详细信息
                const promises = [];
                
                for (const item of res.data.managedComputers) {
                    const computer = item.computer;
                    const managedCostCenter = item.managedCostCenter;
                    
                    if (computer && computer.ciName) {
                        const promise = httpUtil.get("/sysControl/getComputerInfoByCiName", {
                            params: { ciName: computer.ciName }
                        }).then(response => {
                            if (response && response.data) {
                                const computerDetail = response.data;
                                // 格式化日期
                                if (computerDetail.lifeCycleStart) {
                                    computerDetail.lifeCycleStart = formatDate(computerDetail.lifeCycleStart);
                                }
                                // 添加管理的成本中心标识
                                computerDetail.managedCostCenter = managedCostCenter;
                                return computerDetail;
                            }
                            return null;
                        });
                        promises.push(promise);
                    }
                }
                
                // 等待所有请求完成
                Promise.all(promises)
                    .then(computers => {
                        approverComputers.value = computers.filter(computer => computer !== null);
                    })
                    .catch(err => {
                        console.error("获取审批人管理设备详细信息失败:", err);
                    })
                    .finally(() => {
                        loadingApproverComputers.value = false;
                    });
            } else {
                approverComputers.value = [];
                loadingApproverComputers.value = false;
            }
        } else {
            isApprover.value = false;
            approverComputers.value = [];
            approverDevicesTotal.value = 0;
            loadingApproverComputers.value = false;
        }
    }).catch(err => {
        console.error("获取审批人管理设备信息失败:", err);
        isApprover.value = false;
        approverComputers.value = [];
        approverDevicesTotal.value = 0;
        loadingApproverComputers.value = false;
    })
}

/**
 * 处理成本中心筛选变化
 */
const handleCostCenterFilterChange = () => {
    approverDevicesPageNum.value = 1; // 重置到第一页
    loadApproverManagedComputers();
}

/**
 * 处理审批人设备分页变化
 */
const handleApproverDevicesPageChange = (page) => {
    approverDevicesPageNum.value = page;
    loadApproverManagedComputers();
}

/**
 * 处理审批人设备每页大小变化
 */
const handleApproverDevicesPageSizeChange = (size) => {
    approverDevicesPageSize.value = size;
    approverDevicesPageNum.value = 1; // 重置到第一页
    loadApproverManagedComputers();
}
</script>

<template>
    <div class="welcome_panel">
        <el-row>
            <el-col :span="24">
                <div class="tech-card">
                    <div class="card-content">
                        <div class="logo-container">
                            <img src="/SEG-logo.png" alt="SEG Logo" class="seg-logo" />
                        </div>
                        <div class="greeting-container">
                            <h2 class="greeting-text">
                                {{ greeting }}，{{ userInfoStore.userInfo.userNick }}
                                <el-button class="refresh-btn" @click="loadGreeting">
                                    <el-icon :size="20">
                                        <Refresh/>
                                    </el-icon>
                                </el-button>
                            </h2>
                            <div class="welcome-messages">
                                <p v-for="text in welcomes" class="welcome-message">
                                    {{ text }}
                                </p>
                            </div>
                        </div>
                        <div class="approval-reminders">
                            <div class="reminders-header">
                                <el-icon :size="20"><Calendar /></el-icon>
                                <span>{{ t.todayTasks }}</span>
                                <el-button 
                                    type="text" 
                                    class="refresh-approvals" 
                                    @click="loadApprovalCounts" 
                                    title="刷新">
                                    <el-icon><Refresh /></el-icon>
                                </el-button>
                            </div>
                            <div class="reminders-content">
                                <div v-if="approvalData.isApprover && approvalData.pendingCount > 0" class="pending-approvals">
                                    <div class="pending-badge">
                                        <span class="badge-count">{{ approvalData.pendingCount }}</span>
                                    </div>
                                    <div class="pending-info">
                                        <p>{{ t.pendingApprovals.replace('{count}', approvalData.pendingCount) }}</p>
                                        <el-button type="primary" size="small" @click="goToApprovalPage" class="approve-btn">
                                            <el-icon><Bell /></el-icon>
                                            {{ t.goToApprove }}
                                        </el-button>
                                    </div>
                                </div>
                                <div v-else-if="approvalData.isApprover" class="all-approved">
                                    <div class="approved-icon">
                                        <el-icon :size="30"><Check /></el-icon>
                                    </div>
                                    <p>{{ t.allApproved.replace('{count}', approvalData.processedCount) }}</p>
                                </div>
                                <div v-else class="no-pending-tasks">
                                    <div class="no-tasks-icon">
                                        <el-icon :size="30"><Calendar /></el-icon>
                                    </div>
                                    <p>{{ t.noPendingTasks }}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </el-col>
        </el-row>

        <!-- 用户电脑信息和审批人管理的设备信息 -->
        <el-row style="margin-top: 20px;" :gutter="20">
        <!-- 用户电脑信息 -->
            <el-col :span="isApprover ? 12 : 24" :xs="24" :sm="24" :md="isApprover ? 12 : 24">
                <div class="computer-info-card">
                        <div class="card-header">
                        <h3 class="card-title">
                            <el-icon :size="22" class="header-icon"><Monitor /></el-icon>
                            {{ t.myDevices }}
                        </h3>
                        <el-button type="primary" circle size="small" @click="loadUserComputers" :loading="loading">
                            <el-icon><Refresh /></el-icon>
                        </el-button>
                    </div>
                    
                    <div v-if="loading" class="loading-container">
                        <el-skeleton :rows="3" animated />
                    </div>
                    
                    <div v-else-if="computerList.length === 0" class="empty-container">
                        <el-empty :description="t.noDevices" />
                    </div>
                    
                    <div v-else class="computer-list">
                        <div v-for="(computer, index) in computerList" :key="index" class="computer-item">
                            <div class="computer-icon">
                                <el-icon :size="36">
                                    <component :is="computer.deviceClass === 'Laptop' ? 'Monitor' : 'Monitor'" />
                                </el-icon>
                            </div>
                            <div class="computer-details">
                                <div class="computer-header">
                                    <div class="header-info">
                                        <div class="computer-name">{{ computer.ciName }}</div>
                                        <div class="user-device-info">
                                            <span v-if="computer.firstName || computer.lastName" class="user-name">
                                                <el-icon><User /></el-icon>
                                                {{ computer.lastName }}{{ computer.firstName ? ' ' + computer.firstName : '' }}
                                            </span>
                                            <span v-if="computer.deviceClass" class="device-class">
                                                <el-icon><Monitor /></el-icon>
                                                {{ computer.deviceClass }}
                                            </span>
                                        </div>
                                    </div>
                                    <el-tag :type="computer.pcClass === 'Internal User' ? 'success' : 'warning'" size="small">
                                        {{ computer.pcStatus }}
                                    </el-tag>
                        </div>
                                
                                <el-descriptions :column="2" size="small" border class="mt-10">
                                    <el-descriptions-item v-if="computer.modelOrVersion" :label="t.model">
                                        <el-icon><House /></el-icon>
                                        <span>{{ computer.modelOrVersion }}</span>
                                    </el-descriptions-item>
                                    <el-descriptions-item v-if="computer.lifeCycleStart" :label="t.productionDate">
                                        <span>{{ computer.lifeCycleStart }}</span>
                                    </el-descriptions-item>
                                    <el-descriptions-item v-if="computer.manufacture" :label="t.manufacturer">
                                        <span>{{ computer.manufacture }}</span>
                                    </el-descriptions-item>
                                    <el-descriptions-item v-if="computer.pcClass" :label="t.pcClass">
                                        <span>{{ computer.pcClass }}</span>
                                    </el-descriptions-item>
                                </el-descriptions>
                            </div>
                        </div>
                    </div>
                </div>
            </el-col>

            <!-- 审批人管理的设备信息 -->
            <el-col v-if="isApprover" :span="12" :xs="24" :sm="24" :md="12" style="margin-top: 20px;" class="managed-devices-col">
                <div class="computer-info-card">
                    <div class="card-header">
                        <h3 class="card-title">
                            <el-icon :size="22" class="header-icon"><Collection /></el-icon>
                            {{ t.managedDevices }}
                        </h3>
                        <div class="header-actions">
                            <el-select 
                                v-model="selectedCostCenter" 
                                :placeholder="t.costCenterFilter" 
                                size="small" 
                                style="width: 180px; margin-right: 10px;"
                                @change="handleCostCenterFilterChange"
                                clearable>
                                <el-option :label="t.allCostCenters" value="all"></el-option>
                                <el-option 
                                    v-for="costCenter in approverCostCenters" 
                                    :key="costCenter" 
                                    :label="costCenter" 
                                    :value="costCenter">
                                </el-option>
                            </el-select>
                            <el-button type="primary" circle size="small" @click="loadApproverManagedComputers" :loading="loadingApproverComputers">
                                <el-icon><Refresh /></el-icon>
                            </el-button>
                        </div>
                    </div>
                    
                    <div v-if="loadingApproverComputers" class="loading-container">
                        <el-skeleton :rows="3" animated />
                    </div>
                    
                    <div v-else-if="approverComputers.length === 0" class="empty-container">
                        <el-empty :description="t.noManagedDevices" />
                    </div>
                    
                    <div v-else>
                        <div class="computer-list">
                            <div v-for="(computer, index) in approverComputers" :key="index" class="computer-item">
                                <div class="computer-icon">
                                    <el-icon :size="36">
                                        <component :is="computer.deviceClass === 'Laptop' ? 'Notebook' : 'Monitor'" />
                                    </el-icon>
                                </div>
                                <div class="computer-details">
                                    <div class="computer-header">
                                        <div class="header-info">
                                            <div class="computer-name">{{ computer.ciName }}</div>
                                            <div class="user-device-info">
                                                <span v-if="computer.firstName || computer.lastName" class="user-name">
                                                    <el-icon><User /></el-icon>
                                                    {{ computer.lastName }}{{ computer.firstName ? ' ' + computer.firstName : '' }}
                                                </span>
                                                <span v-if="computer.deviceClass" class="device-class">
                                                    <el-icon><Monitor /></el-icon>
                                                    {{ computer.deviceClass }}
                                                </span>
                                                <span v-if="computer.managedCostCenter" class="cost-center-tag">
                                                    <el-icon><Operation /></el-icon>
                                                    {{ t.costCenterLabel }} {{ computer.managedCostCenter }}
                                                </span>
                                            </div>
                                        </div>
                                        <el-tag :type="computer.pcClass === 'Internal User' ? 'success' : 'warning'" size="small">
                                            {{ computer.pcStatus }}
                                        </el-tag>
                                    </div>
                                    
                                    <el-descriptions :column="2" size="small" border class="mt-10">
                                        <el-descriptions-item v-if="computer.modelOrVersion" :label="t.model">
                                            <el-icon><House /></el-icon>
                                            <span>{{ computer.modelOrVersion }}</span>
                                        </el-descriptions-item>
                                        <el-descriptions-item v-if="computer.lifeCycleStart" :label="t.productionDate">
                                            <span>{{ computer.lifeCycleStart }}</span>
                                        </el-descriptions-item>
                                        <el-descriptions-item v-if="computer.manufacture" :label="t.manufacturer">
                                            <span>{{ computer.manufacture }}</span>
                                        </el-descriptions-item>
                                        <el-descriptions-item v-if="computer.pcClass" :label="t.pcClass">
                                            <span>{{ computer.pcClass }}</span>
                                        </el-descriptions-item>
                                    </el-descriptions>
                                </div>
                            </div>
                        </div>
                        
                        <!-- 分页组件 -->
                        <div class="pagination-container">
                            <el-pagination
                                v-model:current-page="approverDevicesPageNum"
                                v-model:page-size="approverDevicesPageSize"
                                :page-sizes="[5, 10, 20, 50]"
                                :total="approverDevicesTotal"
                                layout="total, sizes, prev, pager, next, jumper"
                                @size-change="handleApproverDevicesPageSizeChange"
                                @current-change="handleApproverDevicesPageChange"
                                background
                                small
                            />
                        </div>
                    </div>
                </div>
            </el-col>
        </el-row>
    </div>
</template>

<style scoped>
.welcome_panel {
    padding: 20px;
}

.tech-card {
    position: relative;
    background: linear-gradient(135deg, #ffffff, #f0f5fa);
    border-radius: 12px;
    box-shadow: 0 8px 30px rgba(10, 84, 139, 0.15);
    overflow: hidden;
    border-top: none;
    padding: 30px;
    transition: all 0.3s ease;
    position: relative;
}

.tech-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, 
        rgb(10, 84, 139), 
        rgb(0, 150, 136), 
        rgb(10, 84, 139), 
        rgb(0, 150, 136));
    background-size: 300% 100%;
    z-index: 2;
    animation: borderFlow 6s linear infinite;
}

@keyframes borderFlow {
    0% {
        background-position: 0% 0%;
    }
    100% {
        background-position: 100% 0%;
    }
}

.tech-card:hover {
    box-shadow: 0 12px 40px rgba(10, 84, 139, 0.25);
}

.tech-card:hover::before {
    background: linear-gradient(90deg, rgb(0, 150, 136), rgb(10, 84, 139));
}

.card-content {
    display: flex;
    z-index: 2;
    position: relative;
    justify-content: center;
    align-items: center;
}

.logo-container {
    width: 180px;
    display: flex;
    justify-content: center;
    align-items: center;
    padding-right: 30px;
}

.seg-logo {
    width: 200px;
    max-height: 140px;
    object-fit: contain;
}

.greeting-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.greeting-text {
    color: rgb(10, 84, 139);
    font-weight: 700;
    font-size: 28px;
    margin: 0 0 10px 0;
    display: flex;
    align-items: center;
}

.refresh-btn {
    margin-left: 10px;
    color: rgb(10, 84, 139);
    transition: all 0.3s ease;
}

.refresh-btn:hover {
    color: rgb(0, 150, 136);
    transform: rotate(180deg);
}

.welcome-messages {
    color: #2c3e50;
    margin: 0;
}

.welcome-message {
    margin: 0;
    line-height: 1.5;
    font-size: 22px;
    position: relative;
    padding-left: 0;
}

.welcome-message::before {
    display: none;
}

.tech-decoration {
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    z-index: 1;
    overflow: hidden;
    opacity: 0.12;
    pointer-events: none;
    transition: opacity 0.3s ease;
}

.tech-card:hover .tech-decoration {
    opacity: 0.18;
}

.tech-circle {
    position: absolute;
    border-radius: 50%;
    border: 2px solid transparent;
    background-clip: padding-box;
    position: relative;
}

.tech-circle::before {
    content: '';
    position: absolute;
    top: -2px;
    left: -2px;
    right: -2px;
    bottom: -2px;
    border-radius: 50%;
    background: linear-gradient(135deg, 
        rgb(10, 84, 139), 
        rgb(0, 150, 136), 
        rgb(10, 84, 139));
    background-size: 300% 100%;
    z-index: -1;
    animation: circleFlow 8s linear infinite;
}

@keyframes circleFlow {
    0% {
        background-position: 0% 0%;
    }
    100% {
        background-position: 100% 0%;
    }
}

.circle-1 {
    width: 80px;
    height: 80px;
    top: -40px;
    right: 10%;
}

.circle-2 {
    width: 150px;
    height: 150px;
    bottom: -60px;
    right: -60px;
}

.circle-3 {
    width: 40px;
    height: 40px;
    top: 30%;
    left: 15%;
    background: rgba(10, 84, 139, 0.2);
    border: none;
}

.tech-line {
    position: absolute;
    background: linear-gradient(90deg, 
        rgb(10, 84, 139), 
        rgb(0, 150, 136),
        rgb(10, 84, 139), 
        rgb(0, 150, 136));
    background-size: 300% 100%;
    animation: lineFlow 4s linear infinite;
}

@keyframes lineFlow {
    0% {
        background-position: 0% 0%;
    }
    100% {
        background-position: 100% 0%;
    }
}

.line-1 {
    width: 120px;
    height: 2px;
    transform: rotate(45deg);
    top: 20%;
    left: -20px;
}

.line-2 {
    width: 80px;
    height: 2px;
    transform: rotate(-30deg);
    bottom: 30%;
    right: 20%;
}

.el-row .el-col {
    padding-bottom: 20px;
}

@media (max-width: 768px) {
    .card-content {
        flex-direction: column;
    }

    .logo-container {
        width: 100%;
        margin-bottom: 20px;
        padding-right: 0;
    }

    .greeting-text {
        text-align: center;
        justify-content: center;
    }

    .computer-item {
        flex-direction: column;
    }

    .computer-icon {
        margin-right: 0;
        margin-bottom: 12px;
        width: 100%;
    }

    .computer-meta {
        flex-direction: column;
    }

    .meta-item {
        margin-bottom: 8px;
    }
}

/* Computer info card styles */
.computer-info-card {
    background: linear-gradient(135deg, #ffffff, #f0f5fa);
    border-radius: 12px;
    box-shadow: 0 8px 30px rgba(10, 84, 139, 0.15);
    overflow: hidden;
    border-top: none;
    padding: 0;
    transition: all 0.3s ease;
    position: relative;
}

.computer-info-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, 
        rgb(0, 150, 136),
        rgb(10, 84, 139),
        rgb(0, 150, 136),
        rgb(10, 84, 139));
    background-size: 300% 100%;
    z-index: 2;
    animation: borderFlow 6s linear infinite;
    animation-direction: reverse;
}

.card-header {
    padding: 16px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid rgba(10, 84, 139, 0.1);
}

.header-actions {
    display: flex;
    align-items: center;
}

.card-title {
    margin: 0;
    color: rgb(10, 84, 139);
    font-size: 18px;
    display: flex;
    align-items: center;
}

.header-icon {
    margin-right: 8px;
}

.loading-container, .empty-container {
    padding: 30px;
    display: flex;
    justify-content: center;
}

.computer-list {
    padding: 16px;
}

.computer-item {
    display: flex;
    padding: 14px;
    margin-bottom: 10px;
    background: rgba(255, 255, 255, 0.7);
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(10, 84, 139, 0.08);
    transition: all 0.2s ease;
}

.computer-item:hover {
    box-shadow: 0 6px 16px rgba(10, 84, 139, 0.15);
    transform: translateY(-2px);
}

.computer-item:last-child {
    margin-bottom: 0;
}

.computer-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 70px;
    height: 70px;
    border-radius: 8px;
    margin-right: 16px;
    background: linear-gradient(135deg, rgba(10, 84, 139, 0.1), rgba(0, 150, 136, 0.1));
    color: rgb(10, 84, 139);
}

.computer-details {
    flex: 1;
}

.computer-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 8px;
}

.header-info {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
}

.computer-name {
    font-size: 18px;
    font-weight: 500;
    color: rgb(10, 84, 139);
    margin-bottom: 4px;
}

.user-device-info {
    display: flex;
    align-items: center;
    font-size: 14px;
    color: #606266;
}

.user-name, .device-class {
    display: flex;
    align-items: center;
    margin-right: 12px;
}

.user-name .el-icon, .device-class .el-icon {
    margin-right: 4px;
    font-size: 14px;
}

.user-name {
    color: rgb(1, 149, 109);
}

.device-class {
    color: #909399;
}

.cost-center-tag {
    color: rgb(10, 84, 139);
    font-weight: 500;
    background: rgba(10, 84, 139, 0.1);
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 12px;
}

.cost-center-tag .el-icon {
    margin-right: 4px;
    font-size: 12px;
}

.computer-item .el-descriptions__title {
    margin-top: 0 !important;
    font-size: 14px !important;
    line-height: 1.4 !important;
    margin-bottom: 8px !important;
}

.computer-meta, .meta-item, .meta-label, .computer-specs, .spec-item {
    display: none; /* 不再使用的样式 */
}

.computer-item .el-tabs__header,
.computer-item .el-tabs__item.is-active,
.computer-item .el-tabs__active-bar,
.computer-item .el-tabs--border-card>.el-tabs__header .el-tabs__item.is-active {
    display: none; /* 不再使用的tab样式 */
}

.mt-10 {
    margin-top: 8px;
}

.el-descriptions-item__content {
    display: flex;
    align-items: center;
    padding: 6px 10px !important;
}

.el-descriptions-item__label {
    padding: 6px 10px !important;
    font-weight: normal;
}

.el-descriptions-item__content .el-icon {
    margin-right: 5px;
    color: rgb(0, 150, 136);
}

.approval-reminders {
    width: 280px;
    padding: 20px;
    background: rgba(10, 84, 139, 0.05);
    border-radius: 10px;
    margin-left: 20px;
    border-left: 1px solid rgba(10, 84, 139, 0.1);
    position: relative;
    box-shadow: 0 4px 12px rgba(10, 84, 139, 0.06);
}

.reminders-header {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    color: rgb(10, 84, 139);
    font-weight: 500;
    font-size: 16px;
    border-bottom: 1px solid rgba(10, 84, 139, 0.1);
    padding-bottom: 10px;
}

.reminders-header .el-icon {
    margin-right: 8px;
    color: rgb(0, 150, 136);
}

.refresh-approvals {
    margin-left: auto;
    color: rgba(10, 84, 139, 0.6);
    transition: all 0.3s ease;
}

.refresh-approvals:hover {
    color: rgb(0, 150, 136);
    transform: rotate(180deg);
}

.reminders-content {
    color: #2c3e50;
    display: flex;
    flex-direction: column;
    justify-content: center;
    min-height: 80px;
}

.pending-approvals {
    display: flex;
    align-items: center;
    background: rgba(10, 84, 139, 0.08);
    border-radius: 8px;
    padding: 12px;
    margin-bottom: 10px;
}

.pending-badge {
    background: rgb(10, 84, 139);
    width: 40px;
    height: 40px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 14px;
    flex-shrink: 0;
    box-shadow: 0 2px 6px rgba(10, 84, 139, 0.2);
}

.badge-count {
    color: white;
    font-weight: bold;
    font-size: 18px;
}

.pending-info {
    flex: 1;
}

.pending-info p {
    margin: 0 0 10px 0;
    color: rgb(10, 84, 139);
    font-weight: 500;
}

.approve-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: rgb(0, 150, 136);
    border-color: rgb(0, 150, 136);
}

.approve-btn:hover {
    background-color: rgba(0, 150, 136, 0.8);
    border-color: rgba(0, 150, 136, 0.8);
}

.approve-btn .el-icon {
    margin-right: 5px;
}

.all-approved, .no-pending-tasks {
    display: flex;
    align-items: center;
    border-radius: 8px;
    padding: 15px;
    text-align: center;
}

.all-approved {
    background: rgba(0, 150, 136, 0.08);
    flex-direction: column;
}

.no-pending-tasks {
    background: rgba(10, 84, 139, 0.05);
    flex-direction: column;
}

.approved-icon, .no-tasks-icon {
    margin-bottom: 10px;
    border-radius: 50%;
    width: 50px;
    height: 50px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.approved-icon {
    background: rgba(0, 150, 136, 0.15);
    color: rgb(0, 150, 136);
}

.no-tasks-icon {
    background: rgba(10, 84, 139, 0.1);
    color: rgb(10, 84, 139);
}

.all-approved p, .no-pending-tasks p {
    margin: 0;
    line-height: 1.5;
}

.all-approved p {
    color: rgb(0, 150, 136);
}

.no-pending-tasks p {
    color: rgb(10, 84, 139);
}

/* Responsive adjustments */
@media (max-width: 992px) {
    .card-content {
        flex-wrap: wrap;
    }
    
    .approval-reminders {
        width: 100%;
        margin-left: 0;
        margin-top: 20px;
        border-left: none;
        border-top: 1px solid rgba(10, 84, 139, 0.1);
    }
}

@media (max-width: 768px) {
    .card-content {
        flex-direction: column;
    }

    .logo-container {
        width: 100%;
        margin-bottom: 20px;
        padding-right: 0;
    }

    .greeting-text {
        text-align: center;
        justify-content: center;
    }
    
    .card-header {
        flex-direction: column;
        align-items: flex-start;
    }
    
    .header-actions {
        margin-top: 12px;
        width: 100%;
        justify-content: space-between;
    }
    
    .header-actions .el-select {
        width: calc(100% - 50px) !important;
        margin-right: 10px !important;
    }
}

.device-info-row {
    display: flex;
    flex-wrap: wrap;
    background: rgba(255, 255, 255, 0.7);
    border-radius: 6px;
    padding: 10px 15px;
    margin-top: 10px;
    box-shadow: 0 1px 3px rgba(10, 84, 139, 0.08);
    align-items: center;
}

.device-info-item {
    display: flex;
    align-items: center;
    margin-right: 20px;
    white-space: nowrap;
}

.info-label {
    color: rgb(10, 84, 139);
    font-size: 14px;
    margin-right: 5px;
}

.info-value {
    color: #606266;
    font-size: 14px;
}

/* Responsive adjustments for the device info row */
@media (max-width: 768px) {
    .device-info-row {
        flex-direction: column;
        align-items: flex-start;
    }
    
    .device-info-item {
        margin-right: 0;
        margin-bottom: 8px;
        width: 100%;
    }
}

.pagination-container {
    display: flex;
    justify-content: center;
    padding: 20px 0;
    border-top: 1px solid rgba(10, 84, 139, 0.1);
    margin-top: 16px;
}

.pagination-container .el-pagination {
    justify-content: center;
}

/* 响应式设计 */
@media (max-width: 992px) {
    .managed-devices-col {
        margin-top: 20px !important;
    }
}

@media (min-width: 993px) {
    .managed-devices-col {
        margin-top: 0 !important;
    }
}
</style>
