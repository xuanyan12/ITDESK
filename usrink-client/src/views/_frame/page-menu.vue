<script setup>
import {nextTick, onMounted, ref, computed} from "vue";
import httpUtil from "@/utils/HttpUtil";
import TreeUtil from "@/utils/TreeUtil";
import {Warning} from "@element-plus/icons-vue";
import UsrChooseIcon from "@/components/_frame/common/usr-choose-icon.vue";
import { useLanguageStore } from '@/stores/_frame/languageStore';

// Import language store
const languageStore = useLanguageStore();
const currentLang = computed(() => languageStore.currentLang);

// Language text definitions
const langText = computed(() => {
    return {
        // Form labels and placeholders
        menuName: currentLang.value === 'zh' ? '菜单名称' : 'Menu Name',
        status: currentLang.value === 'zh' ? '状态' : 'Status',
        normal: currentLang.value === 'zh' ? '正常' : 'Normal',
        deleted: currentLang.value === 'zh' ? '已删除' : 'Deleted',
        selectStatus: currentLang.value === 'zh' ? '选择状态' : 'Select Status',
        search: currentLang.value === 'zh' ? '搜索' : 'Search',
        
        // Buttons
        expandCollapse: currentLang.value === 'zh' ? '展开/折叠' : 'Expand/Collapse',
        addMenu: currentLang.value === 'zh' ? '添加菜单' : 'Add Menu',
        add: currentLang.value === 'zh' ? '添加' : 'Add',
        edit: currentLang.value === 'zh' ? '编辑' : 'Edit',
        delete: currentLang.value === 'zh' ? '删除' : 'Delete',
        restore: currentLang.value === 'zh' ? '恢复' : 'Restore',
        cancel: currentLang.value === 'zh' ? '取消' : 'Cancel',
        confirm: currentLang.value === 'zh' ? '确定' : 'Confirm',
        
        // Table headers
        sortOrder: currentLang.value === 'zh' ? '排序' : 'Sort Order',
        menuType: currentLang.value === 'zh' ? '菜单类型' : 'Menu Type',
        permissionKey: currentLang.value === 'zh' ? '权限标识符' : 'Permission Key',
        pageRoute: currentLang.value === 'zh' ? '页面路由' : 'Page Route',
        pageComponent: currentLang.value === 'zh' ? '页面组件' : 'Page Component',
        sceneType: currentLang.value === 'zh' ? '场景类型' : 'Scene Type',
        operations: currentLang.value === 'zh' ? '操作' : 'Operations',
        noData: currentLang.value === 'zh' ? '暂无数据' : 'No Data',
        
        // Menu types
        directory: currentLang.value === 'zh' ? '目录' : 'Directory',
        menu: currentLang.value === 'zh' ? '菜单' : 'Menu',
        button: currentLang.value === 'zh' ? '按钮' : 'Button',
        
        // Scene types
        mainMenu: currentLang.value === 'zh' ? '主菜单' : 'Main Menu',
        normalPage: currentLang.value === 'zh' ? '普通页面' : 'Normal Page',
        
        // Form fields
        parentMenu: currentLang.value === 'zh' ? '父菜单' : 'Parent Menu',
        selectParentMenu: currentLang.value === 'zh' ? '选择父类菜单' : 'Select Parent Menu',
        selectMenuScene: currentLang.value === 'zh' ? '选择菜单场景' : 'Select Menu Scene',
        selectMenuType: currentLang.value === 'zh' ? '选择菜单类型' : 'Select Menu Type',
        icon: currentLang.value === 'zh' ? '图标' : 'Icon',
        selectIcon: currentLang.value === 'zh' ? '点击选择图标' : 'Click to select icon',
        
        // Dialog titles
        addMenuTitle: currentLang.value === 'zh' ? '添加菜单' : 'Add Menu',
        editMenuTitle: currentLang.value === 'zh' ? '编辑菜单' : 'Edit Menu',
        deleteConfirmTitle: currentLang.value === 'zh' ? '删除确认' : 'Delete Confirmation',
        restoreConfirmTitle: currentLang.value === 'zh' ? '恢复确认' : 'Restore Confirmation',
        permanentDeleteTitle: currentLang.value === 'zh' ? '彻底删除' : 'Permanent Deletion',
        
        // Validation messages
        menuNameRequired: currentLang.value === 'zh' ? '菜单名称不能为空！' : 'Menu name cannot be empty!',
        orderIndexRequired: currentLang.value === 'zh' ? '排序不能为空！' : 'Sort order cannot be empty!',
        sceneTypeRequired: currentLang.value === 'zh' ? '使用场景不能为空！' : 'Scene type cannot be empty!',
        menuTypeRequired: currentLang.value === 'zh' ? '菜单类型不能为空！' : 'Menu type cannot be empty!',
        permKeyRequired: currentLang.value === 'zh' ? '菜单类型为【按钮】时候，权限标识符不能为空！！' : 'Permission key cannot be empty when menu type is [Button]!',
        
        // Tooltips
        sceneTypeTooltip: currentLang.value === 'zh' 
            ? '使用场景类型，<br/>【主菜单】为页面左侧的主菜单的页面；<br/>【普通页面】一般为非主菜单页面，如个人中心等。' 
            : 'Scene type,<br/>[Main Menu] for pages in the left main menu;<br/>[Normal Page] for non-main menu pages, like user profile, etc.',
        menuTypeTooltip: currentLang.value === 'zh' 
            ? '菜单类型，<br/>【目录】为页面的父节点；<br/>【菜单】一般为页面；<br/>【按钮】一般为页面上的操作。' 
            : 'Menu type,<br/>[Directory] is a parent node for pages;<br/>[Menu] is generally a page;<br/>[Button] is generally an operation on a page.',
        permKeyTooltip: currentLang.value === 'zh' 
            ? "Shiro权限控制标识符，如：@RequiresPermissions('sys:menu:add')" 
            : "Shiro permission identifier, e.g.: @RequiresPermissions('sys:menu:add')",
            
        // Confirmation messages
        deleteMenuConfirm: (menuName) => currentLang.value === 'zh' 
            ? `确定删除菜单【${menuName}】吗？` 
            : `Are you sure you want to delete menu [${menuName}]?`,
        restoreMenuConfirm: (menuName) => currentLang.value === 'zh' 
            ? `确定恢复菜单【${menuName}】吗？` 
            : `Are you sure you want to restore menu [${menuName}]?`,
        permanentDeleteConfirm: (menuName) => currentLang.value === 'zh' 
            ? `确定彻底删除菜单【${menuName}】吗？删除之后将无法恢复！` 
            : `Are you sure you want to permanently delete menu [${menuName}]? This cannot be undone!`
    }
});

