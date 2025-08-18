<template>
    <div style="padding: 20px;">
      <!-- 我的电脑 -->
      <el-card shadow="never" class="usr_card_override top">
        <div class="title-with-select">
          <h3>{{ t('我的电脑') }}</h3>
          <el-select 
            v-model="selectedComputer" 
            :placeholder="t('名下电脑选择')" 
            @change="handleComputerSelect"
            style="width: 200px">
            <el-option 
              v-for="item in computerList" 
              :key="item" 
              :label="item" 
              :value="item">
            </el-option>
          </el-select>
        </div>
        <div v-if="myComputer" class="my-computer-container">
          <el-descriptions :column="3" border size="small" class="elegant-descriptions" :label-width="120" :content-width="200">
            <el-descriptions-item :label="t('电脑名称')" width="300">{{ myComputer.ciName }}</el-descriptions-item>
            <el-descriptions-item :label="t('NT账号')" width="300">{{ myComputer.ntAccount }}</el-descriptions-item>
            <el-descriptions-item :label="t('部门号')" width="300">{{ myComputer.department }}</el-descriptions-item>
            <el-descriptions-item :label="t('姓')" width="300">{{ myComputer.lastName }}</el-descriptions-item>
            <el-descriptions-item :label="t('名')" width="300">{{ myComputer.firstName }}</el-descriptions-item>
            <el-descriptions-item :label="t('电脑使用状态')" width="300">{{ myComputer.pcStatus }}</el-descriptions-item>
            <el-descriptions-item :label="t('电脑归属情况')" v-if="myComputer.pcClass" width="300">{{ myComputer.pcClass }}</el-descriptions-item>
            <el-descriptions-item :label="t('电脑情况备注')" width="300">{{ myComputer.comment }}</el-descriptions-item>
            <el-descriptions-item :label="t('出厂时间')" width="300">{{ formatDate(myComputer.lifeCycleStart) }}</el-descriptions-item>
            <el-descriptions-item :label="t('制造商')" v-if="myComputer.manufacturer" width="300">{{ myComputer.manufacturer }}</el-descriptions-item>
            <el-descriptions-item :label="t('型号')" v-if="myComputer.model" width="300">{{ myComputer.model }}</el-descriptions-item>
            <el-descriptions-item :label="t('资产标签')" v-if="myComputer.assetTag" width="300">{{ myComputer.assetTag }}</el-descriptions-item>
            <el-descriptions-item :label="t('IP地址')" v-if="myComputer.ipAddress" width="300">{{ myComputer.ipAddress }}</el-descriptions-item>
            <el-descriptions-item :label="t('MAC地址')" v-if="myComputer.macAddress" width="300">{{ myComputer.macAddress }}</el-descriptions-item>
            <el-descriptions-item :label="t('操作系统')" v-if="myComputer.os" width="300">{{ myComputer.os }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <div v-else class="no-computer-data">
          <el-empty :description="t('暂无电脑信息')" />
        </div>
      </el-card>
  
      <!-- 维修申请表单 -->
      <el-card shadow="never" class="usr_card_override top">
        <h3>{{ t('维修申请') }}</h3>
        <el-form :model="maintenanceForm" ref="maintenanceFormRef" label-width="150px" class="maintenance-form" :rules="rules">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item :label="t('使用人')" prop="applicant">
                <el-autocomplete
                  v-model="maintenanceForm.applicant"
                  :fetch-suggestions="searchUsers"
                  :placeholder="t('请输入使用人')"
                  style="width: 100%"
                  @select="handleUserSelect"
                  @input="handleUserInput"
                  ref="userInputRef">
                  <template #default="{ item }">
                    <div class="user-suggestion-item">
                      <span>{{ item.value }}</span>
                      <small v-if="item.department">({{ item.department }})</small>
                    </div>
                  </template>
                </el-autocomplete>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item :label="t('电脑名称')" prop="computerName">
                <el-input
                  v-model="maintenanceForm.computerName"
                  :placeholder="t('请填写电脑名称')"
                  style="width: 100%"
                  :disabled="isComputerNameDisabled">
                </el-input>
              </el-form-item>
            </el-col>
  
            <el-col :span="12">
              <el-form-item :label="t('成本中心')" prop="costCenter">
                <el-select v-model="maintenanceForm.costCenter" :placeholder="t('请选择成本中心')" style="width: 100%" :disabled="isCostCenterDisabled">
                  <el-option v-for="center in costCenters" :key="center" :label="center" :value="center"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
  
            <el-col :span="12">
              <el-form-item :label="t('所属公司')" prop="company">
                <el-select v-model="maintenanceForm.company" :placeholder="t('请选择所属公司')" style="width: 100%" @change="handleCompanyChange">
                  <el-option v-for="company in companies" :key="company" :label="company" :value="company"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
  
            <el-col :span="12">
              <el-form-item :label="t('责任人')" prop="responsiblility">
                <el-select v-model="maintenanceForm.responsiblility" :placeholder="t('责任人将根据使用人自动填充')" style="width: 100%" :disabled="isResponsibleDisabled">
                  <el-option v-for="person in responsiblePersons" :key="person" :label="person" :value="person"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
  
            <el-col :span="12">
              <el-form-item :label="t('维修类别')" prop="fixCategory">
                <el-select 
                  v-model="maintenanceForm.fixCategory" 
                  :placeholder="t('请选择维修类别')" 
                  style="width: 100%" 
                  @change="handleMaintenanceTypeChange">
                  <el-option :label="t('质量问题维修')" value="qualityIssueRepair"></el-option>
                  <el-option :label="t('人为问题维修')" value="humanIssueRepair"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
  
            <el-col :span="24">
              <el-form-item :label="t('故障描述')" prop="problemDescription">
                <el-input
                  v-model="maintenanceForm.problemDescription"
                  type="textarea"
                  :rows="4"
                  :placeholder="t('请详细描述故障现象和问题')"
                  maxlength="500"
                  show-word-limit>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
  
          <el-form-item>
            <el-button type="primary" @click="submitMaintenance" :loading="loading">
              {{ t('提交维修申请') }}
            </el-button>
            <el-button @click="resetForm">{{ t('重置') }}</el-button>
          </el-form-item>
        </el-form>
      </el-card>
  
      <!-- 我的维修申请状态 -->
      <el-card shadow="never" class="usr_card_override top" style="margin-top: 20px;">
        <div class="table-header">
          <h3 class="section-title">{{ t('我的维修申请状态') }}</h3>
          <el-button type="primary" size="small" @click="getMaintenanceList">
            <i class="el-icon-refresh"></i> {{ t('刷新') }}
          </el-button>
        </div>
        <div class="table-container">
          <el-table 
            :data="maintenanceList" 
            :loading="loading" 
            style="width: 100%; overflow-x: auto;"
            border
            stripe
            row-key="maintenanceId"
            highlight-current-row
            :table-layout="'fixed'"
            class="maintenance-table">
            <el-table-column :label="t('申请时间')" prop="createdAt" width="160"></el-table-column>
            <el-table-column :label="t('电脑名')" prop="ciName" width="120">
              <template #default="{ row }">
                {{ row.ciName || '-' }}
              </template>
            </el-table-column>
            <el-table-column :label="t('使用人')" prop="userName" width="100">
              <template #default="{ row }">
                {{ row.userName || row.applicantName || '-' }}
              </template>
            </el-table-column>
            <el-table-column :label="t('责任人')" prop="responsibilityName" width="100">
              <template #default="{ row }">
                {{ row.responsibilityName || '-' }}
              </template>
            </el-table-column>
            <el-table-column :label="t('维修类别')" prop="fixCategory" width="120">
              <template #default="{ row }">
                {{ getMaintenanceTypeName(row.fixCategory) }}
              </template>
            </el-table-column>
            <el-table-column :label="t('故障描述')" prop="problemDescription" show-overflow-tooltip min-width="200">
              <template #default="{ row }">
                {{ row.problemDescription || row.deviceSituation || '-' }}
              </template>
            </el-table-column>
            <el-table-column :label="t('状态')" prop="status" width="130" align="center" fixed="right">
              <template #default="{ row }">
                <div style="text-align: center;">
                  <el-tag :type="statusTagType(row.status)" @click="viewMaintenanceProgress(row)">
                    {{ getLocalizedStatus(row.status) }}
                  </el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column :label="t('操作')" width="100" fixed="right" align="center">
              <template #default="{ row }">
                <div style="text-align: center; display: flex; justify-content: center;">
                  <el-button type="primary" text @click="viewMaintenanceProgress(row)">{{ t('查看进度') }}</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[5, 10, 20, 50]"
              :small="false"
              :disabled="loading"
              :background="true"
              layout="total, sizes, prev, pager, next, jumper"
              :total="totalItems"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              :prev-text="t('上一页')"
              :next-text="t('下一页')"
              :popper-class="'pagination-popper'"
              style="margin-top: 20px; text-align: right;">
            </el-pagination>
          </div>
        </div>
      </el-card>
  
      <!-- 维修进度弹窗 -->
      <el-dialog v-model="maintenanceProgressDialogVisible" :title="t('维修进度')" width="850px">
        <div v-if="maintenanceProgress" class="maintenance-progress-container custom-steps">
          <el-steps :active="maintenanceProgressStep" :process-status="getProcessStatus()" align-center>
            <el-step 
              v-for="(step, index) in maintenanceProgress" 
              :key="index" 
              :title="t(step.title)" 
              :status="getStepStatus(index)"
              :class="{'final-step': index === maintenanceProgress.length - 1}">
              <template #description>
                <div class="step-description">
                  <div>{{ step.description }}</div>
                  <div v-if="index === 1 && step.status" class="step-status" 
                    :class="{
                      'error-status': step.status === '审批不通过',
                      'success-status': step.status === '审批通过',
                      'warning-status': step.status.includes('上一级审批不通过')
                    }">
                    {{ t(step.status) }}
                  </div>
                  <div v-if="index === 2 && step.status" class="step-status" 
                    :class="{
                      'error-status': step.status === '审批不通过',
                      'success-status': step.status === '审批通过',
                      'warning-status': step.status.includes('上一级审批不通过')
                    }">
                    {{ t(step.status) }}
                  </div>
                  <div v-if="(index === 1 || index === 2) && step.updatedAt" class="step-update-time">
                    {{ t('更新时间') }}: {{ step.updatedAt }}
                  </div>
                  <div v-if="(index === 1 || index === 2) && step.approvalReason" class="step-reason">
                    {{ t('审批理由') }}: {{ step.approvalReason }}
                  </div>
                </div>
              </template>
            </el-step>
          </el-steps>
        </div>
      </el-dialog>
    </div>
  </template>
  
  <script>
  import { ref, reactive, onMounted, computed, watch } from "vue";
  import httpUtil from "@/utils/HttpUtil";
  import { ElMessage } from "element-plus";
  import { useLanguageStore } from '@/stores/_frame/languageStore';
  import { useUserInfoStore } from '@/stores/_frame/userInfoStore';
  import { Refresh } from '@element-plus/icons-vue';
  
  export default {
    setup() {
      // 引入语言状态管理
      const languageStore = useLanguageStore();
      const currentLang = computed(() => languageStore.currentLang);
  
      // Watch for language changes to update HTML lang attribute
      const updateHtmlLang = () => {
        document.documentElement.lang = languageStore.currentLang;
      };
      // Set initial language
      updateHtmlLang();
      // Watch for changes
      watch(() => languageStore.currentLang, updateHtmlLang);
  
      // Create translation dictionary
      const translations = {
        // My Computer section
        "我的电脑": { en: "My Computer", zh: "我的电脑" },
        "名下电脑选择": { en: "Select PC", zh: "名下电脑选择" },
        "电脑名称": { en: "PC Name", zh: "电脑名称" },
        "电脑名": { en: "PC Name", zh: "电脑名" },
        "NT账号": { en: "NT Account", zh: "NT账号" },
        "部门号": { en: "Dept. ID", zh: "部门号" },
        "姓": { en: "Last Name", zh: "姓" },
        "名": { en: "First Name", zh: "名" },
        "电脑使用状态": { en: "PC Status", zh: "电脑使用状态" },
        "电脑归属情况": { en: "PC Ownership", zh: "电脑归属情况" },
        "电脑情况备注": { en: "Notes", zh: "电脑情况备注" },
        "出厂时间": { en: "Prod. Date", zh: "出厂时间" },
        "暂无电脑信息": { en: "No PC info", zh: "暂无电脑信息" },
        
        // Technical fields
        "制造商": { en: "Manufacturer", zh: "制造商" },
        "型号": { en: "Model", zh: "型号" },
        "资产标签": { en: "Asset Tag", zh: "资产标签" },
        "IP地址": { en: "IP Address", zh: "IP地址" },
        "MAC地址": { en: "MAC Address", zh: "MAC地址" },
        "操作系统": { en: "OS", zh: "操作系统" },
        
        // Maintenance Application section
        "维修申请": { en: "Maintenance Application", zh: "维修申请" },
        "使用人": { en: "User", zh: "使用人" },
        "请输入使用人": { en: "Enter username", zh: "请输入使用人" },
        "未找到相关用户": { en: "No users found", zh: "未找到相关用户" },
        "成本中心": { en: "Cost Center", zh: "成本中心" },
        "请选择成本中心": { en: "Select cost center", zh: "请选择成本中心" },
        "所属公司": { en: "Company", zh: "所属公司" },
        "请选择所属公司": { en: "Select company", zh: "请选择所属公司" },
        "责任人": { en: "Owner", zh: "责任人" },
        "请选择责任人": { en: "Select owner", zh: "请选择责任人" },
        "维修类别": { en: "Maintenance Type", zh: "维修类别" },
        "请选择维修类别": { en: "Select maintenance type", zh: "请选择维修类别" },
        "紧急程度": { en: "Urgency Level", zh: "紧急程度" },
        "低": { en: "Low", zh: "低" },
        "中": { en: "Medium", zh: "中" },
        "高": { en: "High", zh: "高" },
        "紧急": { en: "Urgent", zh: "紧急" },
        "故障描述": { en: "Fault Description", zh: "故障描述" },
        "请详细描述故障现象和问题": { en: "Please describe the fault phenomenon and problem in detail", zh: "请详细描述故障现象和问题" },
        "联系方式": { en: "Contact Info", zh: "联系方式" },
        "请输入联系电话或邮箱": { en: "Enter phone number or email", zh: "请输入联系电话或邮箱" },
        "期望处理时间": { en: "Expected Time", zh: "期望处理时间" },
        "请选择期望处理时间": { en: "Select expected processing time", zh: "请选择期望处理时间" },
        "提交维修申请": { en: "Submit Maintenance", zh: "提交维修申请" },
        "重置": { en: "Reset", zh: "重置" },
        
        // Maintenance Type Options
        "硬件故障维修": { en: "Hardware Repair", zh: "硬件故障维修" },
        "软件问题维修": { en: "Software Repair", zh: "软件问题维修" },
        "系统重装": { en: "System Reinstall", zh: "系统重装" },
        "网络问题维修": { en: "Network Repair", zh: "网络问题维修" },
        "其他维修": { en: "Other Repair", zh: "其他维修" },
        
        // Application Status section
        "我的维修申请状态": { en: "My Maintenance Status", zh: "我的维修申请状态" },
        "刷新": { en: "Refresh", zh: "刷新" },
        "申请时间": { en: "Application Time", zh: "申请时间" },
        "更新时间": { en: "Update Time", zh: "更新时间" },
        "状态": { en: "Status", zh: "状态" },
        "操作": { en: "Actions", zh: "操作" },
        "查看进度": { en: "View Progress", zh: "查看进度" },
        
        // Status
        "审批通过": { en: "Approved", zh: "审批通过" },
        "审批不通过": { en: "Rejected", zh: "审批不通过" },
        "审批中": { en: "Under Review", zh: "审批中" },
        "已通过": { en: "Approved", zh: "已通过" },
        "已驳回": { en: "Rejected", zh: "已驳回" },
        "处理中": { en: "Processing", zh: "处理中" },
        "已完成": { en: "Completed", zh: "已完成" },
        
        // Progress dialog
        "维修进度": { en: "Maintenance Progress", zh: "维修进度" },
        "已提交": { en: "Submitted", zh: "已提交" },
        "审批人1": { en: "Approver 1", zh: "审批人1" },
        "审批人2": { en: "Approver 2", zh: "审批人2" },
        "完成": { en: "Completed", zh: "完成" },
        "上一级审批不通过，审批流程终止": { en: "Previous approval rejected, process terminated", zh: "上一级审批不通过，审批流程终止" },
        
        // Validation messages
        "请选择维修类别": { en: "Please select maintenance type", zh: "请选择维修类别" },
        "请选择成本中心": { en: "Please select cost center", zh: "请选择成本中心" },
        "请选择所属公司": { en: "Please select company", zh: "请选择所属公司" },
        "请输入使用人的NT账号或姓名": { en: "Please enter user's NT account or name", zh: "请输入使用人的NT账号或姓名" },
        "请选择责任人": { en: "Please select responsible person", zh: "请选择责任人" },
        "责任人将根据使用人自动填充": { en: "Responsible person will be auto-filled based on user", zh: "责任人将根据使用人自动填充" },
        "请详细描述故障现象和问题": { en: "Please describe the fault phenomenon and problem in detail", zh: "请详细描述故障现象和问题" },
        "请完成表单填写": { en: "Please complete the form", zh: "请完成表单填写" },
        
        // Messages
        "获取用户信息失败，请手动填写": { en: "Failed to get user info, please fill in manually", zh: "获取用户信息失败，请手动填写" },
        "获取电脑信息失败": { en: "Failed to get computer information", zh: "获取电脑信息失败" },
        "申请提交成功": { en: "Application submitted successfully", zh: "申请提交成功" },
        "申请提交失败": { en: "Application submission failed", zh: "申请提交失败" },
        
        // Pagination translations
        "上一页": { en: "Previous", zh: "上一页" },
        "下一页": { en: "Next", zh: "下一页" },
        "前往": { en: "Go to", zh: "前往" },
        "页": { en: "Page", zh: "页" },
        "共": { en: "Total", zh: "共" },
        "条": { en: "items", zh: "条" },
        "更新时间": { en: "Update Time", zh: "更新时间" },
        "审批理由": { en: "Approval Reason", zh: "审批理由" },
        "使用人": { en: "User", zh: "使用人" },
        "电脑名称": { en: "Computer Name", zh: "电脑名称" },
        "请输入电脑名称": { en: "Please enter computer name", zh: "请输入电脑名称" },
        "电脑名称将根据使用人自动填充": { en: "Computer name will be auto-filled based on user", zh: "电脑名称将根据使用人自动填充" },
        "质量问题维修": { en: "Quality Issue Repair", zh: "质量问题维修" },
        "人为问题维修": { en: "Human Issue Repair", zh: "人为问题维修" },
        "请输入使用人的NT账号或姓名": { en: "Enter user's NT account or name", zh: "请输入使用人的NT账号或姓名" },
        "未找到相关用户": { en: "No users found", zh: "未找到相关用户" }
      };
  
      // Get current user info
      const userInfoStore = useUserInfoStore();
      // Get current username
      const currentUser = ref(userInfoStore.userInfo?.userName || '');
      
      // Translation helper function
      const t = (key) => {
        if (!translations[key]) return key;
        return translations[key][languageStore.currentLang] || key;
      };
      
      // Maintenance form data
      const maintenanceForm = reactive({
        fixCategory: '', // 维修类别
        costCenter: '', // 成本中心
        company: '', // 所属公司
        applicant: currentUser.value, // 使用人id
        computerName: '', // 电脑名称
        responsiblility: '', // 责任人
        problemDescription: '', // 故障描述
      });
  
      // Form validation rules
      const rules = {
        fixCategory: [{ required: true, message: computed(() => t('请选择维修类别')), trigger: 'change' }],
        costCenter: [{ required: true, message: computed(() => t('请选择成本中心')), trigger: 'change' }],
        company: [{ required: true, message: computed(() => t('请选择所属公司')), trigger: 'change' }],
        applicant: [{ required: true, message: computed(() => t('请输入使用人')), trigger: 'blur' }],
        computerName: [{ required: true, message: computed(() => t('请输入电脑名称')), trigger: 'blur' }],
        responsiblility: [{ required: true, message: computed(() => t('请选择责任人')), trigger: 'change' }],
        problemDescription: [{ required: true, message: computed(() => t('请详细描述故障现象和问题')), trigger: 'blur' }]
      };
  
      const maintenanceFormRef = ref(null);
      const maintenanceList = ref([]);
      const loading = ref(false);
      const maintenanceProgressDialogVisible = ref(false);
      const maintenanceProgress = ref([]);
      const maintenanceProgressStep = ref(0);
      
      // Dynamic data
      const costCenters = ref([]); // Cost centers 
      const companies = ref([]); // Companies
      const responsiblePersons = ref([]); // Responsible persons
      
      // Computed property to check if responsible person should be disabled
      const isResponsibleDisabled = computed(() => {
        return !maintenanceForm.applicant || maintenanceForm.applicant.trim() === '';
      });

      // Computed property to check if computer name should be disabled
      const isComputerNameDisabled = computed(() => {
        return !maintenanceForm.applicant || maintenanceForm.applicant.trim() === '';
      });
      
      // Computed property to check if cost center should be disabled
      const isCostCenterDisabled = ref(false);
      
      // User search related
      const userSearchResults = ref([]);
      const showUserResults = ref(false);
      const searchTimeout = ref(null);
      const userInputRef = ref(null);
  
      // Computer dropdown selection related
      const computerList = ref([]);
      const selectedComputer = ref('');
  
      // Pagination related
      const currentPage = ref(1);
      const pageSize = ref(10);
      const totalItems = ref(0);
      const paginatedList = ref([]);
  
      // My computer information
      const myComputer = ref(null);
  
      // Submit maintenance application
      const submitMaintenance = () => {
        maintenanceFormRef.value.validate((valid) => {
          if (valid) {
            loading.value = true;
            
            // Convert English maintenance type to Chinese
            const maintenanceTypeText = getMaintenanceTypeName(maintenanceForm.fixCategory);
            
            // Create a submission object containing all necessary fields
            const submitData = {
              userName: maintenanceForm.applicant, // 使用人用户名
              computerName: maintenanceForm.computerName, // 电脑名称
              fixCategory: maintenanceTypeText, // 维修类别 (using Chinese text)
              costCenter: maintenanceForm.costCenter, // 成本中心
              company: maintenanceForm.company, // 所属公司
              responsiblility: maintenanceForm.responsiblility, // 责任人
              problemDescription: maintenanceForm.problemDescription, // 故障描述
            };
            
            console.log(t('提交的维修申请数据') + ':', submitData);
            console.log('当前用户信息:', userInfoStore.userInfo);
            console.log('currentUser值:', currentUser.value);
            console.log('maintenanceForm.applicant值:', maintenanceForm.applicant);
            
            httpUtil.post("/sysMaintenance/submitMaintenance", submitData, {
              headers: {
                'Content-Type': 'application/json'
              }
            }).then(res => {
              ElMessage({
                type: 'success',
                message: t('申请提交成功')
              });
              resetForm();
              // Reset pagination to first page
              currentPage.value = 1;
              // Refresh list data
              getMaintenanceList();
            }).catch(err => {
              console.error(t('提交维修申请失败') + ":", err);
              ElMessage({
                type: 'error',
                message: t('申请提交失败')
              });
            }).finally(() => {
              loading.value = false;
            });
          } else {
            ElMessage({
              type: 'warning',
              message: t('请完成表单填写')
            });
            return false;
          }
        });
      };
  
      // Reset form
      const resetForm = () => {
        if (maintenanceFormRef.value) {
          maintenanceFormRef.value.resetFields();
        }
        // Reset to default values
        maintenanceForm.applicant = currentUser.value;
        maintenanceForm.computerName = '';
        // Re-fetch current user info
        if (currentUser.value) {
          fetchUserInfo(currentUser.value);
          autoFillComputerName(currentUser.value);
        }
      };
  
      // Get maintenance list
      const getMaintenanceList = () => {
        loading.value = true;
        const params = {
          pageNum: currentPage.value,
          pageSize: pageSize.value,
          userName: currentUser.value
        };
        
        httpUtil.get("/sysMaintenance/getMaintenanceList", { params }).then(res => {
          if (res.data && res.data.list) {
            maintenanceList.value = res.data.list;
            totalItems.value = res.data.total || 0;
          }
        }).catch(err => {
          console.error("获取维修申请列表失败", err);
          ElMessage.error("获取维修申请列表失败");
        }).finally(() => {
          loading.value = false;
        });
      };
  
      // Handle pagination
      const handleCurrentChange = (val) => {
        currentPage.value = val;
        getMaintenanceList();
      };
  
      const handleSizeChange = (val) => {
        pageSize.value = val;
        currentPage.value = 1;
        getMaintenanceList();
      };
  
      // View maintenance progress
      const viewMaintenanceProgress = (row) => {
        httpUtil.get("/sysMaintenance/getMaintenanceProgress", {
          params: { maintenanceId: row.approvalId }
        }).then(res => {
          const flowRoles = res.data;
          const status1 = flowRoles.status1 || '';
          const status2 = flowRoles.status2 || '';
          
          // Check if conditions "status1 is not approved and status2 is under review" are met
          let status2Display = flowRoles.status2 || t('待审批');
          if (status1 === '审批不通过' && (status2 === '审批中' || status2 === '待审批')) {
            status2Display = '上一级审批不通过，审批流程终止';
          }
          
          maintenanceProgress.value = [
            { 
              title: '已提交',
              description: flowRoles.username || '未知用户',
              status: '已提交'
            },
            { 
              title: '审批人1', 
              description: flowRoles.approver1 || t('暂无审批'),
              status: flowRoles.status1 || t('待审批'),
              updatedAt: flowRoles.updatedAt1 || '',
              approvalReason: flowRoles.approvalReason1 || ''
            },
            { 
              title: '审批人2', 
              description: flowRoles.approver2 || t('暂无批准'),
              status: status2Display,
              updatedAt: flowRoles.updatedAt2 || '',
              approvalReason: flowRoles.approvalReason2 || ''
            },
            { 
              title: '完成', 
              description: '',
              status: row.status || '处理中'
            }
          ];
          
          // Determine current active step based on status1 and status2
          let activeStep = 0;
          
          if (status1 === '审批通过') {
            activeStep = 1;
            if (status2 === '审批通过') {
              activeStep = 2;
            } else if (status2 === '审批不通过') {
              activeStep = 2;
            }
          } else if (status1 === '审批不通过') {
            activeStep = 1;
          }
          
          maintenanceProgressStep.value = activeStep;
          maintenanceProgressDialogVisible.value = true;
        }).catch(err => {
          console.error("获取维修进度失败", err);
          ElMessage.error("获取维修进度失败");
        });
      };
  
      // Get maintenance type name
      const getMaintenanceTypeName = (type) => {
        const types = {
          'qualityIssueRepair': '质量问题维修',
          'humanIssueRepair': '人为问题维修'
        };
        return types[type] || type;
      };
  
      // Get localized status
      const getLocalizedStatus = (status) => {
        if (status === '审批中') return t('审批中');
        if (status === '审批通过') return t('审批通过');
        if (status === '审批不通过') return t('审批不通过');
        if (status === '待审批' || status === 'Pending') return t('待审批');
        if (status === '已通过') return t('已通过');
        if (status === '已驳回') return t('已驳回');
        if (status === '处理中') return t('处理中');
        if (status === '已完成') return t('已完成');
        return status;
      };
  
      // Status tag type
      const statusTagType = (status) => {
        switch (status) {
          case '审批通过':
          case '已通过':
          case '已完成':
            return 'success';
          case '审批不通过':
          case '已驳回':
            return 'danger';
          case '审批中':
          case '处理中':
            return 'warning';
          case '待审批':
          case 'Pending':
            return 'info';
          default:
            return 'info';
        }
      };

      // Get process status
      const getProcessStatus = () => {
        const currentStep = maintenanceProgressStep.value;
        const steps = maintenanceProgress.value;
        
        if (!steps || steps.length === 0) return 'wait';
        
        const currentStepData = steps[currentStep];
        if (currentStepData && currentStepData.status) {
          if (currentStepData.status === '审批通过' || currentStepData.status === '已通过') {
            return 'success';
          } else if (currentStepData.status === '审批不通过' || currentStepData.status === '已驳回') {
            return 'error';
          } else if (currentStepData.status === '审批中' || currentStepData.status === '处理中') {
            return 'process';
          }
        }
        
        return 'wait';
      };

      // Get step status
      const getStepStatus = (index) => {
        const steps = maintenanceProgress.value;
        if (!steps || index >= steps.length) return 'wait';
        
        const step = steps[index];
        if (step.status) {
          if (step.status === '审批通过' || step.status === '已通过') {
            return 'success';
          } else if (step.status === '审批不通过' || step.status === '已驳回') {
            return 'error';
          } else if (step.status === '审批中' || step.status === '处理中') {
            return 'process';
          }
        }
        
        return 'wait';
      };

      // User search function
      const searchUsers = (queryString, cb) => {
        if (queryString.length < 2) {
          cb([]);
          return;
        }
        
        httpUtil.get('/sysApply/searchUsers', {
          params: { query: queryString }
        }).then(res => {
          if (res.data && res.data.list) {
            const suggestions = res.data.list.map(user => ({
              value: user.userName,
              department: user.department
            }));
            cb(suggestions);
          } else {
            cb([]);
          }
        }).catch(err => {
          console.error('搜索用户失败:', err);
          cb([]);
        });
      };

      // Handle user selection
      const handleUserSelect = (item) => {
        maintenanceForm.applicant = item.value;
        fetchUserInfo(item.value);
        // 自动填充电脑名称
        autoFillComputerName(item.value);
      };

      // Handle user input
      const handleUserInput = (value) => {
        if (value) {
          fetchUserInfo(value);
          // 自动填充电脑名称
          autoFillComputerName(value);
        }
      };

      // Auto fill computer name based on user
      const autoFillComputerName = (userName) => {
        if (!userName) return;
        
        // 如果电脑列表已经加载，直接使用第一个电脑
        if (computerList.value && computerList.value.length > 0) {
          maintenanceForm.computerName = computerList.value[0];
        } else {
          // 如果电脑列表还没有加载，重新加载电脑列表
          httpUtil.get('/sysControl/getComputerListByUserName', {
            params: { userName: userName }
          }).then(res => {
            if (res.data && res.data.list && res.data.list.length > 0) {
              maintenanceForm.computerName = res.data.list[0];
            } else {
              maintenanceForm.computerName = '';
            }
          }).catch(err => {
            console.error('获取用户电脑列表失败:', err);
            maintenanceForm.computerName = '';
          });
        }
      };

      // Fetch user information
      const fetchUserInfo = (userName) => {
        if (!userName) return Promise.reject(new Error(t('用户名为空')));
        
        return httpUtil.get('/sysApply/getUserInfoByUserName', {
          params: { userName: userName }
        }).then(res => {
          if (res.data) {
            const userInfo = res.data;
            
            // Set cost center
            if (userInfo.costCenter) {
              maintenanceForm.costCenter = userInfo.costCenter;
              if (!costCenters.value.includes(userInfo.costCenter)) {
                costCenters.value.push(userInfo.costCenter);
              }
            }
            
            // Set company - this needs to be handled according to actual backend
            // If backend returns company information, use it
            if (userInfo.company) {
              // Check if the returned company matches the preset options
              const presetCompanies = ["SGCS", "SGCC", "SES"];
              const companyMatch = presetCompanies.find(company => 
                company.toLowerCase() === userInfo.company.toLowerCase());
              
              if (companyMatch) {
                // If a preset company is matched, use the preset format
                maintenanceForm.company = companyMatch;
              } else {
                // If not matched to preset, use the backend returned
                maintenanceForm.company = userInfo.company;
                // Only add to list if company is not already in the list
                if (!companies.value.includes(userInfo.company)) {
                  companies.value.push(userInfo.company);
                }
              }
            }
            
            // Set responsible person - this needs to be handled according to actual backend
            // If backend returns responsible person information, use it
            if (userInfo.responsibility) {
              maintenanceForm.responsiblility = userInfo.responsibility;
              // If responsible person is not in the list, add to the list
              if (!responsiblePersons.value.includes(userInfo.responsibility)) {
                responsiblePersons.value.push(userInfo.responsibility);
              }
            }
          }
          return res;
        }).catch(err => {
          console.error('获取用户信息失败:', err);
          ElMessage.warning(t('获取用户信息失败，请手动填写'));
          return Promise.reject(err);
        });
      };

      // Handle company change
      const handleCompanyChange = () => {
        // Reset cost center when company changes
        maintenanceForm.costCenter = '';
        // You can add logic to fetch cost centers based on company
      };

      // Handle maintenance type change
      const handleMaintenanceTypeChange = () => {
        // 当选择质量问题维修时，成本中心自动变成IT的成本中心：69F105
        // 当选择人为问题维修时，成本中心则是用户自己的成本中心
        if (maintenanceForm.fixCategory === 'qualityIssueRepair') {
          // 质量问题维修 - 使用IT成本中心
          maintenanceForm.costCenter = '69F105';
          // 禁用成本中心选择
          isCostCenterDisabled.value = true;
        } else if (maintenanceForm.fixCategory === 'humanIssueRepair') {
          // 人为问题维修 - 使用用户自己的成本中心
          // 如果当前用户信息存在，重新获取用户的成本中心
          if (maintenanceForm.applicant) {
            fetchUserInfo(maintenanceForm.applicant).then(() => {
              // fetchUserInfo会自动设置用户的成本中心
              // 启用成本中心选择
              isCostCenterDisabled.value = false;
            });
          } else {
            // 如果没有用户信息，清空成本中心并启用选择
            maintenanceForm.costCenter = '';
            isCostCenterDisabled.value = false;
          }
        }
      };

      // Handle computer selection
      const handleComputerSelect = (computerName) => {
        if (computerName) {
          // 设置选中的电脑名称
          selectedComputer.value = computerName; // 确保下拉选择与当前选择匹配
          
          // 自动填充维修申请表单中的电脑名称
          maintenanceForm.computerName = computerName;
          
          httpUtil.get('/sysControl/getComputerInfoByCiName', {
            params: { ciName: computerName }
          }).then(res => {
            if (res.data) {
              myComputer.value = res.data;
              console.log('更新后的电脑信息:', myComputer.value);
            }
          }).catch(err => {
            console.error('获取电脑信息失败:', err);
            ElMessage.error(t('获取电脑信息失败'));
          });
        } else {
          myComputer.value = null;
          selectedComputer.value = '';
          // 清空维修申请表单中的电脑名称
          maintenanceForm.computerName = '';
        }
      };

      // Format date
      const formatDate = (dateString) => {
        if (!dateString) return '';
        try {
          const date = new Date(dateString);
          return date.toLocaleDateString();
        } catch (error) {
          return dateString;
        }
      };

      // Load computer list
      const loadComputerList = () => {
        httpUtil.get('/sysControl/getComputerListByUserName', {
          params: { userName: currentUser.value }
        }).then(res => {
          if (res.data && res.data.list && res.data.list.length > 0) {
            computerList.value = res.data.list;
            console.log('电脑列表:', computerList.value);
            
            // 自动选择第一个电脑
            if (res.data.list.length > 0 && !selectedComputer.value) {
              selectedComputer.value = res.data.list[0];
              console.log('自动选择第一个电脑:', selectedComputer.value);
              
              // 自动获取第一个电脑的详细信息
              handleComputerSelect(res.data.list[0]);
              
              // 自动填充电脑名称到表单
              if (maintenanceForm.applicant === currentUser.value) {
                maintenanceForm.computerName = res.data.list[0];
              }
            }
          } else {
            // 如果没有电脑列表，清空相关数据
            computerList.value = [];
            selectedComputer.value = '';
          }
        }).catch(err => {
          console.error('获取电脑列表失败:', err);
          // 出错时清空电脑列表和选中的电脑
          computerList.value = [];
          selectedComputer.value = '';
        });
      };

      // Load initial data
      const loadInitialData = () => {
        // Re-check user info
        if (!currentUser.value && userInfoStore.userInfo?.userName) {
          currentUser.value = userInfoStore.userInfo.userName;
          maintenanceForm.applicant = currentUser.value;
        }
        
        // 初始化时检查维修类别，设置成本中心的可编辑状态
        if (maintenanceForm.fixCategory === 'qualityIssueRepair') {
          maintenanceForm.costCenter = '69F105';
          isCostCenterDisabled.value = true;
        } else {
          isCostCenterDisabled.value = false;
        }
        
        // Load companies
        companies.value = ['SGCS', 'SGCC', 'SES'];
        
        // Load responsible persons
        responsiblePersons.value = [];
        
        // Load current user info to populate form fields
        if (currentUser.value) {
          fetchUserInfo(currentUser.value);
          loadComputerList();
          getMaintenanceList();
          // 自动填充电脑名称
          autoFillComputerName(currentUser.value);
        } else {
          ElMessage({
            type: 'warning',
            message: '未能获取当前用户信息，请手动填写'
          });
        }
      };

      // On mounted
      onMounted(() => {
        loadInitialData();
      });

      return {
        // Data
        maintenanceForm,
        maintenanceFormRef,
        maintenanceList,
        loading,
        maintenanceProgressDialogVisible,
        maintenanceProgress,
        maintenanceProgressStep,
        costCenters,
        companies,
        responsiblePersons,
        computerList,
        selectedComputer,
        myComputer,
        currentPage,
        pageSize,
        totalItems,
        userInputRef,
        
        // Computed
        isResponsibleDisabled,
        isComputerNameDisabled,
        isCostCenterDisabled,
        
        // Methods
        t,
        submitMaintenance,
        resetForm,
        getMaintenanceList,
        handleCurrentChange,
        handleSizeChange,
        viewMaintenanceProgress,
        getMaintenanceTypeName,
        getLocalizedStatus,
        statusTagType,
        getProcessStatus,
        getStepStatus,
        searchUsers,
        handleUserSelect,
        handleUserInput,
        handleCompanyChange,
        handleMaintenanceTypeChange,
        handleComputerSelect,
        formatDate,
        Refresh
      };
    }
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

.section-title {
  font-weight: 600 !important;
  font-size: 16px;
  margin: 0;
}

/* 卡片内容区域 */
.usr_card_override :deep(.el-card__body) {
  padding: 24px;
  position: relative;
}

/* 表单科技风格 */
.maintenance-form :deep(.el-form-item__label) {
  font-weight: 500;
}

.maintenance-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid rgba(0, 83, 137, 0.2);
  transition: all 0.3s ease;
}

