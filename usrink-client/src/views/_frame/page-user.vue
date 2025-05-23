<script setup>
import {onMounted, ref, watch, computed} from "vue";
import httpUtil from "@/utils/HttpUtil";
import {Warning} from "@element-plus/icons-vue";
import loginUtil from "@/utils/LoginUtil";
import {ElMessage} from "element-plus";
import { useLanguageStore } from '@/stores/_frame/languageStore';

// Import language store
const languageStore = useLanguageStore();
const currentLang = computed(() => languageStore.currentLang);

// Language text definitions
const langText = computed(() => {
    return {
        // Form labels and placeholders
        ntAccount: currentLang.value === 'zh' ? 'NT账号' : 'NT Account',
        department: currentLang.value === 'zh' ? '部门' : 'Department',
        userName: currentLang.value === 'zh' ? '用户名称' : 'User Name',
        company: currentLang.value === 'zh' ? '公司' : 'Company',
        status: currentLang.value === 'zh' ? '状态' : 'Status',
        selectCompany: currentLang.value === 'zh' ? '选择公司' : 'Select Company',
        selectStatus: currentLang.value === 'zh' ? '选择状态' : 'Select Status',
        normal: currentLang.value === 'zh' ? '正常' : 'Normal',
        disabled: currentLang.value === 'zh' ? '禁用' : 'Disabled',
        
        // Buttons
        search: currentLang.value === 'zh' ? '搜索' : 'Search',
        updateAdUsers: currentLang.value === 'zh' ? '更新AD域用户' : 'Update AD Users',
        updateApprover: currentLang.value === 'zh' ? '审批人更新' : 'Update Approvers',
        addUser: currentLang.value === 'zh' ? '添加用户' : 'Add User',
        cancel: currentLang.value === 'zh' ? '取消' : 'Cancel',
        confirm: currentLang.value === 'zh' ? '确定' : 'Confirm',
        assignRole: currentLang.value === 'zh' ? '分配角色' : 'Assign Role',
        highestAuthority: currentLang.value === 'zh' ? '最高权限' : 'Highest Authority',
        accountDisabled: currentLang.value === 'zh' ? '账号已禁用' : 'Account Disabled',
        
        // Table headers
        phone: currentLang.value === 'zh' ? '手机号' : 'Phone',
        email: currentLang.value === 'zh' ? '邮箱' : 'Email',
        role: currentLang.value === 'zh' ? '角色' : 'Role',
        responsiblePerson: currentLang.value === 'zh' ? '责任人' : 'Responsible Person',
        costCenter: currentLang.value === 'zh' ? '成本中心' : 'Cost Center',
        gender: currentLang.value === 'zh' ? '性别' : 'Gender',
        createTime: currentLang.value === 'zh' ? '创建时间' : 'Create Time',
        updateTime: currentLang.value === 'zh' ? '更新时间' : 'Update Time',
        operations: currentLang.value === 'zh' ? '操作' : 'Operations',
        
        // Gender options
        female: currentLang.value === 'zh' ? '女' : 'Female',
        male: currentLang.value === 'zh' ? '男' : 'Male',
        unknown: currentLang.value === 'zh' ? '未知' : 'Unknown',
        
        // Dialog titles and prompts
        addUserTitle: currentLang.value === 'zh' ? '添加用户' : 'Add User',
        editUserTitle: currentLang.value === 'zh' ? '编辑用户' : 'Edit User',
        deleteUserTitle: currentLang.value === 'zh' ? '删除用户' : 'Delete User',
        restoreUserTitle: currentLang.value === 'zh' ? '恢复用户' : 'Restore User',
        resetPasswordTitle: currentLang.value === 'zh' ? '重置密码' : 'Reset Password',
        assignRoleTitle: currentLang.value === 'zh' ? '分配角色' : 'Assign Role',
        updateApproverTitle: currentLang.value === 'zh' ? '审批人更新' : 'Update Approver',
        
        // Form fields
        enterUsername: currentLang.value === 'zh' ? '请输入用户名' : 'Please enter username',
        enterNickname: currentLang.value === 'zh' ? '请输入用户昵称' : 'Please enter nickname',
        enterPhone: currentLang.value === 'zh' ? '请输入手机号' : 'Please enter phone number',
        selectDepartment: currentLang.value === 'zh' ? '请选择部门' : 'Please select department',
        enterEmail: currentLang.value === 'zh' ? '请输入邮箱地址' : 'Please enter email address',
        selectGender: currentLang.value === 'zh' ? '选择性别' : 'Select gender',
        enterPassword: currentLang.value === 'zh' ? '请输入密码' : 'Please enter password',
        selectRole: currentLang.value === 'zh' ? '请选择角色' : 'Please select role',
        
        // Tooltips and warnings
        usernameTooltip: currentLang.value === 'zh' 
            ? '用户名为登录账号，<br/>系统唯一，不能重复；<br/>不能被修改，请谨慎设置。'
            : 'Username is login account,<br/>must be unique in system;<br/>cannot be modified later.',
        selectExcelFile: currentLang.value === 'zh' ? '选择Excel文件' : 'Select Excel File',
        uploadTip: currentLang.value === 'zh' ? '请上传.xlsx或.xls格式的文件' : 'Please upload .xlsx or .xls file',
        approverUpdateInfo: currentLang.value === 'zh' ? '审批人信息更新' : 'Approver Information Update',
        uploadExcelPrompt: currentLang.value === 'zh' ? '请上传包含审批人信息的Excel文件' : 'Please upload Excel file with approver information',
        
        // Validation messages
        usernameRequired: currentLang.value === 'zh' ? '请输入用户名' : 'Please enter username',
        usernameExists: currentLang.value === 'zh' ? '用户名已存在' : 'Username already exists',
        nicknameRequired: currentLang.value === 'zh' ? '请输入用户昵称' : 'Please enter nickname',
        passwordRequired: currentLang.value === 'zh' ? '请输入密码' : 'Please enter password',
        selectExcelPrompt: currentLang.value === 'zh' ? '请选择Excel文件' : 'Please select an Excel file',
        updateSuccess: currentLang.value === 'zh' ? '审批人更新成功' : 'Approver update successful',
        updateFailed: currentLang.value === 'zh' ? '审批人更新失败' : 'Approver update failed',
        
        // Confirmation messages
        deleteUserConfirm: (role, nick) => currentLang.value === 'zh'
            ? `确定删除用户【${role} - ${nick}】吗？`
            : `Are you sure you want to delete user [${role} - ${nick}]?`,
        restoreUserConfirm: (role, nick) => currentLang.value === 'zh'
            ? `确定恢复用户【${role} - ${nick}】吗？`
            : `Are you sure you want to restore user [${role} - ${nick}]?`,
        deleteUserPermanentConfirm: (role, nick) => currentLang.value === 'zh'
            ? `确定彻底删除用户【${role} - ${nick}】吗？删除后不可恢复！`
            : `Are you sure you want to permanently delete user [${role} - ${nick}]? This cannot be undone!`,
        resetPasswordConfirm: (role, nick) => currentLang.value === 'zh'
            ? `确定重置用户【${role} - ${nick}】的密码吗？<br />重置后密码为：123456`
            : `Are you sure you want to reset password for user [${role} - ${nick}]?<br />Password will be: 123456`,
        
        // Filters
        allRoles: currentLang.value === 'zh' ? '全部角色' : 'All Roles',
        keyword: currentLang.value === 'zh' ? '关键字' : 'Keyword',
        noData: currentLang.value === 'zh' ? '暂无数据' : 'No Data'
    }
});

