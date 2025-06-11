<script setup>
import {computed, ref} from "vue";
import {useLanguageStore} from "@/stores/_frame/languageStore";
import { ArrowDown, Close, Search } from '@element-plus/icons-vue';

const languageStore = useLanguageStore()

// 当前语言
const currentLang = computed(() => languageStore.currentLang)

// 搜索关键词
const searchKeyword = ref('')

// 多语言文本
const langText = computed(() => {
    return {
        title: currentLang.value === 'zh' ? 'FAQ - 常见问题解答' : 'FAQ - Frequently Asked Questions',
        searchPlaceholder: currentLang.value === 'zh' ? '搜索问题...' : 'Search questions...',
        loginSection: currentLang.value === 'zh' ? '一、登录相关' : '1. Login Related',
        applicationSection: currentLang.value === 'zh' ? '二、申请相关' : '2. Application Related',
        approvalSection: currentLang.value === 'zh' ? '三、审批相关' : '3. Approval Related',
        interfaceSection: currentLang.value === 'zh' ? '四、界面相关' : '4. Interface Related',
        close: currentLang.value === 'zh' ? '关闭' : 'Close'
    }
})

// QA数据
const qaData = computed(() => {
    if (currentLang.value === 'zh') {
        return {
            login: [
                {
                    question: 'Q:登录时提示AD域连接异常',
                    answer: 'A:请检查网络连接，确认密码正确；如果确认密码无误，则需要通过点击获取临时密码的按钮，此时会收到一封带有临时密码的邮件，请输入这段临时密码并点击登录（该临时密码请勿告知他人，以免造成密码泄露）；如果问题仍然持续，请联系IT部门进行处理。'
                }
            ],
            application: [
                {
                    question: 'Q:申请新电脑需要什么条件？',
                    answer: 'A:根据申请类别来进行判断，需要满足相应的条件。\n1.选择"办公电脑超六年换新"时，要求需要更换的电脑出厂日期距今超过六年，成本中心将自动置为IT的成本中心，费用计入IT的成本中心。\n2.选择"办公电脑未超六年换新"时，要求需要更换的电脑出厂日期距今未超过六年，成本中心将自动置为使用人的成本中心，费用计入使用人的成本中心。\n3.选择"办公电脑未超六年换旧"时，要求需要更换的电脑出厂日期距今未超过六年，成本中心将自动置为IT的成本中心，费用计入IT的成本中心。\n4.选择"秘书代申请新岗位员工电脑"时，成本中心将自动置为IT的成本中心，费用计入IT的成本中心。\n5.选择"秘书代申请替代岗位员工电脑"时，成本中心将自动置为IT的成本中心，费用计入IT的成本中心。\n6.选择"秘书代申请新实习生/外服电脑"时，成本中心将自动置为IT的成本中心，费用计入IT的成本中心。\n7.选择"其他用途电脑申请"时，需要自行选择成本中心，成本中心将自动置为所选择的成本中心，费用计入所选择的成本中心。'
                },
                {
                    question: 'Q:申请被驳回了，如何重新申请？',
                    answer: 'A:查看驳回理由，并根据驳回理由进行修正后再次进行申请。'
                },
                {
                    question: 'Q:我的电脑信息显示不正确怎么办，名下出现不属于我的设备怎么办？',
                    answer: 'A:请联系IT部门进行核实。'
                },
                {
                    question: 'Q:电脑申请表单中的"使用人"的定义是什么？',
                    answer: 'A:使用人指的是实际上需要申请/更换电脑的人，给自己申请时使用人填自己的名字（英文，如xia jun）或者NT账号，给他人申请时使用人填他的名字（英文，如xia jun）或者NT账号，并在返回的下拉列表中进行用户选择。'
                },
                {
                    question: 'Q:如果我有多台电脑，我要更换的只是其中一台怎么办？',
                    answer: 'A:我的电脑一栏的最右侧有电脑选择下拉表单，点击选择自己需要更换的电脑即可。'
                },
                {
                    question: 'Q:电脑类型的几个选项是代表什么？',
                    answer: 'A:Standard Notebook指的是标准笔记本（轻薄，标准性能，适用于办公用户），Performance Notebook指的是性能笔记本（较重，高性能，适用于研发及设计工程师），Standard Desktop指的是标准台式机（小型，标准性能，适用于产线班长及办公用户），Performance Desktop指的是性能台式机（较重，高性能，适用于研发及设计工程师）。'
                },
                {
                    question: 'Q:为什么我填写不了申请理由？',
                    answer: 'A:申请理由仅有在申请类别选择"其他用途电脑申请"的情况下才需要填写。'
                }
            ],
            approval: [
                {
                    question: 'Q:提交审批后多久能够完成审批？',
                    answer: 'A:一般情况下，审批流程会通过智能审批进行一键识别审批并进入等待分配状态，只有在申请类别选择"办公电脑未超六年换新"与"其他用途电脑申请"时才会进入人工审批，具体时效取决于审批人的处理速度。'
                },
                {
                    question: 'Q:如何查看申请的审批进度？',
                    answer: 'A:在申请记录中点击"查看详情"或点击状态栏中的状态条均可以看到详细的审批进度和状态，以及审批理由。'
                },
                {
                    question: 'Q:我是多个成本中心的审批人，怎么审批对应成本中心的申请单？',
                    answer: 'A:申请界面的最右侧有一个选择审批身份的下拉框，点击选择即可。'
                }
            ],
            interface: [
                {
                    question: 'Q:我想使用英文界面，怎么进行切换？',
                    answer: 'A:用户名左侧有一个下拉框可以进行语言配置的切换。'
                }
            ]
        }
    } else {
        return {
            login: [
                {
                    question: 'Q: AD domain connection error during login',
                    answer: 'A: Please check your network connection and confirm your password is correct. If you are certain the password is correct, click the "Get Temporary Password" button. You will receive an email with a temporary password. Please enter this temporary password and click login (do not share this temporary password with others to prevent password leakage). If the problem persists, please contact the IT department.'
                }
            ],
            application: [
                {
                    question: 'Q: What are the requirements for applying for a new computer?',
                    answer: 'A: Requirements depend on the application category:\n1. "Office Computer Replacement (Over 6 Years)": The computer to be replaced must be over 6 years old from manufacture date. Cost center will be automatically set to IT department.\n2. "Office Computer Replacement (Under 6 Years - New)": The computer to be replaced must be under 6 years old from manufacture date. Cost center will be automatically set to the user\'s cost center.\n3. "Office Computer Replacement (Under 6 Years - Used)": The computer to be replaced must be under 6 years old from manufacture date. Cost center will be automatically set to IT department.\n4. "Secretary Application for New Employee Computer": Cost center will be automatically set to IT department.\n5. "Secretary Application for Replacement Employee Computer": Cost center will be automatically set to IT department.\n6. "Secretary Application for New Intern/External Computer": Cost center will be automatically set to IT department.\n7. "Other Purpose Computer Application": You need to select the cost center manually.'
                },
                {
                    question: 'Q: My application was rejected, how do I reapply?',
                    answer: 'A: Review the rejection reason and make corrections based on the feedback before reapplying.'
                },
                {
                    question: 'Q: My computer information is incorrect or there are devices under my name that don\'t belong to me, what should I do?',
                    answer: 'A: Please contact the IT department for verification.'
                },
                {
                    question: 'Q: What is the definition of "User" in the computer application form?',
                    answer: 'A: "User" refers to the person who actually needs to apply for/replace the computer. When applying for yourself, enter your name (in English, e.g., xia jun) or NT account. When applying for others, enter their name (in English, e.g., xia jun) or NT account, and select from the returned dropdown list.'
                },
                {
                    question: 'Q: If I have multiple computers and only want to replace one, what should I do?',
                    answer: 'A: There is a computer selection dropdown on the far right of the "My Computer" section. Click to select the computer you want to replace.'
                },
                {
                    question: 'Q: What do the computer type options represent?',
                    answer: 'A: Standard Notebook refers to standard laptops (lightweight, standard performance, suitable for office users), Performance Notebook refers to performance laptops (heavier, high performance, suitable for R&D and design engineers), Standard Desktop refers to standard desktops (compact, standard performance, suitable for production line supervisors and office users), Performance Desktop refers to performance desktops (heavier, high performance, suitable for R&D and design engineers).'
                },
                {
                    question: 'Q: Why can\'t I fill in the application reason?',
                    answer: 'A: Application reason only needs to be filled when the application category is "Other Purpose Computer Application".'
                }
            ],
            approval: [
                {
                    question: 'Q: How long does it take to complete approval after submission?',
                    answer: 'A: Generally, the approval process will use intelligent approval for one-click recognition and enter the waiting allocation status. Manual approval is only required when the application category is "Office Computer Replacement (Under 6 Years - New)" or "Other Purpose Computer Application". The specific timeframe depends on the approver\'s processing speed.'
                },
                {
                    question: 'Q: How can I check the approval progress of my application?',
                    answer: 'A: Click "View Details" in the application records or click the status bar to see detailed approval progress, status, and approval reasons.'
                },
                {
                    question: 'Q: I am an approver for multiple cost centers, how do I approve applications for the corresponding cost center?',
                    answer: 'A: There is a dropdown for selecting approval identity on the far right of the application interface. Click to select.'
                }
            ],
            interface: [
                {
                    question: 'Q: I want to use the English interface, how do I switch?',
                    answer: 'A: There is a dropdown box to the left of the username for language configuration switching.'
                }
            ]
        }
    }
})

