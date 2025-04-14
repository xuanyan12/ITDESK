<template>
  <div style="padding: 20px;">
    <!-- 设备申请表单 -->
    <el-card shadow="never" class="usr_card_override top">
      <el-form :model="applicationForm" ref="applicationFormRef" label-width="100px" class="application-form">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="设备大类" prop="deviceCategory">
              <el-select v-model="applicationForm.deviceCategory" placeholder="请选择设备大类" @change="onDeviceCategoryChange">
                <el-option label="电脑" value="computer"></el-option>
                <el-option label="手机" value="phone"></el-option>
                <el-option label="显示器" value="monitor"></el-option>
                <el-option label="打印机" value="printer"></el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="设备类型" prop="deviceType">
              <el-select v-model="applicationForm.deviceType" placeholder="请选择设备类型" @change="onDeviceTypeChange">
                <el-option v-for="type in deviceTypes" :key="type" :label="type" :value="type"></el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="设备选择" prop="deviceName">
              <el-select v-model="applicationForm.deviceName" placeholder="请选择设备">
                <el-option
                  v-for="device in filteredDevices"
                  :key="device.id"
                  :label="`${device.name} (${device.quantity} 可用)`"
                  :value="device.name"
                  :disabled="device.quantity === 0"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="8" style="padding-top:10px">
            <el-form-item label="数量" prop="quantity">
              <el-input v-model="applicationForm.quantity" type="number" :min="1" :max="maxQuantity" placeholder="请输入数量"></el-input>
            </el-form-item>
          </el-col>

          <el-col :span="16" style="padding-top:10px">
            <el-form-item label="申请理由" prop="reason">
              <el-input v-model="applicationForm.reason" type="textarea" placeholder="请输入申请理由" rows="1"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" @click="submitApplication">提交申请</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 申请状态显示 -->
    <el-card shadow="never" class="usr_card_override top" style="margin-top: 20px;">
      <h3>我的申请状态</h3>
      <el-table :data="applicationList" :loading="loading" style="width: 100%;">
        <el-table-column label="流程发起时间" prop="createdAt"></el-table-column>
        <el-table-column label="设备名称" prop="deviceName"></el-table-column>
        <el-table-column label="数量" prop="quantity"></el-table-column>
        <el-table-column label="申请理由" prop="reason"></el-table-column>
        <el-table-column label="流程更新时间" prop="updatedAt"></el-table-column>
        <el-table-column label="申请状态" prop="status">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" @click="viewApprovalProgress(row)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="text" @click="viewApplicationDetails(row)">撤回申请</el-button>
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
import { ref, reactive, onMounted } from 'vue';
import httpUtil from "@/utils/HttpUtil";

