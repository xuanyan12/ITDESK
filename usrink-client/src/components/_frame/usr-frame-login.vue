<script setup>
import {ref, computed} from "vue";
import loginUtil from "@/utils/LoginUtil";
import httpUtil from "@/utils/HttpUtil";
import {useRouter} from 'vue-router'
import {ElMessage} from "element-plus";
import { ArrowDown, QuestionFilled } from '@element-plus/icons-vue';
import qaPage from "@/views/public/qa-page.vue";
import {useLanguageStore} from "@/stores/_frame/languageStore";

const router = useRouter()
const languageStore = useLanguageStore()

// 登录按钮Loading状态
const loading = ref(false)

// 登录表单
const loginForm = ref({
    userName: '',
    password: ''
})

// 使用全局语言状态
const currentLang = computed(() => languageStore.currentLang)

// QA页面显示状态
const showQaPage = ref(false)

// 语言切换处理函数 - 使用全局方法
const handleLangChange = (command) => {
    languageStore.setLanguage(command)
}

// 多语言文本
const langText = computed(() => {
    return {
        title: currentLang.value === 'zh' ? 'SEG IT管理系统' : 'SEG IT Mgt System',
        systemLogin: currentLang.value === 'zh' ? '系统登录' : 'System Login',
        username: currentLang.value === 'zh' ? '输入NT账号（电脑开机账号）' : 'Enter NT account (computer login account)',
        password: currentLang.value === 'zh' ? '输入开机密码' : 'Enter login password',
        login: currentLang.value === 'zh' ? '登录' : 'Login',
        footer: currentLang.value === 'zh' ? 'Copyright © 2025 SEG IT 部门. 保留所有权利' : 'Copyright © 2025 SEG IT Department. All Rights Reserved',
        usernameRequired: currentLang.value === 'zh' ? '用户名不能为空，请输入用户名！' : 'Username cannot be empty!',
        passwordRequired: currentLang.value === 'zh' ? '密码不能为空，请输入密码！' : 'Password cannot be empty!',
        getTempPassword: currentLang.value === 'zh' ? '获取临时密码' : 'Get Temporary Password',
        qa: currentLang.value === 'zh' ? 'FAQ' : 'FAQ'
    }
})

// 登录事件
const submitLogin = () => {
    // 验证表单
    if (!loginForm.value.userName) {
        ElMessage.error(langText.value.usernameRequired)
        return
    }
    if (!loginForm.value.password) {
        ElMessage.error(langText.value.passwordRequired)
        return
    }

    loading.value = true
    // 发送登录请求 - 不再加密密码
    httpUtil.post('/login', {...loginForm.value}, {
        timeout: 120000  // 设置超时时间为120秒
    }).then(async res => {
        // 缓存用户信息
        loginUtil.cacheUserInfo(res.data)
        // 模拟登录耗时操作
        // await new Promise(resolve => setTimeout(resolve, 500))
        router.push('/')
    }).catch(e => {
        // 清空密码
        loginForm.value.password = ''
    }).finally(() => {
        // 关闭Loading
        loading.value = false
    })
}

// 获取临时密码
const getTempPassword = () => {
    // 验证用户名
    if (!loginForm.value.userName) {
        ElMessage.error(langText.value.usernameRequired)
        return
    }
    
    httpUtil.get(`/sysUser/getTempPassword?userName=${loginForm.value.userName}`, {
        timeout: 120000  // 设置超时时间为120秒
    }).then(res => {
        ElMessage({
            type: 'success',
            message: res.msg
        })
    }).catch(err => {
        console.error(err)
    })
}

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
    <section class="tech-login-container">
        <!-- 背景动画元素 -->
        <div class="tech-background">
            <div class="glow-circle top-left"></div>
            <div class="glow-circle bottom-right"></div>
            <div class="particle-network"></div>
        </div>

        <!-- 语言切换下拉框 - 移到屏幕右上角 -->
        <div class="lang-dropdown">
            <div class="qa-btn" @click="showQA">
                <el-icon :size="18">
                    <QuestionFilled/>
                </el-icon>
                <span class="qa-text">{{ langText.qa }}</span>
            </div>
            <el-dropdown @command="handleLangChange">
                <span class="lang-dropdown-link">
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

        <div class="login-card">
            <!-- 左侧品牌区域 -->
            <div class="brand-section">
            <div class="logo-container">
                <img src="/SEG-logo.png" alt="SEG Logo" class="seg-logo">
                </div>
                <h2 class="brand-title">{{ langText.title }}</h2>
            </div>
            
            <!-- 右侧登录表单区域 -->
            <div class="login-form-section">
            <el-form
                :model="loginForm"
                class="tech-form"
                @submit.native.prevent @keyup.enter="submitLogin">
                <h3 class="tech-title">
                        <span class="gradient-text">{{ langText.systemLogin }}</span>
                </h3>
                <div class="title-decoration"></div>
                
                <el-form-item>
                    <el-input
                        v-model="loginForm.userName"
                            :placeholder="langText.username"
                        size="large"
                        prefix-icon="UserFilled"
                        clearable
                        class="tech-input"/>
                </el-form-item>
                <el-form-item>
                    <el-input
                        v-model="loginForm.password"
                            :placeholder="langText.password"
                        type="password"
                        size="large"
                        prefix-icon="Lock"
                        show-password
                        class="tech-input"/>
                </el-form-item>
                <el-form-item>
                    <div class="button-container">
                        <el-button
                            type="primary"
                            :loading="loading"
                            @click="submitLogin"
                            size="large"
                            class="tech-button login-btn">
                                {{ langText.login }}
                        </el-button>
                        <el-button
                            type="primary"
                            @click="getTempPassword"
                            size="large"
                            class="tech-button temp-password-btn">
                                {{ langText.getTempPassword }}
                        </el-button>
                    </div>
                </el-form-item>
                </el-form>
            </div>
                    </div>
            
            <div class="tech-footer">
            <span>{{ langText.footer }}</span>
        </div>
        
        <!-- QA页面弹窗 -->
        <qa-page v-if="showQaPage" @close="closeQA" />
    </section>
