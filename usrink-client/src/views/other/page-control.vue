<script setup>
import { onMounted, ref, watch } from "vue";
import httpUtil from "@/utils/HttpUtil";
import { Warning, Download, UploadFilled, InfoFilled, Check } from "@element-plus/icons-vue";
import { ElTooltip, ElMessage } from 'element-plus';



const queryForm = ref({
    id: '',
    pcStatus: '',
    ciName: '',
    serialNumber:'',
    deviceClass: '',
    manufacture: '',
    modelOrVersion: '',
    ntAccount: '',
    pcClass:'',
    comment: '',
    lastName: '',
    firstName: '',
    emailAddress: '',
    department: '',
    telephone: '',
    costCenter: '',
    lifeCycleStart:'',
    yrsToDay:'',
    cpu: '',
    memory: '',
    disk: '',
    graphic: '',
    hardwareStatus: '',
    pr: '',
    po: '',
    vendor: '',
    company: '',
    wbsNum: '',
    price: '',
    temp: '',
    pageNum: 1,
    pageSize: 10
})

const editPartForm = ref({
    id: '',
    pcStatus: '',
    ciName: '',
    serialNumber:'',
    deviceClass: '',
    manufacture: '',
    modelOrVersion: '',
    ntAccount: '',
    pcClass:'',
    comment: '',
    lastName: '',
    firstName: '',
    emailAddress: '',
    department: '',
    telephone: '',
    costCenter: '',
    lifeCycleStart:'',
    yrsToDay:'',
    cpu: '',
    memory: '',
    disk: '',
    graphic: '',
    hardwareStatus: '',
    pr: '',
    po: '',
    vendor: '',
    company: '',
    wbsNum: '',
    price: '',
    temp: ''
})

const deletePartForm = ref([])

const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const partList = ref([])
const editPartDialogVisible = ref(false);
const deletePartDialogVisible = ref(false);
const editPartFormRef = ref(null)
const deletePartIng = ref(false)
const deletePartTips = ref('')

// 添加文件上传相关变量
const uploadFileDialogVisible = ref(false);
const uploadLoading = ref(false);
const uploadFile = ref(null);

onMounted(() => {
    // 查询配件列表
    selectPartListData()

    // // 嵌入式 AIBOT 组件
    // const script = document.createElement('script');
    // script.src = 'http://10.219.184.36:3101/embed/anythingllm-chat-widget.min.js';
    // script.setAttribute('data-embed-id', '42f40229-230d-43a6-814b-8aa72018a0b3');
    // script.setAttribute('data-base-api-url', 'http://10.219.184.36:3101/api/embed');
    // script.async = true;
    // document.body.appendChild(script);

    // // 自定义属性
    // script.setAttribute('data-username', 'embed-AIBOT');  // 设置聊天框名字
    // script.setAttribute('data-chat-icon', 'chatBubble');  // 修改聊天图标
    // script.setAttribute('data-brand-image-url', '/img/SEGIMAGE.jpg');  // 设置品牌图标URL
    // script.setAttribute('data-greeting', 'Hi!我是SEG的AIBOT，欢迎向我提问！');  // 设置欢迎信息
    // script.setAttribute('data-no-sponsor', 'true');  // 隐藏赞助商
    // // script.setAttribute('data-sponsor-link', 'https://seg.org/');  // 修改赞助商链接
    // // script.setAttribute('data-sponsor-text', 'Powered by SEG');  // 修改赞助商文本
    // script.setAttribute('data-assistant-name', 'SEG-AIBOT');  // 修改助手名称
    // script.setAttribute('data-assistant-icon', '/img/SEGIMAGE.jpg');  // 设置助手图标
    // script.setAttribute('data-window-height', '450px');  // 设置聊天窗口高度
    // script.setAttribute('data-window-width', '400px');  // 设置聊天窗口宽度

    
})

/**
 * 查询配件列表
 */
const selectPartListData = () => {
    loading.value = true;
    partList.value = [];
    // 获取配件列表
    httpUtil.post("/sysControl/selectSysControlList", { ...queryForm.value }, {
        headers: {
            'Content-Type': 'application/json'
        }
    }).then(res => {
        // 从响应数据中获取 sysControlModelLists 和 total
        partList.value = res.data.sysControlModelLists || [];
        total.value = res.data.total;
        pageNum.value = res.data.pageNum;
        pageSize.value = res.data.pageSize;
    }).catch(err => {
        console.error(err);
    }).finally(() => {
        loading.value = false;
    });
}


/**
 * 分页查询
 * @param val 当前页码
 */
