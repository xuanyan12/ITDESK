<script setup>
import {computed, onMounted, ref, watch} from "vue"
import {useNavStore} from "@/stores/_frame/navStore"
import {useLanguageStore} from "@/stores/_frame/languageStore"
import {useRouter} from "vue-router"

const router = useRouter()
const navStore = useNavStore()
const languageStore = useLanguageStore()

// 当前语言
const currentLang = computed(() => languageStore.currentLang)

// Tabs 列表
// 数据结构：{label: '欢迎页', path: '/welcome', closable: false}
const tabs = computed(() => {
    // 获取原始标签数据
    const originalTabs = navStore.tabs;
    
    // 如果是中文，直接返回原始标签
    if (currentLang.value === 'zh') {
        return originalTabs;
    }
    
    // 如果是英文，需要进行深拷贝并翻译标签文本
    return originalTabs.map(tab => {
        const newTab = {...tab};
        newTab.label = translateTabLabel(tab.label);
        return newTab;
    });
});

// 单个标签项翻译 - 根据常见中文标签项提供英文翻译
const translateTabLabel = (label) => {
    const translations = {
        '欢迎页': 'Welcome',
        '控制台': 'Dashboard',
        '系统管理': 'System',
        '用户管理': 'Users',
        '角色管理': 'Roles',
        '权限管理': 'Permissions',
        '菜单管理': 'Menus',
        '部门管理': 'Departments',
        '日志管理': 'Logs',
        '系统日志': 'System Logs',
        '登录日志': 'Login Logs',
        '操作日志': 'Operation Logs',
        '个人中心': 'Profile',
        '修改密码': 'Change Password',
        '我的消息': 'Messages',
        '设备管理': 'Devices',
        '设备列表': 'Device List',
        '设备分类': 'Device Categories',
        '设备监控': 'Device Monitoring',
        '报表中心': 'Reports',
        '统计分析': 'Statistics',
        '数据导出': 'Data Export',
        '网络管理': 'Network',
        '通知公告': 'Announcements',
        'IT管理系统': 'IT Mgt System',
        '电脑管理系统': 'PC',
        '电脑管理': 'PC Management',
        '电脑申请': 'PC Request',
        '电脑审批': 'PC Approval',
        '订单管理': 'Orders',
        '电脑追溯': 'PC Tracking',
        '后台管理': 'Admin'
    };
    
    // 返回翻译后的文本，如果没有翻译则返回原文
    return translations[label] || label;
};

// 当前激活的Tab的name，也就是当前路由的path
const activeTab = ref('')

// 获取当前路由访问的route
onMounted(() => {
    // 组件重新加载后(开发时候热更新)，`watch`无法监听到 useNavStore 的变化，所以这里主动调用一下
    let routeInfo = navStore.routeInfo
    tabsChange(routeInfo)
})

// 监听当前导航`routeInfo`的变化
// 当前路由会实时保存到Store的`routeInfo`中
watch(() => navStore.routeInfo, (routeInfo) => {
    tabsChange(routeInfo)
})

// 多语言文本
const langText = computed(() => {
    return {
        closeOther: currentLang.value === 'zh' ? '关闭其他' : 'Close Others',
        closeLeft: currentLang.value === 'zh' ? '关闭左边' : 'Close Left',
        closeRight: currentLang.value === 'zh' ? '关闭右边' : 'Close Right',
        closeAll: currentLang.value === 'zh' ? '关闭全部' : 'Close All'
    }
});

/**
 * tabs change 事件
 * 如果Tabs中已经存在当前导航，那么设置高亮
 * 如果不存在，添加当前导航到Tabs中，并设置其高亮
 * @param routeInfo 当前访问的路由信息
 */
const tabsChange = (routeInfo) => {
    if (Object.keys(routeInfo).length === 0) {
        // routeInfo不为{}时候，才更改Tab信息
        return
    }

    let findTab = navStore.tabs.find((tab) => tab.path === routeInfo.path)
    if (findTab) {
        activeTab.value = findTab.path
    } else {
        navStore.tabs.push({
            label: routeInfo.label,
            path: routeInfo.path,
            closable: routeInfo.path !== '/welcome'
        })
        activeTab.value = routeInfo.path
    }
}

/**
 * Tab 点击事件
 * @param tab 当前点击的tab
 */
