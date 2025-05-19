<template>
  <div style="padding: 20px;">
    <!-- 我的电脑 -->
    <el-card shadow="never" class="usr_card_override top">
      <div class="title-with-select">
        <h3>我的电脑</h3>
        <el-select 
          v-model="selectedComputer" 
          placeholder="名下电脑选择" 
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
          <el-descriptions-item label="电脑名称" width="300">{{ myComputer.ciName }}</el-descriptions-item>
          <el-descriptions-item label="NT账号" width="300">{{ myComputer.ntAccount }}</el-descriptions-item>
          <el-descriptions-item label="部门号" width="300">{{ myComputer.department }}</el-descriptions-item>
          <el-descriptions-item label="姓" width="300">{{ myComputer.lastName }}</el-descriptions-item>
          <el-descriptions-item label="名" width="300">{{ myComputer.firstName }}</el-descriptions-item>
          <el-descriptions-item label="电脑使用状态" width="300">{{ myComputer.pcStatus }}</el-descriptions-item>
          <el-descriptions-item label="电脑归属情况" v-if="myComputer.pcClass" width="300">{{ myComputer.pcClass }}</el-descriptions-item>
          <el-descriptions-item label="电脑情况备注" width="300">{{ myComputer.comment }}</el-descriptions-item>
          <el-descriptions-item label="出厂时间" width="300">{{ formatDate(myComputer.lifeCycleStart) }}</el-descriptions-item>
          <el-descriptions-item label="manufacturer" v-if="myComputer.manufacturer" width="300">{{ myComputer.manufacturer }}</el-descriptions-item>
          <el-descriptions-item label="model" v-if="myComputer.model" width="300">{{ myComputer.model }}</el-descriptions-item>
          <el-descriptions-item label="assetTag" v-if="myComputer.assetTag" width="300">{{ myComputer.assetTag }}</el-descriptions-item>
          <el-descriptions-item label="ipAddress" v-if="myComputer.ipAddress" width="300">{{ myComputer.ipAddress }}</el-descriptions-item>
          <el-descriptions-item label="macAddress" v-if="myComputer.macAddress" width="300">{{ myComputer.macAddress }}</el-descriptions-item>
          <el-descriptions-item label="os" v-if="myComputer.os" width="300">{{ myComputer.os }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div v-else class="no-computer-data">
        <el-empty description="暂无电脑信息" />
      </div>
    </el-card>

    <!-- 设备申请表单 -->
    <el-card shadow="never" class="usr_card_override top">
      <h3>电脑申请</h3>
      <el-form :model="applicationForm" ref="applicationFormRef" label-width="120px" class="application-form" :rules="rules">
        <el-row :gutter="20">


          <el-col :span="12">
            <el-form-item label="使用人" prop="user">
              <div class="user-input-container">
                <el-input 
                  v-model="applicationForm.user" 
                  placeholder="请输入使用人" 
                  style="width: 100%"
                  clearable
                  @input="handleUserInput"
                  @blur="handleUserBlur"
                  @keyup.enter="confirmUserChange">
                </el-input>
                <!-- 用户查询结果下拉框 -->
                <div v-if="showUserResults" class="user-results-dropdown">
                  <div v-if="userSearchResults.length > 0">
                    <div 
                      v-for="(user, index) in userSearchResults" 
                      :key="index" 
                      class="user-result-item"
                      @click="selectUser(user)">
                      {{ user.userName }} {{ user.userNick ? `(${user.userNick})` : '' }}
                    </div>
                  </div>
                  <div v-else class="no-results">
                    未找到相关用户
                  </div>
                </div>
              </div>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="成本中心" prop="costCenter">
              <el-select v-model="applicationForm.costCenter" placeholder="请选择成本中心" style="width: 100%" :disabled="isCostCenterDisabled">
                <el-option v-for="item in costCenters" :key="item" :label="item" :value="item"></el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="所属公司" prop="company">
              <el-select v-model="applicationForm.company" placeholder="请选择所属公司" style="width: 100%">
                <el-option v-for="item in companies" :key="item" :label="item" :value="item"></el-option>
                <el-option label="SGCS" value="SGCS"></el-option>
                <el-option label="SGCC" value="SGCC"></el-option>
                <el-option label="SES" value="SES"></el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="责任人" prop="responsible">
              <el-select v-model="applicationForm.responsible" placeholder="请选择责任人" style="width: 100%">
                <el-option v-for="item in responsiblePersons" :key="item" :label="item" :value="item"></el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="申请类别" prop="applicationType">
              <el-select 
                v-model="applicationForm.applicationType" 
                placeholder="请选择申请类别" 
                style="width: 100%" 
                @change="handleApplicationTypeChange"
                :disabled="isApplicationTypeDisabled">
                <el-option label="办公电脑超六年换新" value="pcRenewalOverSixYears" :disabled="isPublicUseComputer"></el-option>
                <el-option label="办公电脑未超六年换新" value="pcRenewalUnderSixYears" :disabled="isPublicUseComputer"></el-option>
                <el-option label="办公电脑未超六年换旧" value="pcRenewalUnderSixYearsOld" :disabled="isPublicUseComputer"></el-option>
                <el-option label="秘书代申请新岗位员工电脑" value="secretaryNewEmployee" :disabled="isPublicUseComputer"></el-option>
                <el-option label="秘书代申请替代岗位员工电脑" value="secretaryReplacement" :disabled="isPublicUseComputer"></el-option>
                <el-option label="秘书代申请新实习生/外服电脑" value="secretaryIntern" :disabled="isPublicUseComputer"></el-option>
                <el-option label="其他用途电脑申请" value="specialPurpose"></el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="电脑类型" prop="deviceType">
              <el-select v-model="applicationForm.deviceType" placeholder="请选择电脑类型" style="width: 100%">
                <el-option label="Standard Notebook" value="Standard Notebook"></el-option>
                <el-option label="Performance Notebook" value="Performance Notebook"></el-option>
                <el-option label="Standard Desktop" value="Standard Desktop"></el-option>
                <el-option label="Performance Desktop" value="Performance Desktop"></el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="电脑情形" prop="computerCondition">
              <el-radio-group v-model="applicationForm.computerCondition" :disabled="isComputerConditionDisabled">
                <el-radio label="新电脑">新电脑</el-radio>
                <el-radio label="库存旧电脑">库存旧电脑</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="公司系统" prop="companySys">
              <el-radio-group v-model="applicationForm.companySys">
                <el-radio label="是（公司系统）">是（公司系统）</el-radio>
                <el-radio label="否（非标系统）" disabled>否（非标系统）</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="申请理由" prop="reason">
              <el-input
                v-model="applicationForm.reason"
                :placeholder="reasonPlaceholder"
                type="textarea"
                :rows="3"
                :disabled="isReasonDisabled"
                style="width: 100%">
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" @click="submitApplication">提交申请</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 申请状态显示 -->
    <el-card shadow="never" class="usr_card_override top" style="margin-top: 20px;">
      <div class="table-header">
        <h3 class="section-title">我的申请状态</h3>
        <el-button type="primary" size="small" @click="refreshApplyList">
          <i class="el-icon-refresh"></i> 刷新
        </el-button>
      </div>
      <el-table 
        :data="paginatedList" 
        :loading="loading" 
        style="width: 100%; overflow-x: auto;"
        border
        stripe
        row-key="approvalId"
        highlight-current-row
        :table-layout="'fixed'"
        class="application-table">
        <el-table-column label="申请时间" prop="createdAt" width="160"></el-table-column>
        <el-table-column label="使用人" prop="userName" width="250"></el-table-column>
        <el-table-column label="责任人" prop="responsibilityName" width="150"></el-table-column>
        <el-table-column label="电脑类型" prop="deviceType" width="180">
          <template #default="{ row }">
            {{ getDeviceTypeName(row.deviceType) }}
          </template>
        </el-table-column>
        <el-table-column label="电脑名称" prop="ciName" width="140">
          <template #default="{ row }">
            {{ row.ciName || '申请新电脑' }}
          </template>
        </el-table-column>
        <el-table-column label="申请类别" prop="deviceCategory" width="220">
          <template #default="{ row }">
            {{ getApplicationTypeName(row.deviceCategory) }}
          </template>
        </el-table-column>
        <el-table-column label="申请理由" prop="reason" min-width="250" show-overflow-tooltip></el-table-column>
        <el-table-column label="更新时间" prop="updatedAt" width="160"></el-table-column>
        <el-table-column label="状态" prop="status" width="130" align="center" fixed="right">
          <template #default="{ row }">
            <div style="text-align: center;">
              <el-tag :type="statusTagType(row.status)" @click="viewApprovalProgress(row)">
                {{ row.status }}
              </el-tag>
              <!-- 如果状态为审批通过，显示分配状态 -->
              <el-tag 
                v-if="row.status === '审批通过' || row.status === '已通过'" 
                :type="getAssignStatusTagType(row.assignStatus)"
                style="margin-top: 5px; cursor: pointer;"
                @click="viewAssignProgress(row)"
              >
                {{ row.assignStatus || '分配中' }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <div style="text-align: center; display: flex; justify-content: center;">
              <el-button type="primary" text @click="viewApplicationDetails(row)">查看详情</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
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
        />
      </div>
    </el-card>

    <!-- 审批进度弹窗 -->
    <el-dialog v-model="approvalProgressDialogVisible" title="审批进度" width="700px">
      <div v-if="approvalProgress" class="approval-progress-container custom-steps">
        <el-steps :active="approvalProgressStep" :process-status="getProcessStatus()" align-center>
          <el-step 
            v-for="(step, index) in approvalProgress" 
            :key="index" 
            :title="step.title" 
            :status="getStepStatus(index)"
            :class="{'final-step': index === approvalProgress.length - 1}">
            <template #description>
              <div class="step-description">
                <div>{{ step.description }}</div>
                <div v-if="index === 1 && step.status" class="step-status" 
                  :class="{
                    'error-status': step.status === '审批不通过',
                    'success-status': step.status === '审批通过',
                    'warning-status': step.status.includes('上一级审批不通过')
                  }">
                  {{ step.status }}
                </div>
                <div v-if="index === 2 && step.status" class="step-status" 
                  :class="{
                    'error-status': step.status === '审批不通过',
                    'success-status': step.status === '审批通过',
                    'warning-status': step.status.includes('上一级审批不通过')
                  }">
                  {{ step.status }}
                </div>
              </div>
            </template>
          </el-step>
        </el-steps>
        
        <!-- 状态详情 -->
        <div class="approval-status-detail" v-if="currentApplication">
          <div class="status-box" :class="getStatusClass(currentApplication.status)">
            <div class="status-icon">
              <i :class="getStatusIcon(currentApplication.status)"></i>
            </div>
            <div class="status-text">
              当前状态: <span class="status-value">{{ currentApplication.status }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="approvalProgressDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 分配进度弹窗 -->
    <el-dialog v-model="assignProgressDialogVisible" title="分配进度" width="700px">
      <div v-if="currentApplication" class="approval-progress-container custom-steps">
        <el-steps :active="assignProgressStep" :process-status="getAssignProcessStatus()" align-center>
          <el-step 
            v-for="(step, index) in assignProgress" 
            :key="index" 
            :title="step.title" 
            :status="getAssignStepStatus(index)"
            :class="{'final-step': index === assignProgress.length - 1}">
            <template #description>
              <div class="step-description">
                <div>{{ step.description }}</div>
              </div>
            </template>
          </el-step>
        </el-steps>
        
        <!-- 状态详情 -->
        <div class="approval-status-detail" v-if="currentApplication">
          <div class="status-box" :class="getAssignStatusClass(currentApplication.assignStatus)">
            <div class="status-icon">
              <i :class="getAssignStatusIcon(currentApplication.assignStatus)"></i>
            </div>
            <div class="status-text">
              当前状态: <span class="status-value">{{ currentApplication.assignStatus || '分配中' }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="assignProgressDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 申请详情弹窗 -->
    <el-dialog 
      v-model="applicationDetailDialogVisible" 
      title=""
      width="700px"
      class="application-detail-dialog"
      destroy-on-close>
      <div v-if="currentApplication" class="application-detail-container">
        <!-- 顶部概要信息 -->
        <div class="detail-header">
          <div class="application-title">
            <span class="computer-name">{{ currentApplication.ciName || '申请新电脑' }}</span>
            <el-tag class="status-tag" :type="statusTagType(currentApplication.status)">
              {{ currentApplication.status }}
            </el-tag>
          </div>
          <div class="application-info">
            <span class="info-item">
              <i class="el-icon-user"></i> 申请人: {{ currentApplication.userName }}
            </span>
            <span class="info-item">
              <i class="el-icon-date"></i> 申请时间: {{ currentApplication.createdAt }}
            </span>
          </div>
        </div>

        <!-- 详细信息 -->
        <div class="detail-content">
          <el-descriptions :column="2" border size="default" class="detail-descriptions">
            <el-descriptions-item label="申请类别" label-class-name="item-label" content-class-name="item-content">
              {{ getApplicationTypeName(currentApplication.deviceCategory) }}
            </el-descriptions-item>
            <el-descriptions-item label="电脑类型" label-class-name="item-label" content-class-name="item-content">
              {{ getDeviceTypeName(currentApplication.deviceType) }}
            </el-descriptions-item>
            <el-descriptions-item label="成本中心" label-class-name="item-label" content-class-name="item-content">
              {{ currentApplication.costCenter }}
            </el-descriptions-item>
            <el-descriptions-item label="所属公司" label-class-name="item-label" content-class-name="item-content">
              {{ currentApplication.company }}
            </el-descriptions-item>
            <el-descriptions-item label="责任人" label-class-name="item-label" content-class-name="item-content">
              {{ currentApplication.responsibilityName }}
            </el-descriptions-item>
            <el-descriptions-item label="电脑情形" label-class-name="item-label" content-class-name="item-content">
              {{ currentApplication.deviceSituation }}
            </el-descriptions-item>
            <el-descriptions-item label="公司系统" label-class-name="item-label" content-class-name="item-content">
              {{ currentApplication.companySystem }}
            </el-descriptions-item>
            <el-descriptions-item label="有效期" label-class-name="item-label" content-class-name="item-content">
              {{ currentApplication.timestamp }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间" label-class-name="item-label" content-class-name="item-content">
              {{ currentApplication.updatedAt }}
            </el-descriptions-item>
          </el-descriptions>
          
          <!-- 申请理由区域 -->
          <div class="reason-section">
            <div class="reason-title">申请理由</div>
            <div class="reason-content">{{ currentApplication.reason }}</div>
          </div>
        </div>
        
        <!-- 底部操作区 -->
        <div class="approval-actions">
          <el-button type="primary" @click="applicationDetailDialogVisible = false">
            关闭
          </el-button>
          <el-button type="info" @click="viewApprovalProgress(currentApplication)">
            查看审批进度
          </el-button>
          <el-button 
            v-if="currentApplication.status === '审批通过' || currentApplication.status === '已通过'" 
            type="success" 
            @click="viewAssignProgress(currentApplication)">
            查看分配进度
          </el-button>
        </div>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue';
import httpUtil from "@/utils/HttpUtil";
import { ElMessage, ElMessageBox } from 'element-plus';
import { useUserInfoStore } from "@/stores/_frame/userInfoStore";

export default {
  setup() {
    // 获取当前用户信息
    const userInfoStore = useUserInfoStore();
    // 获取当前用户名，确保获取正确的userName字段
    const currentUser = ref(userInfoStore.userInfo?.userName || '');
    
    // 设备申请表单数据
    const applicationForm = reactive({
      applicationType: '', // 申请类别
      deviceType: '', // 电脑类型
      costCenter: '', // 成本中心
      company: '', // 所属公司
      user: currentUser.value, // 使用人，默认当前用户名
      responsible: '', // 责任人
      computerCondition: '新电脑', // 电脑情形，默认新电脑
      companySys: '是（公司系统）', // 公司系统，默认是
      reason: '', // 申请理由
      ciName: '申请新电脑', // 电脑名称，默认为"申请新电脑"
    });

    // 表单验证规则
    const rules = {
      applicationType: [{ required: true, message: '请选择申请类别', trigger: 'change' }],
      deviceType: [{ required: true, message: '请选择电脑类型', trigger: 'change' }],
      costCenter: [{ required: true, message: '请选择成本中心', trigger: 'change' }],
      company: [{ required: true, message: '请选择所属公司', trigger: 'change' }],
      user: [{ required: true, message: '请输入使用人的NT账号或姓名', trigger: 'blur' }],
      responsible: [{ required: true, message: '请选择责任人', trigger: 'change' }],
      reason: [{ required: true, message: '请选择或输入申请理由', trigger: 'change' }]
    };

    const applicationFormRef = ref(null);
    const applicationList = ref([]);
    const loading = ref(false);
    const approvalProgressDialogVisible = ref(false);
    const approvalProgress = ref([]);
    const approvalProgressStep = ref(0);
    
    // 分配进度相关
    const assignProgressDialogVisible = ref(false);
    const assignProgress = ref([]);
    const assignProgressStep = ref(0);

    // 动态数据
    const costCenters = ref(['IT']); // 成本中心
    const companies = ref([]); // 公司列表
    const responsiblePersons = ref([]); // 责任人列表
    
    // 用户查询相关
    const userSearchResults = ref([]); // 用户查询结果
    const showUserResults = ref(false); // 是否显示用户查询结果
    const searchTimeout = ref(null); // 搜索防抖定时器
    const userInputRef = ref(null); // 用户输入框引用

    // 电脑下拉选择相关
    const computerList = ref([]);
    const selectedComputer = ref('');

    // 分页相关
    const currentPage = ref(1);
    const pageSize = ref(10);
    const totalItems = ref(0);
    const paginatedList = ref([]);

    // 在setup函数内添加格式化日期的函数
    const formatDate = (dateString) => {
      if (!dateString) return '';
      
      // 如果日期字符串包含T，则截取T之前的部分
      if (dateString.includes('T')) {
        return dateString.split('T')[0];
      }
      
      return dateString;
    };

    // 获取用户信息（成本中心、所属公司、责任人）
    const fetchUserInfo = (userName) => {
      if (!userName) return Promise.reject(new Error('用户名为空'));
      
      return httpUtil({
        method: 'get',
        url: '/sysApply/getUserInfoByUserName',
        params: { userName }
      }).then(response => {
        if (response.data) {
          // 设置成本中心
          if (response.data.costCenter) {
            applicationForm.costCenter = response.data.costCenter;
            if (!costCenters.value.includes(response.data.costCenter)) {
              costCenters.value.push(response.data.costCenter);
            }
          }
          
          // 设置所属公司 - 这里需要根据实际情况处理
          // 如果后端返回了公司信息，则使用后端返回的
          if (response.data.company) {
            // 检查返回的公司是否与预设的选项匹配
            const presetCompanies = ["SGCS", "SGCC", "SES"];
            const companyMatch = presetCompanies.find(company => 
              company.toLowerCase() === response.data.company.toLowerCase());
            
            if (companyMatch) {
              // 如果匹配到预设公司，则使用预设的格式
              applicationForm.company = companyMatch;
            } else {
              // 如果不匹配预设，则使用后端返回的
              applicationForm.company = response.data.company;
              // 只有当公司不在列表中时，才添加到列表
              if (!companies.value.includes(response.data.company)) {
                companies.value.push(response.data.company);
              }
            }
          }
          
          // 设置责任人 - 这里需要根据实际情况处理
          // 如果后端返回了责任人信息，则使用后端返回的
          if (response.data.responsibility) {
            applicationForm.responsible = response.data.responsibility;
            // 如果责任人不在列表中，添加到列表
            if (!responsiblePersons.value.includes(response.data.responsibility)) {
              responsiblePersons.value.push(response.data.responsibility);
            }
          }
        }
        return response;
      }).catch(error => {
        ElMessage({
          type: 'warning',
          message: '获取用户信息失败，请手动填写'
        });
        return Promise.reject(error);
      });
    };

    // 处理用户输入
    const handleUserInput = (value) => {
      // 清除之前的定时器
      if (searchTimeout.value) {
        clearTimeout(searchTimeout.value);
      }
      
      // 设置新的定时器，用户停止输入500ms后执行搜索
      searchTimeout.value = setTimeout(() => {
        if (value && value.trim() !== '') {
          searchUser();
        } else {
          showUserResults.value = false;
        }
      }, 500);
    };
    
    // 处理用户输入框失焦
    const handleUserBlur = () => {
      // 延迟关闭下拉框，以便用户能够点击下拉框中的选项
      setTimeout(() => {
        showUserResults.value = false;
      }, 200);
    };
    
    // 确认用户变更
    const confirmUserChange = () => {
      const newUserName = applicationForm.user.trim();
      
      // 如果用户名为空，不进行操作
      if (!newUserName) {
        ElMessage.warning('用户名不能为空');
        return;
      }
      
      // 如果用户名没有变化，不进行操作
      if (newUserName === currentUser.value) {
        return;
      }
      
      // 如果用户名发生变化，重置表单并获取新用户的数据
      if (newUserName !== currentUser.value) {
        // 在重置表单前，先手动重置重要字段以确保它们被清除
        applicationForm.applicationType = '';
        applicationForm.reason = '';
        
        // 清空当前电脑信息
        myComputer.value = null;
        computerList.value = [];
        selectedComputer.value = '';
        applicationForm.ciName = '申请新电脑'; // 重置为默认值
        
        // 同步获取用户信息和电脑信息
        fetchMyComputer(newUserName)
          .then(() => {
            // 获取用户名下所有电脑
            return fetchComputerList(newUserName);
          })
          .then(() => {
            // 确保下拉选择框中的选择与当前显示的电脑匹配
            if (myComputer.value && myComputer.value.ciName) {
              // 确保已加载的电脑在下拉列表中
              if (!computerList.value.includes(myComputer.value.ciName)) {
                computerList.value.push(myComputer.value.ciName);
              }
              // 设置下拉框选中值为当前电脑
              selectedComputer.value = myComputer.value.ciName;
            }
            // 获取用户信息，同时重置表单（除了网络请求获取的字段）
            return fetchUserInfo(newUserName).then(() => {
              // 获取数据成功后重置表单（保留网络请求获取的字段）
              resetFormExceptNetworkFields();
              // 更新当前用户
              currentUser.value = newUserName;
              
              ElMessage({
                type: 'success',
                message: '已切换到用户: ' + newUserName
              });
            });
          })
          .catch(error => {
            console.error('获取用户数据失败:', error);
            ElMessage({
              type: 'error',
              message: '获取用户数据失败'
            });
          });
      }
    };
    
    // 选择用户
    const selectUser = (user) => {
      // 获取新用户名
      const newUserName = user.userName;
      
      // 设置新用户名
      applicationForm.user = newUserName;
      showUserResults.value = false;
      
      // 只有当选择了不同的用户时才发送请求
      if (newUserName !== currentUser.value) {
        // 在重置表单前，先手动重置重要字段以确保它们被清除
        applicationForm.applicationType = '';
        applicationForm.reason = '';
        
        // 先清空电脑信息，避免显示旧信息
        myComputer.value = null;
        // 清空电脑列表和选择的电脑
        computerList.value = [];
        selectedComputer.value = '';
        applicationForm.ciName = '申请新电脑'; // 重置为默认值
        
        // 同步获取用户信息和电脑信息
        Promise.all([
          fetchUserInfo(newUserName),
          fetchMyComputer(newUserName),
          fetchComputerList(newUserName)
        ]).then(([userInfoRes, computerRes, computerListRes]) => {
          // 获取数据成功后重置表单（保留网络请求获取的字段）
          resetFormExceptNetworkFields();
          // 重置申请类别
          applicationForm.applicationType = '';
          // 更新当前用户
          currentUser.value = newUserName;
          
          ElMessage({
            type: 'success',
            message: '已切换到用户: ' + newUserName
          });
        }).catch(error => {
          console.error('获取用户数据失败:', error);
          ElMessage({
            type: 'error',
            message: '获取用户数据失败'
          });
        });
      }
    };
    
    // 点击文档其他地方关闭下拉框
    const handleDocumentClick = (event) => {
      // 检查点击是否在用户输入框或下拉框之外
      const userInputContainer = document.querySelector('.user-input-container');
      if (userInputContainer && !userInputContainer.contains(event.target)) {
        showUserResults.value = false;
      }
    };

    // 获取申请列表
    const getApplyList = () => {
      loading.value = true;
      // 构建请求参数，使用后端期望的参数名称
      const params = {
        pageNum: currentPage.value,  // 当前页码
        pageSize: pageSize.value     // 每页条数
      };
      
      httpUtil.post("/sysApply/getApplyList", params).then(res => {
        applicationList.value = res.data.list || [];
        totalItems.value = res.data.total || 0;
        paginatedList.value = applicationList.value;
      }).catch(err => {
        console.error("获取申请列表失败:", err);
        ElMessage.error("获取申请列表失败");
      }).finally(() => {
        loading.value = false;
      });
    };

    // 提交申请
    const submitApplication = () => {
      applicationFormRef.value.validate((valid) => {
        if (valid) {
          loading.value = true;
          
          // 检查是否需要附加电脑归属情况信息到申请理由
          if (myComputer.value && myComputer.value.pcClass) {
            // 在申请理由后附加电脑归属情况信息
            applicationForm.reason = `${applicationForm.reason}\n电脑归属情况：${myComputer.value.pcClass}`;
          }
          
          // 将英文申请类别转为中文
          const deviceCategoryText = getApplicationTypeName(applicationForm.applicationType);
          
          // 简化公司系统的值
          let companySystemValue = applicationForm.companySys;
          if (companySystemValue.includes('是')) {
            companySystemValue = '是';
          } else if (companySystemValue.includes('否')) {
            companySystemValue = '否';
          }
          
          // 创建一个包含所有必要字段的提交对象，确保字段名与后端一致
          const submitData = {
            // 使用后端期望的字段名称
            deviceCategory: deviceCategoryText, // 申请类别（使用中文文本）
            deviceType: applicationForm.deviceType, // 电脑类型
            costCenter: applicationForm.costCenter, // 成本中心
            company: applicationForm.company, // 所属公司
            userName: applicationForm.user, // 使用人
            responsibilityName: applicationForm.responsible, // 责任人
            deviceSituation: applicationForm.computerCondition, // 电脑情形
            companySystem: companySystemValue, // 公司系统（简化为"是"或"否"）
            reason: applicationForm.reason, // 申请理由
            ciName: applicationForm.ciName, // 电脑名称
            // 保留原始字段以防万一，但覆盖已转换的值
            ...applicationForm,
            companySys: companySystemValue
          };
          
          console.log('提交的申请数据:', submitData);
          
          httpUtil.post("/sysApply/submitApply", submitData, {
            headers: {
              'Content-Type': 'application/json'
            }
          }).then(res => {
            ElMessage({
              type: 'success',
              message: '申请提交成功'
            });
            resetForm();
            // 重置分页到第一页
            currentPage.value = 1;
            // 刷新列表数据
            getApplyList();
          }).catch(err => {
            console.error("提交申请失败:", err);
            ElMessage({
              type: 'error',
              message: '申请提交失败'
            });
          }).finally(() => {
            loading.value = false;
          });
        } else {
          ElMessage({
            type: 'warning',
            message: '请完成表单填写'
          });
          return false;
        }
      });
    };

    // 重置表单
    const resetForm = () => {
      if (applicationFormRef.value) {
        applicationFormRef.value.resetFields();
      }
      // 重置为默认值
      applicationForm.user = currentUser.value;
      applicationForm.computerCondition = '新电脑';
      applicationForm.companySys = '是（公司系统）';
      applicationForm.ciName = '申请新电脑'; // 重置电脑名称为默认值
      // 重新获取当前用户信息
      if (currentUser.value) {
        fetchUserInfo(currentUser.value);
      }
    };

    // 除了网络请求获取的字段外，重置其他表单字段
    const resetFormExceptNetworkFields = () => {
      // 保存网络请求获取的字段
      const currentUser = applicationForm.user;
      const currentCostCenter = applicationForm.costCenter;
      const currentCompany = applicationForm.company;
      const currentResponsible = applicationForm.responsible;
      // 保存自动填充的电脑类型
      const currentDeviceType = applicationForm.deviceType;
      
      // 创建初始状态的表单
      const initialForm = {
        applicationType: '', // 申请类别 - 总是重置
        deviceType: currentDeviceType, // 电脑类型 - 保留自动填充的值
        costCenter: currentCostCenter, // 成本中心 - 保留
        company: currentCompany, // 所属公司 - 保留
        user: currentUser, // 使用人 - 保留
        responsible: currentResponsible, // 责任人 - 保留
        computerCondition: '新电脑', // 电脑情形，默认新电脑
        companySys: '是（公司系统）', // 公司系统，默认是
        reason: '', // 申请理由 - 总是重置
        ciName: '申请新电脑', // 电脑名称，默认为"申请新电脑"
      };
      
      // 使用初始状态替换当前表单内容
      Object.keys(applicationForm).forEach(key => {
        applicationForm[key] = initialForm[key];
      });
    };

    // 查看申请详情
    const viewApplicationDetails = (row) => {
      currentApplication.value = row;
      applicationDetailDialogVisible.value = true;
    };

    // 查看审批进度
    const viewApprovalProgress = (row) => {
      currentApplication.value = row; // 设置当前应用
      
      httpUtil.get("/sysApply/getApproversByAprrovalId", {
        params: { approvalId: row.approvalId }
      }).then(res => {
        const flowRoles = res.data.list;
        const status1 = flowRoles.status1 || '';
        const status2 = flowRoles.status2 || '';
        
        // 检查是否符合"status1为审批不通过，且status2为审批中"的条件
        let status2Display = flowRoles.status2 || '待审批';
        if (status1 === '审批不通过' && (status2 === '审批中' || status2 === '待审批')) {
          status2Display = '上一级审批不通过，审批流程终止';
        }
        
        approvalProgress.value = [
          { 
            title: '已提交',
            description: flowRoles.username || '未知用户',
            status: '已提交'
          },
          { 
            title: '审批人1', 
            description: flowRoles.approver1 || '暂无审批',
            status: flowRoles.status1 || '待审批'
          },
          { 
            title: '审批人2', 
            description: flowRoles.approver2 || '暂无批准',
            status: status2Display
          },
          { 
            title: '完成', 
            description: row.status || '处理中',
            status: row.status || '处理中'
          }
        ];
        
        // 根据status1和status2确定当前的活跃步骤
        
        // 根据规则设置活跃步骤
        let activeStep = 0;
        
        // 1. status1为待审批时，step置为1（当前活跃节点为第二个节点——审批人1）
        if (status1 === '待审批' || status1 === '审批中') {
          activeStep = 1;
        }
        // 2. status1为审批通过，status2为待审批时，step置为2（当前活跃节点为第三个节点——审批人2）
        else if (status1 === '审批通过' && (status2 === '待审批' || status2 === '审批中')) {
          activeStep = 2;
        }
        // 3. status1，status2均为审批通过时，step置为3（当前活跃节点为第四个节点——完成）
        else if (status1 === '审批通过' && status2 === '审批通过') {
          activeStep = 3;
        }
        // 4. status1为审批通过，status2为审批不通过时，step置为3（当前活跃节点为第四个节点——完成）
        else if (status1 === '审批通过' && status2 === '审批不通过') {
          activeStep = 3;
        }
        // 5. status1为审批不通过，step直接置为3（当前活跃节点为第四个节点——完成）
        else if (status1 === '审批不通过') {
          activeStep = 3;
        }
        // 兼容旧状态值
        else if (row.status === '审批通过' || row.status === '已通过') {
          activeStep = 3;
        }
        else if (row.status === '审批不通过' || row.status === '已驳回') {
          activeStep = 3;
        }
        // 默认状态设置为已提交
        else {
          activeStep = 0;
        }
        
        approvalProgressStep.value = activeStep;
        approvalProgressDialogVisible.value = true;
      }).catch(err => {
        console.error("获取审批进度失败:", err);
        ElMessage({
          type: 'error',
          message: '获取审批进度失败'
        });
      });
    };

    // 获取步骤状态
    const getStepStatus = (index) => {
      const status = currentApplication.value?.status || '';
      const currentStep = approvalProgressStep.value;
      const isLastStep = index === approvalProgress.value.length - 1;
      
      // 从审批数据中获取状态信息
      let status1 = '';
      let status2 = '';
      
      if (approvalProgress.value && approvalProgress.value.length > 1) {
        status1 = approvalProgress.value[1].status || '';
        if (approvalProgress.value.length > 2) {
          status2 = approvalProgress.value[2].status || '';
        }
      }
      
      // 检查是否有任何拒绝状态
      const hasRejection = status1 === '审批不通过' || status2 === '审批不通过' || 
                          status === '审批不通过' || status === '已驳回';
                          
      // 如果有任何拒绝状态，确定当前拒绝的节点
      if (hasRejection) {
        // 找出哪个节点拒绝了
        const rejectionNodeIndex = status1 === '审批不通过' ? 1 : 
                                 status2 === '审批不通过' ? 2 : 3;
        
        // 如果是最终节点，总是显示为错误
        if (isLastStep) {
          return 'error';
        }
        // 如果是拒绝的节点，显示为错误
        else if (index === rejectionNodeIndex) {
          return 'error';
        }
        // 如果是拒绝节点之后的节点，也显示为错误
        else if (index > rejectionNodeIndex) {
          return 'error';
        }
        // 如果是拒绝节点之前的节点，保持成功
        else {
          return 'success';
        }
      }
      // 审批通过时的逻辑
      else if ((status1 === '审批通过' && status2 === '审批通过') || 
               status === '审批通过' || status === '已通过') {
        return 'success';
      }
      // 处理中的状态
      else {
        // 已完成的步骤
        if (index < currentStep) {
          return 'success';
        }
        // 当前步骤
        else if (index === currentStep) {
          return 'process';
        }
        // 未完成的步骤
        else {
          return 'wait';
        }
      }
    };

    // 获取审批步骤
    const getApprovalStep = (status) => {
      switch (status) {
        case '已提交': return 0;
        case '审批中': return 1;
        case '审核中': return 2;
        case '审批通过': return 3;
        case '审批不通过': return 3;
        case '已通过': return 3; // 兼容旧数据
        case '已驳回': return 3; // 兼容旧数据
        default: return 0;
      }
    };

    // 获取状态标签类型
    const statusTagType = (status) => {
      switch (status) {
        case '审批中': return 'primary';
        case '审批通过': return 'success';
        case '审批不通过': return 'danger';
        case '已通过': return 'success'; // 兼容旧数据
        case '已驳回': return 'danger'; // 兼容旧数据
        default: return 'info';
      }
    };

    // 获取申请类型名称
    const getApplicationTypeName = (type) => {
      const types = {
        'pcRenewalOverSixYears': '办公电脑超六年换新',
        'pcRenewalUnderSixYears': '办公电脑未超六年换新',
        'pcRenewalUnderSixYearsOld': '办公电脑未超六年换旧',
        'secretaryNewEmployee': '秘书代申请新岗位员工电脑',
        'secretaryReplacement': '秘书代申请替代岗位员工电脑',
        'secretaryIntern': '秘书代申请新实习生/外服电脑',
        'publicComputer': '申请公共电脑',
        'specialPurpose': '其他用途电脑申请',
        // 保留旧映射以兼容已有数据
        'officePcRenewal': '办公电脑换新',
        'newEmployeePc': '新正式员工电脑',
        'internPc': '新实习生/外服电脑',
        'otherPurpose': '其他用途电脑'
      };
      return types[type] || type;
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

    // 获取我的电脑信息
    const myComputer = ref(null);
    
    const fetchMyComputer = (userName) => {
      if (!userName) return Promise.reject(new Error('用户名为空'));
      
      return httpUtil({
        method: 'get',
        url: '/sysControl/getInternalComputerByUserName',
        params: { userName }
      }).then(response => {
        if (response.data) {
          myComputer.value = response.data;
          
          // 如果有电脑信息，设置默认选中的电脑
          if (response.data.ciName) {
            selectedComputer.value = response.data.ciName;
            applicationForm.ciName = response.data.ciName; // 设置申请表单的电脑名称
            
            // 确保computerList中包含当前电脑名称
            if (!computerList.value.includes(response.data.ciName)) {
              // 如果当前电脑不在列表中，添加到列表
              computerList.value.push(response.data.ciName);
            }
          }
          
          // 自动填充电脑类型
          if (response.data.deviceClass) {
            // 将后端返回的deviceClass值映射到前端下拉框选项
            const deviceClass = response.data.deviceClass.trim();
            
            // 检查是否为四个选项中的一个，如果是则直接使用
            const validOptions = [
              "Standard Notebook", 
              "Performance Notebook", 
              "Standard Desktop", 
              "Performance Desktop"
            ];
            
            if (validOptions.includes(deviceClass)) {
              applicationForm.deviceType = deviceClass;
            } else {
              // 如果不是有效选项，尝试根据关键字匹配
              if (deviceClass.toLowerCase().includes('notebook') || deviceClass.toLowerCase().includes('laptop')) {
                if (deviceClass.toLowerCase().includes('performance') || deviceClass.toLowerCase().includes('high')) {
                  applicationForm.deviceType = 'Performance Notebook';
                } else {
                  applicationForm.deviceType = 'Standard Notebook';
                }
              } else if (deviceClass.toLowerCase().includes('desktop') || deviceClass.toLowerCase().includes('pc')) {
                if (deviceClass.toLowerCase().includes('performance') || deviceClass.toLowerCase().includes('high')) {
                  applicationForm.deviceType = 'Performance Desktop';
                } else {
                  applicationForm.deviceType = 'Standard Desktop';
                }
              }
              // 如果无法匹配，则不自动填充，由用户手动选择
            }
          }
          
          // 如果当前申请类别是办公电脑换新，则检查年限
          if (applicationForm.applicationType === 'officePcRenewal') {
            checkComputerLifespan();
          }
          
          // 重置申请类别
          applicationForm.applicationType = '';
          
          // 检查是否为Public Use电脑
          if (response.data.pcClass && response.data.pcClass.includes('Public Use')) {
            // 如果是Public Use电脑，强制设置为其他用途电脑申请
            applicationForm.applicationType = 'specialPurpose';
            applicationForm.reason = '';
            ElMessage({
              type: 'info',
              message: '当前电脑归属情况为Public Use，已自动设置为其他用途电脑申请且无法修改'
            });
            // 获取成本中心列表
            fetchCostCenterList();
          }
          
          // 重新获取用户信息以更新成本中心等数据
          if (response.data.ntAccount) {
            fetchUserInfo(response.data.ntAccount).then(() => {
              ElMessage({
                type: 'success',
                message: '已更新用户信息和电脑: ' + response.data.ciName
              });
            }).catch(error => {
              console.error('获取用户信息失败:', error);
              ElMessage({
                type: 'success',
                message: '已切换到电脑: ' + response.data.ciName + '，但用户信息更新失败'
              });
            });
          } else {
            ElMessage({
              type: 'success',
              message: '已切换到电脑: ' + response.data.ciName
            });
          }
        } else {
          // 如果没有获取到电脑信息，清空相关数据
          myComputer.value = null;
          selectedComputer.value = '';
        }
        return response;
      }).catch(error => {
        ElMessage({
          type: 'warning',
          message: '获取电脑信息失败'
        });
        myComputer.value = null;
        selectedComputer.value = '';
        return Promise.reject(error);
      });
    };

    // 处理申请类别变化
    const handleApplicationTypeChange = () => {
      // 获取当前选中的申请类别
      const selectedType = applicationForm.applicationType;
      
      // 如果没有选择申请类别，直接返回
      if (!selectedType) return;
      
      // 判断是否有电脑信息
      if (!myComputer.value || !myComputer.value.lifeCycleStart) {
        if (selectedType === 'pcRenewalOverSixYears' || selectedType === 'pcRenewalUnderSixYears' || 
            selectedType === 'pcRenewalUnderSixYearsOld') {
          ElMessage({
            type: 'warning',
            message: '未找到当前电脑的出厂时间信息，无法判断使用年限'
          });
          applicationForm.applicationType = ''; // 重置申请类别
          return;
        }
      }
      
      // 如果选择了与年限相关的选项，检查电脑使用年限
      if (selectedType === 'pcRenewalOverSixYears' || selectedType === 'pcRenewalUnderSixYears' || 
          selectedType === 'pcRenewalUnderSixYearsOld') {
        // 获取电脑生命周期开始日期
        const lifeCycleStartStr = myComputer.value.lifeCycleStart;
        
        // 尝试解析日期，支持多种可能的格式
        let lifeCycleStartDate;
        try {
          // 尝试解析YYYY-MM-DD格式
          if (lifeCycleStartStr.includes('-')) {
            lifeCycleStartDate = new Date(lifeCycleStartStr);
          } 
          // 尝试解析MM/DD/YYYY格式
          else if (lifeCycleStartStr.includes('/')) {
            const parts = lifeCycleStartStr.split('/');
            if (parts.length === 3) {
              // 假设格式为MM/DD/YYYY
              lifeCycleStartDate = new Date(parts[2], parts[0] - 1, parts[1]);
            }
          }
          // 其他可能的格式...
        } catch (error) {
          console.error('日期解析错误:', error);
          ElMessage({
            type: 'error',
            message: '无法解析电脑生命周期开始日期'
          });
          applicationForm.applicationType = ''; // 重置申请类别
          return;
        }
        
        // 如果无法解析日期
        if (!lifeCycleStartDate || isNaN(lifeCycleStartDate.getTime())) {
          ElMessage({
            type: 'warning',
            message: '无法确定电脑使用年限，请联系IT部门'
          });
          applicationForm.applicationType = ''; // 重置申请类别
          return;
        }
        
        // 获取当前日期
        const currentDate = new Date();
        
        // 计算年限差异
        const yearsDiff = currentDate.getFullYear() - lifeCycleStartDate.getFullYear();
        
        // 处理不同选项的年限检查
        if (selectedType === 'pcRenewalOverSixYears') {
          // 超六年检查
          if (yearsDiff < 6) {
            ElMessageBox.alert(
              `当前电脑使用年限为${yearsDiff}年，未超过六年，不能选择"办公电脑超六年换新"`,
              '年限不符合要求',
              {
                confirmButtonText: '确定',
                type: 'warning'
              }
            ).then(() => {
              applicationForm.applicationType = ''; // 重置申请类别
            }).catch(() => {
              applicationForm.applicationType = ''; // 重置申请类别
            });
            return;
          } else {
            // 满足条件，设置对应的表单字段
            applicationForm.costCenter = '69F105';
            applicationForm.computerCondition = '新电脑';
            applicationForm.reason = '办公电脑超六年换新';
          }
        } else if (selectedType === 'pcRenewalUnderSixYears' || selectedType === 'pcRenewalUnderSixYearsOld') {
          // 未超六年检查
          if (yearsDiff >= 6) {
            ElMessageBox.alert(
              `当前电脑使用年限为${yearsDiff}年，已超过六年，不能选择"办公电脑未超六年换新/换旧"`,
              '年限不符合要求',
              {
                confirmButtonText: '确定',
                type: 'warning'
              }
            ).then(() => {
              applicationForm.applicationType = ''; // 重置申请类别
            }).catch(() => {
              applicationForm.applicationType = ''; // 重置申请类别
            });
            return;
          } else {
            // 满足条件，设置对应的表单字段
            if (selectedType === 'pcRenewalUnderSixYears') {
              // 未超六年换新 - 显示费用计入部门提示
              ElMessageBox.alert(
                '需要更换的电脑未超过6年，该电脑更换费用将计入电脑使用人所属部门的成本中心',
                '费用提示',
                {
                  confirmButtonText: '确定',
                  type: 'warning',
                }
              ).then(() => {
                // 用户确认后，设置表单字段
                applicationForm.costCenter = myComputer.value.costCenter || ''; // 使用用户自己的成本中心
                applicationForm.computerCondition = '新电脑';
                applicationForm.reason = '办公电脑未超六年换新';
              }).catch(() => {
                // 用户取消，不改变任何设置
              });
            } else {
              // 未超六年换旧
              applicationForm.costCenter = '69F105';
              applicationForm.computerCondition = '库存旧电脑';
              applicationForm.reason = '办公电脑未超六年换旧';
            }
          }
        }
      } else {
        // 处理其他申请类型的逻辑
        switch (selectedType) {
          case 'secretaryNewEmployee':
            applicationForm.costCenter = '69F105';
            applicationForm.computerCondition = '新电脑';
            applicationForm.reason = '秘书代申请新岗位员工电脑';
            break;
            
          case 'secretaryReplacement':
            applicationForm.costCenter = '69F105';
            applicationForm.computerCondition = '库存旧电脑';
            applicationForm.reason = '秘书代申请替代岗位员工电脑';
            break;
            
          case 'secretaryIntern':
            applicationForm.costCenter = '69F105';
            applicationForm.computerCondition = '库存旧电脑';
            applicationForm.reason = '秘书代申请新实习生/外服电脑';
            break;
            
          case 'specialPurpose':
            // 其他用途电脑申请时，从后端获取成本中心列表
            // 清空当前成本中心
            applicationForm.costCenter = '';
            // 清空申请理由
            applicationForm.reason = '';
            // 获取成本中心列表
            fetchCostCenterList();
            break;
            
          default:
            break;
        }
      }
    };
    
    // 检查电脑使用年限
    const checkComputerLifespan = () => {
      // 获取电脑生命周期开始日期
      const lifeCycleStartStr = myComputer.value.lifeCycleStart;
      
      // 尝试解析日期，支持多种可能的格式
      let lifeCycleStartDate;
      try {
        // 尝试解析YYYY-MM-DD格式
        if (lifeCycleStartStr.includes('-')) {
          lifeCycleStartDate = new Date(lifeCycleStartStr);
        } 
        // 尝试解析MM/DD/YYYY格式
        else if (lifeCycleStartStr.includes('/')) {
          const parts = lifeCycleStartStr.split('/');
          if (parts.length === 3) {
            // 假设格式为MM/DD/YYYY
            lifeCycleStartDate = new Date(parts[2], parts[0] - 1, parts[1]);
          }
        }
        // 其他可能的格式...
      } catch (error) {
        console.error('日期解析错误:', error);
        ElMessage({
          type: 'error',
          message: '无法解析电脑生命周期开始日期'
        });
        return;
      }
      
      // 如果无法解析日期
      if (!lifeCycleStartDate || isNaN(lifeCycleStartDate.getTime())) {
        ElMessage({
          type: 'warning',
          message: '无法确定电脑使用年限，请联系IT部门'
        });
        return;
      }
      
      // 获取当前日期
      const currentDate = new Date();
      
      // 计算年限差异
      const yearsDiff = currentDate.getFullYear() - lifeCycleStartDate.getFullYear();
      
      // 如果超过6年
      if (yearsDiff >= 6) {
        // 将成本中心修改为IT
        applicationForm.costCenter = 'IT';
        ElMessage({
          type: 'success',
          message: `电脑使用已超过${yearsDiff}年，符合换新条件，成本中心已自动设置为IT`
        });
      } else {
        // 弹出确认窗口
        ElMessageBox.confirm(
          `当前电脑使用年限为${yearsDiff}年，未超六年，年限内换新是否本部门承担新电脑费用（包括换成其他类型的新电脑）？`,
          '提示',
          {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'warning',
          }
        ).catch(() => {
          // 用户取消，可以重置申请类型
          applicationForm.applicationType = '';
        });
      }
    };

    // 搜索用户 - 实现模糊搜索支持姓名查询
    const searchUser = () => {
      const searchTerm = applicationForm.user;
      if (!searchTerm || searchTerm.trim() === '') {
        showUserResults.value = false;
        return;
      }
      
      httpUtil.get("/sysApply/searchUsers", {
        params: { query: searchTerm }
      }).then(res => {
        if (res.data && Array.isArray(res.data.list) && res.data.list.length > 0) {
          // 将查询结果添加到结果列表
          userSearchResults.value = res.data.list;
          showUserResults.value = true;
        } else {
          userSearchResults.value = [];
          showUserResults.value = true;
        }
      }).catch(err => {
        console.error("搜索用户失败:", err);
        userSearchResults.value = [];
        showUserResults.value = true;
      });
    };

    // 申请详情对话框相关
    const applicationDetailDialogVisible = ref(false);
    const currentApplication = ref(null);

    // 获取处理状态样式
    const getProcessStatus = () => {
      const status = currentApplication.value?.status || '';
      
      // 尝试从流程数据中获取审批状态
      let status1 = '';
      let status2 = '';
      
      if (approvalProgress.value && approvalProgress.value.length > 1) {
        status1 = approvalProgress.value[1].status || '';
        if (approvalProgress.value.length > 2) {
          status2 = approvalProgress.value[2].status || '';
        }
      }
      
      // 如果任一审批者拒绝，返回错误状态
      if (status1 === '审批不通过' || status2 === '审批不通过' || 
          status === '审批不通过' || status === '已驳回') {
        return 'error';
      }
      
      // 如果所有审批者都通过，返回成功状态
      else if ((status1 === '审批通过' && status2 === '审批通过') || 
               status === '审批通过' || status === '已通过') {
        return 'success';
      }
      
      // 其他情况返回处理中状态
      else {
        return 'process';
      }
    };

    // 处理页面大小变化
    const handleSizeChange = (newSize) => {
      pageSize.value = newSize;
      currentPage.value = 1; // 重置为第一页
      getApplyList(); // 重新获取数据
    };

    // 处理当前页变化
    const handleCurrentChange = (newPage) => {
      currentPage.value = newPage;
      getApplyList(); // 重新获取数据
    };

    // 刷新申请列表
    const refreshApplyList = () => {
      getApplyList();
    };

    // 获取状态对应的CSS类名
    const getStatusClass = (status) => {
      switch (status) {
        case '审批通过':
        case '已通过':
          return 'status-success';
        case '审批不通过':
        case '已驳回':
          return 'status-error';
        case '审批中':
        case '待审批':
          return 'status-process';
        default:
          return 'status-info';
      }
    };

    // 获取状态对应的图标
    const getStatusIcon = (status) => {
      switch (status) {
        case '审批通过':
        case '已通过':
          return 'el-icon-check-circle';
        case '审批不通过':
        case '已驳回':
          return 'el-icon-close-circle';
        case '审批中':
        case '待审批':
          return 'el-icon-loading';
        default:
          return 'el-icon-info-circle';
      }
    };

    // 获取用户名下电脑列表
    const fetchComputerList = (userName) => {
      if (!userName) return Promise.reject(new Error('用户名为空'));
      
      return httpUtil({
        method: 'get',
        url: '/sysControl/getComputerListByUserName',
        params: { userName }
      }).then(response => {
        if (response.data && response.data.list && response.data.list.length > 0) {
          // 直接使用返回的字符串数组
          computerList.value = response.data.list;
          // 如果有电脑列表，默认选中第一个
          if (response.data.list.length > 0) {
            selectedComputer.value = response.data.list[0];
            applicationForm.ciName = response.data.list[0]; // 设置申请表单的电脑名称
          }
        } else {
          // 如果没有电脑列表，清空相关数据
          computerList.value = [];
          selectedComputer.value = '';
          applicationForm.ciName = '申请新电脑'; // 设置为默认值
        }
        return response;
      }).catch(error => {
        ElMessage({
          type: 'warning',
          message: '获取电脑列表失败'
        });
        // 出错时也清空电脑列表和选择的电脑
        computerList.value = [];
        selectedComputer.value = '';
        applicationForm.ciName = '申请新电脑'; // 设置为默认值
        return Promise.reject(error);
      });
    };

    // 处理电脑选择
    const handleComputerSelect = (ciName) => {
      if (!ciName) {
        // 如果未选择电脑，设置默认值
        applicationForm.ciName = '申请新电脑';
        return;
      }
      
      // 设置选中的电脑名称
      applicationForm.ciName = ciName;
      selectedComputer.value = ciName; // 确保下拉框选择与当前选择匹配
      
      httpUtil({
        method: 'get',
        url: '/sysControl/getComputerInfoByCiName',
        params: { ciName }
      }).then(response => {
        if (response.data) {
          // 更新"我的电脑"模块中的展示内容
          myComputer.value = response.data;
          
          // 更新"办公电脑申请"模块中绑定的电脑信息
          // 自动填充电脑类型，与原有逻辑保持一致
          if (response.data.deviceClass) {
            // 将后端返回的deviceClass值映射到前端下拉框选项
            const deviceClass = response.data.deviceClass.trim();
            
            // 检查是否为四个选项中的一个，如果是则直接使用
            const validOptions = [
              "Standard Notebook", 
              "Performance Notebook", 
              "Standard Desktop", 
              "Performance Desktop"
            ];
            
            if (validOptions.includes(deviceClass)) {
              applicationForm.deviceType = deviceClass;
            } else {
              // 如果不是有效选项，尝试根据关键字匹配
              if (deviceClass.toLowerCase().includes('notebook') || deviceClass.toLowerCase().includes('laptop')) {
                if (deviceClass.toLowerCase().includes('performance') || deviceClass.toLowerCase().includes('high')) {
                  applicationForm.deviceType = 'Performance Notebook';
                } else {
                  applicationForm.deviceType = 'Standard Notebook';
                }
              } else if (deviceClass.toLowerCase().includes('desktop') || deviceClass.toLowerCase().includes('pc')) {
                if (deviceClass.toLowerCase().includes('performance') || deviceClass.toLowerCase().includes('high')) {
                  applicationForm.deviceType = 'Performance Desktop';
                } else {
                  applicationForm.deviceType = 'Standard Desktop';
                }
              }
              // 如果无法匹配，则不自动填充，由用户手动选择
            }
          }
          
          // 重置申请类别
          applicationForm.applicationType = '';
          
          // 检查是否为Public Use电脑
          if (response.data.pcClass && response.data.pcClass.includes('Public Use')) {
            // 如果是Public Use电脑，强制设置为其他用途电脑申请
            applicationForm.applicationType = 'specialPurpose';
            applicationForm.reason = '';
            ElMessage({
              type: 'info',
              message: '当前电脑归属情况为Public Use，已自动设置为其他用途电脑申请且无法修改'
            });
            // 获取成本中心列表
            fetchCostCenterList();
          }
          
          // 如果电脑信息中包含用户信息，重新获取用户信息以更新成本中心等数据
          if (response.data.ntAccount) {
            fetchUserInfo(response.data.ntAccount).then(() => {
              ElMessage({
                type: 'success',
                message: '已更新用户信息和电脑: ' + response.data.ciName
              });
            }).catch(error => {
              console.error('获取用户信息失败:', error);
              ElMessage({
                type: 'success',
                message: '已切换到电脑: ' + response.data.ciName + '，但用户信息更新失败'
              });
            });
          } else {
            ElMessage({
              type: 'success',
              message: '已切换到电脑: ' + response.data.ciName
            });
          }
        }
      }).catch(error => {
        console.error('获取电脑信息失败:', error);
        ElMessage({
          type: 'error',
          message: '获取电脑信息失败'
        });
      });
    };

    // 获取成本中心列表
    const fetchCostCenterList = () => {
      httpUtil({
        method: 'get',
        url: '/sysApply/getCostCenterList'
      }).then(response => {
        if (response.data && response.data.list && Array.isArray(response.data.list)) {
          // 更新成本中心列表，并移除IT选项
          costCenters.value = response.data.list.filter(center => center !== 'IT');
          
          // 删除成功消息提示
          // ElMessage({
          //   type: 'success',
          //   message: '成本中心列表获取成功'
          // });
        } else {
          ElMessage({
            type: 'warning',
            message: '成本中心列表为空'
          });
          // 确保列表不包含IT
          costCenters.value = [];
        }
      }).catch(error => {
        console.error('获取成本中心列表失败:', error);
        ElMessage({
          type: 'error',
          message: '获取成本中心列表失败'
        });
        
        // 出错时也确保列表不包含IT
        costCenters.value = [];
      });
    };

    onMounted(() => {
      // 重新检查用户信息
      if (!currentUser.value && userInfoStore.userInfo?.userName) {
        currentUser.value = userInfoStore.userInfo.userName;
        applicationForm.user = currentUser.value;
      }
      
      // 确保首先使用当前登录用户的userName发送请求获取数据
      if (currentUser.value) {
        // 获取电脑信息和电脑列表，确保电脑信息加载后再加载电脑列表
        fetchMyComputer(currentUser.value)
          .then(() => {
            // 获取用户名下所有电脑
            return fetchComputerList(currentUser.value);
          })
          .then(() => {
            // 确保下拉选择框中的选择与当前显示的电脑匹配
            if (myComputer.value && myComputer.value.ciName) {
              // 确保已加载的电脑在下拉列表中
              if (!computerList.value.includes(myComputer.value.ciName)) {
                computerList.value.push(myComputer.value.ciName);
              }
              // 设置下拉框选中值为当前电脑
              selectedComputer.value = myComputer.value.ciName;
            }
            // 获取用户信息
            return fetchUserInfo(currentUser.value);
          })
          .catch(error => {
            console.error('初始化获取数据失败:', error);
          });
      } else {
        ElMessage({
          type: 'warning',
          message: '未能获取当前用户信息，请手动填写'
        });
      }
      
      // 初始化获取成本中心列表，确保IT被过滤掉
      fetchCostCenterList();
      
      // 获取申请列表
      getApplyList();
      // 添加全局点击事件监听
      document.addEventListener('click', handleDocumentClick);
    });
    
    onBeforeUnmount(() => {
      // 移除全局点击事件监听
      document.removeEventListener('click', handleDocumentClick);
    });

    // 在setup函数中添加计算属性
    const isReasonDisabled = computed(() => {
      const appType = applicationForm.applicationType;
      return ['pcRenewalOverSixYears', 'pcRenewalUnderSixYears', 'pcRenewalUnderSixYearsOld', 
              'secretaryNewEmployee', 'secretaryReplacement', 'secretaryIntern', 'publicComputer'].includes(appType);
    });

    // 添加成本中心是否禁用的计算属性
    const isCostCenterDisabled = computed(() => {
      return applicationForm.applicationType !== 'specialPurpose' && applicationForm.applicationType !== '';
    });

    // 添加申请类别是否禁用的计算属性
    const isApplicationTypeDisabled = computed(() => {
      return myComputer.value?.pcClass?.includes('Public Use') && applicationForm.applicationType === 'specialPurpose';
    });

    // 添加是否为公共使用电脑的计算属性
    const isPublicUseComputer = computed(() => {
      return myComputer.value?.pcClass?.includes('Public Use');
    });

    // 添加电脑情形是否禁用的计算属性
    const isComputerConditionDisabled = computed(() => {
      // 当申请类型不是其他用途电脑申请，且已经选择了申请类型时，电脑情形禁用
      return applicationForm.applicationType !== '' && applicationForm.applicationType !== 'specialPurpose';
    });

    // 添加申请理由的占位符文本
    const reasonPlaceholder = computed(() => {
      if (applicationForm.applicationType === 'specialPurpose' || 
          (myComputer.value?.pcClass?.includes('Public Use') && applicationForm.applicationType === 'specialPurpose')) {
        return '请输入其他用途电脑申请理由';
      }
      return '请输入申请理由';
    });

    // 获取分配状态标签类型
    const getAssignStatusTagType = (status) => {
      switch (status) {
        case '分配完成': return 'success';
        case '暂分配': return 'warning';
        case '分配中': return 'primary';
        default: return 'primary';
      }
    };
    
    // 查看分配进度
    const viewAssignProgress = (row) => {
      currentApplication.value = row;
      
      // 确定分配状态的步骤
      let steps = [];
      let activeStep = 0;
      
      // 默认两个节点：分配中和分配完成
      steps = [
        { 
          title: '分配中',
          description: '设备正在分配中',
        },
        { 
          title: '分配完成', 
          description: '设备已完成分配',
        }
      ];
      
      // 如果状态是暂分配，则在中间插入暂分配节点
      if (row.assignStatus === '暂分配') {
        steps.splice(1, 0, {
          title: '暂分配',
          description: '设备暂时分配',
        });
        activeStep = 1; // 当前在暂分配阶段
      } else if (row.assignStatus === '分配完成') {
        // 如果是分配完成状态
        activeStep = steps.length - 1;
      } else {
        // 分配中状态
        activeStep = 0;
      }
      
      assignProgress.value = steps;
      assignProgressStep.value = activeStep;
      assignProgressDialogVisible.value = true;
    };
    
    // 获取分配进度状态
    const getAssignProcessStatus = () => {
      const status = currentApplication.value?.assignStatus || '';
      
      if (status === '分配完成') {
        return 'success';
      } else if (status === '暂分配') {
        return 'warning';
      } else {
        return 'process';
      }
    };
    
    // 获取分配步骤状态
    const getAssignStepStatus = (index) => {
      const status = currentApplication.value?.assignStatus || '分配中';
      const currentStep = assignProgressStep.value;
      
      // 已完成的步骤
      if (index < currentStep) {
        return 'success';
      }
      // 当前步骤
      else if (index === currentStep) {
        return 'process';
      }
      // 未完成的步骤
      else {
        return 'wait';
      }
    };
    
    // 获取分配状态对应的CSS类名
    const getAssignStatusClass = (status) => {
      switch (status) {
        case '分配完成':
          return 'status-success';
        case '暂分配':
          return 'status-process';
        case '分配中':
        default:
          return 'status-info';
      }
    };
    
    // 获取分配状态对应的图标
    const getAssignStatusIcon = (status) => {
      switch (status) {
        case '分配完成':
          return 'el-icon-check-circle';
        case '暂分配':
          return 'el-icon-warning';
        case '分配中':
        default:
          return 'el-icon-loading';
      }
    };

    return {
      applicationForm,
      applicationFormRef,
      rules,
      applicationList,
      loading,
      costCenters,
      companies,
      responsiblePersons,
      approvalProgressDialogVisible,
      approvalProgress,
      approvalProgressStep,
      applicationDetailDialogVisible,
      currentApplication,
      handleApplicationTypeChange,
      submitApplication,
      resetForm,
      confirmUserChange,
      viewApplicationDetails,
      viewApprovalProgress,
      viewAssignProgress,
      statusTagType,
      getApplicationTypeName,
      getDeviceTypeName,
      // 用户查询相关
      userSearchResults,
      showUserResults,
      handleUserInput,
      handleUserBlur,
      searchUser,
      selectUser,
      // 我的电脑相关
      myComputer,
      // 电脑下拉选择相关
      computerList,
      selectedComputer,
      handleComputerSelect,
      // 分页相关
      currentPage,
      pageSize,
      totalItems,
      paginatedList,
      handleSizeChange,
      handleCurrentChange,
      // 刷新申请列表
      refreshApplyList,
      // 状态和步骤相关
      getApprovalStep,
      getStepStatus,
      getStatusClass,
      getStatusIcon,
      getProcessStatus,
      // 分配进度相关
      assignProgressDialogVisible,
      assignProgress,
      assignProgressStep,
      // UI状态
      isReasonDisabled,
      isCostCenterDisabled,
      isApplicationTypeDisabled,
      isPublicUseComputer,
      reasonPlaceholder,
      formatDate,
      getAssignStatusTagType,
      getAssignProcessStatus,
      getAssignStepStatus,
      getAssignStatusClass,
      getAssignStatusIcon,
      isComputerConditionDisabled
    };
  }
};
</script>

<style scoped>
.application-form {
  margin-top: 20px;
}

.usr_card_override {
  margin-bottom: 20px;
}

.top {
  margin-top: 10px;
}

.no-computer-data {
  padding: 20px;
  text-align: center;
}

/* 用户输入框容器 */
.user-input-container {
  position: relative;
  width: 100%;
}

/* 用户查询结果下拉框样式 */
.user-results-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  max-height: 300px;
  overflow-y: auto;
  background-color: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 100;
  margin-top: 5px;
  scrollbar-width: thin;
  scrollbar-color: #909399 #f4f4f5;
}

/* 为 Webkit 浏览器自定义滚动条样式 */
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

.my-computer-container {
  margin: 0;
  padding: 4px 0;
}

.elegant-descriptions {
  width: 100%;
  table-layout: fixed;
}

.elegant-descriptions :deep(.el-descriptions__label) {
  padding: 8px 12px;
  font-size: 13px;
  color: #606266;
  font-weight: bold;
  background-color: #f5f7fa;
  width: 120px;
  min-width: 120px;
  max-width: 120px;
}

.elegant-descriptions :deep(.el-descriptions__content) {
  padding: 8px 12px;
  font-size: 13px;
  word-break: break-all;
  width: 200px;
  min-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.elegant-descriptions :deep(.el-descriptions__body) {
  background-color: #ffffff;
  border-radius: 4px;
}

.approval-progress-container {
  padding: 30px 20px;
}

/* 自定义审批步骤样式 */
.approval-progress-container :deep(.el-step__title.is-process) {
  color: #409EFF;
  font-size: 16px;
  font-weight: bold;
}

.approval-progress-container :deep(.el-step__head.is-process) {
  color: #409EFF;
  border-color: #409EFF;
}

.approval-progress-container :deep(.el-step__head.is-process .el-step__icon.is-text) {
  background-color: #409EFF;
  color: #fff;
}

.approval-progress-container :deep(.el-step__description.is-process) {
  color: #409EFF;
  font-size: 14px;
}

/* 增加所有步骤的字体大小 */
.approval-progress-container :deep(.el-step__title) {
  font-size: 16px;
  font-weight: 500;
}

.approval-progress-container :deep(.el-step__description) {
  font-size: 14px;
}

/* 增加图标大小 */
.approval-progress-container :deep(.el-step__icon) {
  width: 32px;
  height: 32px;
  font-size: 16px;
}

/* 最终节点样式 */
.final-step:deep(.el-step__head.is-success) {
  color: #409EFF;
  border-color: #409EFF;
}

.final-step:deep(.el-step__head.is-success .el-step__icon) {
  background-color: #409EFF;
  color: #fff;
}

.final-step:deep(.el-step__title.is-success) {
  color: #409EFF;
}

.final-step:deep(.el-step__description.is-success) {
  color: #409EFF;
}

.final-step:deep(.el-step__head.is-error) {
  color: #F56C6C;
  border-color: #F56C6C;
}

.final-step:deep(.el-step__head.is-error .el-step__icon) {
  background-color: #F56C6C;
  color: #fff;
}

.final-step:deep(.el-step__title.is-error) {
  color: #F56C6C;
}

.final-step:deep(.el-step__description.is-error) {
  color: #F56C6C;
}

/* 对话框标题样式 */
:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: bold;
}

:deep(.el-dialog__header) {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-dialog__body) {
  padding: 0;
}

:deep(.el-dialog__footer) {
  padding: 15px 20px;
  border-top: 1px solid #f0f0f0;
}

.dialog-footer {
  text-align: right;
}

.title-with-select {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.title-with-select h3 {
  margin: 0;
}

/* 状态详情样式 */
.approval-status-detail {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.status-box {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  border-radius: 8px;
  background-color: #f8f9fa;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border-left: 4px solid #909399;
}

.status-success {
  border-left-color: #67c23a;
  background-color: rgba(103, 194, 58, 0.1);
}

.status-error {
  border-left-color: #f56c6c;
  background-color: rgba(245, 108, 108, 0.1);
}

.status-process {
  border-left-color: #409EFF;
  background-color: rgba(64, 158, 255, 0.1);
}

.status-info {
  border-left-color: #909399;
  background-color: rgba(144, 147, 153, 0.1);
}

.status-icon {
  font-size: 28px;
  margin-right: 16px;
}

.status-success .status-icon {
  color: #67c23a;
}

.status-error .status-icon {
  color: #f56c6c;
}

.status-process .status-icon {
  color: #409EFF;
}

.status-text {
  font-size: 16px;
  color: #606266;
}

.status-value {
  font-weight: bold;
  font-size: 16px;
  color: #303133;
}

.status-success .status-value {
  color: #67c23a;
}

.status-error .status-value {
  color: #f56c6c;
}

.status-process .status-value {
  color: #409EFF;
}

/* 分页组件样式 */
.pagination-container {
  margin-top: 20px;
  padding: 10px 0;
  text-align: right;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  border-top: 1px solid #f0f0f0;
  padding-top: 15px;
}

.pagination-container:deep(.el-pagination) {
  padding: 0;
  margin: 0;
  font-weight: normal;
}

.pagination-container:deep(.el-pagination__total) {
  margin-right: 16px;
  color: #606266;
}

.pagination-container:deep(.el-pagination__sizes) {
  margin-right: 16px;
}

.pagination-container:deep(.el-pagination .el-select .el-input) {
  width: 110px;
}

.pagination-container:deep(.el-pagination .btn-prev),
.pagination-container:deep(.el-pagination .btn-next) {
  background-color: #f4f4f5;
  border-radius: 4px;
  margin: 0 5px;
}

.pagination-container:deep(.el-pagination .el-pager li) {
  margin: 0 2px;
  background-color: #f4f4f5;
  border-radius: 4px;
  color: #606266;
}

.pagination-container:deep(.el-pagination .el-pager li.active) {
  background-color: #409EFF;
  color: #ffffff;
}

/* 表格样式 */
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.table-header h3.section-title {
  font-size: 16px;
  margin: 0;
}

.application-table {
  margin-bottom: 0;
  border-radius: 4px;
  overflow: hidden;
  width: 100%;
  table-layout: fixed;
}

.application-table:deep(.el-table__body) {
  width: 100% !important;
  table-layout: fixed !important;
}

.application-table:deep(.el-scrollbar__wrap) {
  overflow-x: auto;
}

.application-table:deep(.el-table__row) {
  cursor: pointer;
  transition: all 0.3s ease;
}

.application-table:deep(.el-table__row:hover) {
  background-color: #f0f7ff !important;
}

.application-table:deep(.el-table__header-wrapper) {
  background-color: #f5f7fa;
}

.application-table:deep(.el-table__header) {
  background-color: #f5f7fa;
}

.application-table:deep(.el-table__header th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: bold;
  padding: 12px 0;
}

/* 步骤描述样式 */
.step-description {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  line-height: 1.4;
  min-height: 40px;
}

.step-status {
  margin-top: 4px;
  font-size: 12px;
  color: #409EFF;
  background-color: rgba(64, 158, 255, 0.1);
  padding: 2px 8px;
  border-radius: 10px;
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

/* 自定义步骤线样式 */
.custom-steps:deep(.el-step__line) {
  background-color: #dcdfe6 !important; /* 统一设置为灰色 */
}

/* 覆盖不同状态下的连接线样式 */
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
  
  /* Remove the grid pattern and add shimmer animation */
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
  /* Add subtle text shadow for better readability */
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  /* Add subtle animation on load */
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
  
  /* Add subtle hover effect */
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
  
  /* Remove the grid pattern */
  &::before {
    display: none;
  }
}

.detail-descriptions {
  margin-bottom: 20px;
  
  :deep(.item-label) {
    background-color: rgba(100, 180, 255, 0.1);
    width: 100px;
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
  
  &:hover {
    background-color: rgba(240, 247, 255, 0.5);
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

.approval-actions {
  padding: 15px 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  background-color: #fff;
  border-top: 1px solid #ebeef5;
  
  .el-button {
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
    
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
  
  .detail-descriptions {
    :deep(.el-descriptions__body) {
      margin: 0;
    }
  }
}

/* Add keyframe animations */
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

/* 审批进度弹窗样式 */
.el-dialog__wrapper:has(.approval-progress-container) {
  :deep(.el-dialog__header) {
    background: linear-gradient(135deg, #2580bf 0%, #20b2aa 100%);
    color: #fff;
    padding: 20px;
    position: relative;
    overflow: hidden;
    border-bottom: none;
    
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
  
  :deep(.el-dialog__title) {
    color: #fff;
    font-weight: 600;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
    animation: fadeInLeft 0.5s ease-out;
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
    border-radius: 6px;
    overflow: hidden;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  }
  
  :deep(.el-dialog__body) {
    background-color: #f8fafc;
    padding: 0;
  }
  
  :deep(.el-dialog__footer) {
    background-color: #f8fafc;
    padding: 16px 20px;
    border-top: 1px solid rgba(37, 128, 191, 0.1);
  }
}

.approval-progress-container {
  padding: 30px 20px;
  background-color: #f8fafc;
  animation: fadeIn 0.5s ease-out;
}

/* 自定义审批步骤样式 */
.approval-progress-container :deep(.el-step__title.is-process) {
  color: #2580bf;
  font-size: 16px;
  font-weight: bold;
}

.approval-progress-container :deep(.el-step__head.is-process) {
  color: #2580bf;
  border-color: #2580bf;
}

.approval-progress-container :deep(.el-step__head.is-process .el-step__icon.is-text) {
  background-color: #2580bf;
  color: #fff;
}

.approval-progress-container :deep(.el-step__description.is-process) {
  color: #2580bf;
  font-size: 14px;
}

.approval-progress-container :deep(.el-step__title.is-success) {
  color: #20b2aa;
}

.approval-progress-container :deep(.el-step__head.is-success) {
  color: #20b2aa;
  border-color: #20b2aa;
}

.approval-progress-container :deep(.el-step__head.is-success .el-step__icon.is-text) {
  background-color: #20b2aa;
  color: #fff;
}

/* 增加所有步骤的字体大小 */
.approval-progress-container :deep(.el-step__title) {
  font-size: 16px;
  font-weight: 500;
  transition: transform 0.3s ease, color 0.3s ease;
}

.approval-progress-container :deep(.el-step__description) {
  font-size: 14px;
  transition: transform 0.3s ease, color 0.3s ease;
}

/* 步骤动画效果 */
.approval-progress-container :deep(.el-step) {
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
.approval-progress-container :deep(.el-step__icon) {
  width: 32px;
  height: 32px;
  font-size: 16px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  
  &.is-text {
    box-shadow: 0 0 0 4px rgba(32, 178, 170, 0.1);
  }
}

.approval-progress-container :deep(.el-step__head) {
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

.final-step:deep(.el-step__description.is-success) {
  color: #20b2aa;
}

.final-step:deep(.el-step__head.is-error) {
  color: #f5587b;
  border-color: #f5587b;
}

.final-step:deep(.el-step__head.is-error .el-step__icon) {
  background-color: #f5587b;
  color: #fff;
  box-shadow: 0 0 0 4px rgba(245, 88, 123, 0.2);
}

.final-step:deep(.el-step__title.is-error) {
  color: #f5587b;
}

.final-step:deep(.el-step__description.is-error) {
  color: #f5587b;
}

/* 步骤描述样式 */
.step-description {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  line-height: 1.4;
  min-height: 40px;
  animation: fadeIn 0.6s ease-out;
}

.step-status {
  margin-top: 4px;
  font-size: 12px;
  color: #2580bf;
  background-color: rgba(37, 128, 191, 0.1);
  padding: 2px 10px;
  border-radius: 20px;
  font-weight: 500;
  transition: all 0.3s ease;
  animation: scaleIn 0.4s ease-out;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  }
}

.error-status {
  color: #f5587b;
  background-color: rgba(245, 88, 123, 0.1);
}

.success-status {
  color: #20b2aa;
  background-color: rgba(32, 178, 170, 0.1);
}

.warning-status {
  color: #e6a23c;
  background-color: rgba(230, 162, 60, 0.1);
}

/* 状态详情样式 */
.approval-status-detail {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  animation: fadeInUp 0.5s ease-out 0.3s both;
}

.status-box {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  border-radius: 8px;
  background-color: #f8f9fa;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  border-left: 4px solid #909399;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  position: relative;
  overflow: hidden;
  
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

.status-success {
  border-left-color: #20b2aa;
  background-color: rgba(32, 178, 170, 0.05);
}

.status-error {
  border-left-color: #f5587b;
  background-color: rgba(245, 88, 123, 0.05);
}

.status-process {
  border-left-color: #2580bf;
  background-color: rgba(37, 128, 191, 0.05);
}

.status-info {
  border-left-color: #909399;
  background-color: rgba(144, 147, 153, 0.05);
}

.status-icon {
  font-size: 28px;
  margin-right: 16px;
  animation: pulse 2s infinite;
}

.status-success .status-icon {
  color: #20b2aa;
}

.status-error .status-icon {
  color: #f5587b;
}

.status-process .status-icon {
  color: #2580bf;
}

.status-text {
  font-size: 16px;
  color: #606266;
}

.status-value {
  font-weight: bold;
  font-size: 16px;
  color: #303133;
  position: relative;
  
  &::after {
    content: '';
    position: absolute;
    bottom: -2px;
    left: 0;
    width: 100%;
    height: 1px;
    background: currentColor;
    transform: scaleX(0);
    transform-origin: right;
    transition: transform 0.3s ease;
  }
}

.status-box:hover .status-value::after {
  transform: scaleX(1);
  transform-origin: left;
}

.status-success .status-value {
  color: #20b2aa;
}

.status-error .status-value {
  color: #f5587b;
}

.status-process .status-value {
  color: #2580bf;
}

/* 自定义步骤线样式 */
.custom-steps:deep(.el-step__line) {
  background: linear-gradient(to right, rgba(37, 128, 191, 0.2), rgba(32, 178, 170, 0.2)) !important;
  height: 2px !important;
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
</style>
