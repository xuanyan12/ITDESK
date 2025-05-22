<script setup>
import {onMounted, ref, watch, computed} from "vue";
import httpUtil from "@/utils/HttpUtil";
import {ElMessage} from "element-plus";
import UsrDescriptions from "@/components/_frame/common/usr-descriptions.vue";
import UsrDescriptionsItem from "@/components/_frame/common/usr-descriptions-item.vue";
import { useLanguageStore } from '@/stores/_frame/languageStore';

const queryForm = ref({
    userName: '',
    location: '',
    status: '0',
    startTime: '',
    endTime: '',
    pageNum: 1,
    pageSize: 10
})
const queryDateRange = ref([])
const loading = ref(false)
const logLoginList = ref([])
const total = ref(0)

// Import language store
const languageStore = useLanguageStore();
const currentLang = computed(() => languageStore.currentLang);

// Language text definitions
const langText = computed(() => {
    return {
        // Form labels and placeholders
        username: currentLang.value === 'zh' ? '用户名' : 'Username',
        loginAddress: currentLang.value === 'zh' ? '登录地址' : 'Login Address',
        loginTime: currentLang.value === 'zh' ? '登录时间' : 'Login Time',
        status: currentLang.value === 'zh' ? '状态' : 'Status',
        startTime: currentLang.value === 'zh' ? '开始时间' : 'Start Time',
        endTime: currentLang.value === 'zh' ? '结束时间' : 'End Time',
        selectStatus: currentLang.value === 'zh' ? '选择状态' : 'Select Status',
        normal: currentLang.value === 'zh' ? '正常' : 'Normal',
        failed: currentLang.value === 'zh' ? '失败' : 'Failed',
        
        // Buttons
        search: currentLang.value === 'zh' ? '搜索' : 'Search',
        deleteByDate: currentLang.value === 'zh' ? '按日期删除' : 'Delete by Date',
        deleteAll: currentLang.value === 'zh' ? '删除全部' : 'Delete All',
        details: currentLang.value === 'zh' ? '详情' : 'Details',
        delete: currentLang.value === 'zh' ? '删除' : 'Delete',
        cancel: currentLang.value === 'zh' ? '取消' : 'Cancel',
        confirm: currentLang.value === 'zh' ? '确定' : 'Confirm',
        close: currentLang.value === 'zh' ? '关闭' : 'Close',
        
        // Table headers
        recordId: currentLang.value === 'zh' ? '记录ID' : 'Record ID',
        requestMethod: currentLang.value === 'zh' ? '请求方式' : 'Request Method',
        requestIP: currentLang.value === 'zh' ? '请求IP' : 'Request IP',
        browser: currentLang.value === 'zh' ? '浏览器' : 'Browser',
        os: currentLang.value === 'zh' ? '操作系统' : 'Operating System',
        loginDuration: currentLang.value === 'zh' ? '登录耗时(MS)' : 'Login Duration (MS)',
        operations: currentLang.value === 'zh' ? '操作' : 'Operations',
        
        // Dialog titles and messages
        deleteConfirmation: currentLang.value === 'zh' ? '删除确认' : 'Delete Confirmation',
        deleteDateRangeConfirm: (startTime, endTime) => {
            return currentLang.value === 'zh' 
                ? `确定删除【${startTime}】-【${endTime}】的登录日志吗？`
                : `Are you sure you want to delete login logs from ${startTime} to ${endTime}?`;
        },
        deleteAllConfirm: currentLang.value === 'zh' 
            ? '确定清空全部登录日志吗？'
            : 'Are you sure you want to delete all login logs?',
        deleteLogConfirm: (userName) => {
            return currentLang.value === 'zh'
                ? `确定删除【${userName}】的这条登录日志吗？`
                : `Are you sure you want to delete this login log for ${userName}?`;
        },
        pleaseSelectDateRange: currentLang.value === 'zh'
            ? '请选择要删除的日期范围！'
            : 'Please select a date range to delete!',
        noData: currentLang.value === 'zh' ? '暂无数据' : 'No Data',
        
        // Log details
        logDetails: currentLang.value === 'zh' ? '日志详情' : 'Log Details',
        logNumber: currentLang.value === 'zh' ? '日志编号' : 'Log Number',
        requestURL: currentLang.value === 'zh' ? '请求URL' : 'Request URL',
        requestParams: currentLang.value === 'zh' ? '请求参数' : 'Request Parameters',
        duration: currentLang.value === 'zh' ? '耗时(MS)' : 'Duration (MS)',
        result: currentLang.value === 'zh' ? '结果' : 'Result'
    }
});