export default {
  setup() {
    // 设备申请表单数据
    const applicationForm = reactive({
      deviceCategory: '',
      deviceType: '',
      deviceName: '',
      quantity: '',
      reason: '',
      pageNum: 1,
      pageSize: 6
    });

    let flowRoles = reactive({
      username: '',
      approver1: '',
      approver2: ''
    });

    const agree = ref(false);
    const applicationList = ref([]);
    const loading = ref(false);
    const devices = ref([]);
    const filteredDevices = ref([]);
    const maxQuantity = ref(0);
    const approvalProgressDialogVisible = ref(false);
    const selectedApplication = ref(null);
    const approvalProgress = ref([]);
    const approvalProgressStep = ref(0);
    const deviceCategories = ref(['computer', 'phone', 'monitor', 'printer']); // 设备大类
    const deviceTypes = ref([]); // 存储设备类型

    // 根据设备大类获取设备类型
    const getDeviceTypesByCategory = (category) => {
      const types = {
        computer: ['laptop', 'desktop'],
        phone: ['smartphone', 'tablet'],
        monitor: ['LED', 'OLED'],
        printer: ['laser', 'inkjet']
      };
      return types[category] || [];
    };

    // 获取设备数据
    const fetchDevices = () => {
      devices.value = [
        { id: 1, name: 'MacBook Pro', category: 'computer', type: 'laptop', quantity: 5 },
        { id: 2, name: 'Dell XPS 13', category: 'computer', type: 'laptop', quantity: 2 },
        { id: 3, name: 'iPhone 14', category: 'phone', type: 'smartphone', quantity: 0 },
        { id: 4, name: 'Samsung Galaxy S23', category: 'phone', type: 'smartphone', quantity: 10 },
        { id: 5, name: 'Dell UltraSharp 27', category: 'monitor', type: 'LED', quantity: 3 },
        { id: 6, name: 'HP LaserJet Pro', category: 'printer', type: 'laser', quantity: 0 }
      ];
    };

    // 设备大类变化时，筛选设备类型和设备名称
    const onDeviceCategoryChange = () => {
      deviceTypes.value = getDeviceTypesByCategory(applicationForm.deviceCategory);
      applicationForm.deviceType = '';
      filteredDevices.value = devices.value.filter(device => device.category === applicationForm.deviceCategory);
      applicationForm.deviceName = '';
      maxQuantity.value = 0;
    };

    // 设备类型变化时，筛选设备名称
    const onDeviceTypeChange = () => {
      filteredDevices.value = devices.value.filter(device => device.type === applicationForm.deviceType);
      applicationForm.deviceName = '';
      maxQuantity.value = 0;
    };

    // 提交申请
    const submitApplication = () => {
      const newApplication = { ...applicationForm, status: 'Pending' };
      httpUtil.post("/sysApply/submitApply", newApplication, {
        headers: {
          'Content-Type': 'application/json'
        }
      }).then(res => {
        getApplyList();
        this.$message.success('设备申请提交成功！');
      }).catch(err => {
        console.error(err);
        this.$message.error('设备申请提交失败！');
      }).finally(() => {
        resetForm();
      });
    };

    // 重置表单
    const resetForm = () => {
      applicationForm.deviceCategory = '';
      applicationForm.deviceType = '';
      applicationForm.deviceName = '';
      applicationForm.quantity = '';
      applicationForm.reason = '';
      agree.value = false;
    };

    // 获取申请列表
    const getApplyList = () => {
      loading.value = true;
      httpUtil.post("/sysApply/getApplyList", applicationForm).then(res => {
        applicationList.value = res.data.list || [];
      }).catch(err => {
        console.log("出错了");
        console.error(err);
      }).finally(() => {
        loading.value = false;
      });
    };


    // 发起请求获取审批人信息
    const viewApprovalProgress = (row) => {
      httpUtil.get("/sysApply/getApproversByAprrovalId", {
        params: {
          approvalId: row.approvalId  // 确保参数名是 aprrovalId
        },
        headers: {
          'Content-Type': 'application/json'
        }
      }).then(res => {
        // 假设返回的审批人数据是 res.data
        flowRoles = res.data.list;
        console.log(row);
        
        // 可以在这里更新 approvalProgress 或其他地方使用 approvers
        console.log('审批人数据：', flowRoles);
        
        // 例如更新流程中的审批人
        approvalProgress.value = [
          { title: '已提交', description: flowRoles.username || '未知用户' },
          { title: '审核中', description: flowRoles.approver1 || '暂无审批' },
          { title: '已批准', description: flowRoles.approver2 || '暂无批准' }
        ];
        approvalProgressStep.value = approvalProgress.value.findIndex(step => step.title === row.status);
        approvalProgressDialogVisible.value = true;
        // 展示成功消息
        // this.$message.success('审批人信息获取成功！');
      }).catch(err => {
        console.error(err);
        // 失败时弹出错误提示
        // this.$message.error('获取审批人信息失败！');
      }).finally(() => {
        // 可以在这里进行一些清理操作或加载状态更新
      });
    };

    // 审批进度viewApprovalProgress
// const viewApprovalProgress = (row) => {

//     // 根据 row 中的相关数据动态更新 flowRoles
//     const flowRoles = reactive({
//       username: row.username || '',  // 确保从 row 中获取 username
//       approver1: row.approver1 || '',  // 从 row 获取 approver1
//       approver2: row.approver2 || ''   // 从 row 获取 approver2
//     });
//     console.log(row.approvalId);

//     // 发送网络请求获取approver1,approver2
//     httpUtil.post("/sysApply/submitApply", row.approvalId, {
//         headers: {
//           'Content-Type': 'application/json'
//         }
//       }).then(res => {
//         getApplyList();
//         this.$message.success('设备申请提交成功！');
//       }).catch(err => {
//         console.error(err);
//         this.$message.error('设备申请提交失败！');
//       }).finally(() => {
//         resetForm();
//       });

//     approvalProgress.value = [
//         { title: '已提交', description: flowRoles.username || '未知用户' },
//         { title: '审核中', description: flowRoles.approver1 || '暂无审批' },
//         { title: '已批准', description: flowRoles.approver2 || '暂无批准' }
//     ];

//     approvalProgressStep.value = approvalProgress.value.findIndex(step => step.title === row.status);
//     approvalProgressDialogVisible.value = true;
// };

    // 申请状态标签样式
    const statusTagType = (status) => {
      if (status === 'Pending') return 'warning';
      if (status === 'Approved') return 'success';
      if (status === 'Rejected') return 'danger';
      return '';
    };

    // 组件加载时获取设备数据和申请列表
    onMounted(() => {
      fetchDevices();
      getApplyList();
    });

    return {
      applicationForm,
      agree,
      applicationList,
      loading,
      devices,
      filteredDevices,
      maxQuantity,
      approvalProgressDialogVisible,
      selectedApplication,
      approvalProgress,
      approvalProgressStep,
      deviceCategories,
      deviceTypes,
      flowRoles,
      onDeviceCategoryChange,
      onDeviceTypeChange,
      submitApplication,
      resetForm,
      getApplyList,
      viewApprovalProgress,
      statusTagType
    };
  }
};
</script>

<style scoped>
.application-form {
  width: 100%;
  margin: 20px auto;
}

.application-form .el-form-item {
  margin-bottom: 12px;
}

.application-form .el-button {
  width: 100%;
  max-width: 200px;
  margin-top: 10px;
  border-radius: 6px;
  background-color: rgb(236, 245, 255);
  color: rgb(64, 158, 255);
}

.application-form .el-button:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
  color: white;
}

.usr_card_override.top .el-form {
  display: flex;
  flex-wrap: wrap;
}

.usr_card_override.top .el-form .el-form-item {
  margin-bottom: 0;
  margin-right: 0;
}

.el-form-item .el-checkbox {
  display: inline-block;
  width: auto;
  margin-right: 10px;
}

.el-table {
  margin-top: 20px;
}
</style>