// 过滤后的QA数据
const filteredQaData = computed(() => {
    if (!searchKeyword.value.trim()) {
        return qaData.value
    }
    
    const keyword = searchKeyword.value.toLowerCase()
    const filtered = {}
    
    for (const [section, items] of Object.entries(qaData.value)) {
        const filteredItems = items.filter(item => 
            item.question.toLowerCase().includes(keyword) ||
            item.answer.toLowerCase().includes(keyword)
        )
        if (filteredItems.length > 0) {
            filtered[section] = filteredItems
        }
    }
    
    return filtered
})

// 切换语言
const handleLanguageChange = (command) => {
    languageStore.setLanguage(command)
}

// 关闭弹窗
const emit = defineEmits(['close'])
const handleClose = () => {
    emit('close')
}
</script>

<template>
    <div class="qa-overlay" @click.self="handleClose">
        <div class="qa-container">
            <!-- 头部 -->
            <div class="qa-header">
                <h2 class="qa-title">{{ langText.title }}</h2>
                <div class="qa-controls">
                    <!-- 语言切换 -->
                    <el-dropdown @command="handleLanguageChange" class="lang-dropdown">
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
                    <!-- 关闭按钮 -->
                    <el-button @click="handleClose" type="primary" plain class="close-btn">
                        <el-icon><Close /></el-icon>
                        {{ langText.close }}
                    </el-button>
                </div>
            </div>

            <!-- 搜索框 -->
            <div class="qa-search">
                <el-input
                    v-model="searchKeyword"
                    :placeholder="langText.searchPlaceholder"
                    prefix-icon="Search"
                    clearable
                    size="large"
                />
            </div>

            <!-- 内容区域 -->
            <div class="qa-content">
                <!-- 登录相关 -->
                <div v-if="filteredQaData.login" class="qa-section">
                    <h3 class="section-title">{{ langText.loginSection }}</h3>
                    <div class="qa-item" v-for="(item, index) in filteredQaData.login" :key="`login-${index}`">
                        <div class="qa-question">{{ item.question }}</div>
                        <div class="qa-answer" v-html="item.answer.replace(/\n/g, '<br>')"></div>
                    </div>
                </div>

                <!-- 申请相关 -->
                <div v-if="filteredQaData.application" class="qa-section">
                    <h3 class="section-title">{{ langText.applicationSection }}</h3>
                    <div class="qa-item" v-for="(item, index) in filteredQaData.application" :key="`application-${index}`">
                        <div class="qa-question">{{ item.question }}</div>
                        <div class="qa-answer" v-html="item.answer.replace(/\n/g, '<br>')"></div>
                    </div>
                </div>

                <!-- 审批相关 -->
                <div v-if="filteredQaData.approval" class="qa-section">
                    <h3 class="section-title">{{ langText.approvalSection }}</h3>
                    <div class="qa-item" v-for="(item, index) in filteredQaData.approval" :key="`approval-${index}`">
                        <div class="qa-question">{{ item.question }}</div>
                        <div class="qa-answer" v-html="item.answer.replace(/\n/g, '<br>')"></div>
                    </div>
                </div>

                <!-- 界面相关 -->
                <div v-if="filteredQaData.interface" class="qa-section">
                    <h3 class="section-title">{{ langText.interfaceSection }}</h3>
                    <div class="qa-item" v-for="(item, index) in filteredQaData.interface" :key="`interface-${index}`">
                        <div class="qa-question">{{ item.question }}</div>
                        <div class="qa-answer" v-html="item.answer.replace(/\n/g, '<br>')"></div>
                    </div>
                </div>

                <!-- 无搜索结果 -->
                <div v-if="Object.keys(filteredQaData).length === 0" class="no-results">
                    <el-empty :description="currentLang === 'zh' ? '没有找到相关问题' : 'No related questions found'" />
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.qa-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 2000;
    backdrop-filter: blur(3px);
}