const tabClick = (tab) => {
    router.push(tab.props.name)
}

/**
 * Tab 关闭事件
 * @param tabName 当前关闭的tab的name，也就是页面访问的path
 */
const tabClose = (tabName) => {
    // 如果删除的是当前激活的Tab
    // 那么路由跳转到下一个Tab，如果不存在下一个Tab，那么跳上一个Tab
    if (activeTab.value === tabName) {
        navStore.tabs.forEach((tab, index) => {
            if (tab.path === tabName) {
                let nextTab = navStore.tabs[index + 1] || navStore.tabs[index - 1]
                if (nextTab) {
                    // 跳转到下一个Tab
                    router.push(nextTab.path)
                }
            }
        })
    }

    // 删除Tab的选项及页面的缓存
    removeTab(tabName)

    // Tabs 被删完了的话，重新进入欢迎页
    if (navStore.tabs.length === 0) {
        router.push('/welcome')
    }
}

/**
 * 删除Tab的选项及页面的缓存
 * @param tabName tab的name，也就是页面访问的path
 */
const removeTab = (tabName) => {

    // 过滤没有被删的Tab
    navStore.tabs = navStore.tabs.filter(tab => tab.path !== tabName);

    // 销毁被删页面组件的缓存
    navStore.includes = navStore.includes.filter(item => item.path !== tabName)
}

/**
 * 关闭除当前Tab的所有导航
 */
const closeOther = () => {
    let waitCloseTabs = []
    navStore.tabs.forEach((tab) => {
        if (tab.path !== activeTab.value && tab.closable) {
            waitCloseTabs.push(tab.path)
        }
    })
    waitCloseTabs.forEach((tabName) => {
        removeTab(tabName)
    })
}

/**
 * 关闭当前Tab左边的所有导航
 */
const closeLeft = () => {
    let waitCloseTabs = []
    for (let i = 0; i < navStore.tabs.length; i++) {
        let path = navStore.tabs[i].path
        if (path !== activeTab.value && navStore.tabs[i].closable) {
            waitCloseTabs.push(path)
        }
    }
    waitCloseTabs.forEach((tabName) => {
        removeTab(tabName)
    })
}

/**
 * 关闭当前Tab右边的所有导航
 */
const closeRight = () => {
    let waitCloseTabs = []
    for (let i = navStore.tabs.length - 1; i >= 0; i--) {
        let path = navStore.tabs[i].path
        if (path !== activeTab.value && navStore.tabs[i].closable) {
            waitCloseTabs.push(path)
        }
    }
    waitCloseTabs.forEach((tabName) => {
        removeTab(tabName)
    })
}

/**
 * 关闭所有导航
 */
let closeAll = () => {
    let waitCloseTabs = []
    for (let i = 0; i < navStore.tabs.length; i++) {
        let path = navStore.tabs[i].path
        if (navStore.tabs[i].closable) {
            waitCloseTabs.push(path)
        }
    }
    waitCloseTabs.forEach((tabName) => {
        removeTab(tabName)
    })

    // Tabs 被删完了的话，重新进入欢迎页
    router.push('/welcome')
}

// Tab 下拉列表事件驱动表
const mapDrive = new Map();
mapDrive.set("closeOther", closeOther);
mapDrive.set("closeLeft", closeLeft);
mapDrive.set("closeRight", closeRight);
mapDrive.set("closeAll", closeAll);

/**
 * Tab 操作事件
 * @param command 下拉列表的选项名称
 */
const dropdownEvent = (command) => {
    mapDrive.get(command)();

    // Tabs 被删完了的话，重新进入欢迎页
    if (navStore.tabs.length === 0) {
        router.push('/welcome')
    }
}
</script>