/**
 * 监听查询条件日期范围的变化
 */
watch(queryDateRange, (newVal) => {
    // 日期范围为空时，清空查询条件的开始时间和结束时间
    if (!newVal || newVal.length === 0) {
        queryForm.value.startTime = ''
        queryForm.value.endTime = ''
        return
    }
    queryForm.value.startTime = newVal[0]
    queryForm.value.endTime = newVal[1]
})

onMounted(() => {
    // 初始化登录日志列表
    selectLogLoginListData()
})

/**
 * 查询登录日志列表数据
 */
const selectLogLoginListData = () => {
    loading.value = true
    httpUtil.post("/sysLogLogin/selectSysLogLoginList", {...queryForm.value}).then(res => {
        logLoginList.value = res.data.list || [];
        total.value = res.data.total || 0
    }).catch(err => {
        console.error(err)
    }).finally(() => {
        loading.value = false
    })
}

/**
 * 分页查询
 * @param val 当前页码
 */
const handleCurrentChange = (val) => {
    queryForm.value.pageNum = val
    selectLogLoginListData()
}

/**
 * 搜索日志
 */
const queryLogLoginListData = () => {
    // 重置页码
    queryForm.value.pageNum = 1
    selectLogLoginListData()
}

const deleteForm = ref({
    startTime: '',
    endTime: '',
})
const deleteDateRange = ref([])
const deleteDateRangeTips = ref('')
const deleteDateRangeVisible = ref(false)
const deleteDateRangeIng = ref(false)

/**
 * 监听删除条件日期范围的变化
 */
watch(deleteDateRange, (newVal) => {
    if (!newVal || newVal.length === 0) {
        deleteForm.value.startTime = ''
        deleteForm.value.endTime = ''
        return
    }
    deleteForm.value.startTime = newVal[0]
    deleteForm.value.endTime = newVal[1]
})

/**
 * 按日期删除日志Dialog
 */
const deleteLogLoginByDateDialog = () => {
    if (deleteForm.value.startTime === '' || deleteForm.value.endTime === '') {
        ElMessage.warning(langText.value.pleaseSelectDateRange)
        return
    }

    deleteDateRangeTips.value = langText.value.deleteDateRangeConfirm(deleteForm.value.startTime, deleteForm.value.endTime)
    deleteDateRangeVisible.value = true
}

/**
 * 按日期删除日志
 */
const deleteLogLoginByDate = () => {
    deleteDateRangeIng.value = true
    httpUtil.post("/sysLogLogin/deleteSysLogLoginByTime", {...deleteForm.value}).then(res => {
        selectLogLoginListData()
    }).catch(err => {
        console.error(err)
    }).finally(() => {
        deleteDateRangeVisible.value = false
        deleteDateRangeIng.value = false
    })
}

const deleteAllTips = ref('')
const deleteAllVisible = ref(false)
const deleteAllIng = ref(false)

/**
 * 清空全部日志Dialog
 */
const deleteAllDialog = () => {
    deleteAllTips.value = langText.value.deleteAllConfirm
    deleteAllVisible.value = true
}

/**
 * 清空全部日志
 */
const deleteAll = () => {
    deleteAllIng.value = true
    httpUtil.post("/sysLogLogin/cleanSysLogLogin", {}).then(res => {
        selectLogLoginListData()
    }).catch(err => {
        console.error(err)
    }).finally(() => {
        deleteAllVisible.value = false
        deleteAllIng.value = false
    })
}

const deleteLogId = ref('')
const deleteByIdVisible = ref(false)
const deleteByIdTips = ref('')
const deleteByIdIng = ref(false)

/**
 * 删除日志Dialog
 * @param row
 */
const deleteLogLoginDialog = (row) => {
    deleteLogId.value = row.logId
    deleteByIdTips.value = langText.value.deleteLogConfirm(row.userName)
    deleteByIdVisible.value = true
}

/**
 * 删除日志
 */