.maintenance-form :deep(.el-input__wrapper:hover) {
  border-color: rgba(0, 83, 137, 0.4);
  box-shadow: 0 2px 8px rgba(0, 83, 137, 0.1);
}

.maintenance-form :deep(.el-input__wrapper.is-focus) {
  border-color: #005389;
  box-shadow: 0 0 0 2px rgba(0, 83, 137, 0.2);
}

.maintenance-form :deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid rgba(0, 83, 137, 0.2);
  transition: all 0.3s ease;
}

.maintenance-form :deep(.el-select .el-input__wrapper:hover) {
  border-color: rgba(0, 83, 137, 0.4);
  box-shadow: 0 2px 8px rgba(0, 83, 137, 0.1);
}

.maintenance-form :deep(.el-select .el-input__wrapper.is-focus) {
  border-color: #005389;
  box-shadow: 0 0 0 2px rgba(0, 83, 137, 0.2);
}

.maintenance-form :deep(.el-textarea__inner) {
  border-radius: 8px;
  border: 1px solid rgba(0, 83, 137, 0.2);
  transition: all 0.3s ease;
}

.maintenance-form :deep(.el-textarea__inner:hover) {
  border-color: rgba(0, 83, 137, 0.4);
  box-shadow: 0 2px 8px rgba(0, 83, 137, 0.1);
}