</template>

<style scoped>
/* 导入字体 */
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@700;900&display=swap');

.tech-login-container {
    height: 100vh;
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    position: relative;
    overflow: hidden;
    background-color: #f8f9fa;
    font-family: 'Inter', 'Roboto', sans-serif;
}

/* 背景动画元素 */
.tech-background {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 1;
    overflow: hidden;
}

.glow-circle {
    position: absolute;
    border-radius: 50%;
    filter: blur(80px);
    z-index: 0;
    transition: transform 0.5s ease-out;
}

.top-left {
    top: -150px;
    left: -150px;
    width: 500px;
    height: 500px;
    background: radial-gradient(circle, rgba(0, 83, 137, 0.2) 0%, rgba(0, 83, 137, 0.1) 50%, rgba(0, 83, 137, 0) 80%);
}

.bottom-right {
    bottom: -200px;
    right: -200px;
    width: 600px;
    height: 600px;
    background: radial-gradient(circle, rgba(2, 145, 101, 0.2) 0%, rgba(2, 145, 101, 0.1) 50%, rgba(2, 145, 101, 0) 80%);
}

.particle-network {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 2;
    background-image: 
        radial-gradient(rgba(0, 83, 137, 0.1) 1px, transparent 1px),
        radial-gradient(rgba(2, 145, 101, 0.1) 1px, transparent 1px);
    background-size: 40px 40px;
    background-position: 0 0, 20px 20px;
}

/* 登录卡片 */
.login-card {
    display: flex;
    width: 860px;
    max-width: 90%;
    min-height: 480px;
    background-color: #fff;
    border-radius: 16px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    z-index: 10;
    position: relative;
}

/* 语言下拉菜单 - 移到屏幕右上角 */
.lang-dropdown {
    position: fixed;
    top: 20px;
    right: 20px;
    z-index: 100;
    display: flex;
    align-items: center;
    gap: 10px;
}