const queryForm = ref({
    userId: '',
    userName: '',
    userNick: '',
    phone: '',
    userRoleId: '',
    status: '0',
    department: '',
    company: '',
    pageNum: 1,
    pageSize: 10
})
const loading = ref(false)
const total = ref(0)
const userList = ref([])

// 需要过滤的角色名称
const excludedRoles = ['游客', '开发工程师']

onMounted(() => {
    // 查询角色列表
    selectRoleListData()
    // 查询用户列表
    selectUserListData()
})

const roleListLoading = ref(false)
const roleNameFilterText = ref('')
const roleListTreeRef = ref(null)
const roleList = ref([])

/**
 * 查询角色列表
 */
const selectRoleListData = () => {
    roleListLoading.value = true
    // 获取角色列表
    httpUtil.get("/sysRole/selectSysRoleCommonList").then(res => {
        // 过滤掉游客和开发工程师角色
        const filteredList = (res.data.list || []).filter(role => !excludedRoles.includes(role.roleName))
        roleList.value = [{
            roleId: '',
            roleName: langText.value.allRoles,
            children: filteredList
        }]
    }).catch(err => {
        console.error(err)
    }).finally(() => {
        roleListLoading.value = false
    })
}

/**
 * 查询用户列表
 */
const selectUserListData = () => {
    loading.value = true
    // 获取用户列表
    httpUtil.post("/sysUser/selectSysUserList", {...queryForm.value}).then(res => {
        userList.value = res.data.list || []
        total.value = res.data.total
    }).catch(err => {
        console.error(err)
    }).finally(() => {
        loading.value = false
    })
}

// 角色列表过滤，监听关键字变化
watch(roleNameFilterText, (val) => {
    roleListTreeRef.value.filter(val)
})

/**
 * 角色列表过滤
 * @param value 关键字
 * @param node TreeNode
 * @returns {boolean}
 */
const filterRoleListNode = (value, node) => {
    if (!value) return true
    return node.roleName.indexOf(value) !== -1
}

/**
 * 角色点击事件
 * @param treeData 角色树数据
 */
const roleTreeClick = (treeData) => {
    queryForm.value.userRoleId = treeData.roleId
    queryForm.value.pageNum = 1 // 重置页码为第一页
    selectUserListData()
}

/**
 * 分页查询
 * @param val 当前页码
 */
const handleCurrentChange = (val) => {
    queryForm.value.pageNum = val
    selectUserListData()
}

const updateAdUsersLoading = ref(false)