.maintenance-form :deep(.el-textarea__inner:focus) {
  border-color: #005389;
  box-shadow: 0 0 0 2px rgba(0, 83, 137, 0.2);
}

/* Radio组件蓝绿色主题 */
.maintenance-form :deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: #005389;
  border-color: #005389;
}

.maintenance-form :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #005389;
}

.maintenance-form :deep(.el-radio:hover .el-radio__inner) {
  border-color: #005389;
}

/* 科技风格按钮 */
.maintenance-form :deep(.el-button--primary) {
  background: linear-gradient(135deg, #005389, #029165);
  border: none;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 83, 137, 0.2);
  transition: all 0.3s ease;
}

.maintenance-form :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #0068ab, #02a674);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 83, 137, 0.3);
}

/* 标题选择器样式 */
.title-with-select {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title-with-select h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

/* 我的电脑容器 */
.my-computer-container {
  margin-top: 15px;
}

.elegant-descriptions {
  border-radius: 6px;
  overflow: hidden;
}

.no-computer-data {
  text-align: center;
  padding: 40px 0;
}

/* 表单样式 */
.maintenance-form {
  margin-top: 20px;
}

.maintenance-form .el-form-item {
  margin-bottom: 20px;
}

/* 用户搜索样式 */
.user-suggestion-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.user-suggestion-item small {
  color: #909399;
  font-size: 12px;
}

/* 卡片头部样式 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

/* 表格容器 */
.table-container {
  margin-top: 15px;
}

/* 维修进度样式 */
.maintenance-progress-container {
  padding: 20px 0;
}

.step-description {
  margin-top: 8px;
  font-size: 14px;
  color: #606266;
}

.step-status {
  margin-top: 4px;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.error-status {
  color: #F56C6C;
  background-color: rgba(245, 108, 108, 0.1);
}

.success-status {
  color: #67C23A;
  background-color: rgba(103, 194, 58, 0.1);
}

.warning-status {
  color: #E6A23C;
  background-color: rgba(230, 162, 60, 0.1);
}

.step-update-time {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.step-reason {
  margin-top: 4px;
  font-size: 12px;
  color: #606266;
  font-style: italic;
}

/* 自定义步骤样式 */
.custom-steps:deep(.el-step__line) {
  background-color: #dcdfe6 !important;
}

.custom-steps:deep(.el-step__line-inner) {
  border-color: #dcdfe6 !important;
  background-color: #dcdfe6 !important;
}

.custom-steps:deep(.el-step.is-success .el-step__line),
.custom-steps:deep(.el-step.is-process .el-step__line),
.custom-steps:deep(.el-step.is-error .el-step__line),
.custom-steps:deep(.el-step.is-wait .el-step__line) {
  background-color: #dcdfe6 !important;
}

.custom-steps:deep(.el-step.is-success .el-step__line-inner),
.custom-steps:deep(.el-step.is-process .el-step__line-inner),
.custom-steps:deep(.el-step.is-error .el-step__line-inner),
.custom-steps:deep(.el-step.is-wait .el-step__line-inner) {
  border-color: #dcdfe6 !important;
  background-color: #dcdfe6 !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .title-with-select {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .title-with-select .el-select {
    width: 100% !important;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}

/* 加载状态 */
.el-loading-mask {
  background-color: rgba(255, 255, 255, 0.9);
}

/* 按钮样式 */
.el-button {
  border-radius: 6px;
  font-weight: 500;
}

/* 表单验证样式 */
.el-form-item.is-error .el-input__inner {
  border-color: #F56C6C;
}

.el-form-item.is-error .el-textarea__inner {
  border-color: #F56C6C;
}

/* 表格样式 */
.el-table {
  border-radius: 6px;
  overflow: hidden;
}

.el-table th {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

.el-table td {
  padding: 12px 0;
}

/* 标签样式 */
.el-tag {
  border-radius: 4px;
  font-weight: 500;
}

/* 分页样式 */
.el-pagination {
  margin-top: 20px;
  text-align: right;
}

/* 对话框样式 */
.el-dialog {
  border-radius: 8px;
}

.el-dialog__header {
  background: linear-gradient(135deg, #005389, #029165);
  color: white;
  border-radius: 8px 8px 0 0;
}

.el-dialog__title {
  color: white;
  font-weight: 600;
}

.el-dialog__headerbtn .el-dialog__close {
  color: white;
}

.el-dialog__headerbtn:hover .el-dialog__close {
  color: #f0f0f0;
}

/* 默认按钮样式 */
.maintenance-form :deep(.el-button--default) {
  border: 1px solid rgba(0, 83, 137, 0.3);
  color: #005389;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.maintenance-form :deep(.el-button--default:hover) {
  border-color: #005389;
  color: #005389;
  background-color: rgba(0, 83, 137, 0.05);
}

/* 我的电脑描述表格样式 */
.elegant-descriptions :deep(.el-descriptions__label) {
  padding: 8px 12px !important;
  font-size: 13px !important;
  color: #005389;
  font-weight: bold;
  background: linear-gradient(135deg, rgba(0, 83, 137, 0.1), rgba(2, 145, 101, 0.1));
  width: 120px;
  min-width: 120px;
  max-width: 120px;
}

.elegant-descriptions :deep(.el-descriptions__content) {
  padding: 8px 12px !important;
  font-size: 13px !important;
  word-break: break-all;
  width: 200px;
  min-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  border-left: 2px solid rgba(0, 83, 137, 0.1);
}

.elegant-descriptions :deep(.el-descriptions__body) {
  background-color: #ffffff;
  border-radius: 8px;
  border: 1px solid rgba(0, 83, 137, 0.1);
  overflow: hidden;
}

/* 标题和选择器容器 */
.title-with-select {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 2px solid rgba(0, 83, 137, 0.1);
}

.title-with-select h3 {
  margin: 0;
}

.title-with-select :deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid rgba(0, 83, 137, 0.2);
  transition: all 0.3s ease;
}

.title-with-select :deep(.el-select .el-input__wrapper:hover) {
  border-color: rgba(0, 83, 137, 0.4);
  box-shadow: 0 2px 8px rgba(0, 83, 137, 0.1);
}

/* 刷新按钮科技风格 */
.table-header .el-button--primary {
  background: linear-gradient(135deg, #005389, #029165);
  border: none;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 83, 137, 0.2);
  transition: all 0.3s ease;
}

.table-header .el-button--primary:hover {
  background: linear-gradient(135deg, #0068ab, #02a674);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 83, 137, 0.3);
}

/* 分页组件蓝绿色主题 */
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

/* 表格头部样式 */
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-header .section-title {
  font-weight: 600 !important;
  font-size: 16px;
  margin: 0;
}

/* 状态标签样式 */
.status-tag {
  font-weight: 500;
  transition: all 0.3s ease;
  transform-origin: right;
  animation: scaleIn 0.4s ease-out 0.2s both;
}

/* 动画效果 */
@keyframes scaleIn {
  from {
    transform: scale(0.8);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes fadeInUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@keyframes fadeInLeft {
  from {
    transform: translateX(-20px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 用户搜索结果样式 */
.user-results-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 1000;
  max-height: 200px;
  overflow-y: auto;
}

.user-result-item {
  padding: 8px 12px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.user-result-item:hover {
  background-color: #f5f7fa;
}

.no-results {
  padding: 8px 12px;
  color: #909399;
  font-style: italic;
}

/* 用户输入容器 */
.user-input-container {
  position: relative;
}

/* 日期选择器样式 */
.maintenance-form :deep(.el-date-editor .el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid rgba(0, 83, 137, 0.2);
  transition: all 0.3s ease;
}

.maintenance-form :deep(.el-date-editor .el-input__wrapper:hover) {
  border-color: rgba(0, 83, 137, 0.4);
  box-shadow: 0 2px 8px rgba(0, 83, 137, 0.1);
}

.maintenance-form :deep(.el-date-editor .el-input__wrapper.is-focus) {
  border-color: #005389;
  box-shadow: 0 0 0 2px rgba(0, 83, 137, 0.2);
}

/* 分页选择器样式 */
.pagination-container :deep(.el-pagination .el-select .el-input.is-focus .el-input__wrapper) {
  border-color: #005389;
  box-shadow: 0 0 0 1px rgba(0, 83, 137, 0.2);
}

.pagination-container :deep(.el-pagination .el-input__wrapper:hover) {
  border-color: rgba(0, 83, 137, 0.4);
}

/* 顶部间距 */
.top {
  margin-top: 10px;
}

/* 无电脑数据样式 */
.no-computer-data {
  padding: 20px;
  text-align: center;
}

/* 我的电脑容器 */
.my-computer-container {
  margin: 0;
  padding: 4px 0;
}

/* 优雅描述表格 */
.elegant-descriptions {
  width: 100%;
  table-layout: fixed;
}

/* 自定义滚动条样式 */
.user-results-dropdown::-webkit-scrollbar {
  width: 8px;
}

.user-results-dropdown::-webkit-scrollbar-track {
  background: #f4f4f5;
  border-radius: 4px;
}

.user-results-dropdown::-webkit-scrollbar-thumb {
  background-color: #909399;
  border-radius: 4px;
  border: 2px solid #f4f4f5;
}

/* 用户结果项样式 */
.user-result-item {
  padding: 10px 12px;
  cursor: pointer;
  border-bottom: 1px solid #ebeef5;
}

.user-result-item:last-child {
  border-bottom: none;
}

.user-result-item:hover {
  background-color: #f5f7fa;
}

.no-results {
  padding: 15px 12px;
  color: #909399;
  text-align: center;
  font-size: 14px;
}

/* 维修申请表格样式 */
.maintenance-table {
  margin-bottom: 0;
  border-radius: 4px;
  overflow: hidden;
  width: 100%;
  table-layout: fixed;
}

.maintenance-table:deep(.el-table__body) {
  width: 100% !important;
  table-layout: fixed !important;
}

.maintenance-table:deep(.el-scrollbar__wrap) {
  overflow-x: auto;
}

.maintenance-table:deep(.el-table__row) {
  cursor: pointer;
  transition: all 0.3s ease;
}

.maintenance-table:deep(.el-table__row:hover) {
  background-color: #f0f7ff !important;
}

.maintenance-table:deep(.el-table__header-wrapper) {
  background-color: #f5f7fa;
}

.maintenance-table:deep(.el-table__header) {
  background-color: #f5f7fa;
}

.maintenance-table:deep(.el-table__header th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: bold;
  padding: 12px 0;
}

/* 维修进度容器样式 */
.maintenance-progress-container {
  padding: 30px 20px;
  background-color: #f8fafc;
  animation: fadeIn 0.5s ease-out;
}

/* 自定义维修步骤样式 */
.maintenance-progress-container :deep(.el-step__title.is-process) {
  color: #2580bf;
  font-size: 16px;
  font-weight: bold;
}

.maintenance-progress-container :deep(.el-step__head.is-process) {
  color: #2580bf;
  border-color: #2580bf;
}

.maintenance-progress-container :deep(.el-step__head.is-process .el-step__icon.is-text) {
  background-color: #2580bf;
  color: #fff;
}

.maintenance-progress-container :deep(.el-step__description.is-process) {
  color: #2580bf;
  font-size: 14px;
}

.maintenance-progress-container :deep(.el-step__title.is-success) {
  color: #20b2aa;
}

.maintenance-progress-container :deep(.el-step__head.is-success) {
  color: #20b2aa;
  border-color: #20b2aa;
}

.maintenance-progress-container :deep(.el-step__head.is-success .el-step__icon.is-text) {
  background-color: #20b2aa;
  color: #fff;
}

/* 增加所有步骤的字体大小 */
.maintenance-progress-container :deep(.el-step__title) {
  font-size: 16px;
  font-weight: 500;
  transition: transform 0.3s ease, color 0.3s ease;
}

.maintenance-progress-container :deep(.el-step__description) {
  font-size: 14px;
  transition: transform 0.3s ease, color 0.3s ease;
}

/* 步骤动画效果 */
.maintenance-progress-container :deep(.el-step) {
  &:hover {
    .el-step__title, 
    .el-step__description {
      transform: translateY(-2px);
    }
    
    .el-step__head.is-process,
    .el-step__head.is-wait {
      transform: scale(1.05);
    }
  }
}

/* 增加图标大小和动画 */
.maintenance-progress-container :deep(.el-step__icon) {
  width: 32px;
  height: 32px;
  font-size: 16px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  
  &.is-text {
    box-shadow: 0 0 0 4px rgba(32, 178, 170, 0.1);
  }
}

.maintenance-progress-container :deep(.el-step__head) {
  transition: transform 0.3s ease;
}

/* 最终节点样式 */
.final-step:deep(.el-step__head.is-success) {
  color: #20b2aa;
  border-color: #20b2aa;
}

.final-step:deep(.el-step__head.is-success .el-step__icon) {
  background-color: #20b2aa;
  color: #fff;
  box-shadow: 0 0 0 4px rgba(32, 178, 170, 0.2);
}

.final-step:deep(.el-step__title.is-success) {
  color: #20b2aa;
}
</style>