<template>
    <section class="usr_container_header_navbar">
        <div class="usr_navbar_panel">
            <el-tabs
                v-model="activeTab"
                @tab-click="tabClick"
                @tab-remove="tabClose"
                class="usr_el_tabs_override">
                <el-tab-pane
                    v-for="tab in tabs"
                    :name="tab.path"
                    :closable="tab.closable"
                    :key="tab.path">
                    <template #label>
                        <el-icon class="usr_el_icon_home" :size="16" v-if="tab.path === '/welcome'">
                            <HomeFilled/>
                        </el-icon>
                        <span>{{ tab.label }}</span>
                    </template>
                </el-tab-pane>
            </el-tabs>
        </div>
        <div class="usr_navbar_operation">
            <el-dropdown @command="dropdownEvent">
                <span>
                  <el-icon class="usr_navbar_operation_icon"><ArrowDown/></el-icon>
                </span>
                <template #dropdown>
                    <el-dropdown-menu>
                        <el-dropdown-item command="closeOther">
                            <el-icon>
                                <Close/>
                            </el-icon>
                            {{ langText.closeOther }}
                        </el-dropdown-item>
                        <el-dropdown-item command="closeLeft">
                            <el-icon>
                                <ArrowLeft/>
                            </el-icon>
                            {{ langText.closeLeft }}
                        </el-dropdown-item>
                        <el-dropdown-item command="closeRight">
                            <el-icon>
                                <ArrowRight/>
                            </el-icon>
                            {{ langText.closeRight }}
                        </el-dropdown-item>
                        <el-dropdown-item command="closeAll">
                            <el-icon>
                                <CircleClose/>
                            </el-icon>
                            {{ langText.closeAll }}
                        </el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
        </div>
    </section>
</template>

<style scoped>
.usr_container_header_navbar {
    height: 40px;
    position: relative;
}

.usr_navbar_panel {
    padding-right: 40px;
}

:deep(.usr_el_tabs_override) .el-tabs__header {
    margin: 0;
}

:deep(.usr_el_tabs_override) .el-tabs__nav-wrap.is-scrollable {
    padding: 0 40px;
}

:deep(.usr_el_tabs_override) .el-tabs__nav-wrap:after {
    height: 0px;
}

:deep(.usr_el_tabs_override) .el-tabs__nav-prev, :deep(.usr_el_tabs_override) .el-tabs__nav-next {
    width: 40px;
    height: 40px;
    text-align: center;
}

:deep(.usr_el_tabs_override) .el-tabs__nav-prev {
    border-right: 1px solid #f4f4f4;
    transition: background-color .2s;
}

:deep(.usr_el_tabs_override) .el-tabs__nav-next {
    border-left: 1px solid #f4f4f4;
    transition: background-color .2s;
}

:deep(.usr_el_tabs_override) .el-tabs__nav-prev:hover, :deep(.usr_el_tabs_override) .el-tabs__nav-next:hover {
    background-color: #F2F6FC;
}

:deep(.usr_el_tabs_override) .el-tabs__active-bar {
    display: none;
}

:deep(.usr_el_tabs_override) .el-tabs__item {
    color: #646464;
    transition: background-color .3s, color .3s;
    border-right: 1px solid #f4f4f4;
    font-weight: normal;
}

:deep(.usr_el_tabs_override) .el-tabs__item .el-icon {
    margin-left: 8px;
    color: #a7a9af;
}

:deep(.usr_el_tabs_override) .el-tabs__item .usr_el_icon_home {
    margin-left: 0px;
    margin-right: 5px;
}

:deep(.usr_el_tabs_override) .el-tabs__item .el-icon:not(.usr_el_icon_home):hover {
    color: #ffffff;
    background-color: #ff0000;
}

:deep(.usr_el_tabs_override) .el-tabs__item:not(.is-active):hover {
    background-color: rgba(64, 158, 255, 0.05);
    color: #303133;
}

:deep(.usr_el_tabs_override) .el-tabs__item.is-top:nth-child(2) {
    padding-left: 20px;
}

:deep(.usr_el_tabs_override) .el-tabs__item.is-top:last-child {
    padding-right: 20px;
}

:deep(.usr_el_tabs_override) .el-tabs__item.is-active {
    border-bottom: 2px solid #409eff;
    color: #409eff;
    background-color: rgba(64, 158, 255, 0.1);
}

:deep(.usr_el_tabs_override) .el-tabs__item.is-active .usr_el_icon_home {
    color: #409eff;
}

.usr_navbar_operation {
    position: absolute;
    top: 0;
    right: 0;
    border-left: 1px solid #f4f4f4;
    transition: background-color .2s;
}

.usr_navbar_operation:hover {
    background-color: #F2F6FC;
}

.usr_navbar_operation_icon {
    width: 40px;
    height: 40px;
}
</style>
