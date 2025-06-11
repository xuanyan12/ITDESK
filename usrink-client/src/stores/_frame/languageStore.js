import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

// 为了解决导出问题，先定义store然后再导出
const useLanguageStore = defineStore('languageStore', () => {
    // 当前语言: 'zh' 中文, 'en' 英文
    const currentLang = ref('zh')

    // 切换语言
    const toggleLanguage = () => {
        currentLang.value = currentLang.value === 'zh' ? 'en' : 'zh'
    }

    // 设置语言
    const setLanguage = (lang) => {
        if (lang === 'zh' || lang === 'en') {
            currentLang.value = lang
        }
    }

    // 多语言文本 - 侧边栏公共部分
    const sidebarText = computed(() => {
        return {
            systemLogin: currentLang.value === 'zh' ? '系统登录' : 'System Login',
            welcome: currentLang.value === 'zh' ? '欢迎' : 'Welcome',
            console: currentLang.value === 'zh' ? '控制台' : 'Console',
            forbidden: currentLang.value === 'zh' ? '无权访问' : 'Access Denied',
            forbiddenPage: currentLang.value === 'zh' ? '无权访问页面' : 'Access Denied',
            // 新增常用翻译
            itCenter: currentLang.value === 'zh' ? 'IT管理系统' : 'IT Mgt System',
            computerManagementSystem: currentLang.value === 'zh' ? '电脑管理系统' : 'PC Manager',
            computerManagement: currentLang.value === 'zh' ? '电脑管理' : 'PC Management',
            computerRequest: currentLang.value === 'zh' ? '电脑申请' : 'PC Request',
            computerApproval: currentLang.value === 'zh' ? '电脑审批' : 'PC Approval',
            orderManagement: currentLang.value === 'zh' ? '订单管理' : 'Orders',
            computerTracking: currentLang.value === 'zh' ? '电脑追溯' : 'PC Tracking',
            backendManagement: currentLang.value === 'zh' ? '后台管理' : 'Admin',
            // 邮件日志相关翻译
            emailLog: currentLang.value === 'zh' ? '邮件日志' : 'Email Logs',
            emailManagement: currentLang.value === 'zh' ? '邮件管理' : 'Email Management',
            // 订单管理相关翻译
            orders: currentLang.value === 'zh' ? '订单管理' : 'Order Management',
            orderAllocation: currentLang.value === 'zh' ? '订单分配' : 'Order Allocation',
            orderTracking: currentLang.value === 'zh' ? '订单追踪' : 'Order Tracking'
        }
    })

    return { 
        currentLang, 
        toggleLanguage, 
        setLanguage,
        sidebarText
    }
}, {
    persist: {
        enabled: true,
        strategies: [{
            // 保存到localStorage
            storage: localStorage
        }]
    }
})

// 导出store
export { useLanguageStore } 