const queryForm = ref({
    menuName: '',
    status: '0'
})
const loading = ref(true)
const showTable = ref(true)
const menuList = ref([])
const tableExpandAll = ref(true)

onMounted(() => {
    // 初始化菜单列表
    selectMenuListData()
})

/**
 * 展开/折叠
 */
const tableExpandAllChange = () => {
    tableExpandAll.value = !tableExpandAll.value
    showTable.value = false
    nextTick(() => {
        showTable.value = true
    })
}

/**
 * 查询菜单列表
 */
const selectMenuListData = () => {
    loading.value = true
    httpUtil.post("/sysMenu/selectSysMenuList", {...queryForm.value}).then(res => {
        let menuListData = res.data.list || [];
        if (menuListData.length === 0) {
            menuList.value = []
        } else {
            menuList.value = TreeUtil.buildMenuTree(menuListData, menuListData[0].parentMenuId)
        }
    }).catch(err => {
        console.error(err)
    }).finally(() => {
        loading.value = false
    })
}

const availableMenuList = ref([])
const addMenuForm = ref({})
const addMenuDialogVisible = ref(false)
const addMenuDialogIng = ref(false)
const addMenuIng = ref(false)
const addFormRef = ref(null)
const menuFormRules = ref({
    menuName: [
        {required: true, message: () => langText.value.menuNameRequired, trigger: 'blur'}
    ],
    orderIndex: [
        {required: true, message: () => langText.value.orderIndexRequired, trigger: 'blur'}
    ],
    sceneType: [
        {required: true, message: () => langText.value.sceneTypeRequired, trigger: 'change'},
    ],
    menuType: [
        {required: true, message: () => langText.value.menuTypeRequired, trigger: 'change'}
    ],
    permKey: {required: false, message: () => langText.value.permKeyRequired, trigger: 'blur'}
});

/**
 * 添加菜单Dialog
 * @param parentMenuId 父菜单ID
 */