/**
 * 更新AD域用户
 */
const updateAdUsers = () => {
    updateAdUsersLoading.value = true
    httpUtil.get("/sysLadp/linkLDAPRefreshAllInfo", {
        timeout: 120000  // 设置超时时间为120秒
    }).then(res => {
        // 显示成功消息
        if (res.msg) {
            ElMessage.success(res.msg)
        } else if (res.data) {
            ElMessage.success(res.data)
        } else {
            ElMessage.success(currentLang.value === 'zh' ? 'AD域用户更新成功' : 'AD users updated successfully')
        }
        // 刷新用户列表
        selectUserListData()
    }).catch(err => {
        console.error(err)
        // 显示错误消息
        const errorMsg = err.response?.data?.msg || err.message || (currentLang.value === 'zh' ? 'AD域用户更新失败' : 'Failed to update AD users')
        ElMessage.error(errorMsg)
    }).finally(() => {
        updateAdUsersLoading.value = false
    })
}

// 审批人更新相关变量
const updateApproverLoading = ref(false)
const updateApproverDialogVisible = ref(false)
const uploadFile = ref(null)
const uploadLoading = ref(false)

/**
 * 打开审批人更新对话框
 */
const openUpdateApproverDialog = () => {
    updateApproverDialogVisible.value = true
}

/**
 * 上传文件并更新审批人
 */
const updateApprover = () => {
    if (!uploadFile.value) {
        ElMessage.warning(langText.value.selectExcelPrompt)
        return
    }
    
    uploadLoading.value = true
    
    const formData = new FormData()
    formData.append('file', uploadFile.value)
    
    httpUtil.post("/sysLadp/updateApprover", formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    }).then(res => {
        ElMessage.success(langText.value.updateSuccess)
        // 刷新用户列表
        selectUserListData()
    }).catch(err => {
        console.error(err)
        ElMessage.error(langText.value.updateFailed)
    }).finally(() => {
        uploadLoading.value = false
        updateApproverDialogVisible.value = false
        uploadFile.value = null
    })
}

/**
 * 文件上传改变事件
 * @param file 上传的文件
 */
const handleFileChange = (file) => {
    uploadFile.value = file.raw
}

const addUserForm = ref({})
const addUserDialogVisible = ref(false)
const addUserDialogIng = ref(false)
const addUserIng = ref(false)
const addUserFormRef = ref(null)

/**
 * 校验用户名是否存在
 * @param rule 规则
 * @param value 用户名
 * @param callback 回调
 */
const validatorUserName = (rule, value, callback) => {
    if (value === '' || value === undefined || value === null) {
        callback(new Error(langText.value.usernameRequired))
    } else {
        // 校验用户名是否存在
        httpUtil.get("/sysUser/checkUserNameUnique?userName=" + value).then(res => {
            if (!res.data.valid) {
                callback(new Error(langText.value.usernameExists))
            } else {
                callback()
            }
        }).catch(err => {
            console.error(err)
        })
    }
}

const userAddFormRules = ref({
    userName: [
        {required: true, validator: validatorUserName, trigger: 'blur'}
    ],
    userNick: [
        {required: true, message: () => langText.value.nicknameRequired, trigger: 'blur'}
    ],
    userPassword: [
        {required: true, message: () => langText.value.passwordRequired, trigger: 'blur'}
    ]
})

const userEditFormRules = ref({
    userNick: [
        {required: true, message: () => langText.value.nicknameRequired, trigger: 'blur'}
    ]
})

/**
 * 添加用户Dialog
 */
const addUserDialog = () => {
    // 清空表单
    addUserForm.value = {}
    // 设置默认密码
    addUserForm.value.userPassword = '123456'
    addUserDialogVisible.value = true
    if (addUserFormRef.value) {
        addUserFormRef.value.resetFields()
    }
}

/**
 * 添加用户
 */
const addUser = () => {
    addUserFormRef.value.validate((valid) => {
        if (valid) {
            addUserDialogIng.value = true
            addUserIng.value = true
            // 密码加密
            addUserForm.value.userPassword = loginUtil.encryptPassword(addUserForm.value.userPassword)
            httpUtil.post("/sysUser/addSysUser", {...addUserForm.value}).then(res => {
                // 刷新用户列表
                selectUserListData()
            }).catch(err => {
                console.error(err)
            }).finally(() => {
                addUserIng.value = false
                addUserDialogIng.value = false
                addUserDialogVisible.value = false
            })
        }
    })
}

const editUserForm = ref({})
const editUserDialogVisible = ref(false)
const editUserDialogIng = ref(false)
const editUserIng = ref(false)
const editUserFormRef = ref(null)

/**
 * 编辑用户Dialog
 * @param row
 */
const editUserDialog = (row) => {
    // 先清空表单
    editUserForm.value = {}
    editUserForm.value = {...row}
    editUserDialogVisible.value = true
    if (editUserFormRef.value) {
        editUserFormRef.value.resetFields()
    }
}

/**
 * 编辑用户
 */
