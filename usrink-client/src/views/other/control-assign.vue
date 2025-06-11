<template>
  <div style="padding: 20px;">
    <el-card shadow="never" class="usr_card_override">
      <template #header>
        <div class="card-header">
          <span>{{ t('订单管理') }}</span>
        </div>
      </template>
      
      <!-- Search Form -->
      <el-form :model="queryForm" :inline="true" class="search-form">
        <el-form-item :label="t('电脑名')">
          <el-input v-model="queryForm.ciName" :placeholder="t('输入电脑名')" clearable />
        </el-form-item>
        <el-form-item :label="t('电脑类型')">
          <el-select v-model="queryForm.deviceType" :placeholder="t('选择电脑类型')" clearable style="width: 180px;">
            <el-option :label="t('全部')" value="" />
            <el-option label="Standard Desktop" value="Standard Desktop" />
            <el-option label="Standard Notebook" value="Standard Notebook" />
            <el-option label="Performance Notebook" value="Performance Notebook" />
            <el-option label="Performance Desktop" value="Performance Desktop" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('电脑情形')">
          <el-select v-model="queryForm.deviceSituation" :placeholder="t('选择电脑情形')" clearable style="width: 180px;">
            <el-option :label="t('全部')" value="" />
            <el-option :label="t('新电脑')" value="新电脑" />
            <el-option :label="t('库存旧电脑')" value="库存旧电脑" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('公司')">
          <el-select v-model="queryForm.company" :placeholder="t('选择公司')" clearable style="width: 180px;">
            <el-option :label="t('全部')" value="" />
            <el-option label="SGCS" value="SGCS" />
            <el-option label="SES" value="SES" />
            <el-option label="SGCC" value="SGCC" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('分配状态')">
          <el-select v-model="queryForm.assignStatus" :placeholder="t('选择分配状态')" clearable style="width: 180px;">
            <el-option :label="t('全部')" value="" />
            <el-option :label="t('分配中')" value="分配中" />
            <el-option :label="t('暂分配')" value="暂分配" />
            <el-option :label="t('分配完成')" value="分配完成" />
            <el-option :label="t('已领取')" value="已领取" />
            <el-option :label="t('已关闭')" value="已关闭" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchAssignmentList">{{ t('搜索') }}</el-button>
          <el-button @click="resetForm">{{ t('重置') }}</el-button>
        </el-form-item>
      </el-form>
      
      <!-- Data Table -->
      <el-table 
        v-loading="loading" 
        :data="assignmentList" 
        border 
        style="width: 100%; overflow-x: auto;"
        :table-layout="'fixed'"
      >
        <el-table-column type="index" :label="t('序号')" width="80" />
        <el-table-column prop="ciName" :label="t('电脑名')" width="160" />
        <el-table-column prop="deviceType" :label="t('电脑类型')" width="180" />
        <el-table-column prop="deviceSituation" :label="t('电脑情形')" width="180" />
        <el-table-column prop="approvalId" :label="t('订单号')" width="150" />
        <el-table-column prop="company" :label="t('公司')" width="120" />
        <el-table-column prop="user" :label="t('使用人')" width="250" />
        <el-table-column prop="lastLeastUser" :label="t('上一个使用者')" width="250" />
        <el-table-column prop="assigner" :label="t('分配人')" width="250" />
        <el-table-column prop="assignTime" :label="t('分配时间')" width="160" fixed="right" />
        <el-table-column prop="startTime" :label="t('创建时间')" width="160" fixed="right" />
        <el-table-column prop="assignStatus" :label="t('分配状态')" width="180" fixed="right">
          <template #default="scope">
            <el-tag 
              :type="getStatusTagType(scope.row.assignStatus)"
            >
              {{ scope.row.assignStatus === '分配中' ? t('分配中') : 
                 scope.row.assignStatus === '暂分配' ? t('暂分配') : 
                 scope.row.assignStatus === '分配完成' ? t('分配完成') : 
                 scope.row.assignStatus === '已领取' ? t('已领取') : 
                 scope.row.assignStatus === '已关闭' ? t('已关闭') : scope.row.assignStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('操作')" width="200" fixed="right">
          <template #default="scope">
            <div style="display: flex; gap: 8px; flex-wrap: wrap;">
              <!-- 已领取状态：显示禁用的已领取按钮 -->
              <el-button 
                v-if="scope.row.assignStatus === '已领取'"
                type="success" 
                size="small"
                disabled
              >
                {{ t('已领取') }}
              </el-button>
              <!-- 已关闭状态：显示禁用的已关闭按钮 -->
              <el-button 
                v-else-if="scope.row.assignStatus === '已关闭'"
                type="info" 
                size="small"
                disabled
              >
                {{ t('已关闭') }}
              </el-button>
              <!-- 分配完成状态：显示领取和关闭按钮 -->
              <template v-else-if="scope.row.assignStatus === '分配完成'">
                <el-button 
                  size="small"
                  class="receive-button"
                  @click="handleReceive(scope.row)"
                >
                  {{ t('领取') }}
                </el-button>
                <el-button 
                  type="danger" 
                  size="small"
                  @click="handleDelete(scope.row)"
                >
                  {{ t('关闭') }}
                </el-button>
              </template>
              <!-- 暂分配状态：显示再分配和关闭按钮 -->
              <template v-else-if="scope.row.assignStatus === '暂分配'">
                <el-button 
                  type="warning" 
                  size="small"
                  @click="handleAssign(scope.row)"
                >
                  {{ t('再分配') }}
                </el-button>
                <el-button 
                  type="danger" 
                  size="small"
                  @click="handleDelete(scope.row)"
                >
                  {{ t('关闭') }}
                </el-button>
              </template>
              <!-- 分配中状态：显示分配和关闭按钮 -->
              <template v-else>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="handleAssign(scope.row)"
                >
                  {{ t('分配') }}
                </el-button>
                <el-button 
                  type="danger" 
                  size="small"
                  @click="handleDelete(scope.row)"
                >
                  {{ t('关闭') }}
                </el-button>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- Pagination -->
      <div class="pagination-container">
        <el-pagination
          :current-page="pageNum"
          :page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 分配电脑弹框 -->
    <el-dialog
      v-model="assignDialogVisible"
      :title="t('电脑分配')"
      width="80%"
      class="assignment-detail-dialog"
      destroy-on-close
    >
      <div class="assignment-detail-container">
        <!-- 顶部概要信息 -->
        <div class="detail-header">
          <div class="assignment-title">
            <span class="computer-name">{{ t('电脑分配') }}</span>
            <el-tag class="status-tag" type="primary">
              {{ currentAssignRecord?.assignStatus === '分配中' ? t('分配中') : 
                 currentAssignRecord?.assignStatus === '暂分配' ? t('暂分配') : 
                 currentAssignRecord?.assignStatus === '分配完成' ? t('分配完成') : 
                 t('待分配') }}
            </el-tag>
          </div>
          <div class="assignment-info">
            <span class="info-item">
              <i class="el-icon-user"></i> {{ t('申请人') }}: {{ currentAssignRecord?.user }}
            </span>
            <span class="info-item">
              <i class="el-icon-date"></i> {{ t('分配状态') }}: {{ currentAssignRecord?.assignStatus === '分配中' ? t('分配中') : 
                                                       currentAssignRecord?.assignStatus === '暂分配' ? t('暂分配') : 
                                                       currentAssignRecord?.assignStatus === '分配完成' ? t('分配完成') : 
                                                       t('待分配') }}
            </span>
            <span class="info-item">
              <i class="el-icon-time"></i> {{ t('创建时间') }}: {{ currentAssignRecord?.startTime || t('未知') }}
            </span>
          </div>
        </div>

        <!-- 搜索表单 -->
        <div class="search-container">
          <el-form :model="computerQueryForm" :inline="true" class="search-form">
            <el-form-item :label="t('电脑名')">
              <el-input v-model="computerQueryForm.ciName" :placeholder="t('输入电脑名')" clearable style="width: 220px;" />
            </el-form-item>
            <el-form-item :label="t('NT账号')">
              <el-input v-model="computerQueryForm.ntAccount" :placeholder="t('输入NT账号')" clearable style="width: 220px;" />
            </el-form-item>
            <el-form-item :label="t('电脑类型')">
              <el-select v-model="computerQueryForm.deviceType" :placeholder="t('选择电脑类型')" clearable style="width: 220px;">
                <el-option :label="t('全部')" value="" />
                <el-option label="Standard Desktop" value="Standard Desktop" />
                <el-option label="Standard Notebook" value="Standard Notebook" />
                <el-option label="Performance Notebook" value="Performance Notebook" />
                <el-option label="Performance Desktop" value="Performance Desktop" />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('电脑情形')">
              <el-select v-model="computerQueryForm.deviceSituation" :placeholder="t('选择电脑情形')" clearable style="width: 220px;">
                <el-option :label="t('全部')" value="" />
                <el-option :label="t('新电脑')" value="新电脑" />
                <el-option :label="t('库存旧电脑')" value="库存旧电脑" />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('公司')">
              <el-select v-model="computerQueryForm.company" :placeholder="t('选择公司')" clearable style="width: 220px;">
                <el-option :label="t('全部')" value="" />
                <el-option label="SGCS" value="SGCS" />
                <el-option label="SES" value="SES" />
                <el-option label="SGCC" value="SGCC" />
              </el-select>
            </el-form-item>
            <div class="form-buttons">
              <el-button type="primary" @click="fetchComputerList" :icon="Search">{{ t('搜索') }}</el-button>
              <el-button @click="resetComputerForm" :icon="Refresh">{{ t('重置') }}</el-button>
            </div>
          </el-form>
        </div>

        <!-- 电脑列表 -->
        <div class="table-container">
          <el-table
            v-loading="computerLoading"
            :data="computerList"
            border
            style="width: 100%"
            max-height="500"
            :header-cell-style="{backgroundColor: '#f5f7fa'}"
            highlight-current-row
            @row-click="handleRowClick"
            @current-change="handleComputerSelect"
            :row-class-name="rowClassName"
          >
            <el-table-column type="radio" width="55" fixed="left">
              <template #default="scope">
                <el-radio 
                  v-model="selectedRowIndex" 
                  :label="scope.$index"
                  @change="() => handleSelectRadio(scope.row, scope.$index)"
                >&nbsp;</el-radio>
              </template>
            </el-table-column>
            <el-table-column prop="id" label="ID" width="80" fixed="left" />
            <el-table-column prop="ciName" :label="t('电脑名')" width="150" fixed="left" />
            <el-table-column prop="pcStatus" :label="t('电脑状态')" width="150" />
            <el-table-column prop="serialNumber" :label="t('序列号')" width="150" />
            <el-table-column prop="deviceClass" :label="t('设备类型')" width="150" />
            <el-table-column prop="manufacture" :label="t('制造商')" width="150" />
            <el-table-column prop="modelOrVersion" :label="t('型号')" width="120" />
            <el-table-column prop="ntAccount" :label="t('NT账号')" width="150" />
            <el-table-column prop="pcClass" :label="t('电脑归属情况')" width="180" />
            <el-table-column prop="comment" :label="t('备注')" width="150" />
            <el-table-column prop="lastName" :label="t('姓')" width="100" />
            <el-table-column prop="firstName" :label="t('名')" width="100" />
            <el-table-column prop="emailAddress" :label="t('邮箱地址')" width="200" />
            <el-table-column prop="department" :label="t('所属部门')" width="150" />
            <el-table-column prop="telephone" :label="t('电话号码')" width="150" />
            <el-table-column prop="costCenter" :label="t('成本中心')" width="120" />
            <el-table-column prop="lifeCycleStart" :label="t('出厂时间')" width="150" />
            <el-table-column :label="t('出厂日期到今天的时间')" width="250">
              <template #default="scope">
                {{ calculateYearsToToday(scope.row.lifeCycleStart) }} {{ t('年') }}
              </template>
            </el-table-column>
            <el-table-column prop="cpu" label="CPU" width="150" />
            <el-table-column prop="memory" :label="t('内存')" width="100" />
            <el-table-column prop="disk" :label="t('硬盘')" width="100" />
            <el-table-column prop="graphic" :label="t('显卡')" width="100" />
            <el-table-column prop="hardwareStatus" :label="t('硬件状态')" width="150" />
            <el-table-column prop="pr" :label="t('下单号')" width="150" />
            <el-table-column prop="po" :label="t('订单号')" width="150" />
            <el-table-column prop="vendor" :label="t('供应商公司')" width="180" />
            <el-table-column prop="company" :label="t('公司')" width="120" />
            <el-table-column prop="wbsNum" :label="t('资产号')" width="120" />
            <el-table-column prop="price" :label="t('价格')" width="100" />
            <el-table-column prop="temp" :label="t('临时分配标识')" width="120">
              <template #default="scope">
                {{ scope.row.temp === 1 ? t('是') : t('否') }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            :current-page="computerPageNum"
            :page-size="computerPageSize"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="computerTotal"
            @size-change="handleComputerSizeChange"
            @current-change="handleComputerCurrentChange"
          />
        </div>

        <!-- 底部按钮 -->
        <div class="assignment-actions">
          <div class="action-buttons">
            <el-button @click="assignDialogVisible = false" size="large">{{ t('取消') }}</el-button>
            <el-button 
              v-if="currentAssignRecord?.assignStatus !== '暂分配'" 
              type="warning" 
              @click="confirmTempAssign" 
              size="large" 
              :disabled="!selectedComputer"
            >{{ t('暂分配') }}</el-button>
            <el-button type="primary" @click="confirmAssign" size="large" :disabled="!selectedComputer">{{ t('确认分配') }}</el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 设备领取弹框 -->
    <el-dialog
      v-model="receiveDialogVisible"
      :title="t('设备领取')"
      width="500px"
      class="receive-dialog"
      :before-close="handleReceiveDialogClose"
    >
      <div class="receive-container">
        <div class="receive-info">
          <h4>{{ t('设备信息') }}</h4>
          <div class="info-row">
            <span class="label">{{ t('电脑名') }}:</span>
            <span class="value">{{ currentReceiveRecord?.ciName || t('未分配') }}</span>
          </div>
          <div class="info-row">
            <span class="label">{{ t('电脑类型') }}:</span>
            <span class="value">{{ currentReceiveRecord?.deviceType || t('未知') }}</span>
          </div>
          <div class="info-row">
            <span class="label">{{ t('使用人') }}:</span>
            <span class="value">{{ currentReceiveRecord?.user || t('未知') }}</span>
          </div>
        </div>
        
        <div class="receive-form">
          <el-form :model="receiveForm" label-width="120px">
            <el-form-item :label="t('电脑归属情况')" required>
              <el-select 
                v-model="receiveForm.pcClass" 
                :placeholder="t('请选择电脑归属情况')" 
                style="width: 100%;"
                clearable
              >
                <el-option label="External User" value="External User" />
                <el-option label="Fixed-Term User" value="Fixed-Term User" />
                <el-option label="Internal User" value="Internal User" />
                <el-option label="Non-Standard Use" value="Non-Standard Use" />
                <el-option label="Public Use(public/test/connect device)" value="Public Use(public/test/connect device)" />
                <el-option label="ShareNotebook" value="ShareNotebook" />
                <el-option label="To be assigned" value="To be assigned" />
                <el-option label="IN USE" value="IN USE" />
                <el-option label="ShareNoteBook" value="ShareNoteBook" />
                <el-option label="Scrapped" value="Scrapped" />
                <el-option label="To be scrapped" value="To be scrapped" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="receiveDialogVisible = false">{{ t('取消') }}</el-button>
          <el-button 
            type="primary" 
            @click="confirmReceive"
            :disabled="!receiveForm.pcClass"
          >
            {{ t('确认领取') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 分配完成状态关闭订单时的电脑状态更新弹框 -->
    <el-dialog
      v-model="closeDialogVisible"
      :title="t('关闭订单并更新电脑状态')"
      width="500px"
      class="close-dialog"
      :before-close="handleCloseDialogClose"
    >
      <div class="close-container">
        <div class="close-info">
          <h4>{{ t('电脑信息') }}</h4>
          <div class="info-row">
            <span class="label">{{ t('电脑名') }}:</span>
            <span class="value">{{ currentCloseRecord?.ciName || t('未分配') }}</span>
          </div>
          <div class="info-row">
            <span class="label">{{ t('电脑类型') }}:</span>
            <span class="value">{{ currentCloseRecord?.deviceType || t('未知') }}</span>
          </div>
          <div class="info-row">
            <span class="label">{{ t('使用人') }}:</span>
            <span class="value">{{ currentCloseRecord?.user || t('未知') }}</span>
          </div>
        </div>
        
        <div class="close-form">
          <el-form :model="closeForm" label-width="120px">
            <el-form-item :label="t('电脑状态')" required>
              <el-select 
                v-model="closeForm.pcStatus" 
                :placeholder="t('请选择电脑状态')" 
                style="width: 100%;"
                clearable
              >
                <el-option label="To be assigned" value="To be assigned" />
                <el-option label="In Use" value="In Use" />
                <el-option label="ShareNoteBook" value="ShareNoteBook" />
                <el-option label="Scrapped" value="Scrapped" />
                <el-option label="To be scrapped" value="To be scrapped" />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('电脑归属情况')" required>
              <el-select 
                v-model="closeForm.pcClass" 
                :placeholder="t('请选择电脑归属情况')" 
                style="width: 100%;"
                clearable
              >
                <el-option label="External User" value="External User" />
                <el-option label="Fixed-Term User" value="Fixed-Term User" />
                <el-option label="Internal User" value="Internal User" />
                <el-option label="Non-Standard Use" value="Non-Standard Use" />
                <el-option label="Public Use" value="Public Use" />
                <el-option label="ShareNotebook" value="ShareNotebook" />
                <el-option label="To be assigned" value="To be assigned" />
                <el-option label="Waiting for Return" value="Waiting for Return" />
                <el-option label="To be scrapped" value="To be scrapped" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeDialogVisible = false">{{ t('取消') }}</el-button>
          <el-button 
            type="danger" 
            @click="confirmCloseWithUpdate"
            :disabled="!closeForm.pcStatus || !closeForm.pcClass"
          >
            {{ t('确认关闭') }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import httpUtil from '@/utils/HttpUtil'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, User, Calendar, Check, Close } from '@element-plus/icons-vue'
import { useLanguageStore } from '@/stores/_frame/languageStore'

// 获取语言存储
const languageStore = useLanguageStore()

// 创建翻译字典
const translations = {
  "订单管理": { en: "Order Management", zh: "订单管理" },
  "电脑名": { en: "Computer Name", zh: "电脑名" },
  "输入电脑名": { en: "Enter Computer Name", zh: "输入电脑名" },
  "电脑类型": { en: "Computer Type", zh: "电脑类型" },
  "选择电脑类型": { en: "Select Computer Type", zh: "选择电脑类型" },
  "全部": { en: "All", zh: "全部" },
  "电脑情形": { en: "Computer Situation", zh: "电脑情形" },
  "选择电脑情形": { en: "Select Computer Situation", zh: "选择电脑情形" },
  "新电脑": { en: "New Computer", zh: "新电脑" },
  "库存旧电脑": { en: "Inventory Old Computer", zh: "库存旧电脑" },
  "公司": { en: "Company", zh: "公司" },
  "选择公司": { en: "Select Company", zh: "选择公司" },
  "分配状态": { en: "Assignment Status", zh: "分配状态" },
  "选择分配状态": { en: "Select Assignment Status", zh: "选择分配状态" },
  "分配中": { en: "Assigning", zh: "分配中" },
  "暂分配": { en: "Temporary Assignment", zh: "暂分配" },
  "分配完成": { en: "Assignment Completed", zh: "分配完成" },
  "搜索": { en: "Search", zh: "搜索" },
  "重置": { en: "Reset", zh: "重置" },
  "序号": { en: "No.", zh: "序号" },
  "订单号": { en: "Order No.", zh: "订单号" },
  "使用人": { en: "User", zh: "使用人" },
  "上一个使用者": { en: "Previous User", zh: "上一个使用者" },
  "分配人": { en: "Assigner", zh: "分配人" },
  "分配时间": { en: "Assignment Time", zh: "分配时间" },
  "创建时间": { en: "Create Time", zh: "创建时间" },
  "操作": { en: "Actions", zh: "操作" },
  "已分配": { en: "Assigned", zh: "已分配" },
  "再分配": { en: "Reassign", zh: "再分配" },
  "分配": { en: "Assign", zh: "分配" },
  "电脑分配": { en: "Computer Assignment", zh: "电脑分配" },
  "待分配": { en: "Pending Assignment", zh: "待分配" },
  "申请人": { en: "Applicant", zh: "申请人" },
  "未知": { en: "Unknown", zh: "未知" },
  "NT账号": { en: "NT Account", zh: "NT账号" },
  "输入NT账号": { en: "Enter NT Account", zh: "输入NT账号" },
  "电脑状态": { en: "Computer Status", zh: "电脑状态" },
  "序列号": { en: "Serial Number", zh: "序列号" },
  "设备类型": { en: "Device Type", zh: "设备类型" },
  "制造商": { en: "Manufacturer", zh: "制造商" },
  "型号": { en: "Model", zh: "型号" },
  "电脑归属情况": { en: "Computer Ownership", zh: "电脑归属情况" },
  "备注": { en: "Remarks", zh: "备注" },
  "姓": { en: "Last Name", zh: "姓" },
  "名": { en: "First Name", zh: "名" },
  "邮箱地址": { en: "Email Address", zh: "邮箱地址" },
  "所属部门": { en: "Department", zh: "所属部门" },
  "电话号码": { en: "Phone Number", zh: "电话号码" },
  "成本中心": { en: "Cost Center", zh: "成本中心" },
  "出厂时间": { en: "Production Date", zh: "出厂时间" },
  "出厂日期到今天的时间": { en: "Time from Production Date to Today", zh: "出厂日期到今天的时间" },
  "年": { en: "years", zh: "年" },
  "内存": { en: "Memory", zh: "内存" },
  "硬盘": { en: "Disk", zh: "硬盘" },
  "显卡": { en: "Graphics Card", zh: "显卡" },
  "硬件状态": { en: "Hardware Status", zh: "硬件状态" },
  "下单号": { en: "Purchase Requisition", zh: "下单号" },
  "供应商公司": { en: "Vendor Company", zh: "供应商公司" },
  "资产号": { en: "Asset Number", zh: "资产号" },
  "价格": { en: "Price", zh: "价格" },
  "临时分配标识": { en: "Temporary Assignment Mark", zh: "临时分配标识" },
  "是": { en: "Yes", zh: "是" },
  "否": { en: "No", zh: "否" },
  "取消": { en: "Cancel", zh: "取消" },
  "暂分配": { en: "Temporary Assign", zh: "暂分配" },
  "确认分配": { en: "Confirm Assignment", zh: "确认分配" },
  "已领取": { en: "Received", zh: "已领取" },
  "已关闭": { en: "Closed", zh: "已关闭" },
  "领取": { en: "Receive", zh: "领取" },
  "删除": { en: "Delete", zh: "删除" },
  "设备领取": { en: "Device Receiving", zh: "设备领取" },
  "设备信息": { en: "Device Information", zh: "设备信息" },
  "电脑归属情况": { en: "Computer Ownership", zh: "电脑归属情况" },
  "请选择电脑归属情况": { en: "Please Select Computer Ownership", zh: "请选择电脑归属情况" },
  "确认领取": { en: "Confirm Receiving", zh: "确认领取" },
  "未分配": { en: "Unassigned", zh: "未分配" },
  "关闭": { en: "Close", zh: "关闭" },
  "关闭订单并更新电脑状态": { en: "Close Order and Update Computer Status", zh: "关闭订单并更新电脑状态" },
  "请选择电脑状态": { en: "Please Select Computer Status", zh: "请选择电脑状态" },
  "确认关闭": { en: "Confirm Close", zh: "确认关闭" },
  "关闭订单确认": { en: "Close Order Confirmation", zh: "关闭订单确认" },
  "确认关闭订单号": { en: "Confirm closing order", zh: "确认关闭订单号" },
  "并更新电脑状态吗": { en: "and update computer status?", zh: "并更新电脑状态吗？" },
  "吗？关闭后该订单状态将变为已关闭": { en: "? After closing, the order status will become closed.", zh: "吗？关闭后该订单状态将变为已关闭。" },
  "请选择电脑状态和电脑归属情况": { en: "Please select computer status and ownership", zh: "请选择电脑状态和电脑归属情况" },
  "订单关闭成功，电脑状态已更新": { en: "Order closed successfully, computer status updated", zh: "订单关闭成功，电脑状态已更新" },
  "订单关闭失败": { en: "Failed to close order", zh: "订单关闭失败" },
  "订单关闭成功": { en: "Order closed successfully", zh: "订单关闭成功" },
  "订单关闭失败，请稍后重试": { en: "Failed to close order, please try again", zh: "订单关闭失败，请稍后重试" },
  "设备信息": { en: "Device Information", zh: "设备信息" },
  "电脑信息": { en: "Computer Information", zh: "电脑信息" },
  "电脑归属情况": { en: "Computer Ownership", zh: "电脑归属情况" }
};

// 翻译函数
const t = (key) => {
  if (!translations[key]) return key;
  return translations[key][languageStore.currentLang] || key;
};

// 计算从出厂日期到今天的年数，保留一位小数
const calculateYearsToToday = (dateString) => {
  if (!dateString) return '';
  
  const today = new Date();
  const manufactureDate = new Date(dateString);
  
  // 计算时间差（毫秒）
  const diffTime = today.getTime() - manufactureDate.getTime();
  
  // 转换为年（保留一位小数）
  const diffYears = (diffTime / (1000 * 60 * 60 * 24 * 365.25)).toFixed(1);
  return diffYears;
};

// 主表数据
const loading = ref(false)
const assignmentList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const queryForm = ref({
  ciName: '',
  deviceType: '',
  deviceSituation: '',
  company: '',
  assignStatus: '',
  pageNum: 1,
  pageSize: 10
})

// 分配弹框数据
const assignDialogVisible = ref(false)
const currentAssignRecord = ref(null)
const computerLoading = ref(false)
const computerList = ref([])
const computerTotal = ref(0)
const computerPageNum = ref(1)
const computerPageSize = ref(10)
const selectedComputer = ref(null)
const selectedRowIndex = ref(-1) // 当前选中的行索引

const computerQueryForm = ref({
  ciName: '',
  ntAccount: '',
  deviceType: '',
  deviceSituation: '',
  company: '',
  assignStatus: '',
  pageNum: 1,
  pageSize: 10
})

// 获取状态对应的Tag类型
const getStatusTagType = (status) => {
  if (status === '分配中') return 'primary'
  if (status === '暂分配') return 'warning'
  if (status === '分配完成') return 'success'
  if (status === '已领取') return 'success'
  if (status === '已关闭') return 'info'
  return ''
}

// 获取分配列表
const fetchAssignmentList = () => {
  loading.value = true
  assignmentList.value = []
  
  // 构建查询参数对象
  const params = {
    ciName: queryForm.value.ciName || null,
    deviceType: queryForm.value.deviceType || null,
    deviceSituation: queryForm.value.deviceSituation || null,
    company: queryForm.value.company || null,
    assignStatus: queryForm.value.assignStatus || null
  }
  
  // 将分页参数添加到URL中
  const urlParams = new URLSearchParams()
  urlParams.append('pageNum', pageNum.value)
  urlParams.append('pageSize', pageSize.value)
  
  console.log('发送分配列表查询参数:', params, '分页参数:', {pageNum: pageNum.value, pageSize: pageSize.value})
  
  httpUtil.post(`/sysControlAssign/getControlAssignList?${urlParams.toString()}`, params, {
    headers: {
      'Content-Type': 'application/json'
    }
  }).then(res => {
    console.log('获取分配列表响应:', res)
    if (res.data) {
      // If the response is not an array, wrap it in an array for table display
      if (res.data.list && !Array.isArray(res.data.list)) {
        assignmentList.value = [res.data.list]
      } else {
        assignmentList.value = res.data.list || []
      }
      total.value = res.data.total || 0
    }
  }).catch(err => {
    console.error('Failed to fetch assignment list:', err)
    ElMessage.error('获取分配列表失败')
  }).finally(() => {
    loading.value = false
  })
}

// 重置搜索表单
const resetForm = () => {
  queryForm.value.ciName = ''
  queryForm.value.deviceType = ''
  queryForm.value.deviceSituation = ''
  queryForm.value.company = ''
  queryForm.value.assignStatus = ''
  pageNum.value = 1
  queryForm.value.pageNum = 1
  fetchAssignmentList()
}

// 处理页面大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  queryForm.value.pageSize = val
  fetchAssignmentList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  pageNum.value = val
  queryForm.value.pageNum = val
  fetchAssignmentList()
}

// 处理分配操作
const handleAssign = (row) => {
  currentAssignRecord.value = row
  assignDialogVisible.value = true
  
  // 重置计算机查询表单
  resetComputerForm()
  
  // 将当前记录的电脑类型、电脑情形和公司信息传递到电脑查询表单中
  computerQueryForm.value.deviceType = row.deviceType || ''
  computerQueryForm.value.deviceSituation = row.deviceSituation || ''
  computerQueryForm.value.company = row.company || ''
  
  selectedComputer.value = null
  // 加载计算机列表
  fetchComputerList()
}

// 获取计算机列表
const fetchComputerList = () => {
  computerLoading.value = true
  computerList.value = []
  
  // 构建查询参数对象 - 保持与SysControlAssignDTO结构一致
  const params = {
    // SysControlAssignModel字段
    ciName: computerQueryForm.value.ciName || null,
    deviceType: computerQueryForm.value.deviceType || null,
    deviceSituation: computerQueryForm.value.deviceSituation || null,
    company: computerQueryForm.value.company || null,
    // SysControlAssignDTO额外字段
    ntAccount: computerQueryForm.value.ntAccount || null
  }
  
  // 将分页参数添加到URL中
  const urlParams = new URLSearchParams()
  urlParams.append('pageNum', computerPageNum.value)
  urlParams.append('pageSize', computerPageSize.value)
  
  // 日志输出，便于调试
  console.log('发送查询参数:', params, '分页参数:', {pageNum: computerPageNum.value, pageSize: computerPageSize.value})
  
  httpUtil.post(`/sysControl/getComputerByInfo?${urlParams.toString()}`, params, {
    headers: {
      'Content-Type': 'application/json'
    }
  }).then(res => {
    console.log('获取电脑列表响应:', res)
    if (res.data) {
      computerList.value = res.data.list || []
      computerTotal.value = res.data.total || 0
    }
  }).catch(err => {
    console.error('Failed to fetch computer list:', err)
    ElMessage.error('获取电脑列表失败')
  }).finally(() => {
    computerLoading.value = false
  })
}

// 重置计算机搜索表单
const resetComputerForm = () => {
  computerQueryForm.value.ciName = ''
  computerQueryForm.value.ntAccount = ''
  computerQueryForm.value.deviceType = ''
  computerQueryForm.value.deviceSituation = ''
  computerQueryForm.value.company = ''
  computerQueryForm.value.assignStatus = ''
  computerPageNum.value = 1
  computerQueryForm.value.pageNum = 1
  computerQueryForm.value.pageSize = 10
  selectedComputer.value = null
  selectedRowIndex.value = -1 // 重置选中行索引
}

// 处理计算机页面大小变化
const handleComputerSizeChange = (val) => {
  computerPageSize.value = val
  computerQueryForm.value.pageSize = val
  console.log('页大小变更为:', val)
  fetchComputerList()
}

// 处理计算机页码变化
const handleComputerCurrentChange = (val) => {
  computerPageNum.value = val
  computerQueryForm.value.pageNum = val
  console.log('当前页变更为:', val)
  fetchComputerList()
}

// 处理选择变化
const handleComputerSelect = (row) => {
  selectedComputer.value = row
  // 当表格的current-change事件触发时，自动将对应行设为选中
  if (row) {
    const index = computerList.value.findIndex(item => item.id === row.id)
    selectedRowIndex.value = index
  }
}

// 点击行时的处理
const handleRowClick = (row, column, event) => {
  selectedComputer.value = row
  const index = computerList.value.findIndex(item => item.id === row.id)
  selectedRowIndex.value = index
}

// 单选框选择处理
const handleSelectRadio = (row, index) => {
  selectedComputer.value = row
  // selectedRowIndex.value 已通过v-model自动设置为index
}

// 行样式
const rowClassName = ({ row, rowIndex }) => {
  if (selectedComputer.value && row.id === selectedComputer.value.id) {
    return 'selected-row'
  }
  return ''
}

// 确认分配
const confirmAssign = () => {
  if (!selectedComputer.value) {
    ElMessage.warning('请选择一台电脑进行分配')
    return
  }

  // 创建请求参数
  const params = {
    ...selectedComputer.value, // 传递所有选中电脑信息
    applicant: currentAssignRecord.value.applicant, // 使用当前分配记录的applicant作为applicant
    isTemp: 0, // 确认分配，非临时
    approvalId: currentAssignRecord.value.approvalId, // 添加订单号
  }

  ElMessageBox.confirm('确认分配该电脑?', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 发送请求到后端
    httpUtil.post('/sysControlAssign/allocateDevice', params, {
      headers: {
        'Content-Type': 'application/json'
      }
    }).then(res => {
      if (res.code === 200) {
        ElMessage.success('分配成功')
        assignDialogVisible.value = false
        // 刷新列表
        fetchAssignmentList()
      } else {
        ElMessage.error(res.message || '分配失败')
      }
    }).catch(err => {
      console.error('分配失败:', err)
      ElMessage.error('分配失败，请稍后重试')
    })
  }).catch(() => {
    // 用户取消操作
  })
}

// 暂分配
const confirmTempAssign = () => {
  if (!selectedComputer.value) {
    ElMessage.warning('请选择一台电脑进行暂分配')
    return
  }

  // 创建请求参数（与确认分配相同）
  const params = {
    ...selectedComputer.value, // 传递所有选中电脑信息
    applicant: currentAssignRecord.value.applicant, // 使用当前分配记录的applicant作为applicant
    isTemp: 1, // 临时分配
    approvalId: currentAssignRecord.value.approvalId, // 添加订单号
  }

  ElMessageBox.confirm('确认暂时分配该电脑?', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 发送请求到后端
    httpUtil.post('/sysControlAssign/allocateDevice', params, {
      headers: {
        'Content-Type': 'application/json'
      }
    }).then(res => {
      if (res.code === 200) {
        ElMessage.success('暂分配成功')
        assignDialogVisible.value = false
        // 刷新列表
        fetchAssignmentList()
      } else {
        ElMessage.error(res.message || '暂分配失败')
      }
    }).catch(err => {
      console.error('暂分配失败:', err)
      ElMessage.error('暂分配失败，请稍后重试')
    })
  }).catch(() => {
    // 用户取消操作
  })
}

// 设备领取弹框数据
const receiveDialogVisible = ref(false)
const currentReceiveRecord = ref(null)
const receiveForm = ref({
  pcClass: ''
})

// 分配完成状态关闭订单弹框数据
const closeDialogVisible = ref(false)
const currentCloseRecord = ref(null)
const closeForm = ref({
  pcStatus: '',
  pcClass: ''
})

// 处理设备领取对话框关闭
const handleReceiveDialogClose = () => {
  receiveDialogVisible.value = false
  // 重置表单
  receiveForm.value.pcClass = ''
  currentReceiveRecord.value = null
}

// 处理设备领取
const handleReceive = (row) => {
  currentReceiveRecord.value = row
  // 重置表单
  receiveForm.value.pcClass = ''
  receiveDialogVisible.value = true
}

// 处理设备领取确认
const confirmReceive = () => {
  if (!receiveForm.value.pcClass) {
    ElMessage.warning('请选择电脑归属情况')
    return
  }

  // 创建请求参数
  const params = {
    ...currentReceiveRecord.value,
    pcClass: receiveForm.value.pcClass
  }

  ElMessageBox.confirm('确认领取该电脑?', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 发送请求到后端
    httpUtil.post('/sysControlAssign/receiveDevice', params, {
      headers: {
        'Content-Type': 'application/json'
      }
    }).then(res => {
      if (res.code === 200) {
        ElMessage.success('领取成功')
        receiveDialogVisible.value = false
        // 刷新列表
        fetchAssignmentList()
      } else {
        ElMessage.error(res.message || '领取失败')
      }
    }).catch(err => {
      console.error('领取失败:', err)
      ElMessage.error('领取失败，请稍后重试')
    })
  }).catch(() => {
    // 用户取消操作
  })
}

// 处理关闭弹框关闭
const handleCloseDialogClose = () => {
  closeDialogVisible.value = false
  // 重置表单
  closeForm.value.pcStatus = ''
  closeForm.value.pcClass = ''
  currentCloseRecord.value = null
}

// 处理分配完成状态的关闭订单（需要更新电脑状态）
const handleCloseWithUpdate = (row) => {
  currentCloseRecord.value = row
  // 重置表单
  closeForm.value.pcStatus = ''
  closeForm.value.pcClass = ''
  closeDialogVisible.value = true
}

// 确认关闭并更新电脑状态
const confirmCloseWithUpdate = () => {
  if (!closeForm.value.pcStatus || !closeForm.value.pcClass) {
    ElMessage.warning(t('请选择电脑状态和电脑归属情况'))
    return
  }

  ElMessageBox.confirm(
    `${t('确认关闭订单号')} ${currentCloseRecord.value.approvalId} ${t('并更新电脑状态吗')}`,
    t('关闭订单确认'),
    {
      confirmButtonText: t('确认关闭'),
      cancelButtonText: t('取消'),
      type: 'warning'
    }
  ).then(() => {
    // 创建请求参数，包含电脑状态更新信息
    const params = {
      approvalId: currentCloseRecord.value.approvalId,
      ciName: currentCloseRecord.value.ciName,
      pcStatus: closeForm.value.pcStatus,
      pcClass: closeForm.value.pcClass
    }

    httpUtil.post('/sysControlAssign/deleteOrderWithComputerUpdate', params, {
      headers: {
        'Content-Type': 'application/json'
      }
    }).then(res => {
      if (res.code === 200) {
        ElMessage.success(t('订单关闭成功，电脑状态已更新'))
        closeDialogVisible.value = false
        // 刷新列表
        fetchAssignmentList()
      } else {
        ElMessage.error(res.message || t('订单关闭失败'))
      }
    }).catch(err => {
      console.error('订单关闭失败:', err)
      ElMessage.error(t('订单关闭失败，请稍后重试'))
    })
  }).catch(() => {
    // 用户取消操作
  })
}

// 处理删除订单
const handleDelete = (row) => {
  // 如果是分配完成状态，需要更新电脑状态
  if (row.assignStatus === '分配完成') {
    handleCloseWithUpdate(row)
    return
  }

  // 其他状态直接关闭订单
  ElMessageBox.confirm(
    `${t('确认关闭订单号')} ${row.approvalId} ${t('吗？关闭后该订单状态将变为已关闭')}`,
    t('关闭订单确认'),
    {
      confirmButtonText: t('确认关闭'),
      cancelButtonText: t('取消'),
      type: 'warning',
      dangerouslyUseHTMLString: true
    }
  ).then(() => {
    // 创建请求参数
    const params = {
      approvalId: row.approvalId
    }

    httpUtil.post('/sysControlAssign/deleteOrder', params, {
      headers: {
        'Content-Type': 'application/json'
      }
    }).then(res => {
      if (res.code === 200) {
        ElMessage.success(t('订单关闭成功'))
        // 刷新列表
        fetchAssignmentList()
      } else {
        ElMessage.error(res.message || t('订单关闭失败'))
      }
    }).catch(err => {
      console.error('订单关闭失败:', err)
      ElMessage.error(t('订单关闭失败，请稍后重试'))
    })
  }).catch(() => {
    // 用户取消操作
  })
}

// 组件挂载时加载数据
onMounted(() => {
  fetchAssignmentList()
})
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
  z-index: 1;
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
.usr_card_override :deep(.el-card__header) {
  padding: 24px;
  position: relative;
  background-color: #fff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
  color: #005389;
}

/* 卡片内容区域 */
.usr_card_override :deep(.el-card__body) {
  padding: 24px;
  position: relative;
}

/* 搜索表单科技风格 */
.search-form {
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: flex-start;
}

.search-form .el-form-item {
  margin-bottom: 15px;
  margin-right: 0;
}

.search-form :deep(.el-form-item__label) {
  font-weight: 500;
}

.search-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid rgba(0, 83, 137, 0.2);
  transition: all 0.3s ease;
}