const handleCurrentChange = (val) => {
    queryForm.value.pageNum = val
    pageNum.value = val
    selectPartListData()
}

// 配件添加
const addPartForm = ref({})
const addPartDialogVisible = ref(false)
const addPartDialogIng = ref(false)
const addPartFormRef = ref(null)

/**
 * 添加配件Dialog
 */
const addPartDialog = () => {
    addPartForm.value = {}
    addPartDialogVisible.value = true
    if (addPartFormRef.value) {
        addPartFormRef.value.resetFields()
    }
}

/**
 * 添加配件
 */
const addPart = () => {
    addPartFormRef.value.validate((valid) => {
        if (valid) {
            addPartDialogIng.value = true
            httpUtil.post("/part/addPart", { ...addPartForm.value },{
                headers: {
                    'Content-Type': 'application/json'
                }
            }).then(res => {
                // 刷新配件列表
                selectPartListData()
            }).catch(err => {
                console.error(err)
            }).finally(() => {
                addPartDialogIng.value = false
                addPartDialogVisible.value = false
            })
        }
    })
}

// 编辑配件
const editPartDialog = (row) => {
    // 将选中的配件数据传递到编辑表单中
    editPartForm.value = { ...row }
    editPartDialogVisible.value = true
    if (editPartFormRef.value) {
        editPartFormRef.value.resetFields()
    }
}

// TODO 修改逻辑
// 编辑配件请求
const editPart = () => {
    editPartFormRef.value.validate((valid) => {
        if (valid) {
            httpUtil.post("/sysControl/updateSysControl", { ...editPartForm.value },{
                headers: {
                    'Content-Type': 'application/json'
                }
            }).then(res => {
                // 刷新配件列表
                selectPartListData()
                ElMessage.success(res.msg)
            }).catch(err => {
                console.error(err)
                ElMessage.error(res.msg)
            }).finally(() => {
                editPartDialogVisible.value = false
            })
        }
    })
}

// 删除配件
const deletePartDialog = (row) => {
    deletePartTips.value = `确定删除设备【${row.ciName}】吗？`  // 使用配件名称作为提示
    deletePartDialogVisible.value = true
    // 将选中的配件的ID传递到删除表单中
    deletePartForm.value.id = row.id
}

// 删除配件请求
const deletePart = () => {
    deletePartIng.value = true
    httpUtil.post("/sysControl/deleteSysControl", { id: deletePartForm.value.id }).then(res => {
        // 刷新配件列表
        selectPartListData()
        ElMessage.success(res.msg)
    }).catch(err => {
        console.error(err)
        ElMessage.error(res.msg)
    }).finally(() => {
        deletePartIng.value = false
        deletePartDialogVisible.value = false
    })
}

// Add computed properties for date formatting and years calculation
const formatDate = (dateString) => {
    if (!dateString) return '';
    // Handle ISO date format like 2013-08-28T00:00 as well as other formats
    if (dateString.includes('T')) {
        return dateString.split('T')[0];
    }
    // Handle other date formats that might use space as separator
    return dateString.split(' ')[0];
}

const calculateYearsToToday = (dateString) => {
    if (!dateString) return '';
    const manufactureDate = new Date(dateString);
    const today = new Date();
    const diffTime = Math.abs(today - manufactureDate);
    const diffYears = (diffTime / (1000 * 60 * 60 * 24 * 365.25)).toFixed(1);
    return diffYears;
}

/**
 * 打开文件上传对话框
 */
const openUploadFileDialog = () => {
    uploadFileDialogVisible.value = true;
    uploadFile.value = null;
}

/**
 * 文件上传改变事件
 * @param file 上传的文件
 */
const handleFileChange = (file) => {
    uploadFile.value = file.raw;
}

/**
 * 上传文件并更新电脑信息
 */
const importComputerByExcel = () => {
    if (!uploadFile.value) {
        ElMessage.warning('请选择Excel文件');
        return;
    }
    
    uploadLoading.value = true;
    
    const formData = new FormData();
    formData.append('file', uploadFile.value);
    
    httpUtil.post("/sysControl/importComputerByExcel", formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        timeout: 300000 // 增加超时时间到5分钟
    }).then(res => {
        ElMessage.success(res.msg || '电脑信息更新成功');
        // 刷新电脑列表
        selectPartListData();
    }).catch(err => {
        console.error(err);
        ElMessage.error(err.response?.data?.msg || '电脑信息更新失败');
    }).finally(() => {
        uploadLoading.value = false;
        uploadFileDialogVisible.value = false;
        uploadFile.value = null;
    });
}