.qa-container {
    width: 90%;
    max-width: 900px;
    max-height: 90vh;
    background: white;
    border-radius: 12px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.qa-header {
    padding: 20px 30px;
    border-bottom: 1px solid #e4e7ed;
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: linear-gradient(135deg, #005389, #029165);
    color: white;
}

.qa-title {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
    color: white;
}

.qa-controls {
    display: flex;
    align-items: center;
    gap: 15px;
}

.lang-dropdown .lang-dropdown-link {
    display: flex;
    align-items: center;
    cursor: pointer;
    padding: 8px 12px;
    border-radius: 6px;
    color: white;
    font-weight: 500;
    background: rgba(255, 255, 255, 0.1);
    transition: all 0.3s ease;
}

.lang-dropdown .lang-dropdown-link:hover {
    background: rgba(255, 255, 255, 0.2);
}

:deep(.active-lang) {
    color: #005389;
    font-weight: bold;
    background-color: rgba(0, 83, 137, 0.1);
}

.close-btn {
    background: rgba(255, 255, 255, 0.1) !important;
    border-color: rgba(255, 255, 255, 0.3) !important;
    color: white !important;
}

.close-btn:hover {
    background: rgba(255, 255, 255, 0.2) !important;
    border-color: rgba(255, 255, 255, 0.5) !important;
}

.qa-search {
    padding: 20px 30px;
    background: #f8f9fa;
}

.qa-content {
    flex: 1;
    padding: 20px 30px;
    overflow-y: auto;
}

.qa-section {
    margin-bottom: 30px;
}

.section-title {
    font-size: 18px;
    font-weight: 600;
    color: #005389;
    margin: 0 0 15px 0;
    padding-bottom: 8px;
    border-bottom: 2px solid #005389;
}

.qa-item {
    margin-bottom: 20px;
    padding: 15px;
    background: #f8f9fa;
    border-radius: 8px;
    border-left: 4px solid #029165;
}

.qa-question {
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
    font-size: 16px;
}

.qa-answer {
    color: #606266;
    line-height: 1.6;
    font-size: 14px;
}

.no-results {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 200px;
}

/* 滚动条样式 */
.qa-content::-webkit-scrollbar {
    width: 6px;
}

.qa-content::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
}

.qa-content::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
}

.qa-content::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .qa-container {
        width: 95%;
        max-height: 95vh;
    }
    
    .qa-header {
        padding: 15px 20px;
        flex-direction: column;
        gap: 10px;
    }
    
    .qa-title {
        font-size: 20px;
    }
    
    .qa-controls {
        width: 100%;
        justify-content: space-between;
    }
    
    .qa-search {
        padding: 15px 20px;
    }
    
    .qa-content {
        padding: 15px 20px;
    }
}
</style> 