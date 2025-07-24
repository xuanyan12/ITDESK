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

    <!-- 设备申请表单 -->
    <el-card shadow="never" class="usr_card_override top">
      <h3>{{ t('电脑申请') }}</h3>
      <el-form :model="applicationForm" ref="applicationFormRef" label-width="150px" class="application-form" :rules="rules">
        <el-row :gutter="20">


          <el-col :span="12">
            <el-form-item :label="t('使用人')" prop="user">
              <div class="user-input-container">
                <el-input 
                  v-model="applicationForm.user" 
                  :placeholder="t('请输入使用人的NT账号或姓名')" 
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
                    {{ t('未找到相关用户') }}
                  </div>
                </div>
              </div>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item :label="t('成本中心')" prop="costCenter">
              <el-select 
                v-model="applicationForm.costCenter" 
                :placeholder="t('请选择成本中心')" 
                style="width: 100%" 
                :disabled="isCostCenterDisabled"
                filterable
                clearable>
                <el-option v-for="item in costCenters" :key="item" :label="item" :value="item"></el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item :label="t('所属公司')" prop="company">
              <el-select v-model="applicationForm.company" :placeholder="t('请选择所属公司')" style="width: 100%">
                <el-option v-for="item in companies" :key="item" :label="item" :value="item"></el-option>
                <el-option label="SGCS" value="SGCS"></el-option>
                <el-option label="SGCC" value="SGCC"></el-option>
                <el-option label="SES" value="SES"></el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item :label="t('责任人')" prop="responsible">
              <el-select v-model="applicationForm.responsible" :placeholder="t('请选择责任人')" style="width: 100%">
                <el-option v-for="item in responsiblePersons" :key="item" :label="item" :value="item"></el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item :label="t('申请类别')" prop="applicationType">
              <el-select 
                v-model="applicationForm.applicationType" 
                :placeholder="t('请选择申请类别')" 
                style="width: 100%" 
                @change="handleApplicationTypeChange"
                :disabled="isApplicationTypeDisabled">
                <el-option :label="t('办公电脑超六年换新')" value="pcRenewalOverSixYears" :disabled="isPublicUseComputer"></el-option>
                <el-option :label="t('办公电脑未超六年换新')" value="pcRenewalUnderSixYears" :disabled="isPublicUseComputer"></el-option>
                <el-option :label="t('办公电脑未超六年换旧')" value="pcRenewalUnderSixYearsOld" :disabled="isPublicUseComputer"></el-option>
                <el-option :label="t('秘书代申请新岗位员工电脑')" value="secretaryNewEmployee" :disabled="isPublicUseComputer"></el-option>
                <el-option :label="t('秘书代申请替代岗位员工电脑')" value="secretaryReplacement" :disabled="isPublicUseComputer"></el-option>
                <el-option :label="t('秘书代申请新实习生/外服电脑')" value="secretaryIntern" :disabled="isPublicUseComputer"></el-option>
                <el-option :label="t('其他用途电脑申请')" value="specialPurpose"></el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item :label="t('电脑类型')" prop="deviceType">
              <el-select v-model="applicationForm.deviceType" :placeholder="t('请选择电脑类型')" style="width: 100%">
                <el-option label="Standard Notebook" value="Standard Notebook"></el-option>
                <el-option label="Performance Notebook" value="Performance Notebook"></el-option>
                <el-option label="Standard Desktop" value="Standard Desktop"></el-option>
                <el-option label="Performance Desktop" value="Performance Desktop"></el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item :label="t('电脑情形')" prop="computerCondition">
              <el-radio-group v-model="applicationForm.computerCondition" :disabled="isComputerConditionDisabled">
                <el-radio :label="'新电脑'">{{ t('新电脑') }}</el-radio>
                <el-radio :label="'库存旧电脑'">{{ t('库存旧电脑') }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item :label="t('公司系统')" prop="companySys">
              <el-radio-group v-model="applicationForm.companySys">
                <el-radio :label="'是（公司系统）'">{{ t('是（公司系统）') }}</el-radio>
                <el-radio :label="'否（非标系统）'" disabled>{{ t('否（非标系统）') }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item :label="t('申请理由')" prop="reason">
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
          <el-button type="primary" @click="submitApplication">{{ t('提交申请') }}</el-button>
          <el-button @click="resetForm">{{ t('重置') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 申请状态显示 -->
    <el-card shadow="never" class="usr_card_override top" style="margin-top: 20px;">
      <div class="table-header">
        <h3 class="section-title">{{ t('我的申请状态') }}</h3>
        <el-button type="primary" size="small" @click="refreshApplyList">
          <i class="el-icon-refresh"></i> {{ t('刷新') }}
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
        <el-table-column :label="t('申请时间')" prop="createdAt" width="160"></el-table-column>
        <el-table-column :label="t('使用人')" prop="userName" width="250"></el-table-column>
        <el-table-column :label="t('责任人')" prop="responsibilityName" width="250"></el-table-column>
        <el-table-column :label="t('电脑类型')" prop="deviceType" width="180">
          <template #default="{ row }">
            {{ getDeviceTypeName(row.deviceType) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('电脑名称')" prop="ciName" width="140">
          <template #default="{ row }">
            {{ row.ciName || t('申请新电脑') }}
          </template>
        </el-table-column>
        <el-table-column :label="t('申请类别')" prop="deviceCategory" width="220">
          <template #default="{ row }">
            {{ getApplicationTypeName(row.deviceCategory) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('申请理由')" prop="reason" min-width="250" show-overflow-tooltip></el-table-column>
        <el-table-column :label="t('更新时间')" prop="updatedAt" width="160"></el-table-column>
        <el-table-column :label="t('状态')" prop="status" width="130" align="center" fixed="right">
          <template #default="{ row }">
            <div style="text-align: center;">
              <el-tag :type="statusTagType(row.status)" @click="viewApprovalProgress(row)">
                {{ t(row.status) }}
              </el-tag>
              <!-- 如果状态为审批通过，显示分配状态 -->
              <el-tag 
                v-if="row.status === '审批通过' || row.status === '已通过'" 
                :type="getAssignStatusTagType(row.assignStatus)"
                style="margin-top: 5px; cursor: pointer;"
                @click="viewAssignProgress(row)"
              >
                {{ t(row.assignStatus || '分配中') }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('操作')" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <div style="text-align: center; display: flex; justify-content: center;">
              <el-button type="primary" text @click="viewApplicationDetails(row)">{{ t('查看详情') }}</el-button>
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
          :prev-text="t('上一页')"
          :next-text="t('下一页')"
          :popper-class="'pagination-popper'"
        />
      </div>
    </el-card>

    <!-- 审批进度弹窗 -->
    <el-dialog v-model="approvalProgressDialogVisible" :title="t('审批进度')" width="850px">
      <div v-if="approvalProgress" class="approval-progress-container custom-steps">
        <el-steps :active="approvalProgressStep" :process-status="getProcessStatus()" align-center>
          <el-step 
            v-for="(step, index) in approvalProgress" 
            :key="index" 
            :title="t(step.title)" 
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
        
        <!-- 状态详情 -->
        <div class="approval-status-detail" v-if="currentApplication">
          <div class="status-box" :class="getStatusClass(currentApplication.status)">
            <div class="status-icon">
              <i :class="getStatusIcon(currentApplication.status)"></i>
            </div>
            <div class="status-text">
              {{ t('当前状态') }}: <span class="status-value">{{ t(currentApplication.status) }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="approvalProgressDialogVisible = false">{{ t('关闭') }}</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 分配进度弹窗 -->
    <el-dialog v-model="assignProgressDialogVisible" :title="t('分配进度')" width="700px">
      <div v-if="currentApplication" class="approval-progress-container custom-steps">
        <el-steps :active="assignProgressStep" :process-status="getAssignProcessStatus()" align-center>
          <el-step 
            v-for="(step, index) in assignProgress" 
            :key="index" 
            :title="t(step.title)" 
            :status="getAssignStepStatus(index)"
            :class="{'final-step': index === assignProgress.length - 1}">
            <template #description>
              <div class="step-description">
                <div>{{ t(step.description) }}</div>
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
              {{ t('当前状态') }}: <span class="status-value">{{ t(currentApplication.assignStatus || '分配中') }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="assignProgressDialogVisible = false">{{ t('关闭') }}</el-button>
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
            <span class="computer-name">{{ currentApplication.ciName || t('申请新电脑') }}</span>
            <el-tag class="status-tag" :type="statusTagType(currentApplication.status)">
              {{ t(currentApplication.status) }}
            </el-tag>
          </div>
          <div class="application-info">
            <span class="info-item">
              <i class="el-icon-user"></i> {{ t('申请人') }}: {{ currentApplication.userName }}
            </span>
            <span class="info-item">
              <i class="el-icon-date"></i> {{ t('申请时间') }}: {{ currentApplication.createdAt }}
            </span>
          </div>
        </div>

        <!-- 详细信息 -->
        <div class="detail-content">
          <el-descriptions :column="2" border size="default" class="detail-descriptions">
            <el-descriptions-item :label="t('申请类别')" label-class-name="item-label" content-class-name="item-content">
              {{ getApplicationTypeName(currentApplication.deviceCategory) }}
            </el-descriptions-item>
            <el-descriptions-item :label="t('电脑类型')" label-class-name="item-label" content-class-name="item-content">
              {{ getDeviceTypeName(currentApplication.deviceType) }}
            </el-descriptions-item>
            <el-descriptions-item :label="t('成本中心')" label-class-name="item-label" content-class-name="item-content">
              {{ currentApplication.costCenter }}
            </el-descriptions-item>
            <el-descriptions-item :label="t('所属公司')" label-class-name="item-label" content-class-name="item-content">
              {{ currentApplication.company }}
            </el-descriptions-item>
            <el-descriptions-item :label="t('责任人')" label-class-name="item-label" content-class-name="item-content">
              {{ currentApplication.responsibilityName }}
            </el-descriptions-item>
            <el-descriptions-item :label="t('电脑情形')" label-class-name="item-label" content-class-name="item-content">
              {{ currentApplication.deviceSituation }}
            </el-descriptions-item>
            <el-descriptions-item :label="t('公司系统')" label-class-name="item-label" content-class-name="item-content">
              {{ currentApplication.companySystem }}
            </el-descriptions-item>
            <el-descriptions-item :label="t('有效期')" label-class-name="item-label" content-class-name="item-content">
              {{ currentApplication.timestamp }}
            </el-descriptions-item>
            <el-descriptions-item :label="t('更新时间')" label-class-name="item-label" content-class-name="item-content">
              {{ currentApplication.updatedAt }}
            </el-descriptions-item>
          </el-descriptions>
          
          <!-- 申请理由区域 -->
          <div class="reason-section">
            <div class="reason-title">{{ t('申请理由') }}</div>
            <div class="reason-content">{{ currentApplication.reason }}</div>
          </div>
        </div>
        
        <!-- 底部操作区 -->
        <div class="approval-actions">
          <el-button type="primary" @click="applicationDetailDialogVisible = false">
            {{ t('关闭') }}
          </el-button>
          <el-button type="info" @click="viewApprovalProgress(currentApplication)">
            {{ t('查看审批进度') }}
          </el-button>
          <el-button 
            v-if="currentApplication.status === '审批通过' || currentApplication.status === '已通过'" 
            type="success" 
            @click="viewAssignProgress(currentApplication)">
            {{ t('查看分配进度') }}
          </el-button>
        </div>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { ref, reactive, onMounted, onBeforeUnmount, computed, watch } from 'vue';
import httpUtil from "@/utils/HttpUtil";
import { ElMessage, ElMessageBox } from 'element-plus';
import { useUserInfoStore } from "@/stores/_frame/userInfoStore";
import { useLanguageStore } from "@/stores/_frame/languageStore"; // 修正导入路径

export default {
  setup() {
    // Get language store
    const languageStore = useLanguageStore();

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
      
      // Computer Application section
      "电脑申请": { en: "PC Application", zh: "电脑申请" },
      "使用人": { en: "User", zh: "使用人" },
      "请输入使用人": { en: "Enter username", zh: "请输入使用人" },
      "未找到相关用户": { en: "No users found", zh: "未找到相关用户" },
      "成本中心": { en: "Cost Center", zh: "成本中心" },
      "请选择成本中心": { en: "Select cost center", zh: "请选择成本中心" },
      "所属公司": { en: "Company", zh: "所属公司" },
      "请选择所属公司": { en: "Select company", zh: "请选择所属公司" },
      "责任人": { en: "Owner", zh: "责任人" },
      "请选择责任人": { en: "Select owner", zh: "请选择责任人" },
      "申请类别": { en: "App. Type", zh: "申请类别" },
      "请选择申请类别": { en: "Select app. type", zh: "请选择申请类别" },
      "电脑类型": { en: "PC Type", zh: "电脑类型" },
      "请选择电脑类型": { en: "Select PC type", zh: "请选择电脑类型" },
      "电脑情形": { en: "PC Condition", zh: "电脑情形" },
      "新电脑": { en: "New", zh: "新电脑" },
      "库存旧电脑": { en: "Used", zh: "库存旧电脑" },
      "公司系统": { en: "Company Sys", zh: "公司系统" },
      "是（公司系统）": { en: "Yes (Co. Sys)", zh: "是（公司系统）" },
      "否（非标系统）": { en: "No (Non-std)", zh: "否（非标系统）" },
      "申请理由": { en: "Reason", zh: "申请理由" },
      "请输入申请理由": { en: "Enter reason", zh: "请输入申请理由" },
      "请输入其他用途电脑申请理由": { en: "Enter special purpose reason", zh: "请输入其他用途电脑申请理由" },
      "提交申请": { en: "Submit", zh: "提交申请" },
      "重置": { en: "Reset", zh: "重置" },
      
      // Application Type Options
      "办公电脑超六年换新": { en: "PC Renewal (>6y)", zh: "办公电脑超六年换新" },
      "办公电脑未超六年换新": { en: "PC Renewal (<6y)", zh: "办公电脑未超六年换新" },
      "办公电脑未超六年换旧": { en: "PC Used Replacement", zh: "办公电脑未超六年换旧" },
      "秘书代申请新岗位员工电脑": { en: "New Employee PC", zh: "秘书代申请新岗位员工电脑" },
      "秘书代申请替代岗位员工电脑": { en: "Replacement PC", zh: "秘书代申请替代岗位员工电脑" },
      "秘书代申请新实习生/外服电脑": { en: "Intern/Contractor PC", zh: "秘书代申请新实习生/外服电脑" },
      "其他用途电脑申请": { en: "Special Purpose", zh: "其他用途电脑申请" },
      
      // Application Status section
      "我的申请状态": { en: "My Application Status", zh: "我的申请状态" },
      "刷新": { en: "Refresh", zh: "刷新" },
      "申请时间": { en: "Application Time", zh: "申请时间" },
      "更新时间": { en: "Update Time", zh: "更新时间" },
      "状态": { en: "Status", zh: "状态" },
      "操作": { en: "Actions", zh: "操作" },
      "查看详情": { en: "View Details", zh: "查看详情" },
      "申请新电脑": { en: "New Computer Application", zh: "申请新电脑" },
      
      // Status
      "审批通过": { en: "Approved", zh: "审批通过" },
      "审批不通过": { en: "Rejected", zh: "审批不通过" },
      "审批中": { en: "Under Review", zh: "审批中" },
      "已通过": { en: "Approved", zh: "已通过" },
      "已驳回": { en: "Rejected", zh: "已驳回" },
      "分配中": { en: "Assigning", zh: "分配中" },
      "分配完成": { en: "Assigned", zh: "分配完成" },
      "暂分配": { en: "Temporarily Assigned", zh: "暂分配" },
      
      // Approval progress dialog
      "审批进度": { en: "Approval Progress", zh: "审批进度" },
      "已提交": { en: "Submitted", zh: "已提交" },
      "审批人1": { en: "Approver 1", zh: "审批人1" },
      "审批人2": { en: "Approver 2", zh: "审批人2" },
      "完成": { en: "Completed", zh: "完成" },
      "上一级审批不通过，审批流程终止": { en: "Previous approval rejected, process terminated", zh: "上一级审批不通过，审批流程终止" },
      "待审批": { en: "Pending Approval", zh: "待审批" },
      "暂无审批": { en: "No Approver", zh: "暂无审批" },
      "暂无批准": { en: "No Approver", zh: "暂无批准" },
      "当前状态": { en: "Current Status", zh: "当前状态" },
      "关闭": { en: "Close", zh: "关闭" },
      
      // Assignment progress dialog
      "分配进度": { en: "Assignment Progress", zh: "分配进度" },
      "设备正在分配中": { en: "Device is being assigned", zh: "设备正在分配中" },
      "设备暂时分配": { en: "Device is temporarily assigned", zh: "设备暂时分配" },
      "设备已完成分配": { en: "Device has been assigned", zh: "设备已完成分配" },
      
      // Application details dialog
      "申请人": { en: "Applicant", zh: "申请人" },
      "申请类别": { en: "Application Type", zh: "申请类别" },
      "电脑类型": { en: "Computer Type", zh: "电脑类型" },
      "成本中心": { en: "Cost Center", zh: "成本中心" },
      "所属公司": { en: "Company", zh: "所属公司" },
      "责任人": { en: "Responsible Person", zh: "责任人" },
      "电脑情形": { en: "Computer Condition", zh: "电脑情形" },
      "公司系统": { en: "Company System", zh: "公司系统" },
      "有效期": { en: "Valid Until", zh: "有效期" },
      "更新时间": { en: "Update Time", zh: "更新时间" },
      "申请理由": { en: "Application Reason", zh: "申请理由" },
      "查看审批进度": { en: "View Approval Progress", zh: "查看审批进度" },
      "查看分配进度": { en: "View Assignment Progress", zh: "查看分配进度" },
      
      // Form validation messages
      "请选择申请类别": { en: "Please select application type", zh: "请选择申请类别" },
      "请选择电脑类型": { en: "Please select computer type", zh: "请选择电脑类型" },
      "请选择成本中心": { en: "Please select cost center", zh: "请选择成本中心" },
      "请选择所属公司": { en: "Please select company", zh: "请选择所属公司" },
      "请输入使用人的NT账号或姓名": { en: "Please enter user's NT account or name", zh: "请输入使用人的NT账号或姓名" },
      "请选择责任人": { en: "Please select responsible person", zh: "请选择责任人" },
      "请选择或输入申请理由": { en: "Please select or enter application reason", zh: "请选择或输入申请理由" },
      "请完成表单填写": { en: "Please complete the form", zh: "请完成表单填写" },
      
      // Messages
      "获取用户信息失败，请手动填写": { en: "Failed to get user info, please fill in manually", zh: "获取用户信息失败，请手动填写" },
      "当前电脑归属情况为Public Use，已自动设置为其他用途电脑申请且无法修改": { en: "Current computer ownership is Public Use, automatically set to Special Purpose and cannot be changed", zh: "当前电脑归属情况为Public Use，已自动设置为其他用途电脑申请且无法修改" },
      "未找到当前电脑的出厂时间信息，无法判断使用年限": { en: "Production date information not found, unable to determine usage period", zh: "未找到当前电脑的出厂时间信息，无法判断使用年限" },
      "获取电脑信息失败": { en: "Failed to get computer information", zh: "获取电脑信息失败" },
      "申请提交成功": { en: "Application submitted successfully", zh: "申请提交成功" },
      "申请提交失败": { en: "Application submission failed", zh: "申请提交失败" },
      "需要更换的电脑未超过6年，该电脑更换费用将计入电脑使用人所属部门的成本中心": { en: "The computer to be replaced is less than 6 years old. The replacement cost will be charged to the user's department cost center", zh: "需要更换的电脑未超过6年，该电脑更换费用将计入电脑使用人所属部门的成本中心" },
      "费用提示": { en: "Cost Note", zh: "费用提示" },
      "年限不符合要求": { en: "Age requirement not met", zh: "年限不符合要求" },
      "用户名不能为空": { en: "Username cannot be empty", zh: "用户名不能为空" },
      "已切换到用户": { en: "Switched to user", zh: "已切换到用户" },
      "获取用户数据失败": { en: "Failed to get user data", zh: "获取用户数据失败" },
      "已更新用户信息和电脑": { en: "Updated user info and computer", zh: "已更新用户信息和电脑" },
      "已切换到电脑": { en: "Switched to computer", zh: "已切换到电脑" },
      "但用户信息更新失败": { en: "but user info update failed", zh: "但用户信息更新失败" },
      "无法解析电脑生命周期开始日期": { en: "Cannot parse computer lifecycle start date", zh: "无法解析电脑生命周期开始日期" },
      "无法确定电脑使用年限，请联系IT部门": { en: "Cannot determine computer usage period, please contact IT department", zh: "无法确定电脑使用年限，请联系IT部门" },
      "确定": { en: "Confirm", zh: "确定" },
      "处理中": { en: "Processing", zh: "处理中" },
      "获取审批进度失败": { en: "Failed to get approval progress", zh: "获取审批进度失败" },
      "提交的申请数据": { en: "Submitted application data", zh: "提交的申请数据" },
      "提交申请失败": { en: "Application submission failed", zh: "提交申请失败" },
      "电脑归属情况": { en: "PC Ownership", zh: "电脑归属情况" },
      
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
    
    // Application form data
    const applicationForm = reactive({
      applicationType: '', // Application type
      deviceType: '', // Computer type
      costCenter: '', // Cost center
      company: '', // Company
      user: currentUser.value, // User, default to current user
      responsible: '', // Responsible person
      computerCondition: '新电脑', // Computer condition, default to new
      companySys: '是（公司系统）', // Company system, default to yes
      reason: '', // Application reason
      ciName: '申请新电脑', // Computer name, default to "New Computer Application"
    });

    // Form validation rules
    const rules = {
      applicationType: [{ required: true, message: computed(() => t('请选择申请类别')), trigger: 'change' }],
      deviceType: [{ required: true, message: computed(() => t('请选择电脑类型')), trigger: 'change' }],
      costCenter: [{ required: true, message: computed(() => t('请选择成本中心')), trigger: 'change' }],
      company: [{ required: true, message: computed(() => t('请选择所属公司')), trigger: 'change' }],
      user: [{ required: true, message: computed(() => t('请输入使用人的NT账号或姓名')), trigger: 'blur' }],
      responsible: [{ required: true, message: computed(() => t('请选择责任人')), trigger: 'change' }],
      reason: [{ required: true, message: computed(() => t('请选择或输入申请理由')), trigger: 'change' }]
    };

    const applicationFormRef = ref(null);
    const applicationList = ref([]);
    const loading = ref(false);
    const approvalProgressDialogVisible = ref(false);
    const approvalProgress = ref([]);
    const approvalProgressStep = ref(0);
    
    // Assignment progress related
    const assignProgressDialogVisible = ref(false);
    const assignProgress = ref([]);
    const assignProgressStep = ref(0);

    // Dynamic data
    const costCenters = ref(['IT']); // Cost centers 
    const companies = ref([]); // Companies
    const responsiblePersons = ref([]); // Responsible persons
    
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

    // In the setup function, add a formatDate function
    const formatDate = (dateString) => {
      if (!dateString) return '';
      
      // If the date string contains T, split it at T
      if (dateString.includes('T')) {
        return dateString.split('T')[0];
      }
      
      return dateString;
    };

    // Get user info (cost center, company, responsible person)
    const fetchUserInfo = (userName) => {
      if (!userName) return Promise.reject(new Error(t('用户名为空')));
      
      return httpUtil({
        method: 'get',
        url: '/sysApply/getUserInfoByUserName',
        params: { userName }
      }).then(response => {
        if (response.data) {
          // Set cost center
          if (response.data.costCenter) {
            applicationForm.costCenter = response.data.costCenter;
            if (!costCenters.value.includes(response.data.costCenter)) {
              costCenters.value.push(response.data.costCenter);
            }
          }
          
          // Set company - this needs to be handled according to actual backend
          // If backend returns company information, use it
          if (response.data.company) {
            // Check if the returned company matches the preset options
            const presetCompanies = ["SGCS", "SGCC", "SES"];
            const companyMatch = presetCompanies.find(company => 
              company.toLowerCase() === response.data.company.toLowerCase());
            
            if (companyMatch) {
              // If a preset company is matched, use the preset format
              applicationForm.company = companyMatch;
            } else {
              // If not matched to preset, use the backend returned
              applicationForm.company = response.data.company;
              // Only add to list if company is not already in the list
              if (!companies.value.includes(response.data.company)) {
                companies.value.push(response.data.company);
              }
            }
          }
          
          // Set responsible person - this needs to be handled according to actual backend
          // If backend returns responsible person information, use it
          if (response.data.responsibility) {
            applicationForm.responsible = response.data.responsibility;
            // If responsible person is not in the list, add to the list
            if (!responsiblePersons.value.includes(response.data.responsibility)) {
              responsiblePersons.value.push(response.data.responsibility);
            }
          }
        }
        return response;
      }).catch(error => {
        ElMessage({
          type: 'warning',
          message: t('获取用户信息失败，请手动填写')
        });
        return Promise.reject(error);
      });
    };

    // Handle user input
    const handleUserInput = (value) => {
      // Clear previous timer
      if (searchTimeout.value) {
        clearTimeout(searchTimeout.value);
      }
      
      // Set new timer, user stops input for 500ms
      searchTimeout.value = setTimeout(() => {
        if (value && value.trim() !== '') {
          searchUser();
        } else {
          showUserResults.value = false;
        }
      }, 500);
    };
    
    // Handle user input blur
    const handleUserBlur = () => {
      // Delay closing dropdown, so user can click on options in dropdown
      setTimeout(() => {
        showUserResults.value = false;
      }, 200);
    };
    
    // Confirm user change
    const confirmUserChange = () => {
      const newUserName = applicationForm.user.trim();
      
      // If user name is empty, do nothing
      if (!newUserName) {
        ElMessage.warning(t('用户名不能为空'));
        return;
      }
      
      // If user name hasn't changed, do nothing
      if (newUserName === currentUser.value) {
        return;
      }
      
      // If user name has changed, reset form and get new user's data
      if (newUserName !== currentUser.value) {
        // Before resetting form, manually reset important fields to ensure they're cleared
        applicationForm.applicationType = '';
        applicationForm.reason = '';
        
        // Clear current computer information
        myComputer.value = null;
        computerList.value = [];
        selectedComputer.value = '';
        applicationForm.ciName = '申请新电脑'; // Reset to default value
        
        // Sync user info and computer info
        fetchMyComputer(newUserName)
          .then(() => {
            // Get all computers for the user
            return fetchComputerList(newUserName);
          })
          .then(() => {
            // Ensure selected option in dropdown matches current displayed computer
            if (myComputer.value && myComputer.value.ciName) {
              // Ensure loaded computer is in dropdown list
              if (!computerList.value.includes(myComputer.value.ciName)) {
                computerList.value.push(myComputer.value.ciName);
              }
              // Set dropdown selected value to current computer
              selectedComputer.value = myComputer.value.ciName;
            }
            // Get user info, while also resetting form (excluding network request fields)
            return fetchUserInfo(newUserName).then(() => {
              // Reset form after getting data (keeping network request fields)
              resetFormExceptNetworkFields();
              // Update current user
              currentUser.value = newUserName;
              
              ElMessage({
                type: 'success',
                message: t('已切换到用户') + ': ' + newUserName
              });
            });
          })
          .catch(error => {
            console.error('获取用户数据失败:', error);
            ElMessage({
              type: 'error',
              message: t('获取用户数据失败')
            });
          });
      }
    };
    
    // Select user
    const selectUser = (user) => {
      // Get new user name
      const newUserName = user.userName;
      
      // Set new user name
      applicationForm.user = newUserName;
      showUserResults.value = false;
      
      // Only send request if a different user is selected
      if (newUserName !== currentUser.value) {
        // Before resetting form, manually reset important fields to ensure they're cleared
        applicationForm.applicationType = '';
        applicationForm.reason = '';
        
        // Clear computer information first, to avoid showing old information
        myComputer.value = null;
        // Clear computer list and selected computer
        computerList.value = [];
        selectedComputer.value = '';
        applicationForm.ciName = '申请新电脑'; // Reset to default value
        
        // Sync user info and computer info
        Promise.all([
          fetchUserInfo(newUserName),
          fetchMyComputer(newUserName),
          fetchComputerList(newUserName)
        ]).then(([userInfoRes, computerRes, computerListRes]) => {
          // Reset form after getting data (keeping network request fields)
          resetFormExceptNetworkFields();
          // Reset application type
          applicationForm.applicationType = '';
          // Update current user
          currentUser.value = newUserName;
          
          ElMessage({
            type: 'success',
            message: t('已切换到用户') + ': ' + newUserName
          });
        }).catch(error => {
          console.error('获取用户数据失败:', error);
          ElMessage({
            type: 'error',
            message: t('获取用户数据失败')
          });
        });
      }
    };
    
    // Click outside document to close dropdown
    const handleDocumentClick = (event) => {
      // Check if click is outside user input or dropdown
      const userInputContainer = document.querySelector('.user-input-container');
      if (userInputContainer && !userInputContainer.contains(event.target)) {
        showUserResults.value = false;
      }
    };

    // Get application list
    const getApplyList = () => {
      loading.value = true;
      // Build request parameters, using backend's expected parameter names
      const params = {
        pageNum: currentPage.value,  // Current page number
        pageSize: pageSize.value     // Number of items per page
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

    // Submit application
    const submitApplication = () => {
      applicationFormRef.value.validate((valid) => {
        if (valid) {
          loading.value = true;
          
          // Convert English application type to Chinese - Keep this to ensure we send Chinese values
          const deviceCategoryText = getApplicationTypeName(applicationForm.applicationType);
          
          // Simplify company system value
          let companySystemValue = applicationForm.companySys;
          if (companySystemValue.includes('是')) {
            companySystemValue = '是';
          } else if (companySystemValue.includes('否')) {
            companySystemValue = '否';
          }
          
          // Create a submission object containing all necessary fields, ensuring field names match backend
          // and ensuring we submit Chinese values even when displaying English
          const submitData = {
            // Use backend's expected field names with Chinese values
            deviceCategory: deviceCategoryText, // Application type (using Chinese text)
            deviceType: applicationForm.deviceType, // Computer type
            costCenter: applicationForm.costCenter, // Cost center
            company: applicationForm.company, // Company
            userName: applicationForm.user, // User
            responsibilityName: applicationForm.responsible, // Responsible person
            deviceSituation: applicationForm.computerCondition, // Computer condition as Chinese
            companySystem: companySystemValue, // Company system (simplified to "yes" or "no")
            reason: applicationForm.reason, // Application reason
            ciName: applicationForm.ciName, // Computer name
            // Keep original fields in case, but override converted values
            ...applicationForm,
            companySys: companySystemValue
          };
          
          console.log(t('提交的申请数据') + ':', submitData);
          
          httpUtil.post("/sysApply/submitApply", submitData, {
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
            getApplyList();
          }).catch(err => {
            console.error(t('提交申请失败') + ":", err);
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
      if (applicationFormRef.value) {
        applicationFormRef.value.resetFields();
      }
      // Reset to default values
      applicationForm.user = currentUser.value;
      applicationForm.computerCondition = '新电脑';
      applicationForm.companySys = '是（公司系统）';
      applicationForm.ciName = '申请新电脑'; // Reset computer name to default value
      // Re-fetch current user info
      if (currentUser.value) {
        fetchUserInfo(currentUser.value);
      }
    };

    // Reset all form fields except network request fields
    const resetFormExceptNetworkFields = () => {
      // Save network request fields
      const currentUser = applicationForm.user;
      const currentCostCenter = applicationForm.costCenter;
      const currentCompany = applicationForm.company;
      const currentResponsible = applicationForm.responsible;
      // Save auto-filled computer type
      const currentDeviceType = applicationForm.deviceType;
      
      // Create initial form state
      const initialForm = {
        applicationType: '', // Application type - always reset
        deviceType: currentDeviceType, // Computer type - keep auto-filled value
        costCenter: currentCostCenter, // Cost center - keep
        company: currentCompany, // Company - keep
        user: currentUser, // User - keep
        responsible: currentResponsible, // Responsible person - keep
        computerCondition: '新电脑', // Computer condition, default to new
        companySys: '是（公司系统）', // Company system, default to yes
        reason: '', // Application reason - always reset
        ciName: '申请新电脑', // Computer name, default to "New Computer Application"
      };
      
      // Use initial state to replace current form content
      Object.keys(applicationForm).forEach(key => {
        applicationForm[key] = initialForm[key];
      });
    };

    // View application details
    const viewApplicationDetails = (row) => {
      currentApplication.value = row;
      applicationDetailDialogVisible.value = true;
    };

    // View approval progress
    const viewApprovalProgress = (row) => {
      currentApplication.value = row; // Set current application
      
      httpUtil.get("/sysApply/getApproversByAprrovalId", {
        params: { approvalId: row.approvalId }
      }).then(res => {
        const flowRoles = res.data.list;
        const status1 = flowRoles.status1 || '';
        const status2 = flowRoles.status2 || '';
        
        // Check if conditions "status1 is not approved and status2 is under review" are met
        let status2Display = flowRoles.status2 || t('待审批');
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
        
        // Set active step based on rules
        let activeStep = 0;
        
        // 1. If status1 is pending approval or under review, step is set to 1 (current active node is second node - approver1)
        if (status1 === '待审批' || status1 === '审批中') {
          activeStep = 1;
        }
        // 2. If status1 is approved and status2 is pending approval or under review, step is set to 2 (current active node is third node - approver2)
        else if (status1 === '审批通过' && (status2 === '待审批' || status2 === '审批中')) {
          activeStep = 2;
        }
        // 3. If status1 and status2 are both approved, step is set to 3 (current active node is fourth node - completed)
        else if (status1 === '审批通过' && status2 === '审批通过') {
          activeStep = 3;
        }
        // 4. If status1 is approved and status2 is not approved, step is set to 3 (current active node is fourth node - completed)
        else if (status1 === '审批通过' && status2 === '审批不通过') {
          activeStep = 3;
        }
        // 5. If status1 is not approved, step is set directly to 3 (current active node is fourth node - completed)
        else if (status1 === '审批不通过') {
          activeStep = 3;
        }
        // Compatibility for old status values
        else if (row.status === '审批通过' || row.status === '已通过') {
          activeStep = 3;
        }
        else if (row.status === '审批不通过' || row.status === '已驳回') {
          activeStep = 3;
        }
        // Default status is set to submitted
        else {
          activeStep = 0;
        }
        
        approvalProgressStep.value = activeStep;
        approvalProgressDialogVisible.value = true;
      }).catch(err => {
        console.error("获取审批进度失败:", err);
        ElMessage({
          type: 'error',
          message: t('获取审批进度失败')
        });
      });
    };

    // Get step status
    const getStepStatus = (index) => {
      const status = currentApplication.value?.status || '';
      const currentStep = approvalProgressStep.value;
      const isLastStep = index === approvalProgress.value.length - 1;
      
      // Get status information from approval data
      let status1 = '';
      let status2 = '';
      
      if (approvalProgress.value && approvalProgress.value.length > 1) {
        status1 = approvalProgress.value[1].status || '';
        if (approvalProgress.value.length > 2) {
          status2 = approvalProgress.value[2].status || '';
        }
      }
      
      // Check for any rejection status
      const hasRejection = status1 === '审批不通过' || status2 === '审批不通过' || 
                          status === '审批不通过' || status === '已驳回';
                          
      // If there's any rejection status, determine which node was rejected
      if (hasRejection) {
        // Find out which node was rejected
        const rejectionNodeIndex = status1 === '审批不通过' ? 1 : 
                                 status2 === '审批不通过' ? 2 : 3;
        
        // If it's the final node, always display as error
        if (isLastStep) {
          return 'error';
        }
        // If it's a rejected node, display as error
        else if (index === rejectionNodeIndex) {
          return 'error';
        }
        // If it's a node after a rejected node, also display as error
        else if (index > rejectionNodeIndex) {
          return 'error';
        }
        // If it's a node before a rejected node, keep success
        else {
          return 'success';
        }
      }
      // Logic for approved status
      else if ((status1 === '审批通过' && status2 === '审批通过') || 
               status === '审批通过' || status === '已通过') {
        return 'success';
      }
      // Status for processing
      else {
        // Completed steps
        if (index < currentStep) {
          return 'success';
        }
        // Current step
        else if (index === currentStep) {
          return 'process';
        }
        // Uncompleted steps
        else {
          return 'wait';
        }
      }
    };

    // Get approval step
    const getApprovalStep = (status) => {
      switch (status) {
        case '已提交': return 0;
        case '审批中': return 1;
        case '审核中': return 2;
        case '审批通过': return 3;
        case '审批不通过': return 3;
        case '已通过': return 3; // Compatibility for old data
        case '已驳回': return 3; // Compatibility for old data
        default: return 0;
      }
    };

    // Get status tag type
    const statusTagType = (status) => {
      switch (status) {
        case '审批中': return 'primary';
        case '审批通过': return 'success';
        case '审批不通过': return 'danger';
        case '已通过': return 'success'; // Compatibility for old data
        case '已驳回': return 'danger'; // Compatibility for old data
        default: return 'info';
      }
    };

    // Get application type name
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
        // Keep old mapping for compatibility with existing data
        'officePcRenewal': '办公电脑换新',
        'newEmployeePc': '新正式员工电脑',
        'internPc': '新实习生/外服电脑',
        'otherPurpose': '其他用途电脑'
      };
      return types[type] || type;
    };

    // Get device type name
    const getDeviceTypeName = (type) => {
      const types = {
        'Standard Notebook': 'Standard Notebook',
        'Performance Notebook': 'Performance Notebook',
        'Standard Desktop': 'Standard Desktop',
        'Performance Desktop': 'Performance Desktop',
        // Keep old mapping for compatibility with existing data
        'normalLaptop': 'Standard Notebook',
        'workstationLaptop': 'Performance Notebook',
        'normalDesktop': 'Standard Desktop',
        'workstationDesktop': 'Performance Desktop'
      };
      return types[type] || type;
    };

    // Get my computer information
    const myComputer = ref(null);
    
    // Get IT cost center based on company
    const getITCostCenter = (company) => {
      switch (company) {
        case 'SGCS':
          return '69F105';
        case 'SGCC':
          return '69D163';
        case 'SES':
          return '80A105';
        default:
          return '69F105'; // Default to SGCS IT cost center
      }
    };
    
    const fetchMyComputer = (userName) => {
      if (!userName) return Promise.reject(new Error('用户名为空'));
      
      return httpUtil({
        method: 'get',
        url: '/sysControl/getInternalComputerByUserName',
        params: { userName }
      }).then(response => {
        console.log('获取我的电脑响应:', response);
        if (response.data) {
          myComputer.value = response.data;
          console.log('我的电脑信息:', myComputer.value);
          
          // If there's computer information, set default selected computer
          if (response.data.ciName) {
            selectedComputer.value = response.data.ciName;
            applicationForm.ciName = response.data.ciName; // Set computer name in application form
            
            // Ensure current computer is in the list
            if (!computerList.value.includes(response.data.ciName)) {
              // If current computer is not in the list, add it
              computerList.value.push(response.data.ciName);
            }
          }
          
          // Auto-fill computer type
          if (response.data.deviceClass) {
            // Map backend returned deviceClass value to frontend dropdown options
            const deviceClass = response.data.deviceClass.trim();
            
            // Check if it's one of the four options, if so, use directly
            const validOptions = [
              "Standard Notebook", 
              "Performance Notebook", 
              "Standard Desktop", 
              "Performance Desktop"
            ];
            
            if (validOptions.includes(deviceClass)) {
              applicationForm.deviceType = deviceClass;
            } else {
              // If not a valid option, try matching by keywords
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
              // If no match, user manually selects
            }
          }
          
          // If current application type is office PC renewal, check lifespan
          if (applicationForm.applicationType === 'officePcRenewal') {
            checkComputerLifespan();
          }
          
          // Reset application type
          applicationForm.applicationType = '';
          
          // Check if it's Public Use computer
          if (response.data.pcClass && response.data.pcClass.includes('Public Use')) {
            // If it's Public Use computer, force it to be special purpose computer application
            applicationForm.applicationType = 'specialPurpose';
            applicationForm.reason = '';
            ElMessage({
              type: 'info',
              message: t('当前电脑归属情况为Public Use，已自动设置为其他用途电脑申请且无法修改')
            });
            // Get cost center list
            fetchCostCenterList();
          }
          
          // Re-fetch user info to update cost center etc.
          if (response.data.ntAccount) {
            fetchUserInfo(response.data.ntAccount).then(() => {
              ElMessage({
                type: 'success',
                message: t('已更新用户信息和电脑') + ': ' + response.data.ciName
              });
            }).catch(error => {
              console.error('获取用户信息失败:', error);
              ElMessage({
                type: 'success',
                message: t('已切换到电脑') + ': ' + response.data.ciName + '，' + t('但用户信息更新失败')
              });
            });
          } else {
            ElMessage({
              type: 'success',
              message: t('已切换到电脑') + ': ' + response.data.ciName
            });
          }
        } else {
          // If no computer information is retrieved, clear related data
          myComputer.value = null;
          selectedComputer.value = '';
        }
        return response;
      }).catch(error => {
        ElMessage({
          type: 'warning',
          message: t('获取电脑信息失败')
        });
        myComputer.value = null;
        selectedComputer.value = '';
        return Promise.reject(error);
      });
    };

    // Handle application type change
    const handleApplicationTypeChange = () => {
      // Get current selected application type
      const selectedType = applicationForm.applicationType;
      
      // If no application type is selected, return
      if (!selectedType) return;
      
      // If there's computer information, check computer usage period
      if (!myComputer.value || !myComputer.value.lifeCycleStart) {
        if (selectedType === 'pcRenewalOverSixYears' || selectedType === 'pcRenewalUnderSixYears' || 
            selectedType === 'pcRenewalUnderSixYearsOld') {
          ElMessage({
            type: 'warning',
            message: t('未找到当前电脑的出厂时间信息，无法判断使用年限')
          });
          applicationForm.applicationType = ''; // Reset application type
          return;
        }
      }
      
      // If there's a selected option related to lifespan, check computer usage period
      if (selectedType === 'pcRenewalOverSixYears' || selectedType === 'pcRenewalUnderSixYears' || 
          selectedType === 'pcRenewalUnderSixYearsOld') {
        // Get start date of computer lifecycle
        const lifeCycleStartStr = myComputer.value.lifeCycleStart;
        
        // Try parsing date, supporting multiple formats
        let lifeCycleStartDate;
        try {
          // Try parsing YYYY-MM-DD format
          if (lifeCycleStartStr.includes('-')) {
            lifeCycleStartDate = new Date(lifeCycleStartStr);
          } 
          // Try parsing MM/DD/YYYY format
          else if (lifeCycleStartStr.includes('/')) {
            const parts = lifeCycleStartStr.split('/');
            if (parts.length === 3) {
              // Assume format is MM/DD/YYYY
              lifeCycleStartDate = new Date(parts[2], parts[0] - 1, parts[1]);
            }
          }
          // Other possible formats...
        } catch (error) {
          console.error('日期解析错误:', error);
          ElMessage({
            type: 'error',
            message: t('无法解析电脑生命周期开始日期')
          });
          applicationForm.applicationType = ''; // Reset application type
          return;
        }
        
        // If date parsing fails
        if (!lifeCycleStartDate || isNaN(lifeCycleStartDate.getTime())) {
          ElMessage({
            type: 'warning',
            message: t('无法确定电脑使用年限，请联系IT部门')
          });
          applicationForm.applicationType = ''; // Reset application type
          return;
        }
        
        // Get current date
        const currentDate = new Date();
        
        // Calculate year difference
        const yearsDiff = currentDate.getFullYear() - lifeCycleStartDate.getFullYear();
        
        // Handle different lifespan options
        if (selectedType === 'pcRenewalOverSixYears') {
          // Check if over six years
          if (yearsDiff < 6) {
            ElMessageBox.alert(
              t(`当前电脑使用年限为${yearsDiff}年，未超过六年，不能选择"办公电脑超六年换新"`),
              t('年限不符合要求'),
              {
                confirmButtonText: t('确定'),
                type: 'warning',
              }
            ).then(() => {
              applicationForm.applicationType = ''; // Reset application type
            }).catch(() => {
              applicationForm.applicationType = ''; // Reset application type
            });
            return;
          } else {
            // Meet conditions, set corresponding form fields
            applicationForm.costCenter = getITCostCenter(applicationForm.company);
            applicationForm.computerCondition = '新电脑';
            applicationForm.reason = '办公电脑超六年换新';
          }
        } else if (selectedType === 'pcRenewalUnderSixYears' || selectedType === 'pcRenewalUnderSixYearsOld') {
          // Check if not over six years
          if (yearsDiff >= 6) {
            ElMessageBox.alert(
              t(`当前电脑使用年限为${yearsDiff}年，已超过六年，不能选择"办公电脑未超六年换新/换旧"`),
              t('年限不符合要求'),
              {
                confirmButtonText: t('确定'),
                type: 'warning',
              }
            ).then(() => {
              applicationForm.applicationType = ''; // Reset application type
            }).catch(() => {
              applicationForm.applicationType = ''; // Reset application type
            });
            return;
          } else {
            // Meet conditions, set corresponding form fields
            if (selectedType === 'pcRenewalUnderSixYears') {
              // Show cost inclusion prompt for less than 6 years
              ElMessageBox.alert(
                t('需要更换的电脑未超过6年，该电脑更换费用将计入电脑使用人所属部门的成本中心'),
                t('费用提示'),
                {
                  confirmButtonText: t('确定'),
                  type: 'warning',
                }
              ).then(() => {
                // 用户确认后，设置表单字段
                applicationForm.costCenter = myComputer.value.costCenter || ''; // 使用用户自己的成本中心
                applicationForm.computerCondition = '新电脑';
                applicationForm.reason = ''; // 置空，让用户自己填写申请理由
              }).catch(() => {
                // User cancels, no changes are made
              });
            } else {
              // Used computer
              applicationForm.costCenter = getITCostCenter(applicationForm.company);
              applicationForm.computerCondition = '库存旧电脑';
              applicationForm.reason = '办公电脑未超六年换旧';
            }
          }
        }
      } else {
        // Handle logic for other application types
        switch (selectedType) {
          case 'secretaryNewEmployee':
            applicationForm.costCenter = getITCostCenter(applicationForm.company);
            applicationForm.computerCondition = '新电脑';
            applicationForm.reason = '秘书代申请新岗位员工电脑';
            break;
            
          case 'secretaryReplacement':
            applicationForm.costCenter = getITCostCenter(applicationForm.company);
            applicationForm.computerCondition = '库存旧电脑';
            applicationForm.reason = '秘书代申请替代岗位员工电脑';
            break;
            
          case 'secretaryIntern':
            applicationForm.costCenter = getITCostCenter(applicationForm.company);
            applicationForm.computerCondition = '库存旧电脑';
            applicationForm.reason = '秘书代申请新实习生/外服电脑';
            break;
            
          case 'specialPurpose':
            // When applying for special purpose computer, get cost center list from backend
            // Clear current cost center
            applicationForm.costCenter = '';
            // Clear application reason
            applicationForm.reason = '';
            // Get cost center list
            fetchCostCenterList();
            break;
            
          default:
            break;
        }
      }
    };
    
    // Check computer usage period
    const checkComputerLifespan = () => {
      // Get start date of computer lifecycle
      const lifeCycleStartStr = myComputer.value.lifeCycleStart;
      
      // Try parsing date, supporting multiple formats
      let lifeCycleStartDate;
      try {
        // Try parsing YYYY-MM-DD format
        if (lifeCycleStartStr.includes('-')) {
          lifeCycleStartDate = new Date(lifeCycleStartStr);
        } 
        // Try parsing MM/DD/YYYY format
        else if (lifeCycleStartStr.includes('/')) {
          const parts = lifeCycleStartStr.split('/');
          if (parts.length === 3) {
            // Assume format is MM/DD/YYYY
            lifeCycleStartDate = new Date(parts[2], parts[0] - 1, parts[1]);
          }
        }
        // Other possible formats...
      } catch (error) {
        console.error('日期解析错误:', error);
        ElMessage({
          type: 'error',
          message: '无法解析电脑生命周期开始日期'
        });
        return;
      }
      
      // If date parsing fails
      if (!lifeCycleStartDate || isNaN(lifeCycleStartDate.getTime())) {
        ElMessage({
          type: 'warning',
          message: '无法确定电脑使用年限，请联系IT部门'
        });
        return;
      }
      
      // Get current date
      const currentDate = new Date();
      
      // Calculate year difference
      const yearsDiff = currentDate.getFullYear() - lifeCycleStartDate.getFullYear();
      
      // If over six years
      if (yearsDiff >= 6) {
        // Change cost center to IT
        applicationForm.costCenter = 'IT';
        ElMessage({
          type: 'success',
          message: `电脑使用已超过${yearsDiff}年，符合换新条件，成本中心已自动设置为IT`
        });
      } else {
        // Show confirmation window
        ElMessageBox.confirm(
          `当前电脑使用年限为${yearsDiff}年，未超六年，年限内换新是否本部门承担新电脑费用（包括换成其他类型的新电脑）？`,
          '提示',
          {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'warning',
          }
        ).catch(() => {
          // User cancels, can reset application type
          applicationForm.applicationType = '';
        });
      }
    };

    // Search user - implement fuzzy search supporting name query
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
          // Add query results to result list
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

    // Application details dialog related
    const applicationDetailDialogVisible = ref(false);
    const currentApplication = ref(null);

    // Get processing status style
    const getProcessStatus = () => {
      const status = currentApplication.value?.status || '';
      
      // Try getting approval status from workflow data
      let status1 = '';
      let status2 = '';
      
      if (approvalProgress.value && approvalProgress.value.length > 1) {
        status1 = approvalProgress.value[1].status || '';
        if (approvalProgress.value.length > 2) {
          status2 = approvalProgress.value[2].status || '';
        }
      }
      
      // If any approver rejects, return error status
      if (status1 === '审批不通过' || status2 === '审批不通过' || 
          status === '审批不通过' || status === '已驳回') {
        return 'error';
      }
      
      // If all approvers pass, return success status
      else if ((status1 === '审批通过' && status2 === '审批通过') || 
               status === '审批通过' || status === '已通过') {
        return 'success';
      }
      
      // Return processing status for other cases
      else {
        return 'process';
      }
    };

    // Handle page size change
    const handleSizeChange = (newSize) => {
      pageSize.value = newSize;
      currentPage.value = 1; // Reset to first page
      getApplyList(); // Re-fetch data
    };

    // Handle current page change
    const handleCurrentChange = (newPage) => {
      currentPage.value = newPage;
      getApplyList(); // Re-fetch data
    };

    // Refresh application list
    const refreshApplyList = () => {
      getApplyList();
    };

    // Get status corresponding CSS class name
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

    // Get status corresponding icon
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

    // Get list of computers for current user
    const fetchComputerList = (userName) => {
      if (!userName) return Promise.reject(new Error('用户名为空'));
      
      return httpUtil({
        method: 'get',
        url: '/sysControl/getComputerListByUserName',
        params: { userName }
      }).then(response => {
        console.log('获取电脑列表响应:', response);
        if (response.data && response.data.list && response.data.list.length > 0) {
          // Use returned string array directly
          computerList.value = response.data.list;
          console.log('电脑列表:', computerList.value);
          // If there's a computer list and no computer is currently selected, default to select the first one
          if (response.data.list.length > 0 && !selectedComputer.value) {
            selectedComputer.value = response.data.list[0];
            applicationForm.ciName = response.data.list[0]; // Set computer name in application form
            console.log('自动选择第一个电脑:', selectedComputer.value);
            // 自动获取第一个电脑的详细信息
            handleComputerSelect(response.data.list[0]);
          }
        } else {
          // If there's no computer list, clear related data
          computerList.value = [];
          selectedComputer.value = '';
          applicationForm.ciName = '申请新电脑'; // Set to default value
        }
        return response;
      }).catch(error => {
        ElMessage({
          type: 'warning',
          message: '获取电脑列表失败'
        });
        // Clear computer list and selected computer if error occurs
        computerList.value = [];
        selectedComputer.value = '';
        applicationForm.ciName = '申请新电脑'; // Set to default value
        return Promise.reject(error);
      });
    };

    // Handle computer selection
    const handleComputerSelect = (ciName) => {
      if (!ciName) {
        // If no computer is selected, set default value
        applicationForm.ciName = '申请新电脑';
        return;
      }
      
      // Set selected computer name
      applicationForm.ciName = ciName;
      selectedComputer.value = ciName; // Ensure dropdown selection matches current selection
      
      httpUtil({
        method: 'get',
        url: '/sysControl/getComputerInfoByCiName',
        params: { ciName }
      }).then(response => {
        console.log('获取电脑详细信息响应:', response);
        if (response.data) {
          // Update content in "My Computer" module
          myComputer.value = response.data;
          console.log('更新后的电脑信息:', myComputer.value);
          
          // Update computer information in "Office PC Application" module
          // Auto-fill computer type, keeping original logic
          if (response.data.deviceClass) {
            // Map backend returned deviceClass value to frontend dropdown options
            const deviceClass = response.data.deviceClass.trim();
            
            // Check if it's one of the four options, if so, use directly
            const validOptions = [
              "Standard Notebook", 
              "Performance Notebook", 
              "Standard Desktop", 
              "Performance Desktop"
            ];
            
            if (validOptions.includes(deviceClass)) {
              applicationForm.deviceType = deviceClass;
            } else {
              // If not a valid option, try matching by keywords
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
              // If no match, user manually selects
            }
          }
          
          // Reset application type
          applicationForm.applicationType = '';
          
          // Check if it's Public Use computer
          if (response.data.pcClass && response.data.pcClass.includes('Public Use')) {
            // If it's Public Use computer, force it to be special purpose computer application
            applicationForm.applicationType = 'specialPurpose';
            applicationForm.reason = '';
            ElMessage({
              type: 'info',
              message: t('当前电脑归属情况为Public Use，已自动设置为其他用途电脑申请且无法修改')
            });
            // Get cost center list
            fetchCostCenterList();
          }
          
          // If computer information contains user information, re-fetch user info to update cost center etc.
          if (response.data.ntAccount) {
            fetchUserInfo(response.data.ntAccount).then(() => {
              ElMessage({
                type: 'success',
                message: t('已更新用户信息和电脑') + ': ' + response.data.ciName
              });
            }).catch(error => {
              console.error('获取用户信息失败:', error);
              ElMessage({
                type: 'success',
                message: t('已切换到电脑') + ': ' + response.data.ciName + '，' + t('但用户信息更新失败')
              });
            });
          } else {
            ElMessage({
              type: 'success',
              message: t('已切换到电脑') + ': ' + response.data.ciName
            });
          }
        }
      }).catch(error => {
        console.error('获取电脑信息失败:', error);
        ElMessage({
          type: 'error',
          message: t('获取电脑信息失败')
        });
      });
    };

    // Get cost center list
    const fetchCostCenterList = () => {
      httpUtil({
        method: 'get',
        url: '/sysApply/getCostCenterList'
      }).then(response => {
        if (response.data && response.data.list && Array.isArray(response.data.list)) {
          // Update cost center list, removing IT option
          costCenters.value = response.data.list.filter(center => center !== 'IT');
          
          // Remove success message prompt
          // ElMessage({
          //   type: 'success',
          //   message: '成本中心列表获取成功'
          // });
        } else {
          ElMessage({
            type: 'warning',
            message: '成本中心列表为空'
          });
          // Ensure list does not include IT
          costCenters.value = [];
        }
      }).catch(error => {
        console.error('获取成本中心列表失败:', error);
        ElMessage({
          type: 'error',
          message: '获取成本中心列表失败'
        });
        
        // Ensure list does not include IT even if error occurs
        costCenters.value = [];
      });
    };

    onMounted(() => {
      // Re-check user info
      if (!currentUser.value && userInfoStore.userInfo?.userName) {
        currentUser.value = userInfoStore.userInfo.userName;
        applicationForm.user = currentUser.value;
      }
      
      // Ensure request is made with current user's userName first
      if (currentUser.value) {
        // Get computer information and computer list, ensure computer information is loaded before computer list
        fetchMyComputer(currentUser.value)
          .then(() => {
            // Get all computers for the user
            return fetchComputerList(currentUser.value);
          })
          .then(() => {
            // Ensure selected option in dropdown matches current displayed computer
            if (myComputer.value && myComputer.value.ciName) {
              // Ensure loaded computer is in dropdown list
              if (!computerList.value.includes(myComputer.value.ciName)) {
                computerList.value.push(myComputer.value.ciName);
              }
              // Set dropdown selected value to current computer
              selectedComputer.value = myComputer.value.ciName;
            }
            // Get user info
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
      
      // Initialize fetching cost center list, ensuring IT is filtered out
      fetchCostCenterList();
      
      // Get application list
      getApplyList();
      // Add global click event listener
      document.addEventListener('click', handleDocumentClick);
    });
    
    onBeforeUnmount(() => {
      // Remove global click event listener
      document.removeEventListener('click', handleDocumentClick);
    });

    // Add computed property in setup function
    const isReasonDisabled = computed(() => {
      const appType = applicationForm.applicationType;
      return ['pcRenewalOverSixYears', 'pcRenewalUnderSixYearsOld', 
              'secretaryNewEmployee', 'secretaryReplacement', 'secretaryIntern', 'publicComputer'].includes(appType);
    });

    // Add computed property for whether cost center is disabled
    const isCostCenterDisabled = computed(() => {
      return applicationForm.applicationType !== 'specialPurpose' && applicationForm.applicationType !== '';
    });

    // Add computed property for whether application type is disabled
    const isApplicationTypeDisabled = computed(() => {
      return myComputer.value?.pcClass?.includes('Public Use') && applicationForm.applicationType === 'specialPurpose';
    });

    // Add computed property for whether it's public use computer
    const isPublicUseComputer = computed(() => {
      return myComputer.value?.pcClass?.includes('Public Use');
    });

    // Add computed property for whether computer condition is disabled
    const isComputerConditionDisabled = computed(() => {
      // When application type is not special purpose computer application, and application type has been selected, computer condition is disabled
      return applicationForm.applicationType !== '' && applicationForm.applicationType !== 'specialPurpose';
    });

    // Add placeholder text for application reason
    const reasonPlaceholder = computed(() => {
      if (applicationForm.applicationType === 'specialPurpose' || 
          (myComputer.value?.pcClass?.includes('Public Use') && applicationForm.applicationType === 'specialPurpose')) {
        return '请输入其他用途电脑申请理由';
      }
      return '请输入申请理由';
    });

    // Get assign status tag type
    const getAssignStatusTagType = (status) => {
      switch (status) {
        case '分配完成': return 'success';
        case '暂分配': return 'warning';
        case '分配中': return 'primary';
        case '已领取': return 'success';
        case '已关闭': return 'info';
        default: return 'primary';
      }
    };
    
    // View assignment progress
    const viewAssignProgress = (row) => {
      currentApplication.value = row;
      
      // Determine steps for assignment status
      let steps = [];
      let activeStep = 0;
      
      // Default steps: assigning, completed, received
      steps = [
        { 
          title: '分配中',
          description: '设备正在分配中',
        },
        { 
          title: '分配完成', 
          description: '设备已完成分配',
        },
        {
          title: '已领取',
          description: '设备已被用户领取',
        }
      ];
      
      // If status is temporarily assigned, insert temporarily assigned node
      if (row.assignStatus === '暂分配') {
        steps.splice(1, 0, {
          title: '暂分配',
          description: '设备暂时分配',
        });
        activeStep = 1; // Current in temporarily assigned stage
      } else if (row.assignStatus === '分配完成') {
        // If status is completed
        activeStep = 1;
      } else if (row.assignStatus === '已领取') {
        // If status is received
        activeStep = steps.length - 1;
      } else if (row.assignStatus === '已关闭') {
        // If status is closed, show different steps
        steps = [
          { 
            title: '分配中',
            description: '设备正在分配中',
          },
          {
            title: '已关闭',
            description: '订单已关闭',
          }
        ];
        activeStep = 1;
      } else {
        // Assigning status
        activeStep = 0;
      }
      
      assignProgress.value = steps;
      assignProgressStep.value = activeStep;
      assignProgressDialogVisible.value = true;
    };
    
    // Get assignment progress status
    const getAssignProcessStatus = () => {
      const status = currentApplication.value?.assignStatus || '';
      
      if (status === '分配完成' || status === '已领取') {
        return 'success';
      } else if (status === '暂分配') {
        return 'warning';
      } else if (status === '已关闭') {
        return 'error';
      } else {
        return 'process';
      }
    };
    
    // Get assignment step status
    const getAssignStepStatus = (index) => {
      const status = currentApplication.value?.assignStatus || '分配中';
      const currentStep = assignProgressStep.value;
      
      // Completed steps
      if (index < currentStep) {
        return 'success';
      }
      // Current step
      else if (index === currentStep) {
        return 'process';
      }
      // Uncompleted steps
      else {
        return 'wait';
      }
    };
    
    // Get status corresponding CSS class name for assignment
    const getAssignStatusClass = (status) => {
      switch (status) {
        case '分配完成':
        case '已领取':
          return 'status-success';
        case '暂分配':
          return 'status-process';
        case '已关闭':
          return 'status-error';
        case '分配中':
        default:
          return 'status-info';
      }
    };
    
    // Get status corresponding icon for assignment
    const getAssignStatusIcon = (status) => {
      switch (status) {
        case '分配完成':
          return 'el-icon-check-circle';
        case '已领取':
          return 'el-icon-success';
        case '暂分配':
          return 'el-icon-warning';
        case '已关闭':
          return 'el-icon-close-circle';
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
      // User search related
      userSearchResults,
      showUserResults,
      handleUserInput,
      handleUserBlur,
      searchUser,
      selectUser,
      // My computer related
      myComputer,
      // Computer dropdown selection related
      computerList,
      selectedComputer,
      handleComputerSelect,
      // Pagination related
      currentPage,
      pageSize,
      totalItems,
      paginatedList,
      handleSizeChange,
      handleCurrentChange,
      // Refresh application list
      refreshApplyList,
      // Status and step related
      getApprovalStep,
      getStepStatus,
      getStatusClass,
      getStatusIcon,
      getProcessStatus,
      // Assignment progress related
      assignProgressDialogVisible,
      assignProgress,
      assignProgressStep,
      // UI states
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
      isComputerConditionDisabled,
      // Translation helper
      t
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
.application-form :deep(.el-form-item__label) {
  font-weight: 500;
}

.application-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid rgba(0, 83, 137, 0.2);
  transition: all 0.3s ease;
}

.application-form :deep(.el-input__wrapper:hover) {
  border-color: rgba(0, 83, 137, 0.4);
  box-shadow: 0 2px 8px rgba(0, 83, 137, 0.1);
}

.application-form :deep(.el-input__wrapper.is-focus) {
  border-color: #005389;
  box-shadow: 0 0 0 2px rgba(0, 83, 137, 0.2);
}

.application-form :deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid rgba(0, 83, 137, 0.2);
  transition: all 0.3s ease;
}

.application-form :deep(.el-select .el-input__wrapper:hover) {
  border-color: rgba(0, 83, 137, 0.4);
  box-shadow: 0 2px 8px rgba(0, 83, 137, 0.1);
}

.application-form :deep(.el-select .el-input__wrapper.is-focus) {
  border-color: #005389;
  box-shadow: 0 0 0 2px rgba(0, 83, 137, 0.2);
}

.application-form :deep(.el-textarea__inner) {
  border-radius: 8px;
  border: 1px solid rgba(0, 83, 137, 0.2);
  transition: all 0.3s ease;
}

.application-form :deep(.el-textarea__inner:hover) {
  border-color: rgba(0, 83, 137, 0.4);
  box-shadow: 0 2px 8px rgba(0, 83, 137, 0.1);
}

.application-form :deep(.el-textarea__inner:focus) {
  border-color: #005389;
  box-shadow: 0 0 0 2px rgba(0, 83, 137, 0.2);
}

/* Radio组件蓝绿色主题 */
.application-form :deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: #005389;
  border-color: #005389;
}

.application-form :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #005389;
}

.application-form :deep(.el-radio:hover .el-radio__inner) {
  border-color: #005389;
}

/* 科技风格按钮 */
.application-form :deep(.el-button--primary) {
  background: linear-gradient(135deg, #005389, #029165);
  border: none;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 83, 137, 0.2);
  transition: all 0.3s ease;
}

.application-form :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #0068ab, #02a674);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 83, 137, 0.3);
}

.application-form :deep(.el-button--default) {
  border: 1px solid rgba(0, 83, 137, 0.3);
  color: #005389;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.application-form :deep(.el-button--default:hover) {
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

.pagination-container :deep(.el-pagination .el-select .el-input.is-focus .el-input__wrapper) {
  border-color: #005389;
  box-shadow: 0 0 0 1px rgba(0, 83, 137, 0.2);
}

.pagination-container :deep(.el-pagination .el-input__wrapper:hover) {
  border-color: rgba(0, 83, 137, 0.4);
}

.application-form {
  margin-top: 20px;
}

.top {
  margin-top: 10px;
}

.no-computer-data {
  padding: 20px;
  text-align: center;
}

/* User input container */
.user-input-container {
  position: relative;
  width: 100%;
}

/* User search results dropdown style */
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

/* Custom scrollbar style for Webkit browsers */
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
  padding: 40px 30px;
}

/* Custom approval step style */
.approval-progress-container :deep(.el-step__title.is-process) {
  color: #409EFF;
  font-size: 18px;
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

/* Increase font size for all steps */
.approval-progress-container :deep(.el-step__title) {
  font-size: 18px;
  font-weight: 600;
}

.approval-progress-container :deep(.el-step__description) {
  font-size: 15px;
}

/* Increase icon size */
.approval-progress-container :deep(.el-step__icon) {
  width: 38px;
  height: 38px;
  font-size: 18px;
}

/* Final node style */
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

/* Dialog title style */
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

/* Status detail style */
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

/* Pagination component style */
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

.pagination-container:deep(.el-pagination .el-pager li.is-active) {
  background-color: #409EFF !important;
  color: #ffffff !important;
  font-weight: normal !important;
  border-color: #409EFF !important;
}

/* Override pagination texts for internationalization */
.pagination-container:deep(.el-pagination__total) {
  &::before {
    content: attr(data-content);
    display: none;
  }
  &::after {
    content: " " attr(data-total) " " attr(data-items);
    display: none;
  }
}

html[lang="zh"] .pagination-container:deep(.el-pagination__total) {
  &::before {
    content: "共";
    display: inline;
  }
  &::after {
    content: " 条";
    display: inline;
  }
}

html[lang="en"] .pagination-container:deep(.el-pagination__total) {
  &::before {
    content: "Total ";
    display: inline;
  }
  &::after {
    content: " items";
    display: inline;
  }
}

.pagination-container:deep(.el-pagination__jump) {
  .el-pagination__goto {
    display: none;
  }
}

html[lang="zh"] .pagination-container:deep(.el-pagination__jump) {
  &::before {
    content: "前往";
    margin-right: 5px;
  }
  &::after {
    content: "页";
    margin-left: 5px;
  }
}

html[lang="en"] .pagination-container:deep(.el-pagination__jump) {
  &::before {
    content: "Go to";
    margin-right: 5px;
  }
  &::after {
    content: "page";
    margin-left: 5px;
  }
}

/* Table style */
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

/* Step description style */
.step-description {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  line-height: 1.5;
  min-height: 50px;
  font-size: 15px;
}

.step-status {
  margin-top: 4px;
  font-size: 14px;
  color: #409EFF;
  background-color: rgba(64, 158, 255, 0.1);
  padding: 3px 10px;
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

/* Custom step line style */
.custom-steps:deep(.el-step__line) {
  background-color: #dcdfe6 !important; /* Set to gray uniformly */
}

/* Override connection line style for different states */
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

/* Application details dialog style */
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

/* Responsive adjustments */
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

/* Approval progress dialog style */
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

/* Custom approval step style */
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

/* Increase font size for all steps */
.approval-progress-container :deep(.el-step__title) {
  font-size: 16px;
  font-weight: 500;
  transition: transform 0.3s ease, color 0.3s ease;
}

.approval-progress-container :deep(.el-step__description) {
  font-size: 14px;
  transition: transform 0.3s ease, color 0.3s ease;
}

/* Step animation effect */
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

/* Increase icon size and animation */
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

/* Final node style */
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

/* Step description style */
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

/* Status detail style */
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

/* Custom step line style */
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

.step-update-time, .step-reason {
  font-size: 14px;
  color: #606266;
  margin-top: 4px;
  padding: 3px 10px;
  background-color: rgba(144, 147, 153, 0.1);
  border-radius: 4px;
  display: inline-block;
}

.step-update-time {
  color: #409EFF;
  background-color: rgba(64, 158, 255, 0.1);
  margin-top: 8px;
  margin-bottom: 2px;
}

.step-reason {
  color: #409EFF;
  background-color: rgba(64, 158, 255, 0.1);
}

/* 确保鼠标悬停在活动页码上时样式不变 */
.pagination-container:deep(.el-pagination) .el-pager li.is-active:hover {
  background-color: #409EFF !important;
  color: #ffffff !important;
}
</style>

