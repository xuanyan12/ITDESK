<script setup>
import {useRouter} from "vue-router";
import {useCollapseStateStore} from "@/stores/_frame/collapseStateStore";
import {useNavStore} from "@/stores/_frame/navStore";
import {useUserInfoStore} from "@/stores/_frame/userInfoStore";
import {useLanguageStore} from "@/stores/_frame/languageStore";
import {computed, ref} from "vue";
import loginUtil from "@/utils/LoginUtil";
import screenUtil from "@/utils/ScreenUtil";
import {BASE_URL} from "@/utils/Constant.js";
import {ElementPlus, QuestionFilled} from "@element-plus/icons-vue";
import qaPage from "@/views/public/qa-page.vue";

const router = useRouter()
const navStore = useNavStore()
const userInfoStore = useUserInfoStore()
const collapseStateStore = useCollapseStateStore()
const languageStore = useLanguageStore()

// 菜单折叠状态
const collapseState = computed(() => collapseStateStore.collapseState)

// 当前语言
const currentLang = computed(() => languageStore.currentLang)

// 面包屑
const breadcrumb = computed(() => {
    // 获取原始面包屑数据
    let routeInfo = navStore.routeInfo;
    if (!routeInfo.breadcrumb) return [];
    
    const originalBreadcrumb = routeInfo.breadcrumb;
    
    // 如果是中文，直接返回原始面包屑
    if (currentLang.value === 'zh') {
        return originalBreadcrumb;
    }
    
    // 如果是英文，需要进行翻译
    return originalBreadcrumb.map(item => translateBreadcrumb(item));
});