const editUser = () => {
    editUserFormRef.value.validate((valid) => {
        if (valid) {
            editUserDialogIng.value = true
            editUserIng.value = true
            httpUtil.post("/sysUser/updateSysUser", {...editUserForm.value}).then(res => {
                // 刷新用户列表
                selectUserListData()
            }).catch(err => {
                console.error(err)
            }).finally(() => {
                editUserIng.value = false
                editUserDialogIng.value = false
                editUserDialogVisible.value = false
            })
        }
    })
}

const restorePasswordTips = ref('')
const restorePasswordDialogVisible = ref(false)
const restorePasswordIng = ref(false)

/**
 * 重置密码Dialog
 * @param row
 */
const restorePasswordDialog = (row) => {
    editUserForm.value = {} // 清空更新表单
    editUserForm.value.userId = row.userId
    restorePasswordTips.value = langText.value.resetPasswordConfirm(row.roleName, row.userNick)
    restorePasswordDialogVisible.value = true
}

/**
 * 重置密码
 */
const restorePassword = () => {
    restorePasswordIng.value = true
    httpUtil.post("/sysUser/resetUserPwd", {...editUserForm.value}).then(res => {
        // 刷新用户列表
        selectUserListData()
    }).catch(err => {
        console.error(err)
    }).finally(() => {
        restorePasswordIng.value = false
        restorePasswordDialogVisible.value = false
    })
}

const distributeRoleDialogVisible = ref(false)
const distributeRoleDialogIng = ref(false)
const distributeRoleIng = ref(false)
const distributeRoleList = ref([])

/**
 * 分配角色Dialog
 * @param row
 */
const distributeRoleDialog = (row) => {
    editUserForm.value = {} // 清空更新表单
    editUserForm.value.userId = row.userId
    editUserForm.value.userRoleId = row.userRoleId
    distributeRoleDialogVisible.value = true
    distributeRoleDialogIng.value = true
    // 获取角色列表
    httpUtil.get("/sysRole/selectSysRoleCommonList").then(res => {
        // 过滤掉游客和开发工程师角色
        distributeRoleList.value = (res.data.list || []).filter(role => !excludedRoles.includes(role.roleName))
    }).catch(err => {
        console.error(err)
    }).finally(() => {
        distributeRoleDialogIng.value = false
    })
}

/**
 * 分配角色
 */
const distributeRole = () => {
    distributeRoleIng.value = true
    distributeRoleDialogIng.value = true
    httpUtil.post("/sysUser/updateSysUserRole", {...editUserForm.value}).then(res => {
        // 刷新用户列表
        selectUserListData()
    }).catch(err => {
        console.error(err)
    }).finally(() => {
        distributeRoleIng.value = false
        distributeRoleDialogIng.value = false
        distributeRoleDialogVisible.value = false
    })
}

const deleteLogicTips = ref('')
const deleteLogicDialogVisible = ref(false)
const deleteLogicIng = ref(false)

/**
 * 逻辑删除用户Dialog
 * @param row
 */
const deleteLogicRoleDialog = (row) => {
    editUserForm.value = {} // 清空更新表单
    editUserForm.value.userId = row.userId
    deleteLogicTips.value = langText.value.deleteUserConfirm(row.roleName, row.userNick)
    deleteLogicDialogVisible.value = true
}

/**
 * 逻辑删除用户
 */
const deleteLogicRole = () => {
    deleteLogicIng.value = true
    httpUtil.post("/sysUser/deleteSysUserLogic", {...editUserForm.value}).then(res => {
        // 刷新用户列表
        selectUserListData()
    }).catch(err => {
        console.error(err)
    }).finally(() => {
        deleteLogicIng.value = false
        deleteLogicDialogVisible.value = false
    })
}

const restoreTips = ref('')
const restoreDialogVisible = ref(false)
const restoreIng = ref(false)

/**
 * 恢复用户Dialog
 * @param row
 */
const restoreUserDialog = (row) => {
    editUserForm.value = {} // 清空更新表单
    editUserForm.value.userId = row.userId
    restoreTips.value = langText.value.restoreUserConfirm(row.roleName, row.userNick)
    restoreDialogVisible.value = true
}

/**
 * 恢复用户
 */
const restoreUser = () => {
    restoreIng.value = true
    httpUtil.post("/sysUser/recoverSysUser", {...editUserForm.value}).then(res => {
        // 刷新用户列表
        selectUserListData()
    }).catch(err => {
        console.error(err)
    }).finally(() => {
        restoreIng.value = false
        restoreDialogVisible.value = false
    })
}

const deleteTips = ref('')
const deleteDialogVisible = ref(false)
const deleteIng = ref(false)

/**
 * 物理删除用户Dialog
 * @param row
 */
const deleteRoleDialog = (row) => {
    editUserForm.value = {} // 清空更新表单
    editUserForm.value.userId = row.userId
    deleteTips.value = langText.value.deleteUserPermanentConfirm(row.roleName, row.userNick)
    deleteDialogVisible.value = true
}

/**
 * 物理删除删除用户
 */