const deleteLogLoginById = () => {
    deleteByIdIng.value = true
    httpUtil.post("/sysLogLogin/deleteSysLogLogin", {logId: deleteLogId.value}).then(res => {
        selectLogLoginListData()
    }).catch(err => {
        console.error(err)
    }).finally(() => {
        deleteByIdVisible.value = false
        deleteByIdIng.value = false
    })
}

const logLoginInfo = ref({})
const logLoginInfoVisible = ref(false)
const logLoginInfoIng = ref(false)

/**
 * 日志详情Dialog
 * @param row
 */
const logLoginInfoDialog = (row) => {
    logLoginInfo.value = {}
    logLoginInfoVisible.value = true
    logLoginInfoIng.value = true
    httpUtil.post("/sysLogLogin/selectSysLogLoginInfo", {logId: row.logId}).then(res => {
        logLoginInfo.value = res.data || {}
    }).catch(err => {
        console.error(err)
        logLoginInfo.value = {}
    }).finally(() => {
        logLoginInfoIng.value = false
    })
}

</script>

<template>
    <div class="page">
        <el-card shadow="never" class="usr_card_override top">
            <el-form :inline="true" :model="queryForm">
                <el-form-item :label="langText.username">
                    <el-input v-model="queryForm.userName" maxlength="10" :placeholder="langText.username" clearable
                              style="width: 180px"/>
                </el-form-item>
                <el-form-item :label="langText.loginAddress">
                    <el-input v-model="queryForm.location" maxlength="10" :placeholder="langText.loginAddress" clearable
                              style="width: 180px"/>
                </el-form-item>
                <el-form-item :label="langText.loginTime">
                    <el-date-picker
                        v-model="queryDateRange"
                        type="daterange"
                        :editable="false"
                        :start-placeholder="langText.startTime"
                        :end-placeholder="langText.endTime"
                        size="default"
                        value-format="YYYY-MM-DD"
                    />
                </el-form-item>
                <el-form-item :label="langText.status">
                    <el-select v-model="queryForm.status" size="default" :placeholder="langText.selectStatus" clearable
                               style="width: 180px">
                        <el-option :label="langText.normal" value="0"/>
                        <el-option :label="langText.failed" value="-1"/>
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" plain @click="queryLogLoginListData">{{ langText.search }}</el-button>
                </el-form-item>
            </el-form>
        </el-card>
        <div class="top_btn_panel">
            <el-form :inline="true" :model="deleteForm">
                <el-date-picker
                    v-model="deleteDateRange"
                    type="daterange"
                    :editable="false"
                    :start-placeholder="langText.startTime"
                    :end-placeholder="langText.endTime"
                    size="default"
                    value-format="YYYY-MM-DD"
                />
                <el-button type="warning" plain @click="deleteLogLoginByDateDialog">{{ langText.deleteByDate }}</el-button>
                <el-button type="danger" plain @click="deleteAllDialog">{{ langText.deleteAll }}</el-button>
            </el-form>
        </div>
        <el-card v-loading="loading" shadow="never" class="usr_card_override content">
            <el-table
                class="log_login_table"
                :data="logLoginList"
                row-key="logId">
                <el-table-column :label="langText.recordId" width="100" align="center">
                    <template #default="scope">
                        {{ scope.row.logId }}
                    </template>
                </el-table-column>
                <el-table-column :label="langText.username" width="120">
                    <template #default="scope">
                        {{ scope.row.userName }}
                    </template>
                </el-table-column>
                <el-table-column :label="langText.requestMethod" width="130" align="center">
                    <template #default="scope">
                        {{ scope.row.reqType }}
                    </template>
                </el-table-column>
                <el-table-column :label="langText.requestIP" width="150" align="center">
                    <template #default="scope">
                        {{ scope.row.ipAddr }}
                    </template>
                </el-table-column>
                <el-table-column :label="langText.loginAddress" width="180">
                    <template #default="scope">
                        {{ scope.row.location }}
                    </template>
                </el-table-column>
                <el-table-column :label="langText.browser" width="120" align="center">
                    <template #default="scope">
                        {{ scope.row.browser }}
                    </template>
                </el-table-column>
                <el-table-column :label="langText.os" width="120" align="center">
                    <template #default="scope">
                        {{ scope.row.os }}
                    </template>
                </el-table-column>
                <el-table-column :label="langText.loginDuration" width="120" align="center">
                    <template #default="scope">
                        {{ scope.row.costTime }}
                    </template>
                </el-table-column>
                <el-table-column :label="langText.loginTime" width="200">
                    <template #default="scope">
                        {{ scope.row.createTime }}
                    </template>
                </el-table-column>
                <el-table-column :label="langText.status" align="center">
                    <template #default="scope">
                        <el-tag v-if="scope.row.status === 0">{{ langText.normal }}</el-tag>
                        <el-tag v-else-if="scope.row.status === -1" type="danger">{{ langText.failed }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column fixed="right" :label="langText.operations" width="220">
                    <template #default="scope">
                        <div class="action_btn">
                            <el-button type="primary" plain @click="logLoginInfoDialog(scope.row)">{{ langText.details }}</el-button>
                            <el-button type="info" plain @click="deleteLogLoginDialog(scope.row)">{{ langText.delete }}</el-button>
                        </div>
                    </template>
                </el-table-column>
                <template #empty>
                    <el-empty :description="langText.noData"/>
                </template>
            </el-table>
            <el-pagination background layout="prev, pager, next" :current-page="queryForm.pageNum"
                           @current-change="handleCurrentChange" :page-size="queryForm.pageSize" :total="total"/>
        </el-card>
        <!-- 按日期删除日志Dialog -->
        <el-dialog v-model="deleteDateRangeVisible" :title="langText.deleteConfirmation" width="500">
            <span>{{ deleteDateRangeTips }}</span>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="deleteDateRangeVisible = false">{{ langText.cancel }}</el-button>
                    <el-button type="primary" @click="deleteLogLoginByDate" :loading="deleteDateRangeIng">{{ langText.confirm }}
                    </el-button>
                </div>
            </template>
        </el-dialog>
        <!-- 清空全部日志Dialog -->
        <el-dialog v-model="deleteAllVisible" :title="langText.deleteConfirmation" width="500">
            <span>{{ deleteAllTips }}</span>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="deleteAllVisible = false">{{ langText.cancel }}</el-button>
                    <el-button type="primary" @click="deleteAll" :loading="deleteAllIng">{{ langText.confirm }}</el-button>
                </div>
            </template>
        </el-dialog>
        <!-- 删除日志Dialog -->
        <el-dialog v-model="deleteByIdVisible" :title="langText.deleteConfirmation" width="500">
            <span>{{ deleteByIdTips }}</span>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="deleteByIdVisible = false">{{ langText.cancel }}</el-button>
                    <el-button type="primary" @click="deleteLogLoginById" :loading="deleteByIdIng">{{ langText.confirm }}</el-button>
                </div>
            </template>
        </el-dialog>
        <!-- 日志详情Dialog -->
        <el-drawer class="usr_drawer_log_login_info" v-model="logLoginInfoVisible" direction="rtl" size="500">
            <template #header>
                <h4>{{ langText.logDetails }}</h4>
            </template>
            <template #default>
                <div class="usr_log_login_panel" v-loading="logLoginInfoIng">
                    <el-scrollbar>
                        <div v-if="Object.keys(logLoginInfo).length !== 0"
                             style="padding: 0 20px">
                            <usr-descriptions direction="column">
                                <usr-descriptions-item>
                                    <template #name>
                                        <el-text type="info">{{ langText.logNumber }}：</el-text>
                                    </template>
                                    <template #desc>{{ logLoginInfo.logId }}</template>
                                </usr-descriptions-item>
                                <usr-descriptions-item>
                                    <template #name>
                                        <el-text type="info">{{ langText.username }}：</el-text>
                                    </template>
                                    <template #desc>{{ logLoginInfo.userName }}</template>
                                </usr-descriptions-item>
                                <usr-descriptions-item>
                                    <template #name>
                                        <el-text type="info">{{ langText.requestURL }}：</el-text>
                                    </template>
                                    <template #desc>{{ logLoginInfo.reqUrl }}</template>
                                </usr-descriptions-item>
                                <usr-descriptions-item>
                                    <template #name>
                                        <el-text type="info">{{ langText.requestMethod }}：</el-text>
                                    </template>
                                    <template #desc>
                                        <el-tag>{{ logLoginInfo.reqType }}</el-tag>
                                    </template>
                                </usr-descriptions-item>
                                <usr-descriptions-item>
                                    <template #name>
                                        <el-text type="info">{{ langText.requestParams }}：</el-text>
                                    </template>
                                    <template #desc>
                                        <div class="code_info">
                                            {{ logLoginInfo.reqParam }}
                                        </div>
                                    </template>
                                </usr-descriptions-item>
                                <usr-descriptions-item>
                                    <template #name>
                                        <el-text type="info">{{ langText.requestIP }}：</el-text>
                                    </template>
                                    <template #desc>{{ logLoginInfo.ipAddr }}</template>
                                </usr-descriptions-item>
                                <usr-descriptions-item>
                                    <template #name>
                                        <el-text type="info">{{ langText.loginAddress }}：</el-text>
                                    </template>
                                    <template #desc>{{ logLoginInfo.location }}</template>
                                </usr-descriptions-item>
                                <usr-descriptions-item>
                                    <template #name>
                                        <el-text type="info">{{ langText.browser }}：</el-text>
                                    </template>
                                    <template #desc>{{ logLoginInfo.browser }}</template>
                                </usr-descriptions-item>
                                <usr-descriptions-item>
                                    <template #name>
                                        <el-text type="info">{{ langText.os }}：</el-text>
                                    </template>
                                    <template #desc>{{ logLoginInfo.os }}</template>
                                </usr-descriptions-item>
                                <usr-descriptions-item>
                                    <template #name>
                                        <el-text type="info">{{ langText.loginTime }}：</el-text>
                                    </template>
                                    <template #desc>{{ logLoginInfo.createTime }}</template>
                                </usr-descriptions-item>
                                <usr-descriptions-item>
                                    <template #name>
                                        <el-text type="info">{{ langText.duration }}：</el-text>
                                    </template>
                                    <template #desc>{{ logLoginInfo.costTime }}</template>
                                </usr-descriptions-item>
                                <usr-descriptions-item>
                                    <template #name>
                                        <el-text type="info">{{ langText.status }}：</el-text>
                                    </template>
                                    <template #desc>
                                        <el-tag v-if="logLoginInfo.status === 0">{{ langText.normal }}</el-tag>
                                        <el-tag v-else-if="logLoginInfo.status === -1" type="danger">{{ langText.failed }}</el-tag>
                                    </template>
                                </usr-descriptions-item>
                                <usr-descriptions-item>
                                    <template #name>
                                        <el-text type="info">{{ langText.result }}：</el-text>
                                    </template>
                                    <template #desc>
                                        <div class="code_info">
                                            {{ logLoginInfo.result }}
                                        </div>
                                    </template>
                                </usr-descriptions-item>
                            </usr-descriptions>
                        </div>
                        <el-empty v-else :description="langText.noData"/>
                    </el-scrollbar>
                </div>
            </template>
            <template #footer>
                <div style="flex: auto">
                    <el-button @click="logLoginInfoVisible = false">{{ langText.close }}</el-button>
                </div>
            </template>
        </el-drawer>
    </div>
</template>

<style scoped>
.page {
    padding: 20px;
}

.usr_card_override.top .el-form {
    display: flex;
    flex-wrap: wrap;
    column-gap: 20px;
    row-gap: 15px;
}

.usr_card_override.top .el-form .el-form-item {
    margin-bottom: 0;
    margin-right: 0;
}

.top_btn_panel {
    display: flex;
    margin: 15px 0 15px 0;
    justify-content: flex-end;
}

.top_btn_panel .el-form {
    display: flex;
    flex-wrap: wrap;
    column-gap: 10px;
    row-gap: 10px;
}

.top_btn_panel .el-form .el-button + .el-button {
    margin-left: 0;
}

.usr_card_override.content .el-table {
    margin-bottom: 20px;
}

:deep(.log_login_table) .action_btn {
    display: flex;
    flex-wrap: wrap;
    column-gap: 10px;
}

:deep(.log_login_table) .action_btn .el-button + .el-button {
    margin-left: 0;
}

:deep(.usr_drawer_log_login_info) .el-drawer__header {
    padding: 20px;
    margin-bottom: 0;
}

:deep(.usr_drawer_log_login_info) .el-drawer__body {
    padding: 0;
    overflow: hidden;
}

:deep(.usr_drawer_log_login_info) .el-drawer__footer {
    padding-top: 20px;
}

.usr_log_login_panel {
    height: calc(100vh - 60px - 72px);
}

.code_info {
    border: 1px solid #e0e0e0;
    padding: 10px;
    background-color: #f9f9f9;
    border-radius: 3px;
}
</style>