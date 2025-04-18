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
          <el-descriptions-item label="ciName" width="300">{{ myComputer.ciName }}</el-descriptions-item>
          <el-descriptions-item label="ntAccount" width="300">{{ myComputer.ntAccount }}</el-descriptions-item>
          <el-descriptions-item label="department" width="300">{{ myComputer.department }}</el-descriptions-item>
          <el-descriptions-item label="lastName" width="300">{{ myComputer.lastName }}</el-descriptions-item>
          <el-descriptions-item label="firstName" width="300">{{ myComputer.firstName }}</el-descriptions-item>
          <el-descriptions-item label="pcStatus" width="300">{{ myComputer.pcStatus }}</el-descriptions-item>
          <el-descriptions-item label="lifeCycleStart" width="300">{{ myComputer.lifeCycleStart }}</el-descriptions-item>
          <el-descriptions-item label="costCenter" width="300">{{ myComputer.costCenter }}</el-descriptions-item>
          <el-descriptions-item label="manufacturer" v-if="myComputer.manufacturer" width="300">{{ myComputer.manufacturer }}</el-descriptions-item>
          <el-descriptions-item label="model" v-if="myComputer.model" width="300">{{ myComputer.model }}</el-descriptions-item>
          <el-descriptions-item label="serialNumber" v-if="myComputer.serialNumber" width="300">{{ myComputer.serialNumber }}</el-descriptions-item>
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
      <h3>办公电脑申请</h3>
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
                      {{ user.userName }}
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
              <el-select v-model="applicationForm.costCenter" placeholder="请选择成本中心" style="width: 100%">
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
              <el-select v-model="applicationForm.applicationType" placeholder="请选择申请类别" style="width: 100%" @change="handleApplicationTypeChange">
                <el-option label="办公电脑换新" value="officePcRenewal"></el-option>
                <el-option label="新正式员工电脑" value="newEmployeePc"></el-option>
                <el-option label="新实习生/外服电脑" value="internPc"></el-option>
                <el-option label="其他用途电脑" value="otherPurpose"></el-option>
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
              <el-radio-group v-model="applicationForm.computerCondition">
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
              <el-select 
                v-model="applicationForm.reason" 
                placeholder="请选择或输入申请理由" 
                style="width: 100%" 
                clearable 
                allow-create 
                filterable>
                <el-option label="超年限换新" value="超年限换新"></el-option>
                <el-option label="新岗位新电脑" value="新岗位新电脑"></el-option>
                <el-option label="替代岗位库存电脑" value="替代岗位库存电脑"></el-option>
              </el-select>
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
      <h3>我的申请状态</h3>
      <el-table :data="applicationList" :loading="loading" style="width: 100%;">
        <el-table-column label="申请时间" prop="createdAt" width="150"></el-table-column>
        <el-table-column label="使用人" prop="userName" width="100"></el-table-column>
        <el-table-column label="责任人" prop="responsibilityName" width="100"></el-table-column>
        <el-table-column label="电脑类型" prop="deviceType" width="150">
          <template #default="{ row }">
            {{ getDeviceTypeName(row.deviceType) }}
          </template>
        </el-table-column>
        <el-table-column label="电脑名称" prop="ciName" width="150">
          <template #default="{ row }">
            {{ row.ciName || '申请新电脑' }}
          </template>
        </el-table-column>
        <el-table-column label="申请类别" prop="deviceCategory" width="150">
          <template #default="{ row }">
            {{ getApplicationTypeName(row.deviceCategory) }}
          </template>
        </el-table-column>
        <el-table-column label="申请理由" prop="reason" show-overflow-tooltip></el-table-column>
        <el-table-column label="更新时间" prop="updatedAt" width="150"></el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" @click="viewApprovalProgress(row)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text @click="viewApplicationDetails(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 审批进度弹窗 -->
    <el-dialog v-model="approvalProgressDialogVisible" title="审批进度" width="700px">
      <div v-if="approvalProgress" class="approval-progress-container">
        <el-steps :active="approvalProgressStep" finish-status="success" align-center>
          <el-step 
            v-for="(step, index) in approvalProgress" 
            :key="index" 
            :title="step.title" 
            :status="getStepStatus(index, step.title)"
            :description="step.description">
          </el-step>
        </el-steps>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="approvalProgressDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 申请详情弹窗 -->
    <el-dialog v-model="applicationDetailDialogVisible" title="申请详情" width="650px">
      <div v-if="currentApplication">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="需要更换的电脑">{{ currentApplication.ciName || '申请新电脑' }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ currentApplication.userName }}</el-descriptions-item>
          <el-descriptions-item label="申请类别">{{ getApplicationTypeName(currentApplication.deviceCategory) }}</el-descriptions-item>
          <el-descriptions-item label="电脑类型">{{ getDeviceTypeName(currentApplication.deviceType) }}</el-descriptions-item>
          <el-descriptions-item label="成本中心">{{ currentApplication.costCenter }}</el-descriptions-item>
          <el-descriptions-item label="所属公司">{{ currentApplication.company }}</el-descriptions-item>
          <el-descriptions-item label="责任人">{{ currentApplication.responsibilityName }}</el-descriptions-item>
          <el-descriptions-item label="电脑情形">{{ currentApplication.deviceSituation }}</el-descriptions-item>
          <el-descriptions-item label="公司系统">{{ currentApplication.companySystem }}</el-descriptions-item>
          <el-descriptions-item label="申请理由" :span="2">{{ currentApplication.reason }}</el-descriptions-item>
          <el-descriptions-item label="申请状态">
            <el-tag :type="statusTagType(currentApplication.status)">{{ currentApplication.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="有效期">{{ currentApplication.timestamp }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentApplication.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ currentApplication.updatedAt }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue';
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
      user: [{ required: true, message: '请输入使用人', trigger: 'blur' }],
      responsible: [{ required: true, message: '请选择责任人', trigger: 'change' }],
      reason: [{ required: true, message: '请选择或输入申请理由', trigger: 'change' }]
    };

    const applicationFormRef = ref(null);
    const applicationList = ref([]);
    const loading = ref(false);
    const approvalProgressDialogVisible = ref(false);
    const approvalProgress = ref([]);
    const approvalProgressStep = ref(0);

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
          if (response.data.department) {
            applicationForm.costCenter = response.data.department;
            if (!costCenters.value.includes(response.data.department)) {
              costCenters.value.push(response.data.department);
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
    
    // 确认用户变更 - 当用户按下回车键或者手动触发时
    const confirmUserChange = () => {
      // 只有当用户名不为空且与当前用户不同时才发送请求
      const newUserName = applicationForm.user;
      if (newUserName && newUserName.trim() !== '' && newUserName !== currentUser.value) {
        // 先清空电脑信息，避免显示旧信息
        myComputer.value = null;
        // 清空电脑列表和选择的电脑
        computerList.value = [];
        selectedComputer.value = '';
        applicationForm.ciName = '申请新电脑'; // 重置为默认值
        
        // 获取新用户的信息
        Promise.all([
          fetchUserInfo(newUserName),
          fetchMyComputer(newUserName),
          fetchComputerList(newUserName)
        ]).then(() => {
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
            message: '获取用户数据失败，请检查用户名是否正确'
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
      httpUtil.post("/sysApply/getApplyList").then(res => {
        applicationList.value = res.data.list || [];
      }).catch(err => {
        console.error("获取申请列表失败:", err);
      }).finally(() => {
        loading.value = false;
      });
    };

    // 提交申请
    const submitApplication = () => {
      applicationFormRef.value.validate((valid) => {
        if (valid) {
          loading.value = true;
          
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
        applicationType: '', // 申请类别
        deviceType: currentDeviceType, // 电脑类型 - 保留自动填充的值
        costCenter: currentCostCenter, // 成本中心 - 保留
        company: currentCompany, // 所属公司 - 保留
        user: currentUser, // 使用人 - 保留
        responsible: currentResponsible, // 责任人 - 保留
        computerCondition: '新电脑', // 电脑情形，默认新电脑
        companySys: '是（公司系统）', // 公司系统，默认是
        reason: '', // 申请理由
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
      httpUtil.get("/sysApply/getApproversByAprrovalId", {
        params: { approvalId: row.approvalId }
      }).then(res => {
        const flowRoles = res.data.list;
        approvalProgress.value = [
          { title: '已提交', description: flowRoles.username || '未知用户' },
          { title: '审批中', description: flowRoles.approver1 || '暂无审批' },
          { title: '审核中', description: flowRoles.approver2 || '暂无批准' }
        ];
        approvalProgressStep.value = getApprovalStep(row.status);
        approvalProgressDialogVisible.value = true;
      }).catch(err => {
        console.error("获取审批进度失败:", err);
        ElMessage({
          type: 'error',
          message: '获取审批进度失败'
        });
      });
    };

    // 获取审批步骤
    const getApprovalStep = (status) => {
      switch (status) {
        case '已提交': return 0;
        case '审批中': return 1;
        case '审核中': return 2;
        default: return 0;
      }
    };

    // 获取状态标签类型
    const statusTagType = (status) => {
      switch (status) {
        case '审批中': return 'warning';
        case '审批通过': return 'success';
        case '审批驳回': return 'danger';
        default: return 'info';
      }
    };

    // 获取申请类型名称
    const getApplicationTypeName = (type) => {
      const types = {
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
      if (applicationForm.applicationType === 'officePcRenewal') {
        // 只有当有电脑信息时才进行判断
        if (myComputer.value && myComputer.value.lifeCycleStart) {
          checkComputerLifespan();
        } else {
          ElMessage({
            type: 'warning',
            message: '未找到当前用户的电脑信息，无法判断使用年限'
          });
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

    // 搜索用户 - 只搜索用户名，不发送获取详细信息请求
    const searchUser = () => {
      const userName = applicationForm.user;
      if (!userName || userName.trim() === '') {
        showUserResults.value = false;
        return;
      }
      
      httpUtil.get("/sysApply/getUserInfoByUserName", {
        params: { userName }
      }).then(res => {
        if (res.data) {
          // 将查询结果添加到结果列表
          userSearchResults.value = [res.data];
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

    // 获取步骤状态
    const getStepStatus = (index, title) => {
      if (index < approvalProgressStep.value) {
        return 'success';
      } else if (index === approvalProgressStep.value) {
        // 如果当前步骤是"审批中"，返回处理中状态
        return title === '审批中' ? 'process' : 'wait';
      } else {
        return 'wait';
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
          
          // 如果电脑信息中包含用户信息，重新获取用户信息以更新成本中心等数据
          if (response.data.ntAccount) {
            fetchUserInfo(response.data.ntAccount).then(() => {
              ElMessage({
                type: 'success',
                message: '已更新用户信息和电脑: ' + ciName
              });
            }).catch(error => {
              console.error('获取用户信息失败:', error);
              ElMessage({
                type: 'success',
                message: '已切换到电脑: ' + ciName + '，但用户信息更新失败'
              });
            });
          } else {
            ElMessage({
              type: 'success',
              message: '已切换到电脑: ' + ciName
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

    onMounted(() => {
      // 重新检查用户信息
      if (!currentUser.value && userInfoStore.userInfo?.userName) {
        currentUser.value = userInfoStore.userInfo.userName;
        applicationForm.user = currentUser.value;
      }
      
      // 确保首先使用当前登录用户的userName发送请求获取数据
      if (currentUser.value) {
        // 同时获取用户信息、电脑信息和电脑列表
        Promise.all([
          fetchUserInfo(currentUser.value),
          fetchMyComputer(currentUser.value),
          fetchComputerList(currentUser.value)
        ]).catch(error => {
          console.error('初始化获取数据失败:', error);
        });
      } else {
        ElMessage({
          type: 'warning',
          message: '未能获取当前用户信息，请手动填写'
        });
      }
      
      // 获取申请列表
      getApplyList();
      // 添加全局点击事件监听
      document.addEventListener('click', handleDocumentClick);
    });
    
    onBeforeUnmount(() => {
      // 移除全局点击事件监听
      document.removeEventListener('click', handleDocumentClick);
    });

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
      // 审批进度相关
      getApprovalStep,
      getStepStatus
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
</style>
