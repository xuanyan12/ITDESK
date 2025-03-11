<template>
  <div style="padding: 20px;">
    <!-- 设备申请表单 -->
    <el-card shadow="never" class="usr_card_override top">
      <el-form :model="applicationForm" ref="applicationFormRef" label-width="100px" class="application-form">
        <el-form-item label="设备大类" prop="deviceCategory" :rules="[{ required: true, message: '请选择设备大类', trigger: 'blur' }]">
          <el-select v-model="applicationForm.deviceCategory" placeholder="请选择设备大类" @change="onDeviceCategoryChange">
            <el-option label="电脑" value="computer"></el-option>
            <el-option label="手机" value="phone"></el-option>
            <el-option label="显示器" value="monitor"></el-option>
            <el-option label="打印机" value="printer"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="设备类型" prop="deviceType" :rules="[{ required: true, message: '请选择设备类型', trigger: 'blur' }]">
          <el-select v-model="applicationForm.deviceType" placeholder="请选择设备类型" @change="onDeviceTypeChange">
            <el-option v-for="type in deviceTypes" :key="type" :label="type" :value="type"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="设备选择" prop="deviceName" :rules="[{ required: true, message: '请选择设备', trigger: 'blur' }]">
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

        <el-form-item label="数量" prop="quantity" :rules="[{ required: true, message: '请输入数量', trigger: 'blur' }]">
          <el-input v-model="applicationForm.quantity" type="number" :min="1" :max="maxQuantity" placeholder="请输入数量"></el-input>
        </el-form-item>

        <el-form-item label="申请理由" prop="reason" :rules="[{ required: true, message: '请输入申请理由', trigger: 'blur' }]">
          <el-input v-model="applicationForm.reason" type="textarea" placeholder="请输入申请理由" rows="4"></el-input>
        </el-form-item>

        <el-form-item label="期望交付时间" prop="expectedDelivery" :rules="[{ required: true, message: '请选择期望交付时间', trigger: 'change' }]">
          <el-date-picker v-model="applicationForm.expectedDelivery" type="date" placeholder="选择期望交付时间"></el-date-picker>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitApplication">提交申请</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 申请状态显示 -->
    <el-card shadow="never" class="usr_card_override top" style="margin-top: 20px;">
      <h3>我的申请状态</h3>
      <el-table :data="applicationList" :loading="loading" style="width: 100%;">
        <el-table-column label="设备名称" prop="deviceName"></el-table-column>
        <el-table-column label="设备大类" prop="deviceCategory"></el-table-column>
        <el-table-column label="设备类型" prop="deviceType"></el-table-column>
        <el-table-column label="数量" prop="quantity"></el-table-column>
        <el-table-column label="申请理由" prop="reason"></el-table-column>
        <el-table-column label="期望交付时间" prop="expectedDelivery"></el-table-column>
        <el-table-column label="申请状态" prop="status">
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

    <!-- 查看申请详情弹窗 -->
    <el-dialog v-model="viewApplicationDialogVisible" title="申请详情">
      <div v-if="selectedApplication">
        <el-form :model="selectedApplication" label-width="100px">
          <el-form-item label="设备名称">
            <el-input v-model="selectedApplication.deviceName" disabled></el-input>
          </el-form-item>
          <el-form-item label="设备类型">
            <el-input v-model="selectedApplication.deviceType" disabled></el-input>
          </el-form-item>
          <el-form-item label="数量">
            <el-input v-model="selectedApplication.quantity" disabled></el-input>
          </el-form-item>
          <el-form-item label="申请理由">
            <el-input v-model="selectedApplication.reason" disabled type="textarea"></el-input>
          </el-form-item>
          <el-form-item label="期望交付时间">
            <el-input v-model="selectedApplication.expectedDelivery" disabled></el-input>
          </el-form-item>
          <el-form-item label="申请状态">
            <el-input v-model="selectedApplication.status" disabled></el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="viewApplicationDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 审批进度弹窗 -->
    <el-dialog v-model="approvalProgressDialogVisible" title="审批进度">
      <div v-if="approvalProgress">
        <el-steps :active="approvalProgressStep" finish-status="success" style="margin-top: 20px;">
          <el-step v-for="(step, index) in approvalProgress" :key="index" :title="step" :description="step"></el-step>
        </el-steps>
      </div>
      <template #footer>
        <el-button @click="approvalProgressDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      applicationForm: {
        deviceCategory: '',  // 新增设备大类
        deviceType: '',
        deviceName: '',
        quantity: '',
        reason: '',
        expectedDelivery: ''
      },
      applicationList: [],
      loading: false,
      devices: [],
      filteredDevices: [],
      maxQuantity: 0,
      viewApplicationDialogVisible: false,
      approvalProgressDialogVisible: false,
      selectedApplication: null,
      approvalProgress: [],
      approvalProgressStep: 0,
      deviceCategories: ['computer', 'phone', 'monitor', 'printer'],  // 设备大类
      deviceTypes: [],  // 存储设备类型
    };
  },
  methods: {
    onDeviceCategoryChange() {
      this.deviceTypes = this.getDeviceTypesByCategory(this.applicationForm.deviceCategory); // 根据大类筛选设备类型
      this.applicationForm.deviceType = '';
      this.filteredDevices = this.devices.filter(device => device.category === this.applicationForm.deviceCategory);
      this.applicationForm.deviceName = '';
      this.maxQuantity = 0;
    },
    onDeviceTypeChange() {
      this.filteredDevices = this.devices.filter(device => device.type === this.applicationForm.deviceType);
      this.applicationForm.deviceName = '';
      this.maxQuantity = 0;
    },
    fetchDevices() {
      // 模拟从数据库获取设备数据，设备数据中加入 category 字段
      this.devices = [
        { id: 1, name: 'MacBook Pro', category: 'computer', type: 'laptop', quantity: 5 },
        { id: 2, name: 'Dell XPS 13', category: 'computer', type: 'laptop', quantity: 2 },
        { id: 3, name: 'iPhone 14', category: 'phone', type: 'smartphone', quantity: 0 },
        { id: 4, name: 'Samsung Galaxy S23', category: 'phone', type: 'smartphone', quantity: 10 },
        { id: 5, name: 'Dell UltraSharp 27', category: 'monitor', type: 'LED', quantity: 3 },
        { id: 6, name: 'HP LaserJet Pro', category: 'printer', type: 'laser', quantity: 0 }
      ];
    },
    getDeviceTypesByCategory(category) {
      const types = {
        computer: ['laptop', 'desktop'],
        phone: ['smartphone', 'tablet'],
        monitor: ['LED', 'OLED'],
        printer: ['laser', 'inkjet']
      };
      return types[category] || [];
    },
    submitApplication() {
      if (this.$refs.applicationFormRef.validate()) {
        const newApplication = { ...this.applicationForm, status: 'Pending' };
        this.applicationList.push(newApplication);
        this.$message.success('设备申请提交成功！');
        this.resetForm();
      }
    },
    resetForm() {
      this.applicationForm.deviceCategory = '';
      this.applicationForm.deviceType = '';
      this.applicationForm.deviceName = '';
      this.applicationForm.quantity = '';
      this.applicationForm.reason = '';
      this.applicationForm.expectedDelivery = '';
    },
    viewApplicationDetails(row) {
      this.selectedApplication = row;
      this.viewApplicationDialogVisible = true;
    },
    viewApprovalProgress(row) {
      // 模拟审批进度
      this.approvalProgress = ['已提交', '审核中', '已批准'];
      this.approvalProgressStep = this.approvalProgress.indexOf(row.status);
      this.approvalProgressDialogVisible = true;
    },
    statusTagType(status) {
      if (status === 'Pending') return 'warning';
      if (status === 'Approved') return 'success';
      if (status === 'Rejected') return 'danger';
      return '';
    }
  },
  mounted() {
    this.fetchDevices(); // 获取设备数据
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
  background-color: #409EFF;
}

.application-form .el-button:hover {
  background-color: #66b1ff;
}

.usr_card_override.top .el-form {
  display: flex;
  flex-wrap: wrap;
}

.usr_card_override.top .el-form .el-form-item {
  margin-bottom: 0;
  margin-right: 0;
}

/* 表格部分样式 */
.el-table {
  margin-top: 20px;
}
</style>