// 单个面包屑项翻译 - 根据常见中文面包屑项提供英文翻译
const translateBreadcrumb = (label) => {
    const translations = {
        '首页': 'Home',
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
        '无权访问页面': 'Access Denied',
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

/**
 * 菜单折叠按钮点击事件
 */
const collapseClick = () => {
    collapseStateStore.collapseState = !collapseState.value
}

/**
 * 全屏
 */
const screenFull = () => {
    screenUtil.screenFull(val => {
    })
}

/**
 * 退出登录
 */
const logout = () => {
    loginUtil.logout()
    router.push("/login")
}

/**
 * 切换语言
 */
const handleLanguageChange = (command) => {
    languageStore.setLanguage(command)
}

// QA页面显示状态
const showQaPage = ref(false)

// 多语言文本
const langText = computed(() => {
    return {
        collapse: currentLang.value === 'zh' ? '折叠菜单' : 'Collapse Menu',
        logout: currentLang.value === 'zh' ? '退出' : 'Logout',
        fullscreen: currentLang.value === 'zh' ? '全屏' : 'Fullscreen',
        qa: currentLang.value === 'zh' ? 'FAQ' : 'FAQ'
    }
})

// 显示QA页面
const showQA = () => {
    showQaPage.value = true
}

// 关闭QA页面
const closeQA = () => {
    showQaPage.value = false
}

</script>

<template>
    <section class="usr_container_header_menu">
        <div class="usr_container_header_menu_left">
            <div
                @click="collapseClick"
                class="usr_header_item usr_container_header_menu_left_collapse">
                <el-icon :size="18">
                    <component :is="collapseState ? 'Expand' : 'Fold'"/>
                </el-icon>
            </div>
            <div class="usr_header_item usr_container_header_menu_left_breadcrumb">
                <el-breadcrumb separator="/">
                    <el-breadcrumb-item
                        v-for="(crumb, index) of breadcrumb"
                        :key="index">
                        {{ crumb }}
                    </el-breadcrumb-item>
                </el-breadcrumb>
            </div>
        </div>
        <div class="usr_container_header_menu_right">
            <div 
                @click="showQA"
                class="usr_header_item usr_container_header_menu_right_qa">
                <el-icon :size="18">
                    <QuestionFilled/>
                </el-icon>
                <span class="qa-text">{{ langText.qa }}</span>
            </div>
            <div class="usr_header_item usr_container_header_menu_right_language">
                <el-dropdown @command="handleLanguageChange">
                    <span class="language-dropdown-link">
                        {{ currentLang === 'zh' ? 'ZH' : 'EN' }}
                        <el-icon class="el-icon--right"><arrow-down /></el-icon>
                    </span>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item :command="'zh'" :class="{ 'active-lang': currentLang === 'zh' }">ZH 中文</el-dropdown-item>
                            <el-dropdown-item :command="'en'" :class="{ 'active-lang': currentLang === 'en' }">EN English</el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </div>
            <div class="usr_header_item usr_container_header_menu_right_nick no-hover-effect">
                <el-dropdown class="el_dropdown_override" trigger="click">
                    <span class="user-name-wrapper">
                        <span>{{ userInfoStore.userInfo.userNick }}<el-text
                            type="info">({{ userInfoStore.roleInfo.roleName }})</el-text></span>
                    </span>
                    <template #dropdown>
                        <el-dropdown-menu class="el_dropdown_menu_override">
                            <el-dropdown-item
                                @click="logout">
                                <el-icon :size="18">
                                    <SwitchButton/>
                                </el-icon>
                                {{ langText.logout }}
                            </el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </div>
            <div
                @click="screenFull"
                class="usr_header_item usr_container_header_menu_right_fullscreen">
                <el-icon :size="18">
                    <FullScreen/>
                </el-icon>
            </div>
        </div>
    </section>
    
    <!-- QA页面弹窗 -->
    <qa-page v-if="showQaPage" @close="closeQA" />
</template>

<style scoped>
.usr_container_header_menu {
    height: 50px;
    border-bottom: 1px solid #f4f4f4;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.usr_container_header_menu_left, .usr_container_header_menu_right {
    display: flex;
    align-items: center;
}

.usr_header_item {
    height: 50px;
    padding: 0 10px;
    display: flex;
    align-items: center;
    transition: background-color .2s;
}

.usr_header_item:not(.usr_container_header_menu_left_breadcrumb,.usr_container_header_menu_right_nick):hover {
    background-color: #F2F6FC;
    cursor: pointer;
}

/* 语言切换样式 */
.language-dropdown-link {
    display: flex;
    align-items: center;
    cursor: pointer;
    font-weight: 500;
    transition: all 0.3s ease;
}

/* QA按钮样式 */
.usr_container_header_menu_right_qa {
    display: flex;
    align-items: center;
    gap: 5px;
    cursor: pointer;
    transition: all 0.3s ease;
}

.usr_container_header_menu_right_qa:hover {
    background-color: #F2F6FC !important;
    color: #409EFF;
}

.qa-text {
    font-size: 14px;
    font-weight: 500;
}

:deep(.active-lang) {
    color: #409EFF;
    font-weight: bold;
    background-color: rgba(64, 158, 255, 0.1);
}

:deep(.el_badge_override) sup.is-dot {
    right: 8px;
    top: 4px
}

.el_badge_override {
    margin-right: 5px;
}

.el_avatar_override {
    background-color: #409eff;
}

:deep(li).el-dropdown-menu__item {
    padding: 10px 15px;
    min-width: 120px;
    color: #7c7c7c;
}

:deep(li).el-dropdown-menu__item:focus {
    color: #303133;
    background-color: #F2F6FC;
}

:deep(li).el-dropdown-menu__item .el-badge {
    display: flex;
    align-items: center;
    margin-left: 5px;
}

/* 添加用户名称相关样式，去掉黑框 */
.usr_container_header_menu_right_nick.no-hover-effect {
    cursor: pointer;
}

.usr_container_header_menu_right_nick .el_dropdown_override {
    display: block;
}

.usr_container_header_menu_right_nick .user-name-wrapper {
    display: flex;
    align-items: center;
}

/* 移除下拉菜单的边框和背景变化 */
:deep(.el-dropdown-link:hover),
:deep(.el-dropdown-link:focus),
:deep(.el-dropdown-link:active) {
    outline: none !important;
    box-shadow: none !important;
    border-color: transparent !important;
    background-color: transparent !important;
}

:deep(.el-dropdown-selfdefine:focus-visible) {
    outline: none !important;
}
</style>