/**
 * 下载模板文件
 */
const downloadTemplate = () => {
    // 创建一个a标签，用于下载文件
    const link = document.createElement('a');
    link.href = '/template.xlsx'; // 模板文件路径
    link.download = 'template.xlsx';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

</script>


<template>
    <div style="padding:12px">
        <!-- 电脑查询 -->
        <el-card shadow="never" class="usr_card_override top">
            <el-form :model="queryForm" inline class="query-form">
                <el-form-item label="电脑名称" class="form-item">
                    <el-input v-model="queryForm.ciName" placeholder="请输入电脑名称" class="input-field"></el-input>
                </el-form-item>
                <el-form-item label="请输入姓" class="form-item">
                    <el-input v-model="queryForm.lastName" placeholder="请输入姓" class="input-field"></el-input>
                </el-form-item>
                <el-form-item label="请输入名" class="form-item">
                    <el-input v-model="queryForm.firstName" placeholder="请输入名" class="input-field"></el-input>
                </el-form-item>
                <el-form-item label="NT账号" class="form-item">
                    <el-input v-model="queryForm.ntAccount" placeholder="请输入NT账号" class="input-field"></el-input>
                </el-form-item>
                <el-form-item label="所属部门" class="form-item">
                    <el-input v-model="queryForm.department" placeholder="请输入所属部门" class="input-field"></el-input>
                </el-form-item>
                <el-form-item label="电脑状态" class="form-item">
                    <el-select v-model="queryForm.pcStatus" placeholder="请选择电脑状态" class="input-field">
                        <el-option label="ALL STATUS" value=""></el-option>
                        <el-option label="In Use" value="In Use"></el-option>
                        <el-option label="Scrapped" value="Scrapped"></el-option>
                        <el-option label="ShareNoteBook" value="ShareNoteBook"></el-option>
                        <el-option label="To be assigned" value="To be assigned"></el-option>
                        <el-option label="To be scrapped" value="To be scrapped"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="出厂时间" class="form-item">
                    <el-input v-model="queryForm.lifeCycleStart" placeholder="请输入出厂时间" class="input-field"></el-input>
                </el-form-item>
                <el-form-item class="form-item">
                    <el-button type="primary" @click="selectPartListData">查询</el-button>
                    <el-button type="primary" @click="openUploadFileDialog" class="update-btn">电脑更新</el-button>
                </el-form-item>
            </el-form>
        </el-card>


        
        <!-- 电脑列表 -->
        <div style="overflow-x: auto; width: 100%; max-width: 100%;">
            
            <el-table :data="partList" :loading="loading" style="width: 100%;" >
                <!-- 电脑状态 -->
                <el-table-column label="电脑状态" prop="pcStatus" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.pcStatus" placement="top">
                            <div class="text-ellipsis">{{ row.pcStatus }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 电脑名 -->
                <el-table-column label="电脑名" prop="ciName" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.ciName" placement="top">
                            <div class="text-ellipsis">{{ row.ciName }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 设备类型 -->
                <el-table-column label="设备类型" prop="deviceClass" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.deviceClass" placement="top">
                            <div class="text-ellipsis">{{ row.deviceClass }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 电脑序列号 -->
                <el-table-column label="电脑序列号" prop="serialNumber" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.serialNumber" placement="top">
                            <div class="text-ellipsis">{{ row.serialNumber }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>


                <!-- 制造商 -->
                <el-table-column label="制造商" prop="manufacture" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.manufacture" placement="top">
                            <div class="text-ellipsis">{{ row.manufacture }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 电脑型号 -->
                <el-table-column label="电脑型号" prop="modelOrVersion" :width="200">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.modelOrVersion" placement="top">
                            <div class="text-ellipsis">{{ row.modelOrVersion }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- NT账号 -->
                <el-table-column label="NT账号" prop="ntAccount" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.ntAccount" placement="top">
                            <div class="text-ellipsis">{{ row.ntAccount }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 电脑归属情况 -->
                <el-table-column label="电脑归属情况" prop="pcClass" :width="200">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.pcClass" placement="top">
                            <div class="text-ellipsis">{{ row.pcClass }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 备注 -->
                <el-table-column label="备注" prop="comment" :width="200">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.comment" placement="top">
                            <div class="text-ellipsis">{{ row.comment }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 姓 -->
                <el-table-column label="姓" prop="lastName" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.lastName" placement="top">
                            <div class="text-ellipsis">{{ row.lastName }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 名 -->
                <el-table-column label="名" prop="firstName" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.firstName" placement="top">
                            <div class="text-ellipsis">{{ row.firstName }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>


                <!-- 邮箱地址 -->
                <el-table-column label="邮箱地址" prop="emailAddress" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.emailAddress" placement="top">
                            <div class="text-ellipsis">{{ row.emailAddress }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 电话号码 -->
                <el-table-column label="电话号码" prop="telephone" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.telephone" placement="top">
                            <div class="text-ellipsis">{{ row.telephone }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 所属部门 -->
                <el-table-column label="所属部门" prop="department" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.department" placement="top">
                            <div class="text-ellipsis">{{ row.department }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 成本中心 -->
                <el-table-column label="成本中心" prop="costCenter" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.costCenter" placement="top">
                            <div class="text-ellipsis">{{ row.costCenter }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 出厂时间 -->
                <el-table-column label="出厂时间" prop="lifeCycleStart" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="formatDate(row.lifeCycleStart)" placement="top">
                            <div class="text-ellipsis">{{ formatDate(row.lifeCycleStart) }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 出厂日期截至今天的时间 -->
                <el-table-column label="出厂日期截至今天的时间" prop="yrs_To_Day" :width="120">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="calculateYearsToToday(row.lifeCycleStart) + ' 年'" placement="top" open-delay=1000>
                            <div class="text-ellipsis">{{ calculateYearsToToday(row.lifeCycleStart) }} 年</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- CPU -->
                <el-table-column label="CPU" prop="cpu" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.cpu" placement="top">
                            <div class="text-ellipsis">{{ row.cpu }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 内存 -->
                <el-table-column label="内存" prop="memory" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.memory" placement="top">
                            <div class="text-ellipsis">{{ row.memory }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 硬盘 -->
                <el-table-column label="硬盘" prop="disk" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.disk" placement="top">
                            <div class="text-ellipsis">{{ row.disk }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 显卡 -->
                <el-table-column label="显卡" prop="graphic" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.graphic" placement="top">
                            <div class="text-ellipsis">{{ row.graphic }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 硬件状态 -->
                <el-table-column label="硬件状态" prop="hardwareStatus" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.hardwareStatus" placement="top">
                            <div class="text-ellipsis">{{ row.hardwareStatus }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 下单号 -->
                <el-table-column label="下单号" prop="pr" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.pr" placement="top">
                            <div class="text-ellipsis">{{ row.pr }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 订单号 -->
                <el-table-column label="订单号" prop="po" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.po" placement="top">
                            <div class="text-ellipsis">{{ row.po }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 供应商公司 -->
                <el-table-column label="供应商公司" prop="vendor" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.vendor" placement="top">
                            <div class="text-ellipsis">{{ row.vendor }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 公司 -->
                <el-table-column label="公司" prop="company" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.company" placement="top">
                            <div class="text-ellipsis">{{ row.company }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 资产号 -->
                <el-table-column label="wbs号" prop="wbsNum" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.wbsNum" placement="top">
                            <div class="text-ellipsis">{{ row.wbsNum }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 临时分配 -->
                <el-table-column label="临时分配" prop="temp" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.temp === 1 ? '是' : '否'" placement="top">
                            <div class="text-ellipsis">{{ row.temp === 1 ? '是' : '否' }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 操作列：编辑与删除 -->
                <el-table-column label="操作" :width="150" fixed="right">
                    <template #default="{ row }">
                        <el-button type="text" @click="editPartDialog(row)">编辑</el-button>
                        <el-button type="text" @click="deletePartDialog(row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>



        <!-- 分页 -->
        <el-pagination
            :current-page="pageNum.value"
            :page-size="pageSize"
            :total="total"
            @current-change="handleCurrentChange">
        </el-pagination>
        
        <!-- 待删 -->
        <div>
            <!-- 添加配件弹窗 -->
            <el-dialog v-model="addPartDialogVisible" title="添加配件">
                <el-form :model="addPartForm" ref="addPartFormRef" label-width="100px">
                    <el-form-item label="配件名称" prop="partName" :rules="[{ required: true, message: '请输入配件名称', trigger: 'blur' }]">
                        <el-input v-model="addPartForm.ciname"></el-input> <!-- 使用 ciname 字段 -->
                    </el-form-item>
                    <el-form-item label="配件分类" prop="category" :rules="[{ required: true, message: '请输入配件分类', trigger: 'blur' }]">
                        <el-input v-model="addPartForm.deviceClass"></el-input> <!-- 使用 deviceClass 字段 -->
                    </el-form-item>
                    <el-form-item label="状态" prop="status" :rules="[{ required: true, message: '请选择状态', trigger: 'blur' }]">
                        <el-select v-model="addPartForm.pcstatus">
                            <el-option label="启用" value="in use"></el-option>
                            <el-option label="禁用" value="to be assigned"></el-option> <!-- 使用 pcstatus 字段 -->
                        </el-select>
                    </el-form-item>
                </el-form>
                <template #footer>
                    <el-button @click="addPartDialogVisible = false">取 消</el-button>
                    <el-button type="primary" @click="addPart">确 定</el-button>
                </template>
            </el-dialog>


            <!-- 编辑设备弹窗 -->
            <el-dialog v-model="editPartDialogVisible" title="编辑设备" width="80%">
                <el-form :model="editPartForm" ref="editPartFormRef" label-width="100px">
                    <el-row :gutter="20">
                        <el-col :span="8">
                            <el-form-item label="电脑名称" prop="ciName">
                                <el-input v-model="editPartForm.ciName"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item label="电脑状态" prop="pcStatus" :rules="[{ required: true, message: '请选择状态', trigger: 'blur' }]">
                                <el-select v-model="editPartForm.pcStatus" style="width: 100%">
                                    <el-option label="TO BE ASSIGNED" value="TO BE ASSIGNED"></el-option>
                                    <el-option label="IN USE" value="IN USE"></el-option>
                                </el-select>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item label="NT账号" prop="ntAccount" :rules="[{ required: true, message: '请输入NT账号', trigger: 'blur' }]">
                                <el-input v-model="editPartForm.ntAccount"></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>

                    <el-row :gutter="20">
                        <el-col :span="8">
                            <el-form-item label="电脑归属" prop="pcClass" :rules="[{ required: true, message: '请输入电脑归属情况', trigger: 'blur' }]">
                                <el-input v-model="editPartForm.pcClass"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item label="设备类型" prop="deviceClass">
                                <el-input v-model="editPartForm.deviceClass"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item label="电脑序列号" prop="serialNumber">
                                <el-input v-model="editPartForm.serialNumber"></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>

                    <el-row :gutter="20">
                        <el-col :span="8">
                            <el-form-item label="制造商" prop="manufacture">
                                <el-input v-model="editPartForm.manufacture"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item label="电脑型号" prop="modelOrVersion">
                                <el-input v-model="editPartForm.modelOrVersion"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item label="备注" prop="comment">
                                <el-input v-model="editPartForm.comment"></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>

                    <el-row :gutter="20">
                        <el-col :span="8">
                            <el-form-item label="姓" prop="lastName">
                                <el-input v-model="editPartForm.lastName"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item label="名" prop="firstName">
                                <el-input v-model="editPartForm.firstName"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item label="邮箱地址" prop="emailAddress">
                                <el-input v-model="editPartForm.emailAddress"></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>

                    <el-row :gutter="20">
                        <el-col :span="8">
                            <el-form-item label="电话号码" prop="telephone">
                                <el-input v-model="editPartForm.telephone"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item label="所属部门" prop="department">
                                <el-input v-model="editPartForm.department"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item label="成本中心" prop="costCenter">
                                <el-input v-model="editPartForm.costCenter"></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>

                    <el-row :gutter="20">
                        <el-col :span="8">
                            <el-form-item label="出厂时间" prop="lifeCycleStart">
                                <el-input v-model="editPartForm.lifeCycleStart"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item label="CPU" prop="cpu">
                                <el-input v-model="editPartForm.cpu"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item label="内存" prop="memory">
                                <el-input v-model="editPartForm.memory"></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>

                    <el-row :gutter="20">
                        <el-col :span="8">
                            <el-form-item label="硬盘" prop="disk">
                                <el-input v-model="editPartForm.disk"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item label="显卡" prop="graphic">
                                <el-input v-model="editPartForm.graphic"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item label="硬件状态" prop="hardwareStatus">
                                <el-input v-model="editPartForm.hardwareStatus"></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>

                    <el-row :gutter="20">
                        <el-col :span="8">
                            <el-form-item label="下单号" prop="pr">
                                <el-input v-model="editPartForm.pr"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item label="订单号" prop="po">
                                <el-input v-model="editPartForm.po"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item label="供应商公司" prop="vendor">
                                <el-input v-model="editPartForm.vendor"></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>

                    <el-row :gutter="20">
                        <el-col :span="8">
                            <el-form-item label="公司" prop="company">
                                <el-input v-model="editPartForm.company"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item label="wbs号" prop="wbsNum">
                                <el-input v-model="editPartForm.wbsNum"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item label="临时分配" prop="temp">
                                <el-select v-model="editPartForm.temp" style="width: 100%">
                                    <el-option label="否" :value="0"></el-option>
                                    <el-option label="是" :value="1"></el-option>
                                </el-select>
                            </el-form-item>
                        </el-col>
                    </el-row>

                    <el-row :gutter="20">
                        <el-col :span="8">
                            <el-form-item label="价格" prop="price">
                                <el-input v-model="editPartForm.price"></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>
                </el-form>
                <template #footer>
                    <el-button @click="editPartDialogVisible = false">取 消</el-button>
                    <el-button type="primary" @click="editPart">确 定</el-button>
                </template>
            </el-dialog>


            <!-- 删除配件弹窗 -->
            <el-dialog v-model="deletePartDialogVisible" title="删除配件">
                <span>{{ deletePartTips }}</span>
                <template #footer>
                    <el-button @click="deletePartDialogVisible = false">取 消</el-button>
                    <el-button type="primary" @click="deletePart">确 定</el-button>
                </template>
            </el-dialog>

            <!-- 文件上传对话框 -->
            <el-dialog v-model="uploadFileDialogVisible" width="580px" :close-on-click-modal="false" :destroy-on-close="true" custom-class="rounded-dialog" :show-close="true" :title="null">
                <div class="upload-dialog-container" v-loading="uploadLoading">
                    <div class="upload-dialog-left">
                        <div class="upload-icon-container">
                            <div class="upload-icon">
                                <el-icon class="upload-icon-svg"><UploadFilled /></el-icon>
                            </div>
                        </div>
                        <h3 class="upload-title">批量更新电脑信息</h3>
                        <p class="upload-subtitle">请上传符合模板格式的Excel文件</p>
                        
                        <div class="upload-tips">
                            <el-icon><InfoFilled /></el-icon>
                            <span>首次使用请先下载模板，按格式填写</span>
                        </div>
                        
                        <el-button 
                            type="primary" 
                            class="template-download-btn" 
                            @click="downloadTemplate">
                            <el-icon><Download /></el-icon>
                            下载模板
                        </el-button>
                    </div>
                    
                    <div class="upload-dialog-divider"></div>
                    
                    <div class="upload-dialog-right">
                        <div class="upload-container">
                            <el-upload
                                class="upload-excel"
                                drag
                                action="#"
                                :auto-upload="false"
                                :show-file-list="true"
                                :limit="1"
                                accept=".xlsx, .xls"
                                :on-change="handleFileChange">
                                <el-icon class="el-icon--upload"><Upload /></el-icon>
                                <div class="el-upload__text">
                                    拖拽文件到此处或 <em>点击上传</em>
                                </div>
                                <template #tip>
                                    <div class="el-upload__tip">
                                        仅支持 .xlsx/.xls 格式的 Excel 文件
                                    </div>
                                </template>
                            </el-upload>
                        </div>
                    </div>
                </div>
                <template #footer>
                    <div class="dialog-footer">
                        <el-button @click="uploadFileDialogVisible = false" class="cancel-btn">取 消</el-button>
                        <el-button type="primary" @click="importComputerByExcel" :loading="uploadLoading" class="confirm-btn">
                            <el-icon v-if="!uploadLoading"><Check /></el-icon>
                            <span>{{ uploadLoading ? '处理中...' : '确认导入' }}</span>
                        </el-button>
                    </div>
                </template>
            </el-dialog>
        </div>
    </div>
</template>

<style scoped>
/* Add your styles here */
/* 添加文本溢出省略号样式 */
.text-ellipsis {
    white-space: nowrap;          /* 防止文本换行 */
    overflow: hidden;             /* 隐藏超出部分 */
    text-overflow: ellipsis;      /* 显示省略号 */
    max-width: 250px;             /* 根据需要调整最大宽度 */
}

.query-form {
  width: 100%;
  margin: 5px auto;
  padding: 0;
  background: none;
  display: flex;
  flex-wrap: wrap;
}

.query-form .el-form-item {
  margin-bottom: 12px;
  flex: 1 1 23%; /* 每个表单项占30%宽度，并且支持自适应 */
  margin-right: 10px;
  margin-left: 14px;
}

/* 对最后一个表单项进行特殊处理，去掉右边距 */
.query-form .el-form-item:last-child {
  margin-right: 0;
}

.query-form .el-form-item__label {
  width: 100px;
  text-align: left;
  font-size: 14px;
  color: #333;
}

.query-form .el-button {
  width: 100%;
  max-width: 150px;
  margin-top: 0px;
  border-radius: 6px;
  background-color: #409EFF;
  font-size: 16px;
  padding: 0px;
  right: 0;
}

.query-form .el-button:hover {
  background-color: #66b1ff;
}

/* 设置输入框的最大宽度，防止过长 */
.input-field {
  max-width: 200px; /* 设置一个合适的最大宽度 */
  width: 100%;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .query-form .el-form-item {
    flex: 1 1 100%; /* 小屏设备下每个表单项占100% */
    margin-right: 0;
  }
  .query-form .el-button {
    width: 100%; /* 按钮占满全宽 */
  }
}

.el-table__body-wrapper::-webkit-scrollbar {
  /* width: 0;宽度为0隐藏 */
  width: 0px;
}
.el-table__body-wrapper::-webkit-scrollbar-thumb {
  border-radius: 2px;
  height: 50px;
  background: #eee;
}
.el-table__body-wrapper::-webkit-scrollbar-track {
  box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.2);
  border-radius: 2px;
  background: rgba(0, 0, 0, 0.4);
}

.usr_card_override.top .el-form {
    display: flex;
    flex-wrap: wrap;
    /* column-gap: 20px; */
    row-gap: 15px;
}

.usr_card_override.top .el-form .el-form-item {
    margin-bottom: 0;
    margin-right: 0;
}

/* 查询按钮样式 */
.query-form .el-form-item .el-button {
    background-color: rgb(236, 245, 255);
    color: rgb(64, 158, 255);
    font-size: 14px;
    border-radius: 4px;
    width: auto;
    padding: 0 15px;
    margin-right: 10px;
    height: 36px;
}

/* 按钮悬停效果 */
.query-form .el-button:hover {
    background-color: #66b1ff; /* 悬停时按钮背景色 */
    border-color: #66b1ff; /* 悬停时按钮边框颜色 */
    color: white;
}

/* 更新按钮样式 */
.update-btn {
    background-color: rgb(236, 245, 255) !important;
    color: rgb(64, 158, 255) !important;
}

.update-btn:hover {
    background-color: #66b1ff !important;
    border-color: #66b1ff !important;
    color: white !important;
}

/* 圆角对话框样式 */
:deep(.rounded-dialog) {
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1), 
                0 1px 8px rgba(0, 0, 0, 0.06);
}

:deep(.rounded-dialog .el-dialog__header) {
    padding: 12px 15px;
    text-align: right;
    border-bottom: none;
    margin-right: 0;
    background: linear-gradient(to right, #f8fafc, #f0f4ff);
}

:deep(.rounded-dialog .el-dialog__title) {
    display: none;
}

:deep(.rounded-dialog .el-dialog__headerbtn) {
    top: 12px;
    right: 15px;
}

:deep(.rounded-dialog .el-dialog__body) {
    padding: 20px;
    background: linear-gradient(145deg, #ffffff, #fafcff);
}

:deep(.rounded-dialog .el-dialog__footer) {
    border-top: 1px solid #f0f2f5;
    padding: 14px 20px;
    background: linear-gradient(to right, #f8fafc, #f0f4ff);
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    gap: 12px;
}

.dialog-footer .cancel-btn {
    border-color: #dcdfe6;
    color: #606266;
    background: #fff;
    transition: all 0.3s;
    padding: 9px 15px;
}

.dialog-footer .cancel-btn:hover {
    color: #409EFF;
    border-color: #c6e2ff;
    background-color: #f0f7ff;
}

.dialog-footer .confirm-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    background: linear-gradient(135deg, #4db8ff, #1890ff);
    border: none;
    box-shadow: 0 4px 10px rgba(64, 158, 255, 0.3);
    transition: all 0.3s ease;
    min-width: 100px;
    padding: 10px 16px;
    height: auto;
    border-radius: 6px;
    font-weight: 500;
    letter-spacing: 0.5px;
}

.dialog-footer .confirm-btn:hover {
    transform: translateY(-2px);
    background: linear-gradient(135deg, #66c2ff, #3aa0ff);
    box-shadow: 0 6px 15px rgba(64, 158, 255, 0.4);
}

.dialog-footer .confirm-btn:active {
    transform: translateY(0);
    box-shadow: 0 2px 5px rgba(64, 158, 255, 0.3);
}

@keyframes glow {
    from {
        filter: drop-shadow(0 0 2px rgba(64, 158, 255, 0.6));
    }
    to {
        filter: drop-shadow(0 0 8px rgba(64, 158, 255, 0.8));
    }
}

/* 文件上传对话框新布局样式 */
.upload-dialog-container {
    display: flex;
    gap: 0;
    padding: 15px 0;
    min-height: 300px;
}

.upload-dialog-left {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 10px 20px 10px 10px;
}

.upload-dialog-divider {
    width: 1px;
    background: linear-gradient(to bottom, 
        rgba(235, 238, 245, 0), 
        rgba(235, 238, 245, 1) 15%, 
        rgba(235, 238, 245, 1) 85%, 
        rgba(235, 238, 245, 0));
    margin: 20px 0;
}

.upload-dialog-right {
    flex: 1.2;
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: 10px 10px 10px 20px;
}

.upload-icon-container {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
}

.upload-icon {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: linear-gradient(135deg, #f0f4ff, #e6f0ff);
    display: flex;
    justify-content: center;
    align-items: center;
    box-shadow: 0 4px 15px rgba(64, 158, 255, 0.2),
                inset 0 -2px 5px rgba(255, 255, 255, 0.8),
                inset 0 2px 5px rgba(64, 158, 255, 0.1);
}

.upload-icon-svg {
    font-size: 36px;
    color: #409EFF;
    filter: drop-shadow(0 2px 4px rgba(64, 158, 255, 0.3));
}

.upload-title {
    font-size: 20px;
    color: #303133;
    margin: 0 0 12px 0;
    font-weight: 600;
    text-align: center;
    letter-spacing: 0.5px;
}

.upload-subtitle {
    font-size: 14px;
    color: #606266;
    margin: 0 0 24px 0;
    line-height: 1.5;
    text-align: center;
}

.upload-container {
    width: 100%;
    border-radius: 12px;
    background: linear-gradient(145deg, #f8fafc, #f0f4ff);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.05),
                inset 0 1px 1px rgba(255, 255, 255, 0.8);
    overflow: hidden;
    transition: all 0.3s ease;
    padding: 2px;
}

.upload-container:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08),
                inset 0 1px 1px rgba(255, 255, 255, 0.8);
}

.upload-excel {
    width: 100%;
}

.upload-excel :deep(.el-upload-dragger) {
    width: 100%;
    height: 180px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background: linear-gradient(145deg, #ffffff, #f6f9fc);
    border: 1px dashed #c0d3ff;
    border-radius: 10px;
    transition: all 0.3s;
}

.upload-excel :deep(.el-upload-dragger:hover) {
    border-color: #409EFF;
    background: linear-gradient(145deg, #fafcff, #f0f7ff);
    box-shadow: inset 0 2px 8px rgba(64, 158, 255, 0.08);
}

.upload-excel :deep(.el-upload-dragger .el-icon--upload) {
    font-size: 38px;
    color: #409EFF;
    margin-bottom: 12px;
    filter: drop-shadow(0 2px 4px rgba(64, 158, 255, 0.2));
}

.upload-excel :deep(.el-upload__text) {
    color: #606266;
    font-size: 14px;
    line-height: 1.6;
}

.upload-excel :deep(.el-upload__text em) {
    color: #409EFF;
    font-style: normal;
    font-weight: 600;
}

.upload-excel :deep(.el-upload__tip) {
    text-align: center;
    color: #909399;
    line-height: 1.5;
    margin-top: 8px;
}

.upload-tips {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: #909399;
    margin-top: auto;
    margin-bottom: 18px;
    padding: 10px 12px;
    background-color: rgba(255, 236, 204, 0.2);
    border-left: 3px solid #E6A23C;
    border-radius: 4px;
}

.upload-tips .el-icon {
    color: #E6A23C;
    font-size: 16px;
    flex-shrink: 0;
}

.template-download-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    background: linear-gradient(135deg, #4db8ff, #1890ff);
    border: none;
    border-radius: 6px;
    box-shadow: 0 4px 10px rgba(64, 158, 255, 0.3);
    transition: all 0.3s ease;
    margin-top: 0;
    padding: 10px 16px;
    height: auto;
    font-weight: 500;
    letter-spacing: 0.5px;
}

.template-download-btn:hover {
    transform: translateY(-2px);
    background: linear-gradient(135deg, #66c2ff, #3aa0ff);
    box-shadow: 0 6px 15px rgba(64, 158, 255, 0.4);
}

.template-download-btn:active {
    transform: translateY(0);
    box-shadow: 0 2px 5px rgba(64, 158, 255, 0.3);
}
</style>