const addMenuDialog = (parentMenuId) => {
    // 清空添加表单
    addMenuForm.value = {}
    // 如果选择了父菜单，则赋值
    if (parentMenuId) {
        addMenuForm.value.parentMenuId = parentMenuId
    }
    addMenuDialogVisible.value = true
    addMenuDialogIng.value = true
    // 这里清除表单的校验
    if (addFormRef.value) {
        addFormRef.value.clearValidate();
    }
    // 查询可用菜单列表
    httpUtil.get("/sysMenu/selectSysMenuCommonList").then(res => {
        let menuListData = res.data.list || [];
        // 过滤掉按钮类型的菜单，只保留目录和菜单，构建菜单树
        availableMenuList.value = TreeUtil.buildMenuTree(menuListData.filter(item => item.menuType !== 2))
    }).catch(err => {
        console.error(err)
    }).finally(() => {
        addMenuDialogIng.value = false
    })
}

/**
 * 添加菜单
 */
const addMenu = () => {
    addFormRef.value.validate((valid) => {
        if (valid) {
            addMenuDialogIng.value = true
            addMenuIng.value = true
            // 如果选择了父菜单，则取最后一个菜单ID
            if (addMenuForm.value.parentMenuId) {
                // 如果parentMenuId是数组，则取最后一个菜单ID
                if (Array.isArray(addMenuForm.value.parentMenuId)) {
                    addMenuForm.value.parentMenuId = addMenuForm.value.parentMenuId[addMenuForm.value.parentMenuId.length - 1]
                }
            } else {
                addMenuForm.value.parentMenuId = 0
            }
            httpUtil.post("/sysMenu/insertSysMenu", {...addMenuForm.value}).then(res => {
                // 刷新菜单列表
                selectMenuListData()
            }).catch(err => {
                console.error(err)
            }).finally(() => {
                addMenuDialogIng.value = false
                addMenuIng.value = false
                addMenuDialogVisible.value = false
            })
        }
    })
}

/**
 * 添加菜单弹框关闭后调
 */
const addMenuDialogClose = () => {
    // 解决第二次打开添加表单的时候，form触发了验证了
    addFormRef.value.resetFields()
}

const updateForm = ref({})
const editMenuDialogVisible = ref(false)
const editMenuDialogIng = ref(false)
const editMenuIng = ref(false)
const editFormRef = ref(null)

/**
 * 编辑菜单Dialog
 * @param row
 */
const editMenuDialog = (row) => {
    // 先清空表单
    updateForm.value = {}
    updateForm.value = {...row}
    editMenuDialogVisible.value = true
    editMenuDialogIng.value = true
    if (editFormRef.value) {
        editFormRef.value.clearValidate()
    }
    // 查询可用菜单列表
    httpUtil.get("/sysMenu/selectSysMenuCommonList").then(res => {
        let menuListData = res.data.list || [];
        // 过滤掉按钮类型的菜单，只保留目录和菜单，构建菜单树
        availableMenuList.value = TreeUtil.buildMenuTree(menuListData.filter(item => item.menuType !== 2))
    }).catch(err => {
        console.error(err)
    }).finally(() => {
        editMenuDialogIng.value = false
    })
}

/**
 * 编辑菜单
 */
const editMenu = () => {
    editFormRef.value.validate((valid) => {
        if (valid) {
            editMenuDialogIng.value = true
            editMenuIng.value = true
            // 如果选择了父菜单，则取最后一个菜单ID
            if (updateForm.value.parentMenuId) {
                updateForm.value.parentMenuId = updateForm.value.parentMenuId[updateForm.value.parentMenuId.length - 1]
            } else {
                updateForm.value.parentMenuId = 0
            }
            httpUtil.post("/sysMenu/updateSysMenu", {...updateForm.value}).then(res => {
                // 刷新菜单列表
                selectMenuListData()
            }).catch(err => {
                console.error(err)
            }).finally(() => {
                editMenuDialogIng.value = false
                editMenuIng.value = false
                editMenuDialogVisible.value = false
            })
        }
    })
}

/**
 * 表单中菜单类型发生改变的时候回调
 */
const menuTypeOnChange = (value) => {
    if (value !== 'undefined') {
        if (value === 2) {
            // 如果菜单类型为按钮时候，让permKey不能为空
            menuFormRules.value.permKey.required = true
            return;
        }
    }

    // 默认情况下，permKey可以为空
    menuFormRules.value.permKey.required = false
}


