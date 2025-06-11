<script setup>
import {onMounted, ref, watch, computed} from "vue";
import httpUtil from "@/utils/HttpUtil";
import {ElMessage} from "element-plus";
import UsrDescriptions from "@/components/_frame/common/usr-descriptions.vue";
import UsrDescriptionsItem from "@/components/_frame/common/usr-descriptions-item.vue";
import { useLanguageStore } from "@/stores/_frame/languageStore";
import { View } from "@element-plus/icons-vue";

// 获取语言存储
const languageStore = useLanguageStore();

// 创建翻译字典
const translations = {
    "收件人": { en: "Recipient", zh: "收件人" },
    "收件人邮箱": { en: "Recipient Email", zh: "收件人邮箱" },
    "邮件主题": { en: "Email Subject", zh: "邮件主题" },
    "发送时间": { en: "Send Time", zh: "发送时间" },
    "开始时间": { en: "Start Time", zh: "开始时间" },
    "结束时间": { en: "End Time", zh: "结束时间" },
    "状态": { en: "Status", zh: "状态" },
    "选择状态": { en: "Select Status", zh: "选择状态" },
    "搜索": { en: "Search", zh: "搜索" },
    "记录ID": { en: "Record ID", zh: "记录ID" },
    "成功": { en: "Success", zh: "成功" },
    "失败": { en: "Failed", zh: "失败" },
    "操作": { en: "Actions", zh: "操作" },
    "详情": { en: "Details", zh: "详情" },
    "暂无数据": { en: "No Data", zh: "暂无数据" },
    "邮件详情": { en: "Email Details", zh: "邮件详情" },
    "记录ID：": { en: "Record ID: ", zh: "记录ID：" },
    "收件人：": { en: "Recipient: ", zh: "收件人：" },
    "邮件主题：": { en: "Subject: ", zh: "邮件主题：" },
    "发送时间：": { en: "Send Time: ", zh: "发送时间：" },
    "发送状态：": { en: "Status: ", zh: "发送状态：" },
    "错误信息：": { en: "Error: ", zh: "错误信息：" },
    "邮件内容：": { en: "Content: ", zh: "邮件内容：" },
    "关闭": { en: "Close", zh: "关闭" },
    "预览邮件": { en: "Preview Email", zh: "预览邮件" },
    "邮件预览": { en: "Email Preview", zh: "邮件预览" },
};

// 翻译函数
const t = (key) => {
    if (!translations[key]) return key;
    return translations[key][languageStore.currentLang] || key;
};

const queryForm = ref({
    toEmail: '',       // 收件人邮箱
    subject: '',       // 邮件主题
    status: '',        // 发送状态
    startTime: '',     // 开始时间
    endTime: '',       // 结束时间
    pageNum: 1,
    pageSize: 10
})
const queryDateRange = ref([])
const loading = ref(false)
const emailLogList = ref([])
const total = ref(0)

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
    // 初始化邮箱日志列表
    selectEmailLogListData()
})

/**
 * 查询邮箱日志列表数据
 */
