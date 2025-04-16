<script setup>
import { onMounted, ref, watch } from "vue";
import httpUtil from "@/utils/HttpUtil";
import { Warning } from "@element-plus/icons-vue";
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
    assetsNo: '',
    temp: '',
    pageNum: 1,
    pageSize: 6
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
    assetsNo: '',
    temp: ''
})

const deletePartForm = ref([])

const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(6)
const partList = ref([])
const editPartDialogVisible = ref(false);
const deletePartDialogVisible = ref(false);
const editPartFormRef = ref(null)
const deletePartIng = ref(false)
const deletePartTips = ref('')

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
                        <el-option label="TO BE ASSIGNED" value="TO BE ASSIGNED"></el-option>
                        <el-option label="IN USE" value="IN USE"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="出厂时间" class="form-item">
                    <el-input v-model="queryForm.lifeCycleStart" placeholder="请输入出厂时间" class="input-field"></el-input>
                </el-form-item>
                <el-form-item class="form-item">
                    <el-button type="primary" @click="selectPartListData">查询</el-button>
                </el-form-item>
                <el-form-item class="form-item">
                    <el-button type="primary" @click="addPartDialog">新增设备</el-button>
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
                <el-table-column label="电脑型号" prop="modelOrVersion" :width="150">
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
                <el-table-column label="电脑归属情况" prop="pcClass" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.pcClass" placement="top">
                            <div class="text-ellipsis">{{ row.pcClass }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 备注 -->
                <el-table-column label="备注" prop="comment" :width="150">
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
                        <el-tooltip class="item" effect="light" :content="row.lifeCycleStart" placement="top">
                            <div class="text-ellipsis">{{ row.lifeCycleStart }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 出厂日期截至今天的时间 -->
                <el-table-column label="出厂日期截至今天的时间" prop="yrs_To_Day" :width="120">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.yrsToDay" placement="top" open-delay=1000>
                            <div class="text-ellipsis">{{ row.yrsToDay }}</div>
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
                <el-table-column label="资产号" prop="assetsNo" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.assetsNo" placement="top">
                            <div class="text-ellipsis">{{ row.assetsNo }}</div>
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
            <el-dialog v-model="editPartDialogVisible" title="编辑设备">
                <el-form :model="editPartForm" ref="editPartFormRef" label-width="100px">
                    <el-form-item label="NT账号" prop="ntAccount" :rules="[{ required: true, message: '请输入NT账号', trigger: 'blur' }]">
                        <el-input v-model="editPartForm.ntAccount"></el-input>
                    </el-form-item>
                    <el-form-item label="电脑归属" prop="pcClass" :rules="[{ required: true, message: '请输入电脑归属情况', trigger: 'blur' }]">
                        <el-input v-model="editPartForm.pcClass"></el-input>
                    </el-form-item>
                    <el-form-item label="电脑状态" prop="pcStatus" :rules="[{ required: true, message: '请选择状态', trigger: 'blur' }]">
                        <el-select v-model="editPartForm.pcStatus">
                            <el-option label="TO BE ASSIGNED" value="TO BE ASSIGNED"></el-option>
                            <el-option label="IN USE" value="IN USE"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="CPU" prop="cpu">
                        <el-input v-model="editPartForm.cpu"></el-input>
                    </el-form-item>
                    <el-form-item label="内存" prop="memory">
                        <el-input v-model="editPartForm.memory"></el-input>
                    </el-form-item>
                    <el-form-item label="硬盘" prop="disk">
                        <el-input v-model="editPartForm.disk"></el-input>
                    </el-form-item>
                    <el-form-item label="显卡" prop="graphic">
                        <el-input v-model="editPartForm.graphic"></el-input>
                    </el-form-item>
                    <el-form-item label="硬件状态" prop="hardwareStatus">
                        <el-input v-model="editPartForm.hardwareStatus"></el-input>
                    </el-form-item>
                    <el-form-item label="下单号" prop="pr">
                        <el-input v-model="editPartForm.pr"></el-input>
                    </el-form-item>
                    <el-form-item label="订单号" prop="po">
                        <el-input v-model="editPartForm.po"></el-input>
                    </el-form-item>
                    <el-form-item label="供应商公司" prop="vendor">
                        <el-input v-model="editPartForm.vendor"></el-input>
                    </el-form-item>
                    <el-form-item label="公司" prop="company">
                        <el-input v-model="editPartForm.company"></el-input>
                    </el-form-item>
                    <el-form-item label="资产号" prop="assetsNo">
                        <el-input v-model="editPartForm.assetsNo"></el-input>
                    </el-form-item>
                    <el-form-item label="临时分配" prop="temp">
                        <el-select v-model="editPartForm.temp">
                            <el-option label="否" :value="0"></el-option>
                            <el-option label="是" :value="1"></el-option>
                        </el-select>
                    </el-form-item>
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
    background-color: rgb(236, 245, 255);  /* 按钮背景色与之前一致 */
    color: rgb(64, 158, 255);  /* 字体颜色保持白色 */
    font-size: 14px; /* 按钮文字大小与之前一致 */
    border-radius: 4px; /* 保持圆角 */
    width: 20%;
}

/* 按钮悬停效果 */
.query-form .el-button:hover {
    background-color: #66b1ff; /* 悬停时按钮背景色 */
    border-color: #66b1ff; /* 悬停时按钮边框颜色 */
    color: white;
}


</style>