.search-form :deep(.el-input__wrapper:hover) {
  border-color: rgba(0, 83, 137, 0.4);
  box-shadow: 0 2px 8px rgba(0, 83, 137, 0.1);
}

.search-form :deep(.el-input__wrapper.is-focus) {
  border-color: #005389;
  box-shadow: 0 0 0 2px rgba(0, 83, 137, 0.2);
}

.search-form :deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid rgba(0, 83, 137, 0.2);
  transition: all 0.3s ease;
}

.search-form :deep(.el-select .el-input__wrapper:hover) {
  border-color: rgba(0, 83, 137, 0.4);
  box-shadow: 0 2px 8px rgba(0, 83, 137, 0.1);
}

.search-form :deep(.el-select .el-input__wrapper.is-focus) {
  border-color: #005389;
  box-shadow: 0 0 0 2px rgba(0, 83, 137, 0.2);
}

/* 科技风格按钮 */
.search-form :deep(.el-button--primary) {
  background: linear-gradient(135deg, #005389, #029165);
  border: none;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 83, 137, 0.2);
  transition: all 0.3s ease;
}

.search-form :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #0068ab, #02a674);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 83, 137, 0.3);
}

.search-form :deep(.el-button--default) {
  border: 1px solid rgba(0, 83, 137, 0.3);
  color: #005389;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.search-form :deep(.el-button--default:hover) {
  border-color: #005389;
  color: #005389;
  background-color: rgba(0, 83, 137, 0.05);
}

