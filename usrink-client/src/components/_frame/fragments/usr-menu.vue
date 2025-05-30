<script setup>
import usrMenuHeader from "@/components/_frame/fragments/usr-menu-header.vue";
import {computed} from "vue";
import {useCollapseStateStore} from "@/stores/_frame/collapseStateStore";
import {useRoutesStore} from "@/stores/_frame/routesStore";
import {useNavStore} from '@/stores/_frame/navStore';
import {useLanguageStore} from '@/stores/_frame/languageStore';
import {useRouter} from "vue-router";

const router = useRouter()
const routesStore = useRoutesStore()
const navStore = useNavStore()
const collapseStateStore = useCollapseStateStore()
const languageStore = useLanguageStore()

// 菜单折叠状态
const collapseState = computed(() => collapseStateStore.collapseState)
// 根据菜单折叠状态计算菜单的宽度
const width = computed(() => collapseStateStore.collapseState ? '64px' : '220px')
// Menu Height
const height = computed(() => collapseStateStore.collapseState ? '60px' : '135px')

// 当前语言
const currentLang = computed(() => languageStore.currentLang)

// Menu菜单列表
const menus = computed(() => {
    // 首先获取原始菜单数据
    const originalMenus = routesStore.menus;
    
    // 如果是中文，直接返回原始菜单
    if (currentLang.value === 'zh') {
        return originalMenus;
    }
    
    // 如果是英文，需要进行深拷贝并翻译菜单项
    return translateMenus(JSON.parse(JSON.stringify(originalMenus)));
});

// 翻译菜单函数 - 递归处理所有层级
const translateMenus = (menuItems) => {
    return menuItems.map(item => {
        // 翻译当前项的标签
        item.label = translateMenuItem(item.label);
        
        // 如果有子菜单，递归翻译
        if (item.children && item.children.length > 0) {
            item.children = translateMenus(item.children);
        }
        
        return item;
    });
};

// 单个菜单项翻译 - 根据常见中文菜单项提供英文翻译
const translateMenuItem = (label) => {
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
        'IT管理系统': 'IT Mgt System',
        '电脑管理系统': 'PC Manager',
        '电脑管理': 'PC Management',
        '电脑申请': 'PC Request',
        '电脑审批': 'PC Approval',
        '订单管理': 'Orders',
        '电脑追溯': 'PC Tracking',
        '后台管理': 'Admin',
        // 邮件日志相关翻译
        '邮件日志': 'Email Logs',
        '邮件管理': 'Email Management',
        // 订单管理相关翻译
        '分配电脑': 'Assign Computer',
        '电脑分配': 'Computer Assignment',
        '电脑订单': 'Computer Orders',
        '订单分配': 'Order Allocation',
        '订单追踪': 'Order Tracking'
    };
    
    // 返回翻译后的文本，如果没有翻译则返回原文
    return translations[label] || label;
};

// 菜单当前激活项
const activeIndex = computed(() => navStore.routeInfo.id)

// 菜单路由跳转
const goTo = (path) => router.push(path)

</script>

<template>
    <section
        class="usr_menu"
        :style="{width:width}">
        <usr-menu-header/>
        <div class="usr_menu_panel" :style="{height:'calc(100vh - ' + height + ')'}">
            <el-scrollbar>
                <!-- 树形菜单，只支持三级菜单，够用！ -->
                <el-menu
                    class="el_menu_override"
                    :default-active="activeIndex"
                    :default-openeds="['1']"
                    :collapse="collapseState"
                    background-color="white"
                    text-color="rgb(30, 30, 30)">
                    <template
                        v-for="menu in menus"
                        :key="menu.id">
                        <el-sub-menu
                            v-if="menu.children"
                            :index="menu.id">
                            <template #title>
                                <el-icon>
                                    <component :is="menu.icon"/>
                                </el-icon>
                                <span>{{ menu.label }}</span>
                            </template>
                            <div
                                v-for="son in menu.children"
                                :key="son.id">
                                <el-sub-menu
                                    v-if="son.children"
                                    :index="son.id">
                                    <template #title>
                                        <el-icon>
                                            <component :is="son.icon"/>
                                        </el-icon>
                                        <span>{{ son.label }}</span>
                                    </template>
                                    <el-menu-item
                                        v-for="sun in son.children"
                                        @click="goTo(sun.path)"
                                        :index="sun.id"
                                        :key="sun.id">
                                        <el-icon>
                                            <component :is="sun.icon"/>
                                        </el-icon>
                                        <template #title>
                                            {{ sun.label }}
                                        </template>
                                    </el-menu-item>
                                </el-sub-menu>
                                <el-menu-item
                                    @click="goTo(son.path)"
                                    :index="son.id"
                                    v-else>
                                    <el-icon>
                                        <component :is="son.icon"/>
                                    </el-icon>
                                    <template #title>
                                        {{ son.label }}
                                    </template>
                                </el-menu-item>
                            </div>
                        </el-sub-menu>
                        <el-menu-item
                            @click="goTo(menu.path)"
                            :index="menu.id"
                            v-else>
                            <el-icon>
                                <component :is="menu.icon"/>
                            </el-icon>
                            <template #title>
                                {{ menu.label }}
                            </template>
                        </el-menu-item>
                    </template>
                </el-menu>
            </el-scrollbar>
        </div>
    </section>
</template>

<style scoped>
.usr_menu {
    height: 100vh;
    float: left;
    transition: width .5s;
}

.usr_menu_panel {
    background-color: white;
    transition: height .5s;
}

.el_menu_override {
    border-right: solid 1px rgb(246, 248, 249);
}
</style>