const deleteLogicTips = ref('')
const deleteLogicDialogVisible = ref(false)
const deleteLogicIng = ref(false)

/**
 * 逻辑删除菜单Dialog
 * @param row
 */
const deleteLogicMenuDialog = (row) => {
    updateForm.value = {} // 清空更新表单
    updateForm.value.menuId = row.menuId
    deleteLogicTips.value = langText.value.deleteMenuConfirm(row.menuName)
    deleteLogicDialogVisible.value = true
}

/**
 * 逻辑删除菜单
 */
const deleteLogicMenu = () => {
    deleteLogicIng.value = true
    httpUtil.post("/sysMenu/deleteSysMenuLogic", {...updateForm.value}).then(res => {
        // 刷新菜单列表
        selectMenuListData()
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
 * 恢复菜单Dialog
 * @param row
 */
const restoreMenuDialog = (row) => {
    updateForm.value = {} // 清空更新表单
    updateForm.value.menuId = row.menuId
    restoreTips.value = langText.value.restoreMenuConfirm(row.menuName)
    restoreDialogVisible.value = true
}

/**
 * 恢复菜单
 */
const restoreMenu = () => {
    restoreIng.value = true
    httpUtil.post("/sysMenu/recoverSysMenu", {...updateForm.value}).then(res => {
        // 刷新菜单列表
        selectMenuListData()
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
 * 物理删除菜单Dialog
 * @param row
 */
const deleteMenuDialog = (row) => {
    updateForm.value = {} // 清空更新表单
    updateForm.value.menuId = row.menuId
    deleteTips.value = langText.value.permanentDeleteConfirm(row.menuName)
    deleteDialogVisible.value = true
}

/**
 * 物理删除菜单
 */
const deleteMenu = () => {
    deleteIng.value = true
    httpUtil.post("/sysMenu/deleteSysMenuPhysics", {...updateForm.value}).then(res => {
        // 刷新菜单列表
        selectMenuListData()
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
                <el-form-item :label="langText.menuName">
                    <el-input v-model="queryForm.menuName" maxlength="10" :placeholder="langText.menuName" clearable
                              style="width: 180px"/>
                </el-form-item>
                <el-form-item :label="langText.status">
                    <el-select v-model="queryForm.status" size="default" :placeholder="langText.selectStatus" clearable
                               style="width: 180px">
                        <el-option :label="langText.normal" value="0"/>
                        <el-option :label="langText.deleted" value="-1"/>
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" plain @click="selectMenuListData">{{ langText.search }}</el-button>
                </el-form-item>
            </el-form>
        </el-card>
        <div class="top_btn_panel">
            <el-button type="warning" plain @click="tableExpandAllChange()">{{ langText.expandCollapse }}</el-button>
            <el-button type="primary" @click="addMenuDialog(null)">{{ langText.addMenu }}</el-button>
        </div>
        <el-card v-loading="loading" shadow="never" class="usr_card_override content">
            <el-table
                v-if="showTable"
                class="menu_table"
                :data="menuList"
                row-key="menuId"
                :defaultExpandAll="tableExpandAll">
                <el-table-column class-name="coll_1" :label="langText.menuName" width="220">
                    <template #default="scope">
                        <div style="display: flex; align-items: center">
                            <el-icon v-if="scope.row.icon">
                                <component :is="scope.row.icon"/>
                            </el-icon>
                            <el-text>{{ scope.row.menuName }}</el-text>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column :label="langText.sortOrder" align="center" width="80">
                    <template #default="scope">
                        <el-text>{{ scope.row.orderIndex }}</el-text>
                    </template>
                </el-table-column>
                <el-table-column :label="langText.menuType" align="center" width="120">
                    <template #default="scope">
                        <el-tag v-if="scope.row.menuType === 0">{{ langText.directory }}</el-tag>
                        <el-tag v-else-if="scope.row.menuType === 1" type="warning">{{ langText.menu }}</el-tag>
                        <el-tag v-else-if="scope.row.menuType === 2" type="info">{{ langText.button }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column :label="langText.permissionKey" width="200">
                    <template #default="scope">
                        <div class="menu_perm">
                            <el-text>
                                {{ scope.row.permKey }}
                            </el-text>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column :label="langText.pageRoute" width="200">
                    <template #default="scope">
                        <div class="menu_path">
                            <el-text>{{ scope.row.route }}</el-text>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column :label="langText.pageComponent" width="200">
                    <template #default="scope">
                        <div class="menu_component">
                            <el-text>
                                {{ scope.row.component }}
                            </el-text>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column :label="langText.sceneType" align="center" width="150">
                    <template #default="scope">
                        <el-tag v-if="scope.row.sceneType === 0">{{ langText.mainMenu }}</el-tag>
                        <el-tag v-else-if="scope.row.sceneType === 1" type="warning">{{ langText.normalPage }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column :label="langText.status" align="center">
                    <template #default="scope">
                        <el-tag v-if="scope.row.status === 0">{{ langText.normal }}</el-tag>
                        <el-tag v-else-if="scope.row.status === -1" type="warning">{{ langText.deleted }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column fixed="right" :label="langText.operations" width="330">
                    <template #default="scope">
                        <div class="action_btn">
                            <template v-if="scope.row.status === 0">
                                <el-button v-if="scope.row.menuType === 0 || scope.row.menuType === 1" type="primary"
                                           plain @click="addMenuDialog(scope.row.menuId)">{{ langText.add }}
                                </el-button>
                                <el-button type="success" plain @click="editMenuDialog(scope.row)">{{ langText.edit }}</el-button>
                                <el-button type="danger" plain @click="deleteLogicMenuDialog(scope.row)">{{ langText.delete }}
                                </el-button>
                            </template>
                            <template v-if="scope.row.status === -1">
                                <el-button type="warning" plain @click="restoreMenuDialog(scope.row)">{{ langText.restore }}</el-button>
                                <el-button type="info" plain @click="deleteMenuDialog(scope.row)">{{ langText.delete }}</el-button>
                            </template>
                        </div>
                    </template>
                </el-table-column>
                <template #empty>
                    <el-empty :description="langText.noData"/>
                </template>
            </el-table>
        </el-card>
        <!-- 添加菜单Dialog -->
        <el-dialog class="add_menu_dialog" :close-on-click-modal="false" v-model="addMenuDialogVisible" :title="langText.addMenuTitle"
                   @close="addMenuDialogClose" width="600">
            <div class="add_menu_form" v-loading="addMenuDialogIng">
                <el-form :model="addMenuForm" ref="addFormRef" :rules="menuFormRules" label-width="100px">
                    <el-form-item :label="langText.parentMenu">
                        <el-cascader
                            :props="{expandTrigger:'hover',checkStrictly:true, value:'menuId', label:'menuName'}"
                            :options="availableMenuList"
                            size="default"
                            v-model="addMenuForm.parentMenuId" :placeholder="langText.selectParentMenu" style="width: 300px"
                            clearable/>
                    </el-form-item>
                    <el-form-item :label="langText.sceneType" prop="sceneType">
                        <el-select v-model="addMenuForm.sceneType" size="default" :placeholder="langText.selectMenuScene" clearable
                                   style="width: 180px">
                            <el-option :label="langText.mainMenu" :value="0"/>
                            <el-option :label="langText.normalPage" :value="1"/>
                        </el-select>
                        <el-tooltip>
                            <template #content v-html="langText.sceneTypeTooltip"></template>
                            <el-icon :size="18" color="#b1b3b8">
                                <Warning/>
                            </el-icon>
                        </el-tooltip>
                    </el-form-item>
                    <el-form-item :label="langText.menuName" prop="menuName">
                        <el-input v-model="addMenuForm.menuName" maxlength="10" :placeholder="langText.menuName"
                                  style="width: 200px" clearable/>
                    </el-form-item>
                    <el-form-item :label="langText.sortOrder" prop="orderIndex">
                        <el-input-number v-model="addMenuForm.orderIndex" size="default" :min="1" :max="99"
                                         controls-position="right"/>
                    </el-form-item>
                    <el-form-item :label="langText.icon">
                        <el-tooltip class="choose_icon_class" trigger="click" effect="light">
                            <template #content>
                                <div style="padding: 10px 5px 10px 10px">
                                    <UsrChooseIcon :height="180" @onConfirm="val => addMenuForm.icon = val"/>
                                </div>
                            </template>
                            <el-input :prefix-icon="addMenuForm.icon" v-model="addMenuForm.icon" maxlength="50"
                                      :placeholder="langText.selectIcon" style="width: 250px" clearable/>
                        </el-tooltip>
                    </el-form-item>
                    <el-form-item :label="langText.menuType" prop="menuType">
                        <el-select v-model="addMenuForm.menuType" @change="menuTypeOnChange" size="default"
                                   :placeholder="langText.selectMenuType" clearable
                                   style="width: 180px">
                            <el-option :label="langText.directory" :value="0"/>
                            <el-option :label="langText.menu" :value="1"/>
                            <el-option :label="langText.button" :value="2"/>
                        </el-select>
                        <el-tooltip>
                            <template #content v-html="langText.menuTypeTooltip"></template>
                            <el-icon :size="18" color="#b1b3b8">
                                <Warning/>
                            </el-icon>
                        </el-tooltip>
                    </el-form-item>
                    <el-form-item :label="langText.permissionKey" prop="permKey" v-if="addMenuForm.menuType === 2">
                        <el-input v-model="addMenuForm.permKey" maxlength="50" :placeholder="langText.permissionKey"
                                  style="width: 300px" clearable/>
                        <el-tooltip :content="langText.permKeyTooltip">
                            <el-icon :size="18" color="#b1b3b8">
                                <Warning/>
                            </el-icon>
                        </el-tooltip>
                    </el-form-item>
                    <el-form-item :label="langText.pageRoute" v-if="addMenuForm.menuType === 1">
                        <el-input v-model="addMenuForm.route" maxlength="50" :placeholder="langText.pageRoute" style="width: 300px"
                                  clearable/>
                    </el-form-item>
                    <el-form-item :label="langText.pageComponent" v-if="addMenuForm.menuType === 1">
                        <el-input v-model="addMenuForm.component" maxlength="50" :placeholder="langText.pageComponent"
                                  style="width: 300px" clearable/>
                    </el-form-item>
                </el-form>
            </div>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="addMenuDialogVisible = false">{{ langText.cancel }}</el-button>
                    <el-button type="primary" @click="addMenu" :loading="addMenuIng">{{ langText.confirm }}</el-button>
                </div>
            </template>
        </el-dialog>
        <!-- 编辑菜单Dialog -->
        <el-dialog class="edit_menu_dialog" :close-on-click-modal="false" v-model="editMenuDialogVisible"
                   :title="langText.editMenuTitle" width="600">
            <div class="edit_menu_form" v-loading="editMenuDialogIng">
                <el-form :model="updateForm" ref="editFormRef" :rules="menuFormRules" label-width="100px">
                    <el-form-item :label="langText.parentMenu">
                        <el-cascader
                            :props="{expandTrigger:'hover',checkStrictly:true, value:'menuId', label:'menuName'}"
                            :options="availableMenuList"
                            size="default"
                            v-model="updateForm.parentMenuId" :placeholder="langText.selectParentMenu" style="width: 300px"
                            clearable/>
                    </el-form-item>
                    <el-form-item :label="langText.sceneType" prop="sceneType">
                        <el-select v-model="updateForm.sceneType" size="default" :placeholder="langText.selectMenuScene" clearable
                                   style="width: 180px">
                            <el-option :label="langText.mainMenu" :value="0"/>
                            <el-option :label="langText.normalPage" :value="1"/>
                        </el-select>
                        <el-tooltip>
                            <template #content v-html="langText.sceneTypeTooltip"></template>
                            <el-icon :size="18" color="#b1b3b8">
                                <Warning/>
                            </el-icon>
                        </el-tooltip>
                    </el-form-item>
                    <el-form-item :label="langText.menuName" prop="menuName">
                        <el-input v-model="updateForm.menuName" maxlength="10" :placeholder="langText.menuName"
                                  style="width: 200px" clearable/>
                    </el-form-item>
                    <el-form-item :label="langText.sortOrder" prop="orderIndex">
                        <el-input-number v-model="updateForm.orderIndex" size="default" :min="1" :max="99"
                                         controls-position="right"/>
                    </el-form-item>
                    <el-form-item :label="langText.icon">
                        <el-tooltip class="choose_icon_class" trigger="click" effect="light">
                            <template #content>
                                <div style="padding: 10px 5px 10px 10px">
                                    <UsrChooseIcon :height="180" @onConfirm="val => updateForm.icon = val"/>
                                </div>
                            </template>
                            <el-input :prefix-icon="updateForm.icon" v-model="updateForm.icon" maxlength="50"
                                      :placeholder="langText.selectIcon" style="width: 250px" clearable/>
                        </el-tooltip>
                    </el-form-item>
                    <el-form-item :label="langText.menuType" prop="menuType">
                        <el-select v-model="updateForm.menuType" size="default" :placeholder="langText.selectMenuType" clearable
                                   style="width: 180px">
                            <el-option :label="langText.directory" :value="0"/>
                            <el-option :label="langText.menu" :value="1"/>
                            <el-option :label="langText.button" :value="2"/>
                        </el-select>
                        <el-tooltip>
                            <template #content v-html="langText.menuTypeTooltip"></template>
                            <el-icon :size="18" color="#b1b3b8">
                                <Warning/>
                            </el-icon>
                        </el-tooltip>
                    </el-form-item>
                    <el-form-item :label="langText.permissionKey" prop="permKey" v-if="updateForm.menuType === 2">
                        <el-input v-model="updateForm.permKey" maxlength="50" :placeholder="langText.permissionKey"
                                  style="width: 300px" clearable/>
                        <el-tooltip :content="langText.permKeyTooltip">
                            <el-icon :size="18" color="#b1b3b8">
                                <Warning/>
                            </el-icon>
                        </el-tooltip>
                    </el-form-item>
                    <el-form-item :label="langText.pageRoute" v-if="updateForm.menuType === 1">
                        <el-input v-model="updateForm.route" maxlength="50" :placeholder="langText.pageRoute" style="width: 300px"
                                  clearable/>
                    </el-form-item>
                    <el-form-item :label="langText.pageComponent" v-if="updateForm.menuType === 1">
                        <el-input v-model="updateForm.component" maxlength="50" :placeholder="langText.pageComponent"
                                  style="width: 300px" clearable/>
                    </el-form-item>
                </el-form>
            </div>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="editMenuDialogVisible = false">{{ langText.cancel }}</el-button>
                    <el-button type="primary" @click="editMenu" :loading="editMenuIng">{{ langText.confirm }}</el-button>
                </div>
            </template>
        </el-dialog>
        <!-- 逻辑删除菜单Dialog -->
        <el-dialog v-model="deleteLogicDialogVisible" :title="langText.deleteConfirmTitle" width="500">
            <span>{{ deleteLogicTips }}</span>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="deleteLogicDialogVisible = false">{{ langText.cancel }}</el-button>
                    <el-button type="primary" @click="deleteLogicMenu" :loading="deleteLogicIng">{{ langText.confirm }}</el-button>
                </div>
            </template>
        </el-dialog>
        <!-- 恢复菜单Dialog -->
        <el-dialog v-model="restoreDialogVisible" :title="langText.restoreConfirmTitle" width="500">
            <span>{{ restoreTips }}</span>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="restoreDialogVisible = false">{{ langText.cancel }}</el-button>
                    <el-button type="primary" @click="restoreMenu" :loading="restoreIng">{{ langText.confirm }}</el-button>
                </div>
            </template>
        </el-dialog>
        <!-- 物理删除菜单Dialog -->
        <el-dialog v-model="deleteDialogVisible" :title="langText.permanentDeleteTitle" width="500">
            <span>{{ deleteTips }}</span>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="deleteDialogVisible = false">{{ langText.cancel }}</el-button>
                    <el-button type="primary" @click="deleteMenu" :loading="deleteIng">{{ langText.confirm }}</el-button>
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

:deep(.menu_table) .action_btn {
    display: flex;
    flex-wrap: wrap;
    column-gap: 10px;
}

:deep(.menu_table) .el-table__row .coll_1 .cell {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
}

:deep(.menu_table) .el-table__row .coll_1 .cell .el-table__expand-icon {
    display: flex;
    width: 20px;
    height: 20px;
    margin: 0;
    align-items: center;
    justify-content: center;
}

:deep(.menu_table) .action_btn .el-button + .el-button {
    margin-left: 0;
}

:deep(.add_menu_dialog) .el-form .el-form-item__content,
:deep(.edit_menu_dialog) .el-form .el-form-item__content {
    column-gap: 10px;
}

</style>