.form-buttons {
  display: flex;
  gap: 10px;
  margin-top: 4px;
  margin-left: auto;
}

@media (max-width: 1200px) {
  .search-form {
    flex-direction: column;
  }
  
  .form-buttons {
    margin-left: 0;
    justify-content: flex-end;
    width: 100%;
  }
}

/* 分页组件样式保持一致 */
.pagination-container {
  margin-top: 20px;
  text-align: right;
  padding: 15px 20px;
  background-color: #fff;
  border-top: 1px solid #eaeaea;
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

/* 添加表格相关样式 */
.el-table {
  width: 100% !important;
  overflow-x: auto !important;
}

.el-table :deep(.el-table__body-wrapper) {
  overflow-x: auto !important;
}

.el-table :deep(.el-table__fixed-right) {
  height: auto !important;
  box-shadow: -2px 0 4px rgba(0, 0, 0, 0.12) !important;
}

.el-table :deep(.el-table__fixed) {
  height: auto !important;
  box-shadow: 2px 0 4px rgba(0, 0, 0, 0.12) !important;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.assignment-detail-dialog {
  /* Add your styles here */
}

.assignment-detail-container {
  /* Add your styles here */
}

.detail-header {
  /* Add your styles here */
}

.assignment-title {
  /* Add your styles here */
}

.status-tag {
  /* Add your styles here */
}

.assignment-info {
  /* Add your styles here */
}

.info-item {
  /* Add your styles here */
}

.search-container {
  padding: 20px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #eaeaea;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: flex-start;
}

.search-form .el-form-item {
  margin-bottom: 15px;
  margin-right: 0;
}

.search-form .el-select {
  width: 100%;
}

.form-buttons {
  display: flex;
  gap: 10px;
  margin-top: 4px;
  margin-left: auto;
}

@media (max-width: 1200px) {
  .search-form {
    flex-direction: column;
  }
  
  .form-buttons {
    margin-left: 0;
    justify-content: flex-end;
    width: 100%;
  }
}

.table-container {
  padding: 0 20px;
  background-color: #fff;
  position: relative;
  width: 100%;
  overflow-x: auto;
}

.table-container :deep(.el-table) {
  width: 100% !important;
  overflow-x: auto;
}

.table-container :deep(.el-table__body) {
  width: 100% !important;
  table-layout: fixed !important;
}

.assignment-actions {
  /* Add your styles here */
}

.action-buttons {
  /* Add your styles here */
}

.assignment-detail-dialog :deep(.el-dialog) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  animation: dialogFadeIn 0.3s ease-out;
  transform: translateY(0);
  opacity: 1;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes dialogFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.assignment-detail-dialog :deep(.el-dialog__header) {
  padding: 0;
  background: none;
  border-bottom: none;
  display: none;
}

.assignment-detail-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.assignment-detail-dialog :deep(.el-dialog__footer) {
  display: none;
}

.assignment-detail-dialog :deep(.el-dialog__headerbtn) {
  z-index: 10;
  top: 10px;
  right: 10px;
}

.assignment-detail-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #fff;
  font-size: 18px;
  transition: transform 0.3s ease;
}

.assignment-detail-dialog :deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  transform: rotate(90deg);
}