const deleteRole = () => {
    deleteIng.value = true
    httpUtil.post("/sysUser/deleteSysUserPhysics", {...editUserForm.value}).then(res => {
        // 刷新用户列表
        selectUserListData()
    }).catch(err => {
        console.error(err)
    }).finally(() => {
        deleteIng.value = false
        deleteDialogVisible.value = false
    })
}

</script>

<template>
    <div class="page">
        <el-card shadow="never" class="usr_card_override top">
            <el-form :inline="true" :model="queryForm">
                <el-form-item :label="langText.ntAccount">
                    <el-input v-model="queryForm.userName" :placeholder="langText.ntAccount" clearable style="width: 180px"/>
                </el-form-item>
                <el-form-item :label="langText.department">
                    <el-input v-model="queryForm.department" :placeholder="langText.department" clearable style="width: 180px"/>
                </el-form-item>
                <el-form-item :label="langText.userName">
                    <el-input v-model="queryForm.userNick" :placeholder="langText.userName" clearable style="width: 180px"/>
                </el-form-item>
                <el-form-item :label="langText.company">
                    <el-select v-model="queryForm.company" size="default" :placeholder="langText.selectCompany" clearable
                               style="width: 180px">
                        <el-option label="SGCS" value="SGCS"/>
                        <el-option label="SES" value="SES"/>
                        <el-option label="SGCC" value="SGCC"/>
                    </el-select>
                </el-form-item>
                <el-form-item :label="langText.status">
                    <el-select v-model="queryForm.status" size="default" :placeholder="langText.selectStatus" clearable
                               style="width: 180px">
                        <el-option :label="langText.normal" value="0"/>
                        <el-option :label="langText.disabled" value="-1"/>
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" plain @click="selectUserListData">{{ langText.search }}</el-button>
                </el-form-item>
            </el-form>
        </el-card>
        <div class="top_btn_panel">
            <el-button type="primary" @click="updateAdUsers" :loading="updateAdUsersLoading">{{ langText.updateAdUsers }}</el-button>
            <el-button type="success" @click="openUpdateApproverDialog" :loading="updateApproverLoading">{{ langText.updateApprover }}</el-button>
        </div>
        <div class="usr_user_panel">
            <!-- 角色列表 -->
            <div class="usr_user_panel_left">
                <el-card v-loading="roleListLoading" shadow="never">
                    <el-input v-model="roleNameFilterText" :placeholder="langText.keyword" clearable style="width: 180px"/>
                    <div class="user_role_list_panel">
                        <el-scrollbar>
                            <el-tree
                                ref="roleListTreeRef"
                                class="usr_role_tree"
                                :data="roleList"
                                :expand-on-click-node="false"
                                highlight-current
                                :props="{label: 'roleName', children: 'children'}"
                                default-expand-all
                                :filter-node-method="filterRoleListNode"
                                @node-click="roleTreeClick"
                            />
                        </el-scrollbar>
                    </div>
                </el-card>
            </div>
            <!-- 用户列表 -->
            <div class="usr_user_panel_right">
                <el-card v-loading="loading" shadow="never" class="usr_card_override content">
                    <el-table
                        class="user_table"
                        :data="userList"
                        row-key="roleId">
                        <el-table-column prop="userName" :label="langText.ntAccount" min-width="180">
                            <template #default="scope">
                                {{ scope.row.userName }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="userNick" :label="langText.userName" min-width="300">
                            <template #default="scope">
                                {{ scope.row.userNick }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="phone" :label="langText.phone" min-width="300">
                            <template #default="scope">
                                {{ scope.row.phone }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="email" :label="langText.email" min-width="300">
                            <template #default="scope">
                                {{ scope.row.email }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="department" :label="langText.department" min-width="220">
                            <template #default="scope">
                                {{ scope.row.department }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="roleName" :label="langText.role" min-width="180">
                            <template #default="scope">
                                {{ scope.row.roleName }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="responsibility" :label="langText.responsiblePerson" min-width="180">
                            <template #default="scope">
                                {{ scope.row.responsibility }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="company" :label="langText.company" min-width="120">
                            <template #default="scope">
                                {{ scope.row.company }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="costCenter" :label="langText.costCenter" min-width="150">
                            <template #default="scope">
                                {{ scope.row.costCenter }}
                            </template>
                        </el-table-column>
                        <el-table-column :label="langText.gender" align="center" min-width="100">
                            <template #default="scope">
                                <el-text v-if="scope.row.sex === 0">{{ langText.female }}</el-text>
                                <el-text v-else-if="scope.row.sex === 1">{{ langText.male }}</el-text>
                                <el-text v-else>{{ langText.unknown }}</el-text>
                            </template>
                        </el-table-column>
                        <el-table-column prop="createTime" :label="langText.createTime" min-width="150">
                            <template #default="scope">
                                {{ scope.row.createTime }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="updateTime" :label="langText.updateTime" min-width="150">
                            <template #default="scope">
                                {{ scope.row.updateTime }}
                            </template>
                        </el-table-column>
                        <el-table-column :label="langText.status" align="center" min-width="100">
                            <template #default="scope">
                                <el-tag v-if="scope.row.status === 0">{{ langText.normal }}</el-tag>
                                <el-tag v-else-if="scope.row.status === -1" type="warning">{{ langText.disabled }}</el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column fixed="right" :label="langText.operations" min-width="180">
                            <template #default="scope">
                                <div class="action_btn">
                                    <template v-if="scope.row.status === 0">
                                        <template v-if="scope.row.roleName === '超级管理员'">
                                            <el-button class="highest-authority-btn" type="warning" disabled>{{ langText.highestAuthority }}</el-button>
                                        </template>
                                        <template v-else>
                                            <el-button type="primary" plain
                                                    @click="distributeRoleDialog(scope.row)">{{ langText.assignRole }}
                                            </el-button>
                                        </template>
                                    </template>
                                    <template v-if="scope.row.status === -1">
                                        <el-button type="info" plain disabled>{{ langText.accountDisabled }}</el-button>
                                    </template>
                                </div>
                            </template>
                        </el-table-column>
                        <template #empty>
                            <el-empty :description="langText.noData"/>
                        </template>
                    </el-table>
                    <el-pagination background layout="prev, pager, next" :current-page="queryForm.pageNum"
                                   @current-change="handleCurrentChange" :page-size="queryForm.pageSize"
                                   :total="total"/>
                </el-card>
            </div>
        </div>
        <!-- 添加用户 -->
        <el-dialog class="add_user_dialog" :title="langText.addUserTitle" :close-on-click-modal="false" v-model="addUserDialogVisible"
                   width="600">
            <div class="add_user_form" v-loading="addUserDialogIng">
                <el-form :model="addUserForm" :rules="userAddFormRules" ref="addUserFormRef" label-width="100px">
                    <el-form-item :label="langText.ntAccount" prop="userName">
                        <el-input v-model="addUserForm.userName" maxlength="15" :placeholder="langText.enterUsername"
                                  style="width: 250px"
                                  clearable/>
                        <el-tooltip>
                            <template #content v-html="langText.usernameTooltip"></template>
                            <el-icon :size="18" color="#b1b3b8">
                                <Warning/>
                            </el-icon>
                        </el-tooltip>
                    </el-form-item>
                    <el-form-item :label="langText.userName" prop="userNick">
                        <el-input v-model="addUserForm.userNick" maxlength="16" :placeholder="langText.enterNickname"
                                  style="width: 250px"
                                  clearable/>
                    </el-form-item>
                    <el-form-item :label="langText.phone" prop="phone">
                        <el-input v-model="addUserForm.phone" maxlength="20" :placeholder="langText.enterPhone"
                                  style="width: 250px"
                                  clearable/>
                    </el-form-item>
                    <el-form-item :label="langText.department" prop="department">
                        <el-input v-model="addUserForm.department" maxlength="20" :placeholder="langText.selectDepartment"
                                  style="width: 250px"
                                  clearable/>
                    </el-form-item>
                    <el-form-item :label="langText.email" prop="email">
                        <el-input v-model="addUserForm.email" maxlength="30" :placeholder="langText.enterEmail"
                                  style="width: 250px"
                                  clearable/>
                    </el-form-item>
                    <el-form-item :label="langText.gender" prop="sex">
                        <el-select v-model="queryForm.sex" size="default" :placeholder="langText.selectGender" clearable
                                   style="width: 180px">
                            <el-option :label="langText.female" value="0"/>
                            <el-option :label="langText.male" value="1"/>
                            <el-option :label="langText.unknown" value="-1"/>
                        </el-select>
                    </el-form-item>
                    <el-form-item :label="langText.enterPassword" prop="password">
                        <el-input type="password" v-model="addUserForm.userPassword" :placeholder="langText.enterPassword"
                                  style="width: 250px" show-password clearable/>
                    </el-form-item>
                </el-form>
            </div>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="addUserDialogVisible = false">{{ langText.cancel }}</el-button>
                    <el-button type="primary" @click="addUser" :loading="addUserIng">{{ langText.confirm }}</el-button>
                </div>
            </template>
        </el-dialog>
        <!-- 编辑用户 -->
        <el-dialog class="edit_user_dialog" :title="langText.editUserTitle" :close-on-click-modal="false"
                   v-model="editUserDialogVisible"
                   width="600">
            <div class="edit_user_form" v-loading="editUserDialogIng">
                <el-form :model="editUserForm" :rules="userEditFormRules" ref="editUserFormRef" label-width="100px">
                    <el-form-item :label="langText.ntAccount" prop="userName">
                        <el-input v-model="editUserForm.userName" disabled maxlength="15" :placeholder="langText.enterUsername"
                                  style="width: 250px"
                                  clearable/>
                        <el-tooltip>
                            <template #content v-html="langText.usernameTooltip"></template>
                            <el-icon :size="18" color="#b1b3b8">
                                <Warning/>
                            </el-icon>
                        </el-tooltip>
                    </el-form-item>
                    <el-form-item :label="langText.userName" prop="userNick">
                        <el-input v-model="editUserForm.userNick" maxlength="16" :placeholder="langText.enterNickname"
                                  style="width: 250px"
                                  clearable/>
                    </el-form-item>
                    <el-form-item :label="langText.phone" prop="phone">
                        <el-input v-model="editUserForm.phone" maxlength="20" :placeholder="langText.enterPhone"
                                  style="width: 250px"
                                  clearable/>
                    </el-form-item>
                    <el-form-item :label="langText.email" prop="email">
                        <el-input v-model="editUserForm.email" maxlength="30" :placeholder="langText.enterEmail"
                                  style="width: 250px"
                                  clearable/>
                    </el-form-item>
                    <el-form-item :label="langText.gender" prop="sex">
                        <el-select v-model="editUserForm.sex" size="default" :placeholder="langText.selectGender" clearable
                                   style="width: 180px">
                            <el-option :label="langText.female" :value="0"/>
                            <el-option :label="langText.male" :value="1"/>
                            <el-option :label="langText.unknown" :value="-1"/>
                        </el-select>
                    </el-form-item>
                </el-form>
            </div>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="editUserDialogVisible = false">{{ langText.cancel }}</el-button>
                    <el-button type="primary" @click="editUser" :loading="editUserIng">{{ langText.confirm }}</el-button>
                </div>
            </template>
        </el-dialog>
        <!-- 逻辑删除用户 -->
        <el-dialog :title="langText.deleteUserTitle" :close-on-click-modal="false" v-model="deleteLogicDialogVisible" width="400">
            <div class="delete_logic_dialog">
                <p>{{ deleteLogicTips }}</p>
            </div>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="deleteLogicDialogVisible = false">{{ langText.cancel }}</el-button>
                    <el-button type="primary" @click="deleteLogicRole" :loading="deleteLogicIng">{{ langText.confirm }}</el-button>
                </div>
            </template>
        </el-dialog>
        <!-- 恢复用户 -->
        <el-dialog :title="langText.restoreUserTitle" :close-on-click-modal="false" v-model="restoreDialogVisible" width="400">
            <div class="restore_dialog">
                <p>{{ restoreTips }}</p>
            </div>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="restoreDialogVisible = false">{{ langText.cancel }}</el-button>
                    <el-button type="primary" @click="restoreUser" :loading="restoreIng">{{ langText.confirm }}</el-button>
                </div>
            </template>
        </el-dialog>
        <!-- 物理删除用户 -->
        <el-dialog :title="langText.deleteUserTitle" :close-on-click-modal="false" v-model="deleteDialogVisible" width="400">
            <div class="delete_dialog">
                <p>{{ deleteTips }}</p>
            </div>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="deleteDialogVisible = false">{{ langText.cancel }}</el-button>
                    <el-button type="primary" @click="deleteRole" :loading="deleteIng">{{ langText.confirm }}</el-button>
                </div>
            </template>
        </el-dialog>
        <!-- 重置密码 -->
        <el-dialog :title="langText.resetPasswordTitle" :close-on-click-modal="false" v-model="restorePasswordDialogVisible" width="400">
            <div class="restore_password_dialog">
                <p v-html="restorePasswordTips"></p>
            </div>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="restorePasswordDialogVisible = false">{{ langText.cancel }}</el-button>
                    <el-button type="primary" @click="restorePassword" :loading="restorePasswordIng">{{ langText.confirm }}</el-button>
                </div>
            </template>
        </el-dialog>
        <!-- 分配角色 -->
        <el-dialog :title="langText.assignRoleTitle" :close-on-click-modal="false" v-model="distributeRoleDialogVisible" width="400">
            <div class="distribute_role_dialog" v-loading="distributeRoleDialogIng">
                <el-select v-model="editUserForm.userRoleId" :placeholder="langText.selectRole" style="width: 250px">
                    <el-option v-for="item in distributeRoleList" :key="item.roleId" :label="item.roleName"
                               :value="item.roleId"/>
                </el-select>
            </div>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="distributeRoleDialogVisible = false">{{ langText.cancel }}</el-button>
                    <el-button type="primary" @click="distributeRole" :loading="distributeRoleIng">{{ langText.confirm }}</el-button>
                </div>
            </template>
        </el-dialog>
        <!-- 审批人更新对话框 -->
        <el-dialog 
            :title="langText.updateApproverTitle" 
            :close-on-click-modal="false" 
            v-model="updateApproverDialogVisible" 
            width="500px"
            class="update-approver-dialog"
            destroy-on-close>
            <div class="approver-update-container" v-loading="uploadLoading">
                <div class="update-header">
                    <div class="header-title">
                        <span>{{ langText.approverUpdateInfo }}</span>
                    </div>
                    <div class="header-description">{{ langText.uploadExcelPrompt }}</div>
                </div>
                
                <div class="update-content">
                    <el-upload
                        class="upload-excel"
                        action="#"
                        :auto-upload="false"
                        :show-file-list="true"
                        accept=".xlsx, .xls"
                        :limit="1"
                        :on-change="handleFileChange">
                        <el-button type="primary" class="upload-button">
                            <i class="el-icon-upload"></i>
                            {{ langText.selectExcelFile }}
                        </el-button>
                        <template #tip>
                            <div class="el-upload__tip">{{ langText.uploadTip }}</div>
                        </template>
                    </el-upload>
                </div>
            </div>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="updateApproverDialogVisible = false">{{ langText.cancel }}</el-button>
                    <el-button type="primary" @click="updateApprover" :loading="uploadLoading">{{ langText.confirm }}</el-button>
                </div>
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

.top_btn_panel {
    display: flex;
    flex-wrap: wrap;
    column-gap: 10px;
    row-gap: 10px;
    margin: 15px 0 15px 0;
}

.top_btn_panel .el-button + .el-button {
    margin-left: 0;
}

.usr_user_panel {
    display: flex;
    column-gap: 20px;
}

.usr_user_panel_left {
    min-width: 220px;
}

.user_role_list_panel {
    margin-top: 20px;
    height: 250px;
}

:deep(.usr_role_tree) .el-tree-node__content {
    height: 35px;
}

:deep(.usr_role_tree).el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content {
    background-color: #cce9ff;
}

.usr_user_panel_right {
    width: 0;
    flex: 1;
}

.usr_card_override.content .el-table {
    margin-bottom: 20px;
}

:deep(.user_table) {
    /* Ensure table rows have consistent height */
    .el-table__row {
        height: auto;
        line-height: 1.5;
    }
    
    /* Style for action buttons */
    .action_btn {
        display: flex;
        flex-wrap: wrap;
        column-gap: 10px;
    }
    
    .action_btn .el-button + .el-button {
        margin-left: 0;
    }
}

:deep(.add_user_dialog) .el-form .el-form-item__content,
:deep(.edit_user_dialog) .el-form .el-form-item__content {
    column-gap: 10px;
}

/* Style for the highest authority button */
:deep(.highest-authority-btn) {
    background: linear-gradient(45deg, #E6A23C, #F56C6C);
    border: none;
    color: white;
    font-weight: bold;
    position: relative;
    overflow: hidden;
}

:deep(.highest-authority-btn)::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
    animation: shimmer 2s infinite;
}

@keyframes shimmer {
    0% {
        left: -100%;
    }
    100% {
        left: 100%;
    }
}

/* 审批人更新对话框样式 */
.update-approver-dialog {
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
        border-radius: 6px;
        overflow: hidden;
    }
    
    :deep(.el-dialog__wrapper) {
        &:not(.is-visible) {
            .el-dialog {
                transform: translateY(-20px);
                opacity: 0;
            }
        }
    }

    :deep(.el-dialog__footer) {
        background-color: #f8fafc;
        padding: 16px 20px;
        border-top: 1px solid rgba(37, 128, 191, 0.1);
    }
}

.approver-update-container {
    position: relative;
    border-radius: 4px;
    overflow: hidden;
}

.update-header {
    background: linear-gradient(135deg, #2580bf 0%, #20b2aa 100%);
    color: #fff;
    padding: 20px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    position: relative;
    overflow: hidden;
    
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

.header-title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 8px;
    /* Add subtle text shadow for better readability */
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
    /* Add subtle animation on load */
    animation: fadeInLeft 0.5s ease-out;
}

.header-description {
    font-size: 14px;
    opacity: 0.9;
    animation: fadeInUp 0.5s ease-out 0.1s both;
}

.update-content {
    padding: 25px;
    background-color: #f8f9fa;
    animation: fadeIn 0.5s ease-out 0.1s both;
}

.upload-excel {
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    
    :deep(.el-upload) {
        width: 100%;
        text-align: center;
    }
    
    :deep(.el-upload-list) {
        margin-top: 15px;
        width: 100%;
        text-align: left;
    }
}

.upload-button {
    width: 80%;
    height: 45px;
    background: linear-gradient(135deg, #2580bf 0%, #20b2aa 100%);
    border: none;
    color: white;
    font-weight: 500;
    border-radius: 4px;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
    
    &:hover {
        transform: translateY(-2px);
        box-shadow: 0 5px 15px rgba(32, 178, 170, 0.3);
    }
    
    &::before {
        content: '';
        position: absolute;
        top: 0;
        left: -100%;
        width: 100%;
        height: 100%;
        background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
        animation: buttonShimmer 2s infinite;
    }
}

:deep(.el-upload__tip) {
    text-align: center;
    color: #909399;
    font-size: 12px;
    margin-top: 10px;
}

.dialog-footer {
    text-align: right;
    
    .el-button {
        transition: all 0.3s ease;
        position: relative;
        overflow: hidden;
    }
    
    .el-button--primary {
        background: linear-gradient(135deg, #2580bf 0%, #20b2aa 100%);
        border: none;
        
        &:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(32, 178, 170, 0.3);
        }
    }
}

@keyframes buttonShimmer {
    0% {
        left: -100%;
    }
    100% {
        left: 100%;
    }
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

@keyframes fadeIn {
    from {
        opacity: 0;
    }
    to {
        opacity: 1;
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

</style>