const selectEmailLogListData = () => {
    loading.value = true
    httpUtil.post("/sysEmailLog/selectSysEmailLogList", {...queryForm.value}).then(res => {
        emailLogList.value = res.data.list || [];
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
    selectEmailLogListData()
}

/**
 * 搜索日志
 */
const queryEmailLogListData = () => {
    // 重置页码
    queryForm.value.pageNum = 1
    selectEmailLogListData()
}

const emailLogInfo = ref({})
const emailLogInfoVisible = ref(false)
const emailLogInfoIng = ref(false)

// 邮件预览相关变量
const emailPreviewVisible = ref(false)
const previewContent = ref('')

/**
 * 邮件日志详情Dialog
 * @param row
 */
const emailLogInfoDialog = (row) => {
    emailLogInfo.value = row || {};
    emailLogInfoVisible.value = true
}

/**
 * 预览邮件内容
 */
const previewEmail = () => {
    previewContent.value = emailLogInfo.value.content || ''
    emailPreviewVisible.value = true
}
</script>

<template>
    <div class="page">
        <el-card shadow="never" class="usr_card_override top">
            <el-form :inline="true" :model="queryForm">
                <el-form-item :label="t('收件人')">
                    <el-input v-model="queryForm.toEmail" maxlength="50" :placeholder="t('收件人邮箱')" clearable
                              style="width: 220px"/>
                </el-form-item>
                <el-form-item :label="t('邮件主题')">
                    <el-input v-model="queryForm.subject" maxlength="50" :placeholder="t('邮件主题')" clearable
                              style="width: 220px"/>
                </el-form-item>
                <el-form-item :label="t('发送时间')">
                    <el-date-picker
                        v-model="queryDateRange"
                        type="daterange"
                        :editable="false"
                        :start-placeholder="t('开始时间')"
                        :end-placeholder="t('结束时间')"
                        size="default"
                        value-format="YYYY-MM-DD"
                    />
                </el-form-item>
                <el-form-item :label="t('状态')">
                    <el-select v-model="queryForm.status" size="default" :placeholder="t('选择状态')" clearable
                               style="width: 180px">
                        <el-option :label="t('成功')" value="0"/>
                        <el-option :label="t('失败')" value="1"/>
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" plain @click="queryEmailLogListData">{{ t('搜索') }}</el-button>
                </el-form-item>
            </el-form>
        </el-card>
        <el-card v-loading="loading" shadow="never" class="usr_card_override content">
            <el-table
                class="email_log_table"
                :data="emailLogList"
                row-key="id">
                <el-table-column :label="t('记录ID')" prop="id" width="100" align="center" />
                <el-table-column :label="t('收件人')" prop="toEmail" width="220" show-overflow-tooltip />
                <el-table-column :label="t('邮件主题')" prop="subject" min-width="250" show-overflow-tooltip />
                <el-table-column :label="t('发送时间')" prop="createTime" width="180" />
                <el-table-column :label="t('状态')" width="100" align="center">
                    <template #default="scope">
                        <el-tag v-if="scope.row.status === 0" type="success">{{ t('成功') }}</el-tag>
                        <el-tag v-else-if="scope.row.status === 1" type="danger">{{ t('失败') }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column fixed="right" :label="t('操作')" width="100" align="center">
                    <template #default="scope">
                        <div class="action_btn">
                            <el-button type="primary" plain @click="emailLogInfoDialog(scope.row)">{{ t('详情') }}</el-button>
                        </div>
                    </template>
                </el-table-column>
                <template #empty>
                    <el-empty :description="t('暂无数据')"/>
                </template>
            </el-table>
            <el-pagination background layout="prev, pager, next" :current-page="queryForm.pageNum"
                           @current-change="handleCurrentChange" :page-size="queryForm.pageSize" :total="total"/>
        </el-card>
        
        <!-- 邮件详情Dialog -->
        <el-drawer class="usr_drawer_email_log_info" v-model="emailLogInfoVisible" direction="rtl" size="650">
            <template #header>
                <h4>{{ t('邮件详情') }}</h4>
            </template>
            <template #default>
                <div class="usr_email_log_panel">
                    <el-scrollbar>
                        <div v-if="Object.keys(emailLogInfo).length !== 0"
                             style="padding: 0 20px">
                            <usr-descriptions direction="column">
                                <usr-descriptions-item>
                                    <template #name>
                                        <el-text type="info">{{ t('记录ID：') }}</el-text>
                                    </template>
                                    <template #desc>{{ emailLogInfo.id }}</template>
                                </usr-descriptions-item>
                                <usr-descriptions-item>
                                    <template #name>
                                        <el-text type="info">{{ t('收件人：') }}</el-text>
                                    </template>
                                    <template #desc>{{ emailLogInfo.toEmail }}</template>
                                </usr-descriptions-item>
                                <usr-descriptions-item>
                                    <template #name>
                                        <el-text type="info">{{ t('邮件主题：') }}</el-text>
                                    </template>
                                    <template #desc>{{ emailLogInfo.subject }}</template>
                                </usr-descriptions-item>
                                <usr-descriptions-item>
                                    <template #name>
                                        <el-text type="info">{{ t('发送时间：') }}</el-text>
                                    </template>
                                    <template #desc>{{ emailLogInfo.createTime }}</template>
                                </usr-descriptions-item>
                                <usr-descriptions-item>
                                    <template #name>
                                        <el-text type="info">{{ t('发送状态：') }}</el-text>
                                    </template>
                                    <template #desc>
                                        <el-tag v-if="emailLogInfo.status === 0" type="success">{{ t('成功') }}</el-tag>
                                        <el-tag v-else-if="emailLogInfo.status === 1" type="danger">{{ t('失败') }}</el-tag>
                                    </template>
                                </usr-descriptions-item>
                                <usr-descriptions-item v-if="emailLogInfo.status === 1">
                                    <template #name>
                                        <el-text type="info">{{ t('错误信息：') }}</el-text>
                                    </template>
                                    <template #desc>
                                        <div class="code_info error-message">
                                            {{ emailLogInfo.errorMsg }}
                                        </div>
                                    </template>
                                </usr-descriptions-item>
                                <usr-descriptions-item>
                                    <template #name>
                                        <el-text type="info">{{ t('邮件内容：') }}</el-text>
                                    </template>
                                    <template #desc>
                                        <div class="email-content-section">
                                            <div class="email-content-actions">
                                                <el-button type="primary" size="small" @click="previewEmail">
                                                    <el-icon><View /></el-icon>
                                                    {{ t('预览邮件') }}
                                                </el-button>
                                            </div>
                                            <div class="code_info email-content">
                                                <pre>{{ emailLogInfo.content }}</pre>
                                            </div>
                                        </div>
                                    </template>
                                </usr-descriptions-item>
                            </usr-descriptions>
                        </div>
                        <el-empty v-else :description="t('暂无数据')"/>
                    </el-scrollbar>
                </div>
            </template>
            <template #footer>
                <div style="flex: auto">
                    <el-button @click="emailLogInfoVisible = false">{{ t('关闭') }}</el-button>
                </div>
            </template>
        </el-drawer>
        
        <!-- 邮件预览Dialog -->
        <el-dialog 
            v-model="emailPreviewVisible" 
            :title="t('邮件预览')"
            width="80%"
            class="email-preview-dialog">
            <div class="email-preview-container">
                <div class="email-preview-frame">
                    <iframe 
                        :srcdoc="previewContent"
                        frameborder="0"
                        width="100%"
                        height="600px"
                        style="border: 1px solid #e4e7ed; border-radius: 4px;">
                    </iframe>
                </div>
            </div>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="emailPreviewVisible = false">{{ t('关闭') }}</el-button>
                </span>
            </template>
        </el-dialog>
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

.code_info {
    background-color: #f6f8fa;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    padding: 10px;
    font-family: "Courier New", Courier, monospace;
    max-height: 300px;
    overflow: auto;
    white-space: pre-wrap;
}

.code_info.email-content {
    max-height: 400px;
}

.code_info.error-message {
    color: #f56c6c;
    background-color: #fef0f0;
}

.action_btn {
    display: flex;
    gap: 5px;
}

.action_btn .el-button {
    margin-left: 0;
    padding: 4px 9px;
}

/* 调整日志详情抽屉样式 */
:deep(.usr_drawer_email_log_info .el-drawer__header) {
    margin-bottom: 0;
    padding: 15px 20px;
    border-bottom: 1px solid #f0f0f0;
}

:deep(.usr_drawer_email_log_info .el-drawer__body) {
    padding: 0;
}

:deep(.usr_drawer_email_log_info .el-drawer__footer) {
    border-top: 1px solid #f0f0f0;
    padding: 10px 20px;
}

.usr_email_log_panel {
    height: calc(100vh - 130px);
    padding: 20px 0;
}

/* 邮件内容区域样式 */
.email-content-section {
    width: 100%;
}

.email-content-actions {
    margin-bottom: 10px;
    display: flex;
    justify-content: flex-end;
}

/* 邮件预览弹窗样式 */
.email-preview-dialog {
    .el-dialog__body {
        padding: 0 20px;
    }
}

.email-preview-container {
    width: 100%;
    display: flex;
    justify-content: center;
}

.email-preview-frame {
    width: 100%;
    max-width: 100%;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    border-radius: 4px;
    overflow: hidden;
}
</style> 