<template>
  <div style="padding: 20px;">
    <!-- 设备申请表单 -->
    <el-card shadow="never" class="usr_card_override top">
      <h3>办公电脑申请</h3>
      <el-form :model="applicationForm" ref="applicationFormRef" label-width="120px" class="application-form" :rules="rules">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="申请类别" prop="applicationType">
              <el-select v-model="applicationForm.applicationType" placeholder="请选择申请类别" style="width: 100%">
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
                <el-option label="普通笔记本" value="normalLaptop"></el-option>
                <el-option label="工作站笔记本" value="workstationLaptop"></el-option>
                <el-option label="普通台式机" value="normalDesktop"></el-option>
                <el-option label="工作站台式机" value="workstationDesktop"></el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="使用人" prop="user">
              <div class="user-input-container">
                <el-input 
                  v-model="applicationForm.user" 
                  placeholder="请输入使用人" 
                  style="width: 100%"
                  clearable
                  @input="handleUserInput"
                  @blur="handleUserBlur">
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
                <el-radio label="否（非标系统）">否（非标系统）</el-radio>
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
        <el-table-column label="申请时间" prop="createdAt"></el-table-column>
        <el-table-column label="申请类型" prop="applicationType">
          <template #default="{ row }">
            {{ getApplicationTypeName(row.applicationType) }}
          </template>
        </el-table-column>
        <el-table-column label="电脑类型" prop="deviceType">
          <template #default="{ row }">
            {{ getDeviceTypeName(row.deviceType) }}
          </template>
        </el-table-column>
        <el-table-column label="使用人" prop="user"></el-table-column>
        <el-table-column label="申请理由" prop="reason"></el-table-column>
        <el-table-column label="更新时间" prop="updatedAt"></el-table-column>
        <el-table-column label="状态" prop="status">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" @click="viewApprovalProgress(row)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="text" @click="viewApplicationDetails(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 审批进度弹窗 -->
    <el-dialog v-model="approvalProgressDialogVisible" title="审批进度">
      <div v-if="approvalProgress">
        <el-steps :active="approvalProgressStep" finish-status="success" style="margin-top: 20px;">
          <el-step 
            v-for="(step, index) in approvalProgress" 
            :key="index" 
            :title="step.title" 
            :description="step.description">
          </el-step>
        </el-steps>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue';
import httpUtil from "@/utils/HttpUtil";
import { ElMessage } from 'element-plus';
import { useUserInfoStore } from "@/stores/_frame/userInfoStore";

export default {
  setup() {
    // 获取当前用户信息
    const userInfoStore = useUserInfoStore();
    const currentUser = userInfoStore.userInfo?.username || '';

    // 设备申请表单数据
    const applicationForm = reactive({
      applicationType: '', // 申请类别
      deviceType: '', // 电脑类型
      costCenter: '', // 成本中心
      company: '', // 所属公司
      user: currentUser, // 使用人，默认当前用户名
      responsible: '', // 责任人
      computerCondition: '新电脑', // 电脑情形，默认新电脑
      companySys: '是（公司系统）', // 公司系统，默认是
      reason: '', // 申请理由
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

    // 获取用户信息（成本中心、所属公司、责任人）
    const fetchUserInfo = (userName) => {
      if (!userName) return;
      
      httpUtil.get("/sysApply/getUserInfoByUserName", {
        params: { userName }
      }).then(res => {
        if (res.data) {
          // 设置成本中心
          if (res.data.department) {
            applicationForm.costCenter = res.data.department;
            // 如果成本中心不在列表中，添加到列表
            if (!costCenters.value.includes(res.data.department)) {
              costCenters.value.push(res.data.department);
            }
          }
          
          // 设置所属公司 - 这里需要根据实际情况处理
          // 如果后端返回了公司信息，则使用后端返回的
          if (res.data.company) {
            applicationForm.company = res.data.company;
            // 如果公司不在列表中，添加到列表
            if (!companies.value.includes(res.data.company)) {
              companies.value.push(res.data.company);
            }
          }
          
          // 设置责任人 - 这里需要根据实际情况处理
          // 如果后端返回了责任人信息，则使用后端返回的
          if (res.data.responsibility) {
            applicationForm.responsible = res.data.responsibility;
            // 如果责任人不在列表中，添加到列表
            if (!responsiblePersons.value.includes(res.data.responsibility)) {
              responsiblePersons.value.push(res.data.responsibility);
            }
          }
        }
      }).catch(err => {
        console.error("获取用户信息失败:", err);
        ElMessage({
          type: 'error',
          message: '获取用户信息失败'
        });
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
    
    // 搜索用户
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
    
    // 选择用户
    const selectUser = (user) => {
      applicationForm.user = user.userName;
      showUserResults.value = false;
      
      // 获取用户信息
      fetchUserInfo(user.userName);
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
          httpUtil.post("/sysApply/submitApply", applicationForm).then(res => {
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
      applicationForm.user = currentUser;
      applicationForm.computerCondition = '新电脑';
      applicationForm.companySys = '是（公司系统）';
      // 重新获取当前用户信息
      fetchUserInfo(currentUser);
    };

    // 查看申请详情
    const viewApplicationDetails = (row) => {
      // 实现查看详情逻辑
      console.log("查看申请详情:", row);
    };

    // 查看审批进度
    const viewApprovalProgress = (row) => {
      httpUtil.get("/sysApply/getApproversByAprrovalId", {
        params: { approvalId: row.approvalId }
      }).then(res => {
        const flowRoles = res.data.list;
        approvalProgress.value = [
          { title: '已提交', description: flowRoles.username || '未知用户' },
          { title: '审核中', description: flowRoles.approver1 || '暂无审批' },
          { title: '已批准', description: flowRoles.approver2 || '暂无批准' }
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
        case '审核中': return 1;
        case '已批准': return 2;
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
        'normalLaptop': '普通笔记本',
        'workstationLaptop': '工作站笔记本',
        'normalDesktop': '普通台式机',
        'workstationDesktop': '工作站台式机'
      };
      return types[type] || type;
    };

    onMounted(() => {
      // 获取当前用户信息
      fetchUserInfo(currentUser);
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
      submitApplication,
      resetForm,
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
      selectUser
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
</style>
