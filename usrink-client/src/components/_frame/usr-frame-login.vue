<script setup>
import {ref} from "vue";
import loginUtil from "@/utils/LoginUtil";
import httpUtil from "@/utils/HttpUtil";
import {useRouter} from 'vue-router'
import {ElMessage} from "element-plus";

const router = useRouter()

// 登录按钮Loading状态
const loading = ref(false)

// 登录表单
const loginForm = ref({
    userName: '',
    password: ''
})

// 登录事件
const submitLogin = () => {
    // 验证表单
    if (!loginForm.value.userName) {
        ElMessage.error('用户名不能为空，请输入用户名！')
        return
    }
    if (!loginForm.value.password) {
        ElMessage.error('密码不能为空，请输入密码！')
        return
    }

    loading.value = true
    // 密码加密
    loginForm.value.password = loginUtil.encryptPassword(loginForm.value.password)
    // 登录请求
    httpUtil.post('/login', {...loginForm.value}).then(async res => {
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
</script>

<template>
    <section class="tech-login-container">
        <!-- 背景动画元素 -->
        <div class="tech-background">
            <div class="glow-circle top-left"></div>
            <div class="glow-circle bottom-right"></div>
            <div class="particle-network"></div>
        </div>

        <div class="login-content">
            <div class="logo-container">
                <img src="/SEG-logo.png" alt="SEG Logo" class="seg-logo">
            </div>
            
            <el-form
                :model="loginForm"
                class="tech-form"
                @submit.native.prevent @keyup.enter="submitLogin">
                <h3 class="tech-title">
                    <span class="gradient-text">系统登录</span>
                </h3>
                <div class="title-decoration"></div>
                
                <el-form-item>
                    <el-input
                        v-model="loginForm.userName"
                        placeholder="用户名"
                        size="large"
                        prefix-icon="UserFilled"
                        clearable
                        class="tech-input"/>
                </el-form-item>
                <el-form-item>
                    <el-input
                        v-model="loginForm.password"
                        placeholder="密码"
                        type="password"
                        size="large"
                        prefix-icon="Lock"
                        show-password
                        class="tech-input"/>
                </el-form-item>
                <el-form-item>
                    <el-button
                        type="primary"
                        :loading="loading"
                        @click="submitLogin"
                        size="large"
                        class="tech-button">
                        登录
                    </el-button>
                </el-form-item>
                <el-form-item>
                    <div class="guest-account">
                        <el-text type="info">测试账号：admin/xiajun(请勿随意进行操作)</el-text>
                    </div>
                </el-form-item>
            </el-form>
            
            <div class="tech-footer">
                <span>© 2025 SEG IT 部门. 保留所有权利</span>
            </div>
        </div>
    </section>
</template>

<style scoped>
/* 导入字体 */
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@700;900&display=swap');

.tech-login-container {
    height: 100vh;
    width: 100%;
    display: flex;
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

/* 登录内容区域 */
.login-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 100%;
    max-width: 420px;
    z-index: 10;
    position: relative;
}

.logo-container {
    margin-bottom: 30px;
}

.seg-logo {
    width: 160px;
    height: auto;
    object-fit: contain;
}

.tech-form {
    width: 100%;
    padding: 40px;
    background-color: rgba(255, 255, 255, 0.9);
    border-radius: 16px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(0, 83, 137, 0.1);
    position: relative;
    overflow: hidden;
}

.tech-form::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 4px;
    background: linear-gradient(90deg, #005389, #029165);
}

.tech-title {
    font-size: 32px;
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
    width: 120px;
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

.guest-account {
    text-align: center;
    margin-top: 10px;
    font-size: 14px;
    opacity: 0.7;
}

.tech-footer {
    margin-top: 30px;
    color: rgba(0, 83, 137, 0.7);
    font-size: 14px;
    text-align: center;
}

/* 响应式调整 */
@media (max-width: 480px) {
    .tech-form {
        padding: 30px 20px;
    }
    
    .tech-title {
        font-size: 28px;
    }
    
    .seg-logo {
        width: 120px;
    }
}
</style>