.qa-btn {
    display: flex;
    align-items: center;
    gap: 5px;
    cursor: pointer;
    padding: 5px 12px;
    border-radius: 4px;
    color: #ffffff;
    font-size: 14px;
    font-weight: 500;
    background: rgba(0, 83, 137, 0.4);
    backdrop-filter: blur(5px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    transition: all 0.3s ease;
}

.qa-btn:hover {
    background: rgba(0, 83, 137, 0.6);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.qa-text {
    font-size: 14px;
    font-weight: 500;
}

.lang-dropdown-link {
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    padding: 5px 12px;
    border-radius: 4px;
    color: #ffffff;
    font-size: 14px;
    font-weight: 500;
    background: rgba(0, 83, 137, 0.4);
    backdrop-filter: blur(5px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    transition: all 0.3s ease;
}

.lang-dropdown-link:hover {
    background: rgba(0, 83, 137, 0.6);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

:deep(.active-lang) {
    color: #005389;
    font-weight: bold;
    background-color: rgba(0, 83, 137, 0.1);
}

/* 左侧品牌区域 */
.brand-section {
    width: 40%;
    padding: 40px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #005389, #029165);
    color: white;
    position: relative;
    overflow: hidden;
}

.brand-section::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-image: url("data:image/svg+xml,%3Csvg width='80' height='80' viewBox='0 0 80 80' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M50 50c0-5.523 4.477-10 10-10s10 4.477 10 10-4.477 10-10 10c0 5.523-4.477 10-10 10s-10-4.477-10-10 4.477-10 10-10zM10 10c0-5.523 4.477-10 10-10s10 4.477 10 10-4.477 10-10 10c0 5.523-4.477 10-10 10S0 25.523 0 20s4.477-10 10-10zm10 8c4.418 0 8-3.582 8-8s-3.582-8-8-8-8 3.582-8 8 3.582 8 8 8zm40 40c4.418 0 8-3.582 8-8s-3.582-8-8-8-8 3.582-8 8 3.582 8 8 8z' /%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
    opacity: 0.1;
}

.logo-container {
    margin-bottom: 15px; /* 减小间距，使logo和标题更接近 */
    position: relative;
    z-index: 5;
}

.seg-logo {
    width: 160px;
    height: auto;
    object-fit: contain;
    filter: brightness(0) invert(1);
}

.brand-title {
    font-size: 24px;
    font-weight: 700;
    margin-bottom: 10px;
    text-align: center;
    font-family: 'Montserrat', sans-serif;
    position: relative;
    z-index: 5;
}

/* 右侧登录表单区域 */
.login-form-section {
    width: 60%;
    padding: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.tech-form {
    width: 100%;
    max-width: 360px;
}

.tech-title {
    font-size: 28px;
    text-align: center;
    margin-bottom: 15px;
    font-weight: 700;
    font-family: 'Montserrat', sans-serif;
}

.gradient-text {
    background: linear-gradient(90deg, #005389, #029165, #005389);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    background-size: 200% auto;
    animation: textGradient 5s ease infinite;
}

@keyframes textGradient {
    0% { background-position: 0% center; }
    50% { background-position: 100% center; }
    100% { background-position: 0% center; }
}

.title-decoration {
    width: 80px;
    height: 3px;
    background: linear-gradient(90deg, #005389, #029165);
    margin: 0 auto 30px;
    position: relative;
    overflow: hidden;
}

.title-decoration::after {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.8), transparent);
    animation: shine 3s infinite;
}

@keyframes shine {
    0% { left: -100%; }
    100% { left: 100%; }
}

.tech-input :deep(.el-input__wrapper) {
    border-radius: 8px;
    border: 1px solid rgba(0, 83, 137, 0.2);
    box-shadow: 0 3px 10px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
}

.tech-input :deep(.el-input__wrapper:hover) {
    border-color: rgba(0, 83, 137, 0.4);
    box-shadow: 0 5px 15px rgba(0, 83, 137, 0.1);
}

.tech-input :deep(.el-input__wrapper.is-focus) {
    border-color: #005389;
    box-shadow: 0 0 0 1px rgba(0, 83, 137, 0.2);
}

.tech-button {
    width: 100%;
    height: 50px;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 600;
    background: linear-gradient(135deg, #005389, #029165);
    border: none;
    box-shadow: 0 6px 15px rgba(0, 83, 137, 0.15);
    transition: all 0.3s ease;
}

.tech-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0, 83, 137, 0.2);
    background: linear-gradient(135deg, #0068ab, #02a674);
}

.tech-button:active {
    transform: translateY(0);
    box-shadow: 0 4px 10px rgba(0, 83, 137, 0.1);
}

.button-container {
    display: flex;
    gap: 10px;
    width: 100%;
}

.button-container .login-btn,
.button-container .temp-password-btn {
    flex: 1;
    margin-top: 0;
}

.tech-button.temp-password-btn {
    background: linear-gradient(135deg, #005389, #029165);
    border: none;
    box-shadow: 0 6px 15px rgba(0, 83, 137, 0.15);
    transition: all 0.3s ease;
}

.tech-button.temp-password-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0, 83, 137, 0.2);
    background: linear-gradient(135deg, #0068ab, #02a674);
}

.tech-button.temp-password-btn:active {
    transform: translateY(0);
    box-shadow: 0 4px 10px rgba(0, 83, 137, 0.1);
}

.tech-footer {
    margin-top: 20px;
    color: rgba(0, 83, 137, 0.7);
    font-size: 14px;
    text-align: center;
    z-index: 10;
}

/* 响应式调整 */
@media (max-width: 768px) {
    .login-card {
        flex-direction: column;
        width: 90%;
        max-width: 420px;
    }
    
    .brand-section {
        width: 100%;
        padding: 30px 20px;
        min-height: 180px;
    }
    
    .login-form-section {
        width: 100%;
    }
    
    .tech-form {
        max-width: 100%;
    }
    
    .seg-logo {
        width: 120px;
    }
    
    .brand-title {
        font-size: 20px;
    }
    
    /* 响应式语言选择器位置调整 */
    .lang-dropdown {
        top: 10px;
        right: 10px;
    }
}
</style>