.assignment-detail-container {
  display: flex;
  flex-direction: column;
  position: relative;
  border-radius: 4px;
  overflow: hidden;
}

.detail-header {
  padding: 20px;
  background: linear-gradient(135deg, #2580bf 0%, #20b2aa 100%);
  color: #fff;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  
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

.assignment-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  position: relative;
}

.computer-name {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  animation: fadeInLeft 0.5s ease-out;
}

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

.status-tag {
  font-size: 14px;
  padding: 6px 12px;
  border-radius: 20px;
  font-weight: 500;
  transition: all 0.3s ease;
  transform-origin: right;
  animation: scaleIn 0.4s ease-out 0.2s both;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
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

.assignment-info {
  color: #fff;
  font-size: 14px;
  display: flex;
  gap: 20px;
  position: relative;
  animation: fadeInUp 0.5s ease-out 0.1s both;
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

.info-item {
  display: flex;
  align-items: center;
  gap: 5px;
  transition: transform 0.2s ease;
  
  &:hover {
    transform: translateY(-2px);
  }
}

.assignment-actions {
  padding: 20px;
  background-color: #fff;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: center;
  animation: fadeInUp 0.5s ease-out 0.4s both;
}

.action-buttons {
  display: flex;
  gap: 25px;
}

.action-buttons .el-button {
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
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  }
  
  &:active {
    transform: translateY(0);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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

.action-buttons .el-button--primary {
  background: linear-gradient(135deg, #2580bf 0%, #20b2aa 100%);
  border: none;
}

/* Table styles */
.table-container :deep(.el-table__header-wrapper) {
  background-color: #f5f7fa;
}

.table-container :deep(.el-table__row.current-row) {
  background-color: rgba(32, 178, 170, 0.1);
}

.table-container :deep(.el-table__row:hover) {
  background-color: rgba(37, 128, 191, 0.05);
}

/* 选中行的高亮样式 */
.table-container :deep(.selected-row) {
  background-color: rgba(32, 178, 170, 0.2) !important;
}

.table-container :deep(.selected-row td) {
  background-color: transparent;
}

.table-container :deep(.selected-row:hover) {
  background-color: rgba(32, 178, 170, 0.25) !important;
}

/* Radio style */
.table-container :deep(.el-radio__input.is-checked .el-radio__inner) {
  border-color: #20b2aa;
  background: #20b2aa;
}

/* 隐藏单选框文本 */
.table-container :deep(.el-radio__label) {
  display: none;
}

/* Pagination style */
.pagination-container {
  padding: 15px 20px;
  background-color: #fff;
  border-top: 1px solid #eaeaea;
}

.receive-dialog {
  border-radius: 8px;
}

.receive-dialog :deep(.el-dialog) {
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
}

.receive-container {
  padding: 20px 0;
}

.receive-info {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #005389;
}

.receive-info h4 {
  margin: 0 0 15px 0;
  color: #005389;
  font-size: 16px;
  font-weight: 600;
}

.info-row {
  display: flex;
  margin-bottom: 10px;
  align-items: center;
}

.label {
  font-weight: 500;
  color: #666;
  min-width: 80px;
  margin-right: 10px;
}

.value {
  color: #333;
  font-weight: 500;
}

.receive-form {
  padding: 0 20px;
}

.receive-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
}

.receive-form :deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid rgba(0, 83, 137, 0.2);
  transition: all 0.3s ease;
}

.receive-form :deep(.el-select .el-input__wrapper:hover) {
  border-color: rgba(0, 83, 137, 0.4);
}

.receive-form :deep(.el-select .el-input__wrapper.is-focus) {
  border-color: #005389;
  box-shadow: 0 0 0 2px rgba(0, 83, 137, 0.2);
}

/* 自定义领取按钮样式 - 蓝绿色 */
.receive-button {
  background: linear-gradient(135deg, #20b2aa, #029165) !important;
  border: none !important;
  color: #ffffff !important;
  transition: all 0.3s ease !important;
}

.receive-button:hover {
  background: linear-gradient(135deg, #1ba099, #02a674) !important;
  transform: translateY(-1px) !important;
  box-shadow: 0 4px 12px rgba(32, 178, 170, 0.3) !important;
}

.receive-button:active {
  transform: translateY(0) !important;
  box-shadow: 0 2px 8px rgba(32, 178, 170, 0.2) !important;
}

/* 关闭订单弹框样式 */
.close-dialog {
  border-radius: 8px;
}

.close-dialog :deep(.el-dialog) {
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
}

.close-container {
  padding: 20px 0;
}

.close-info {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #e8f4fd;
  border-radius: 8px;
  border-left: 4px solid #005389;
}

.close-info h4 {
  margin: 0 0 15px 0;
  color: #005389;
  font-size: 16px;
  font-weight: 600;
}

.close-form {
  padding: 0 20px;
}

.close-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
}

.close-form :deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid rgba(0, 83, 137, 0.2);
  transition: all 0.3s ease;
}

.close-form :deep(.el-select .el-input__wrapper:hover) {
  border-color: rgba(0, 83, 137, 0.4);
}

.close-form :deep(.el-select .el-input__wrapper.is-focus) {
  border-color: #005389;
  box-shadow: 0 0 0 2px rgba(0, 83, 137, 0.2);
}
</style>
