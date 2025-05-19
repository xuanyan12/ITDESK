<script setup>
import { onMounted, ref, watch, nextTick } from "vue";
import httpUtil from "@/utils/HttpUtil";
import { Warning, Download, UploadFilled, InfoFilled, Check } from "@element-plus/icons-vue";
import { ElTooltip, ElMessage } from 'element-plus';
import { saveAs } from 'file-saver';
import * as XLSX from 'xlsx';



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
    wbsNum: '',
    price: '',
    temp: '',
    pageNum: 1,
    pageSize: 10
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
    wbsNum: '',
    price: '',
    temp: ''
})

const deletePartForm = ref([])

const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const partList = ref([])
const editPartDialogVisible = ref(false);
const deletePartDialogVisible = ref(false);
const editPartFormRef = ref(null)
const deletePartIng = ref(false)
const deletePartTips = ref('')

// 添加文件上传相关变量
const uploadFileDialogVisible = ref(false);
const uploadLoading = ref(false);
const uploadFile = ref(null);

// 添加导出对话框相关的状态变量
const exportDialogVisible = ref(false);
const exportOption = ref('current'); // 'current'表示导出当前页，'all'表示导出所有数据
const exportLoading = ref(false);

// 追溯对话框相关变量
const traceDialogVisible = ref(false);
const traceLoading = ref(false);
const traceRecordList = ref([]);
const traceRecordTotal = ref(0);
const currentComputer = ref({});

// 追溯查询表单
const traceQueryForm = ref({
    ciName: '',
    pageNum: 1,
    pageSize: 10
});

// 归还电脑相关变量
const returnComputerDialogVisible = ref(false);
const currentReturnComputer = ref(null);
const returnComputerLoading = ref(false);
const selectedReturnStatus = ref(''); // Add selected status variable

// 添加用户搜索相关的变量
const userSearchResults = ref([]); // 用户查询结果
const showUserResults = ref(false); // 是否显示用户查询结果
const searchTimeout = ref(null); // 搜索防抖定时器

// 定义输入框引用
const userInputRef = ref(null);

onMounted(() => {
    // 查询配件列表
    selectPartListData()

    // 添加全局点击事件监听，用于关闭用户搜索下拉框
    document.addEventListener('click', handleDocumentClick);
})

// 组件卸载前移除全局事件监听
const handleDocumentClick = (event) => {
    // 检查点击是否在用户输入框或下拉框之外
    const userInputContainer = document.querySelector('.user-input-container');
    if (userInputContainer && !userInputContainer.contains(event.target)) {
        showUserResults.value = false;
    }
};

// 在组件销毁前移除事件监听
const onBeforeUnmount = () => {
    document.removeEventListener('click', handleDocumentClick);
};

// 用户输入事件处理
const handleUserInput = (value) => {
    // 清除之前的定时器
    if (searchTimeout.value) {
        clearTimeout(searchTimeout.value);
    }
    
    // 设置新的定时器，用户停止输入500ms后执行搜索
    searchTimeout.value = setTimeout(() => {
        if (value && value.trim() !== '') {
            searchUser();
        } else {
            showUserResults.value = false;
        }
    }, 500);
};

// 处理用户输入框失焦
const handleUserBlur = () => {
    // 延迟关闭下拉框，以便用户能够点击下拉框中的选项
    setTimeout(() => {
        showUserResults.value = false;
    }, 300); // 增加延迟时间，确保能点击选项
};

// 确认用户变更
const confirmUserChange = () => {
    // 如果输入框为空，则清空NT账号
    if (!queryForm.value.lastName || queryForm.value.lastName.trim() === '') {
        queryForm.value.ntAccount = '';
    }
    selectPartListData();
};

// 搜索用户 - 实现模糊搜索支持姓名查询
const searchUser = () => {
    const searchTerm = queryForm.value.lastName;
    if (!searchTerm || searchTerm.trim() === '') {
        showUserResults.value = false;
        return;
    }
    
    httpUtil.get("/sysApply/searchUsers", {
        params: { query: searchTerm }
    }).then(res => {
        if (res.data && Array.isArray(res.data.list) && res.data.list.length > 0) {
            // 将查询结果添加到结果列表
            userSearchResults.value = res.data.list;
            
            // 确保在更新DOM之前显示结果
            nextTick(() => {
                showUserResults.value = true;
            });
        } else {
            userSearchResults.value = [];
            nextTick(() => {
                showUserResults.value = true;
            });
        }
    }).catch(err => {
        console.error("搜索用户失败:", err);
        userSearchResults.value = [];
        nextTick(() => {
            showUserResults.value = true;
        });
    });
};

// 选择用户
const selectUser = (user) => {
    // 设置NT账号和员工姓名
    queryForm.value.ntAccount = user.userName;
    queryForm.value.lastName = user.userNick || user.userName;
    showUserResults.value = false;
    
    // 注释掉自动查询功能
    // selectPartListData();
};

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

// Add computed properties for date formatting and years calculation
const formatDate = (dateString) => {
    if (!dateString) return '';
    // Handle ISO date format like 2013-08-28T00:00 as well as other formats
    if (dateString.includes('T')) {
        return dateString.split('T')[0];
    }
    // Handle other date formats that might use space as separator
    return dateString.split(' ')[0];
}

const calculateYearsToToday = (dateString) => {
    if (!dateString) return '';
    const manufactureDate = new Date(dateString);
    const today = new Date();
    const diffTime = Math.abs(today - manufactureDate);
    const diffYears = (diffTime / (1000 * 60 * 60 * 24 * 365.25)).toFixed(1);
    return diffYears;
}

/**
 * 打开文件上传对话框
 */
const openUploadFileDialog = () => {
    uploadFileDialogVisible.value = true;
    uploadFile.value = null;
}

/**
 * 文件上传改变事件
 * @param file 上传的文件
 */
const handleFileChange = (file) => {
    uploadFile.value = file.raw;
}

/**
 * 上传文件并更新电脑信息
 */
const importComputerByExcel = () => {
    if (!uploadFile.value) {
        ElMessage.warning('请选择Excel文件');
        return;
    }
    
    uploadLoading.value = true;
    
    const formData = new FormData();
    formData.append('file', uploadFile.value);
    
    httpUtil.post("/sysControl/importComputerByExcel", formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        timeout: 300000 // 增加超时时间到5分钟
    }).then(res => {
        ElMessage.success(res.msg || '电脑信息更新成功');
        // 刷新电脑列表
        selectPartListData();
    }).catch(err => {
        console.error(err);
        ElMessage.error(err.response?.data?.msg || '电脑信息更新失败');
    }).finally(() => {
        uploadLoading.value = false;
        uploadFileDialogVisible.value = false;
        uploadFile.value = null;
    });
}

/**
 * 下载模板文件
 */
const downloadTemplate = () => {
    // 创建一个a标签，用于下载文件
    const link = document.createElement('a');
    link.href = '/template.xlsx'; // 模板文件路径
    link.download = 'template.xlsx';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

/**
 * 打开导出选项对话框
 */
const openExportDialog = () => {
  exportOption.value = 'current'; // 默认选择当前页
  exportDialogVisible.value = true;
};

/**
 * 导出表格数据到Excel
 */
const exportTableData = async () => {
  // 关闭对话框
  exportDialogVisible.value = false;
  
  // 显示加载中提示
  exportLoading.value = true;
  loading.value = true;
  ElMessage.info('正在准备导出数据，请稍候...');
  
  let dataToExport = [];
  
  // 根据用户选择获取要导出的数据
  if (exportOption.value === 'current') {
    // 导出当前页数据
    dataToExport = formatDataForExport(partList.value);
  } else if (exportOption.value === 'all') {
    // 导出所有数据（需要发送请求获取所有数据）
    try {
      // 克隆当前查询条件，但移除分页限制
      const exportQueryParams = { ...queryForm.value };
      exportQueryParams.pageSize = 10000; // 设置一个较大的值以获取所有数据
      exportQueryParams.pageNum = 1;
      
      const response = await httpUtil.post("/sysControl/selectSysControlList", exportQueryParams, {
        headers: {
          'Content-Type': 'application/json'
        }
      });
      
      if (response.data && response.data.sysControlModelLists) {
        dataToExport = formatDataForExport(response.data.sysControlModelLists);
        ElMessage.success(`成功获取${dataToExport.length}条数据，正在导出...`);
      } else {
        ElMessage.warning('未获取到数据');
        exportLoading.value = false;
        loading.value = false;
        return;
      }
    } catch (error) {
      console.error('获取所有数据失败:', error);
      ElMessage.error('获取所有数据失败，请重试');
      exportLoading.value = false;
      loading.value = false;
      return;
    }
  }
  
  // 创建一个工作簿对象
  const wb = XLSX.utils.book_new();
  
  // 将数据转换为工作表
  const ws = XLSX.utils.json_to_sheet(dataToExport);
  
  // 设置列宽
  const wscols = [
    { wch: 15 }, // 电脑状态
    { wch: 15 }, // 电脑名
    { wch: 15 }, // 设备类型
    { wch: 20 }, // 电脑序列号
    { wch: 15 }, // 制造商
    { wch: 20 }, // 电脑型号
    { wch: 15 }, // NT账号
    { wch: 20 }, // 电脑归属情况
    { wch: 30 }, // 备注
    { wch: 10 }, // 姓
    { wch: 10 }, // 名
    { wch: 25 }, // 邮箱地址
    { wch: 15 }, // 电话号码
    { wch: 20 }, // 所属部门
    { wch: 15 }, // 成本中心
    { wch: 15 }, // 出厂时间
    { wch: 10 }, // 使用年限
    { wch: 20 }, // CPU
    { wch: 10 }, // 内存
    { wch: 15 }, // 硬盘
    { wch: 15 }, // 显卡
    { wch: 15 }, // 硬件状态
    { wch: 15 }, // 下单号
    { wch: 15 }, // 订单号
    { wch: 20 }, // 供应商公司
    { wch: 15 }, // 公司
    { wch: 15 }, // WBS号
    { wch: 10 }, // 临时分配
    { wch: 10 }  // 价格
  ];
  ws['!cols'] = wscols;
  
  // 将工作表添加到工作簿
  XLSX.utils.book_append_sheet(wb, ws, '电脑信息');
  
  // 生成Excel文件并下载
  // 获取当前日期作为文件名的一部分
  const date = new Date();
  const dateStr = date.getFullYear() + 
                 ('0' + (date.getMonth() + 1)).slice(-2) + 
                 ('0' + date.getDate()).slice(-2);
  const timeStr = ('0' + date.getHours()).slice(-2) + 
                 ('0' + date.getMinutes()).slice(-2);
  const fileName = `电脑信息_${dateStr}_${timeStr}.xlsx`;
  
  // 导出文件
  const wbout = XLSX.write(wb, { bookType: 'xlsx', type: 'binary' });
  
  // 将二进制数据转换为Blob对象
  function s2ab(s) {
    const buf = new ArrayBuffer(s.length);
    const view = new Uint8Array(buf);
    for (let i = 0; i < s.length; i++) {
      view[i] = s.charCodeAt(i) & 0xFF;
    }
    return buf;
  }
  
  // 使用file-saver保存文件
  saveAs(new Blob([s2ab(wbout)], { type: 'application/octet-stream' }), fileName);
  
  // 隐藏加载提示
  exportLoading.value = false;
  loading.value = false;
  
  // 显示成功消息
  ElMessage.success(`已成功导出${dataToExport.length}条数据`);
};

/**
 * 格式化数据用于导出
 */
const formatDataForExport = (data) => {
  return data.map(item => {
    // 创建一个新对象，只包含我们需要导出的字段
    return {
      '电脑状态': item.pcStatus || '',
      '电脑名': item.ciName || '',
      '设备类型': item.deviceClass || '',
      '电脑序列号': item.serialNumber || '',
      '制造商': item.manufacture || '',
      '电脑型号': item.modelOrVersion || '',
      'NT账号': item.ntAccount || '',
      '电脑归属情况': item.pcClass || '',
      '备注': item.comment || '',
      '姓': item.lastName || '',
      '名': item.firstName || '',
      '邮箱地址': item.emailAddress || '',
      '电话号码': item.telephone || '',
      '所属部门': item.department || '',
      '成本中心': item.costCenter || '',
      '出厂时间': formatDate(item.lifeCycleStart) || '',
      '使用年限': calculateYearsToToday(item.lifeCycleStart) + ' 年',
      'CPU': item.cpu || '',
      '内存': item.memory || '',
      '硬盘': item.disk || '',
      '显卡': item.graphic || '',
      '硬件状态': item.hardwareStatus || '',
      '下单号': item.pr || '',
      '订单号': item.po || '',
      '供应商公司': item.vendor || '',
      '公司': item.company || '',
      'WBS号': item.wbsNum || '',
      '临时分配': item.temp === 1 ? '是' : '否',
      '价格': item.price || ''
    };
  });
};

/**
 * 显示追溯对话框
 * @param row 当前行数据
 */
const showTraceDialog = (row) => {
    currentComputer.value = { ...row };
    console.log('当前电脑信息:', currentComputer.value);
    
    // 重置查询表单
    traceQueryForm.value = {
        ciName: row.ciName,
        pageNum: 1,
        pageSize: 10
    };
    
    console.log('查询电脑历史记录，参数:', traceQueryForm.value);
    traceDialogVisible.value = true;
    
    // 打开对话框后立即获取数据
    fetchTraceRecords();
};

/**
 * 获取追溯记录
 */
const fetchTraceRecords = () => {
    traceLoading.value = true;
    traceRecordList.value = [];
    
    httpUtil.post("/sysControl/getControlRecordList", { ...traceQueryForm.value }, {
        headers: {
            'Content-Type': 'application/json'
        }
    }).then(res => {
        console.log('获取追溯记录响应:', res);
        // 修改这里，后端返回的成功码是200
        if (res.code === 200) {
            // 确保正确解析后端返回的数据结构
            if (res.data && Array.isArray(res.data.list)) {
                traceRecordList.value = res.data.list;
                traceRecordTotal.value = res.data.total || 0;
                console.log('解析后的追溯记录列表:', traceRecordList.value);
                
                if(traceRecordList.value.length === 0) {
                    ElMessage.info('未找到该电脑的历史记录');
                }
            } else {
                console.error('返回的数据结构不符合预期:', res.data);
                ElMessage.warning('返回的数据结构不符合预期');
            }
        } else {
            ElMessage.error(res.msg || '获取追溯记录失败');
        }
    }).catch(err => {
        console.error('获取追溯记录失败:', err);
        ElMessage.error('获取追溯记录失败: ' + (err.message || '未知错误'));
    }).finally(() => {
        traceLoading.value = false;
    });
};

/**
 * 处理追溯分页大小变化
 * @param val 每页条数
 */
const handleTracePageSizeChange = (val) => {
    traceQueryForm.value.pageSize = val;
    // 改变页大小时重置为第一页
    traceQueryForm.value.pageNum = 1;
    fetchTraceRecords();
};

/**
 * 处理追溯页码变化
 * @param val 当前页码
 */
const handleTracePageChange = (val) => {
    traceQueryForm.value.pageNum = val;
    fetchTraceRecords();
};

/**
 * 获取状态标签类型
 * @param status 状态值
 * @returns 标签类型
 */
const getStatusTagType = (status) => {
    if (!status) return '';
    
    const statusLower = String(status).toLowerCase();
    
    // 进行中的状态使用蓝色
    if (statusLower.includes('审批中') || 
        statusLower.includes('分配中') || 
        statusLower === 'to be assigned') {
        return 'primary';
    } 
    // 临时状态使用黄色
    else if (statusLower.includes('暂分配') || 
             statusLower === 'waiting for return') {
        return 'warning';
    }
    // 完成状态使用绿色
    else if (statusLower.includes('分配完成') || 
             statusLower.includes('审批通过') || 
             statusLower === 'in use') {
        return 'success';
    } 
    // 拒绝状态使用红色
    else if (statusLower.includes('拒绝') || 
             statusLower === 'scrapped' || 
             statusLower === 'to be scrapped') {
        return 'danger';
    }
    
    return 'info';
};

/**
 * 获取记录详细信息
 * @param record 记录对象
 * @returns 格式化后的详情对象
 */
const getRecordDetails = (record) => {
    const details = {};
    
    // 添加关键字段到详情中
    if (record.lifeCycleStart) details['出厂时间'] = record.lifeCycleStart;
    if (record.modelOrVersion) details['电脑型号'] = record.modelOrVersion;
    if (record.serialNumber) details['序列号'] = record.serialNumber;
    if (record.lastName || record.firstName) {
        details['用户'] = `${record.lastName || ''} ${record.firstName || ''}`.trim();
    }
    if (record.department) details['部门'] = record.department;
    if (record.costCenter) details['成本中心'] = record.costCenter;
    if (record.emailAddress) details['邮箱'] = record.emailAddress;
    if (record.telephone) details['电话'] = record.telephone;
    if (record.comment) details['备注'] = record.comment;
    
    return details;
};

// 新增：获取当前电脑属性
const getCurrentDetails = () => {
    const details = {};
    
    // 添加关键字段到详情中
    if (currentComputer.value.pcStatus) details['电脑状态'] = currentComputer.value.pcStatus;
    if (currentComputer.value.ntAccount) details['NT账号'] = currentComputer.value.ntAccount;
    if (currentComputer.value.pcClass) details['电脑归属情况'] = currentComputer.value.pcClass;
    if (currentComputer.value.company) details['公司'] = currentComputer.value.company;
    
    // 添加其他详细信息
    if (currentComputer.value.lifeCycleStart) details['出厂时间'] = currentComputer.value.lifeCycleStart;
    if (currentComputer.value.modelOrVersion) details['电脑型号'] = currentComputer.value.modelOrVersion;
    if (currentComputer.value.serialNumber) details['序列号'] = currentComputer.value.serialNumber;
    if (currentComputer.value.lastName || currentComputer.value.firstName) {
        details['用户'] = `${currentComputer.value.lastName || ''} ${currentComputer.value.firstName || ''}`.trim();
    }
    if (currentComputer.value.department) details['部门'] = currentComputer.value.department;
    if (currentComputer.value.costCenter) details['成本中心'] = currentComputer.value.costCenter;
    if (currentComputer.value.emailAddress) details['邮箱'] = currentComputer.value.emailAddress;
    if (currentComputer.value.telephone) details['电话'] = currentComputer.value.telephone;
    if (currentComputer.value.comment) details['备注'] = currentComputer.value.comment;
    
    return details;
};

/**
 * 显示归还电脑对话框
 * @param row 当前行数据
 */
const returnComputerDialog = (row) => {
    currentReturnComputer.value = { ...row };
    // 根据使用年限自动选择初始状态
    const years = calculateYearsToToday(row.lifeCycleStart);
    if (parseFloat(years) > 8) {
        selectedReturnStatus.value = 'To be scrapped';
    } else {
        selectedReturnStatus.value = 'To be assigned';
    }
    returnComputerDialogVisible.value = true;
};

/**
 * 确认归还电脑
 */
const confirmReturnComputer = () => {
    if (!currentReturnComputer.value) {
        ElMessage.error('电脑数据不完整，无法归还');
        return;
    }
    
    returnComputerLoading.value = true;
    
    // 使用用户选择的状态，但不添加到对象中
    const computerData = { ...currentReturnComputer.value };
    
    // 调用专门的归还接口，通过param传递returnStatus参数
    httpUtil.post("/sysControl/returnComputer", computerData, {
        headers: {
            'Content-Type': 'application/json'
        },
        params: {
            returnStatus: selectedReturnStatus.value // 通过URL参数传递用户选择的状态
        }
    }).then(res => {
        ElMessage.success('电脑归还成功');
        // 刷新电脑列表
        selectPartListData();
    }).catch(err => {
        console.error('电脑归还失败:', err);
        ElMessage.error('电脑归还失败: ' + (err.message || '未知错误'));
    }).finally(() => {
        returnComputerLoading.value = false;
        returnComputerDialogVisible.value = false;
    });
};

// 获取下拉框位置
const getUserDropdownPosition = () => {
    if (!userInputRef.value) return {};
    
    // 获取输入框元素位置
    const inputEl = userInputRef.value.$el;
    if (!inputEl) return {};
    
    const rect = inputEl.getBoundingClientRect();
    
    return {
        position: 'fixed',
        top: `${rect.bottom + window.scrollY}px`,
        left: `${rect.left + window.scrollX}px`,
        width: `${Math.max(rect.width * 2, 350)}px`, // 增加宽度为输入框的2倍，最小350px
        maxHeight: '400px'
    };
};

</script>


<template>
    <div style="padding:12px">
        <!-- 电脑查询 -->
        <el-card shadow="never" class="usr_card_override top">
            <el-form :model="queryForm" inline class="query-form">
                <el-form-item label="电脑名称" class="form-item">
                    <el-input v-model="queryForm.ciName" placeholder="请输入电脑名称" class="input-field" @keyup.enter="selectPartListData"></el-input>
                </el-form-item>
                <el-form-item label="员工姓名" class="form-item">
                    <div class="user-input-container">
                        <el-input 
                            ref="userInputRef"
                            v-model="queryForm.lastName" 
                            placeholder="请输入员工姓名" 
                            class="input-field"
                            clearable
                            @input="handleUserInput"
                            @blur="handleUserBlur"
                            @keyup.enter="selectPartListData">
                        </el-input>
                        <!-- 用户查询结果下拉框，使用teleport传送到body下 -->
                        <teleport to="body">
                            <div v-if="showUserResults" class="user-results-dropdown" :style="getUserDropdownPosition()">
                                <div v-if="userSearchResults.length > 0">
                                    <div 
                                        v-for="(user, index) in userSearchResults" 
                                        :key="index" 
                                        class="user-result-item"
                                        @click="selectUser(user)">
                                        {{ user.userName }} {{ user.userNick ? `(${user.userNick})` : '' }}
                                    </div>
                                </div>
                                <div v-else class="no-results">
                                    未找到相关用户
                                </div>
                            </div>
                        </teleport>
                    </div>
                </el-form-item>
                <el-form-item label="电脑归属" class="form-item">
                    <el-select v-model="queryForm.pcClass" placeholder="请选择电脑归属" class="input-field">
                        <el-option label="全部" value=""></el-option>
                        <el-option label="External User" value="External User"></el-option>
                        <el-option label="Fixed-Term User" value="Fixed-Term User"></el-option>
                        <el-option label="Internal User" value="Internal User"></el-option>
                        <el-option label="Non-Standard Use" value="Non-Standard Use"></el-option>
                        <el-option label="Public Use" value="Public Use(public/test/connect device)"></el-option>
                        <el-option label="ShareNotebook" value="ShareNotebook"></el-option>
                        <el-option label="To be scrapped" value="To be scrapped"></el-option>
                        <el-option label="To be assigned" value="To be assigned"></el-option>
                        <el-option label="Waiting for Return" value="Waiting for Return"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="所属部门" class="form-item">
                    <el-input v-model="queryForm.department" placeholder="请输入所属部门" class="input-field"></el-input>
                </el-form-item>
                <el-form-item label="电脑状态" class="form-item">
                    <el-select v-model="queryForm.pcStatus" placeholder="请选择电脑状态" class="input-field">
                        <el-option label="ALL STATUS" value=""></el-option>
                        <el-option label="In Use" value="In Use"></el-option>
                        <el-option label="Scrapped" value="Scrapped"></el-option>
                        <el-option label="ShareNoteBook" value="ShareNoteBook"></el-option>
                        <el-option label="To be assigned" value="To be assigned"></el-option>
                        <el-option label="To be scrapped" value="To be scrapped"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="出厂时间" class="form-item">
                    <el-date-picker
                        v-model="queryForm.lifeCycleStart"
                        type="date"
                        placeholder="选择出厂时间"
                        format="YYYY-MM-DD"
                        value-format="YYYY-MM-DD"
                        class="input-field"
                        :clearable="true"
                        @change="confirmUserChange">
                    </el-date-picker>
                </el-form-item>
                <el-form-item label="成本中心" class="form-item">
                    <el-input v-model="queryForm.costCenter" placeholder="请输入成本中心" class="input-field"></el-input>
                </el-form-item>
                <el-form-item class="form-item">
                    <el-button type="primary" @click="selectPartListData">查询</el-button>
                    <el-button type="primary" @click="openUploadFileDialog" class="update-btn">电脑更新</el-button>
                    <el-button type="primary" @click="openExportDialog" class="update-btn" :loading="exportLoading">
                        <el-icon><Download /></el-icon>
                        导出数据
                    </el-button>
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
                <el-table-column label="电脑型号" prop="modelOrVersion" :width="200">
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
                <el-table-column label="电脑归属情况" prop="pcClass" :width="200">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.pcClass" placement="top">
                            <div class="text-ellipsis">{{ row.pcClass }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 备注 -->
                <el-table-column label="备注" prop="comment" :width="200">
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
                        <el-tooltip class="item" effect="light" :content="formatDate(row.lifeCycleStart)" placement="top">
                            <div class="text-ellipsis">{{ formatDate(row.lifeCycleStart) }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>

                <!-- 出厂日期截至今天的时间 -->
                <el-table-column label="出厂日期截至今天的时间" prop="yrs_To_Day" :width="120">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="calculateYearsToToday(row.lifeCycleStart) + ' 年'" placement="top" open-delay=1000>
                            <div class="text-ellipsis">{{ calculateYearsToToday(row.lifeCycleStart) }} 年</div>
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
                <el-table-column label="wbs号" prop="wbsNum" :width="150">
                    <template #default="{ row }">
                        <el-tooltip class="item" effect="light" :content="row.wbsNum" placement="top">
                            <div class="text-ellipsis">{{ row.wbsNum }}</div>
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
                <el-table-column label="操作" :width="200" fixed="right">
                    <template #default="{ row }">
                        <el-button type="text" @click="editPartDialog(row)">编辑</el-button>
                        <el-button type="text" @click="deletePartDialog(row)">删除</el-button>
                        <el-button type="text" @click="showTraceDialog(row)">追溯</el-button>
                        <el-button 
                            v-if="row.pcStatus === 'In Use'" 
                            type="text" 
                            @click="returnComputerDialog(row)">归还</el-button>
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
            <el-dialog 
                v-model="editPartDialogVisible" 
                title="" 
                width="80%" 
                class="tech-edit-dialog"
                :close-on-click-modal="false"
                destroy-on-close>
                <div class="edit-dialog-container" v-loading="loading">
                    <div class="edit-header">
                        <div class="edit-title">
                            <span>编辑电脑信息</span>
                            <el-tag class="edit-tag" type="success">
                                {{ editPartForm.ciName || '未命名电脑' }}
                            </el-tag>
                        </div>
                        <div class="edit-subtitle">请修改需要更新的电脑信息</div>
                    </div>
                    
                    <div class="edit-content">
                        <el-form :model="editPartForm" ref="editPartFormRef" label-width="100px" class="tech-form">
                            <el-tabs type="border-card" class="tech-tabs">
                                <el-tab-pane label="基础信息">
                                    <el-row :gutter="20">
                                        <el-col :span="8">
                                            <el-form-item label="电脑名称" prop="ciName">
                                                <el-input v-model="editPartForm.ciName"></el-input>
                                            </el-form-item>
                                        </el-col>
                                        <el-col :span="8">
                                            <el-form-item label="电脑状态" prop="pcStatus" :rules="[{ required: true, message: '请选择状态', trigger: 'blur' }]">
                                                <el-select v-model="editPartForm.pcStatus" style="width: 100%">
                                                    <el-option label="TO BE ASSIGNED" value="TO BE ASSIGNED"></el-option>
                                                    <el-option label="IN USE" value="IN USE"></el-option>
                                                    <el-option label="ShareNoteBook" value="ShareNoteBook"></el-option>
                                                    <el-option label="Scrapped" value="Scrapped"></el-option>
                                                    <el-option label="To be scrapped" value="To be scrapped"></el-option>
                                                </el-select>
                                            </el-form-item>
                                        </el-col>
                                        <el-col :span="8">
                                            <el-form-item label="NT账号" prop="ntAccount" :rules="[{ required: true, message: '请输入NT账号', trigger: 'blur' }]">
                                                <el-input v-model="editPartForm.ntAccount"></el-input>
                                            </el-form-item>
                                        </el-col>
                                    </el-row>

                                    <el-row :gutter="20">
                                        <el-col :span="8">
                                            <el-form-item label="电脑归属" prop="pcClass" :rules="[{ required: true, message: '请输入电脑归属情况', trigger: 'blur' }]">
                                                <el-select v-model="editPartForm.pcClass" placeholder="请选择电脑归属" style="width: 100%">
                                                    <el-option label="External User" value="External User"></el-option>
                                                    <el-option label="Fixed-Term User" value="Fixed-Term User"></el-option>
                                                    <el-option label="Internal User" value="Internal User"></el-option>
                                                    <el-option label="Non-Standard Use" value="Non-Standard Use"></el-option>
                                                    <el-option label="Public Use" value="Public Use(public/test/connect device)"></el-option>
                                                    <el-option label="ShareNotebook" value="ShareNotebook"></el-option>
                                                    <el-option label="To be scrapped" value="To be scrapped"></el-option>
                                                    <el-option label="To be assigned" value="To be assigned"></el-option>
                                                    <el-option label="Waiting for Return" value="Waiting for Return"></el-option>
                                                </el-select>
                                            </el-form-item>
                                        </el-col>
                                        <el-col :span="8">
                                            <el-form-item label="设备类型" prop="deviceClass">
                                                <el-input v-model="editPartForm.deviceClass"></el-input>
                                            </el-form-item>
                                        </el-col>
                                        <el-col :span="8">
                                            <el-form-item label="电脑序列号" prop="serialNumber">
                                                <el-input v-model="editPartForm.serialNumber"></el-input>
                                            </el-form-item>
                                        </el-col>
                                    </el-row>

                                    <el-row :gutter="20">
                                        <el-col :span="8">
                                            <el-form-item label="制造商" prop="manufacture">
                                                <el-input v-model="editPartForm.manufacture"></el-input>
                                            </el-form-item>
                                        </el-col>
                                        <el-col :span="8">
                                            <el-form-item label="电脑型号" prop="modelOrVersion">
                                                <el-input v-model="editPartForm.modelOrVersion"></el-input>
                                            </el-form-item>
                                        </el-col>
                                        <el-col :span="8">
                                            <el-form-item label="出厂时间" prop="lifeCycleStart">
                                                <el-date-picker
                                                    v-model="editPartForm.lifeCycleStart"
                                                    type="date"
                                                    placeholder="选择出厂时间"
                                                    format="YYYY-MM-DD"
                                                    value-format="YYYY-MM-DD"
                                                    style="width: 100%"
                                                    :clearable="true">
                                                </el-date-picker>
                                            </el-form-item>
                                        </el-col>
                                    </el-row>
                                </el-tab-pane>
                                
                                <el-tab-pane label="用户信息">
                                    <el-row :gutter="20">
                                        <el-col :span="8">
                                            <el-form-item label="姓" prop="lastName">
                                                <el-input v-model="editPartForm.lastName"></el-input>
                                            </el-form-item>
                                        </el-col>
                                        <el-col :span="8">
                                            <el-form-item label="名" prop="firstName">
                                                <el-input v-model="editPartForm.firstName"></el-input>
                                            </el-form-item>
                                        </el-col>
                                        <el-col :span="8">
                                            <el-form-item label="邮箱地址" prop="emailAddress">
                                                <el-input v-model="editPartForm.emailAddress"></el-input>
                                            </el-form-item>
                                        </el-col>
                                    </el-row>

                                    <el-row :gutter="20">
                                        <el-col :span="8">
                                            <el-form-item label="电话号码" prop="telephone">
                                                <el-input v-model="editPartForm.telephone"></el-input>
                                            </el-form-item>
                                        </el-col>
                                        <el-col :span="8">
                                            <el-form-item label="所属部门" prop="department">
                                                <el-input v-model="editPartForm.department"></el-input>
                                            </el-form-item>
                                        </el-col>
                                        <el-col :span="8">
                                            <el-form-item label="成本中心" prop="costCenter">
                                                <el-input v-model="editPartForm.costCenter"></el-input>
                                            </el-form-item>
                                        </el-col>
                                    </el-row>

                                    <el-row :gutter="20">
                                        <el-col :span="24">
                                            <el-form-item label="备注" prop="comment">
                                                <el-input v-model="editPartForm.comment" type="textarea" :rows="2"></el-input>
                                            </el-form-item>
                                        </el-col>
                                    </el-row>
                                </el-tab-pane>
                                
                                <el-tab-pane label="硬件信息">
                                    <el-row :gutter="20">
                                        <el-col :span="8">
                                            <el-form-item label="CPU" prop="cpu">
                                                <el-input v-model="editPartForm.cpu"></el-input>
                                            </el-form-item>
                                        </el-col>
                                        <el-col :span="8">
                                            <el-form-item label="内存" prop="memory">
                                                <el-input v-model="editPartForm.memory"></el-input>
                                            </el-form-item>
                                        </el-col>
                                        <el-col :span="8">
                                            <el-form-item label="硬盘" prop="disk">
                                                <el-input v-model="editPartForm.disk"></el-input>
                                            </el-form-item>
                                        </el-col>
                                    </el-row>

                                    <el-row :gutter="20">
                                        <el-col :span="8">
                                            <el-form-item label="显卡" prop="graphic">
                                                <el-input v-model="editPartForm.graphic"></el-input>
                                            </el-form-item>
                                        </el-col>
                                        <el-col :span="8">
                                            <el-form-item label="硬件状态" prop="hardwareStatus">
                                                <el-input v-model="editPartForm.hardwareStatus"></el-input>
                                            </el-form-item>
                                        </el-col>
                                        <el-col :span="8">
                                            <el-form-item label="临时分配" prop="temp">
                                                <el-select v-model="editPartForm.temp" style="width: 100%">
                                                    <el-option label="否" :value="0"></el-option>
                                                    <el-option label="是" :value="1"></el-option>
                                                </el-select>
                                            </el-form-item>
                                        </el-col>
                                    </el-row>
                                </el-tab-pane>
                                
                                <el-tab-pane label="订单信息">
                                    <el-row :gutter="20">
                                        <el-col :span="8">
                                            <el-form-item label="下单号" prop="pr">
                                                <el-input v-model="editPartForm.pr"></el-input>
                                            </el-form-item>
                                        </el-col>
                                        <el-col :span="8">
                                            <el-form-item label="订单号" prop="po">
                                                <el-input v-model="editPartForm.po"></el-input>
                                            </el-form-item>
                                        </el-col>
                                        <el-col :span="8">
                                            <el-form-item label="供应商公司" prop="vendor">
                                                <el-input v-model="editPartForm.vendor"></el-input>
                                            </el-form-item>
                                        </el-col>
                                    </el-row>

                                    <el-row :gutter="20">
                                        <el-col :span="8">
                                            <el-form-item label="公司" prop="company">
                                                <el-input v-model="editPartForm.company"></el-input>
                                            </el-form-item>
                                        </el-col>
                                        <el-col :span="8">
                                            <el-form-item label="wbs号" prop="wbsNum">
                                                <el-input v-model="editPartForm.wbsNum"></el-input>
                                            </el-form-item>
                                        </el-col>
                                        <el-col :span="8">
                                            <el-form-item label="价格" prop="price">
                                                <el-input v-model="editPartForm.price"></el-input>
                                            </el-form-item>
                                        </el-col>
                                    </el-row>
                                </el-tab-pane>
                            </el-tabs>
                        </el-form>
                    </div>
                </div>
                <template #footer>
                    <div class="tech-dialog-footer">
                        <el-button @click="editPartDialogVisible = false" class="tech-cancel-btn">取 消</el-button>
                        <el-button type="primary" @click="editPart" class="tech-confirm-btn">
                            <span class="tech-btn-text">保存修改</span>
                            <span class="tech-btn-icon"><i class="el-icon-check"></i></span>
                        </el-button>
                    </div>
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

            <!-- 文件上传对话框 -->
            <el-dialog v-model="uploadFileDialogVisible" width="580px" :close-on-click-modal="false" :destroy-on-close="true" custom-class="rounded-dialog" :show-close="true" :title="null">
                <div class="upload-dialog-container" v-loading="uploadLoading">
                    <div class="upload-dialog-left">
                        <div class="upload-icon-container">
                            <div class="upload-icon">
                                <el-icon class="upload-icon-svg"><UploadFilled /></el-icon>
                            </div>
                        </div>
                        <h3 class="upload-title">批量更新电脑信息</h3>
                        <p class="upload-subtitle">请上传符合模板格式的Excel文件</p>
                        
                        <div class="upload-tips">
                            <el-icon><InfoFilled /></el-icon>
                            <span>首次使用请先下载模板，按格式填写</span>
                        </div>
                        
                        <el-button 
                            type="primary" 
                            class="template-download-btn" 
                            @click="downloadTemplate">
                            <el-icon><Download /></el-icon>
                            下载模板
                        </el-button>
                    </div>
                    
                    <div class="upload-dialog-divider"></div>
                    
                    <div class="upload-dialog-right">
                        <div class="upload-container">
                            <el-upload
                                class="upload-excel"
                                drag
                                action="#"
                                :auto-upload="false"
                                :show-file-list="true"
                                :limit="1"
                                accept=".xlsx, .xls"
                                :on-change="handleFileChange">
                                <el-icon class="el-icon--upload"><Upload /></el-icon>
                                <div class="el-upload__text">
                                    拖拽文件到此处或 <em>点击上传</em>
                                </div>
                                <template #tip>
                                    <div class="el-upload__tip">
                                        仅支持 .xlsx/.xls 格式的 Excel 文件
                                    </div>
                                </template>
                            </el-upload>
                        </div>
                    </div>
                </div>
                <template #footer>
                    <div class="dialog-footer">
                        <el-button @click="uploadFileDialogVisible = false" class="cancel-btn">取 消</el-button>
                        <el-button type="primary" @click="importComputerByExcel" :loading="uploadLoading" class="confirm-btn">
                            <el-icon v-if="!uploadLoading"><Check /></el-icon>
                            <span>{{ uploadLoading ? '处理中...' : '确认导入' }}</span>
                        </el-button>
                    </div>
                </template>
            </el-dialog>

            <!-- 修改导出选项对话框 -->
            <el-dialog v-model="exportDialogVisible" title="数据导出" width="480px" :close-on-click-modal="false" custom-class="tech-dialog" :show-close="true">
                <div class="tech-dialog-content">
                    <div class="tech-header">
                        <div class="tech-icon-container">
                            <el-icon class="tech-icon"><Download /></el-icon>
                        </div>
                        <div class="tech-title-container">
                            <h3 class="tech-title">导出电脑列表数据</h3>
                            <p class="tech-subtitle">请选择要导出的数据范围</p>
                        </div>
                    </div>
                    
                    <div class="tech-options">
                        <div class="tech-option" :class="{ 'tech-option-active': exportOption === 'current' }" @click="exportOption = 'current'">
                            <div class="tech-option-radio">
                                <div class="tech-option-radio-inner" v-if="exportOption === 'current'"></div>
                            </div>
                            <div class="tech-option-content">
                                <div class="tech-option-title">导出当前页数据</div>
                                <div class="tech-option-details">仅导出当前页面显示的 {{ partList.length }} 条记录</div>
                            </div>
                        </div>
                        
                        <div class="tech-option" :class="{ 'tech-option-active': exportOption === 'all' }" @click="exportOption = 'all'">
                            <div class="tech-option-radio">
                                <div class="tech-option-radio-inner" v-if="exportOption === 'all'"></div>
                            </div>
                            <div class="tech-option-content">
                                <div class="tech-option-title">导出全部符合条件的数据</div>
                                <div class="tech-option-details">导出符合当前筛选条件的全部 {{ total }} 条记录</div>
                            </div>
                        </div>
                        
                        <div class="tech-warning" v-if="exportOption === 'all' && total > 1000">
                            <el-icon><Warning /></el-icon>
                            <span>数据量较大，导出可能需要较长时间</span>
                        </div>
                    </div>
                </div>
                <template #footer>
                    <div class="tech-dialog-footer">
                        <el-button @click="exportDialogVisible = false" class="tech-cancel-btn">取消</el-button>
                        <el-button type="primary" @click="exportTableData" :loading="exportLoading" class="tech-confirm-btn">
                            <span class="tech-btn-text">确认导出</span>
                            <span class="tech-btn-icon"><i class="el-icon-right"></i></span>
                        </el-button>
                    </div>
                </template>
            </el-dialog>

            <!-- 追溯记录弹窗 -->
            <el-dialog 
                v-model="traceDialogVisible" 
                title="" 
                width="80%" 
                :close-on-click-modal="false"
                destroy-on-close>
                <div class="trace-dialog-container">
                    <div class="trace-header">
                        <div class="trace-title">
                            <span>电脑历史记录</span>
                            <el-tag class="trace-tag" type="info">
                                {{ currentComputer.ciName || '未命名电脑' }}
                            </el-tag>
                        </div>
                        <div class="trace-subtitle">按时间线追溯电脑历史状态变更</div>
                    </div>
                    
                    <!-- 新增：当前电脑属性 -->
                    <div class="current-properties">
                        <div class="property-header">
                            <span>当前属性</span>
                        </div>
                        <div class="property-content">
                            <div class="property-row">
                                <div class="property-item">
                                    <div class="property-label">电脑状态:</div>
                                    <div class="property-value">
                                        <el-tag :type="getStatusTagType(currentComputer.pcStatus)">
                                            {{ currentComputer.pcStatus || '未知' }}
                                        </el-tag>
                                    </div>
                                </div>
                                <div class="property-item">
                                    <div class="property-label">NT账号:</div>
                                    <div class="property-value">{{ currentComputer.ntAccount || '无' }}</div>
                                </div>
                            </div>
                            <div class="property-row">
                                <div class="property-item">
                                    <div class="property-label">电脑归属情况:</div>
                                    <div class="property-value">{{ currentComputer.pcClass || '无' }}</div>
                                </div>
                                <div class="property-item">
                                    <div class="property-label">公司:</div>
                                    <div class="property-value">{{ currentComputer.company || '无' }}</div>
                                </div>
                            </div>
                            <div class="property-row">
                                <div class="property-item">
                                    <div class="property-label">当前用户:</div>
                                    <div class="property-value">
                                        {{ currentComputer.lastName && currentComputer.firstName ? 
                                           `${currentComputer.lastName} ${currentComputer.firstName}` : 
                                           (currentComputer.ntAccount || '无') }}
                                    </div>
                                </div>
                                <div class="property-item">
                                    <div class="property-label">部门:</div>
                                    <div class="property-value">{{ currentComputer.department || '无' }}</div>
                                </div>
                            </div>
                            <div class="property-actions">
                                <el-popover
                                    placement="right"
                                    :width="400"
                                    trigger="click">
                                    <template #reference>
                                        <el-button type="primary" size="small" class="more-details-btn">更多详情</el-button>
                                    </template>
                                    <div class="record-details">
                                        <div class="record-detail-row" v-for="(value, key) in getCurrentDetails()" :key="key">
                                            <span class="record-detail-label">{{ key }}:</span>
                                            <span class="record-detail-value">{{ value }}</span>
                                        </div>
                                    </div>
                                </el-popover>
                            </div>
                        </div>
                    </div>
                    
                    <div class="trace-content">
                        <div class="trace-history-header">历史记录</div>
                        <div v-if="traceLoading" class="trace-loading">
                            <el-skeleton :rows="5" animated />
                        </div>
                        <template v-else>
                            <el-table 
                                v-if="traceRecordList && traceRecordList.length > 0"
                                :data="traceRecordList" 
                                border 
                                stripe
                                style="width: 100%">
                                <!-- 表格内容保持不变 -->
                                <el-table-column type="index" label="序号" width="80" />
                                <el-table-column prop="updateTime" label="更新时间" width="180" sortable />
                                <el-table-column prop="pcStatus" label="电脑状态" width="150">
                                    <template #default="{ row }">
                                        <el-tag :type="getStatusTagType(row.pcStatus)">
                                            {{ row.pcStatus }}
                                        </el-tag>
                                    </template>
                                </el-table-column>
                                <el-table-column prop="ntAccount" label="NT账号" width="150" />
                                <el-table-column label="用户" width="180">
                                    <template #default="{ row }">
                                        {{ row.lastName && row.firstName ? 
                                           `${row.lastName} ${row.firstName}` : 
                                           (row.ntAccount || '无') }}
                                    </template>
                                </el-table-column>
                                <el-table-column prop="pcClass" label="电脑归属情况" width="300" />
                                <el-table-column prop="company" label="公司" width="150" />
                                <el-table-column label="更多信息" width="100">
                                    <template #default="{ row }">
                                        <el-popover
                                            placement="right"
                                            :width="400"
                                            trigger="click">
                                            <template #reference>
                                                <el-button type="text">查看详情</el-button>
                                            </template>
                                            <div class="record-details">
                                                <div class="record-detail-row" v-for="(value, key) in getRecordDetails(row)" :key="key">
                                                    <span class="record-detail-label">{{ key }}:</span>
                                                    <span class="record-detail-value">{{ value }}</span>
                                                </div>
                                            </div>
                                        </el-popover>
                                    </template>
                                </el-table-column>
                            </el-table>
                            
                            <!-- 表格为空时显示的内容 -->
                            <el-empty v-else description="暂无历史记录数据"></el-empty>
                            
                            <!-- 分页 -->
                            <div class="trace-pagination" v-if="traceRecordList && traceRecordList.length > 0">
                                <el-pagination
                                    :current-page="traceQueryForm.pageNum"
                                    :page-size="traceQueryForm.pageSize"
                                    :page-sizes="[10, 20, 50, 100]"
                                    layout="total, sizes, prev, pager, next, jumper"
                                    :total="traceRecordTotal"
                                    @size-change="handleTracePageSizeChange"
                                    @current-change="handleTracePageChange"
                                />
                            </div>
                        </template>
                    </div>
                </div>
            </el-dialog>

            <!-- 归还电脑确认弹窗 -->
            <el-dialog 
                v-model="returnComputerDialogVisible" 
                title="归还电脑确认" 
                width="480px" 
                :close-on-click-modal="false"
                custom-class="tech-dialog"
                destroy-on-close>
                <div class="tech-dialog-content">
                    <div class="tech-header">
                        <div class="tech-icon-container">
                            <el-icon class="tech-icon"><InfoFilled /></el-icon>
                        </div>
                        <div class="tech-title-container">
                            <h3 class="tech-title">归还电脑确认</h3>
                            <p class="tech-subtitle">请确认是否归还该设备</p>
                        </div>
                    </div>
                    
                    <div class="return-details">
                        <div class="return-detail-item">
                            <div class="return-detail-label">电脑名称:</div>
                            <div class="return-detail-value">{{ currentReturnComputer?.ciName || '未知' }}</div>
                        </div>
                        <div class="return-detail-item">
                            <div class="return-detail-label">当前使用者:</div>
                            <div class="return-detail-value">{{ currentReturnComputer?.lastName }} {{ currentReturnComputer?.firstName }}</div>
                        </div>
                        <div class="return-detail-item">
                            <div class="return-detail-label">使用年限:</div>
                            <div class="return-detail-value">{{ currentReturnComputer ? calculateYearsToToday(currentReturnComputer.lifeCycleStart) + ' 年' : '未知' }}</div>
                        </div>
                        <div class="return-detail-item">
                            <div class="return-detail-label">归还后状态:</div>
                            <div class="return-detail-value">
                                <el-select v-model="selectedReturnStatus" style="width: 100%">
                                    <el-option label="待分配" value="To be assigned"></el-option>
                                    <el-option label="待报废" value="To be scrapped"></el-option>
                                </el-select>
                            </div>
                        </div>
                        
                        <div class="return-note">
                            <template v-if="currentReturnComputer && parseFloat(calculateYearsToToday(currentReturnComputer.lifeCycleStart)) > 8">
                                <div class="note-icon"><el-icon><Warning /></el-icon></div>
                                <div class="note-text">该电脑已使用超过8年，系统建议将其标记为报废状态，您可以根据实际情况调整</div>
                            </template>
                            <template v-else>
                                <div class="note-icon"><el-icon><InfoFilled /></el-icon></div>
                                <div class="note-text">归还后，该电脑将回到您选择的状态</div>
                            </template>
                        </div>
                    </div>
                </div>
                <template #footer>
                    <div class="tech-dialog-footer">
                        <el-button @click="returnComputerDialogVisible = false" class="tech-cancel-btn">取 消</el-button>
                        <el-button type="primary" @click="confirmReturnComputer" :loading="returnComputerLoading" class="tech-confirm-btn">
                            <span class="tech-btn-text">确认归还</span>
                            <span class="tech-btn-icon"><i class="el-icon-check"></i></span>
                        </el-button>
                    </div>
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

/* 用户输入框容器 */
.user-input-container {
    position: relative;
  width: 100%;
    overflow: visible; /* 允许子元素超出容器边界 */
    z-index: 30; /* 确保输入容器在最高层级 */
}

/* 用户查询结果下拉框样式 */
.user-results-dropdown {
    position: fixed !important;
    background-color: #fff;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.2); /* 增强阴影效果 */
    z-index: 9999 !important; /* 提高z-index值，确保显示在其他元素之上 */
    overflow-y: auto;
    scrollbar-width: thin;
    scrollbar-color: #909399 #f4f4f5;
}

.query-form {
    position: relative;
    z-index: 10; /* 确保查询表单在较高层级 */
}

.form-item {
    position: relative;
    z-index: 20; /* 确保表单项在更高层级 */
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
    background-color: rgb(236, 245, 255);
    color: rgb(64, 158, 255);
    font-size: 14px;
    border-radius: 4px;
    width: auto;
    padding: 0 15px;
    margin-right: 10px;
    height: 36px;
}

/* 按钮悬停效果 */
.query-form .el-button:hover {
    background-color: #66b1ff; /* 悬停时按钮背景色 */
    border-color: #66b1ff; /* 悬停时按钮边框颜色 */
    color: white;
}

/* 更新按钮样式 */
.update-btn {
    background-color: rgb(236, 245, 255) !important;
    color: rgb(64, 158, 255) !important;
}

.update-btn:hover {
    background-color: #66b1ff !important;
    border-color: #66b1ff !important;
    color: white !important;
}

/* 科技风格对话框 */
:deep(.tech-dialog) {
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15), 
                0 5px 15px rgba(0, 0, 0, 0.08);
    background: #ffffff;
}

:deep(.tech-dialog .el-dialog__header) {
    padding: 0;
    margin: 0;
    height: 0;
}

:deep(.tech-dialog .el-dialog__title) {
    display: none;
}

:deep(.tech-dialog .el-dialog__headerbtn) {
    top: 16px;
    right: 16px;
    z-index: 10;
}

:deep(.tech-dialog .el-dialog__headerbtn .el-dialog__close) {
    color: #ffffff;
    font-size: 20px;
    transition: all 0.3s ease;
}

:deep(.tech-dialog .el-dialog__headerbtn:hover .el-dialog__close) {
    color: #ffffff;
    transform: rotate(90deg);
}

:deep(.tech-dialog .el-dialog__body) {
    padding: 0;
    margin: 0;
}

:deep(.tech-dialog .el-dialog__footer) {
    padding: 0;
    margin: 0;
}

.tech-dialog-content {
    padding: 0;
}

.tech-header {
    background: linear-gradient(135deg, #2580bf, #20b2aa);
    padding: 25px 30px;
    display: flex;
    align-items: center;
    gap: 20px;
    position: relative;
    overflow: hidden;
    box-shadow: 0 3px 10px rgba(37, 128, 191, 0.3);
}

.tech-header::before {
    display: none;
}

.tech-header::after {
    display: none;
}

.tech-title {
    margin: 0;
    padding: 0;
    font-size: 22px;
    font-weight: 600;
    color: #fff;
    letter-spacing: 0.5px;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.tech-subtitle {
    margin: 6px 0 0 0;
    padding: 0;
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
    letter-spacing: 0.3px;
}

.tech-icon-container {
    background: rgba(255, 255, 255, 0.2);
    width: 64px;
    height: 64px;
    border-radius: 16px;
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
    z-index: 2;
    border: 2px solid rgba(255, 255, 255, 0.3);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
    animation: none;
}

/* 删除不需要的动画定义 */
/* 移除原来的动画样式 */
.tech-header::before, 
.tech-header::after,
.tech-btn-icon,
.tech-title,
.tech-subtitle,
.tech-option,
.upload-icon {
    animation: none !important;
}

/* 追溯弹框的动画也清除 */
.trace-title span,
.trace-tag,
.trace-subtitle,
.trace-content {
    animation: none !important;
}

@keyframes pulse {
    0% {
        display: none;
    }
    100% {
        display: none;
    }
}

@keyframes slide {
    0% {
        display: none;
    }
    100% {
        display: none;
    }
}

.tech-icon {
    font-size: 32px;
    color: #fff;
    filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.tech-title-container {
    position: relative;
    z-index: 2;
}

.tech-options {
    padding: 25px 30px;
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.tech-option {
    display: flex;
    align-items: flex-start;
    padding: 15px;
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.7);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    animation: fadeIn 0.5s ease forwards;
    opacity: 0.9;
    transform: translateY(5px);
    border: 1px solid rgba(8, 213, 207, 0.1);
}

.tech-option:nth-child(1) {
    animation-delay: 0.1s;
}

.tech-option:nth-child(2) {
    animation-delay: 0.2s;
}

@keyframes fadeIn {
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.tech-option::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    height: 100%;
    background: #20b2aa;
    opacity: 0;
    transition: all 0.3s ease;
}

.tech-option:hover {
    background: rgba(255, 255, 255, 0.9);
    box-shadow: 0 4px 12px rgba(32, 178, 170, 0.1);
    transform: translateY(-2px);
}

.tech-option-active {
    background: rgba(255, 255, 255, 0.95) !important;
    box-shadow: 0 5px 15px rgba(32, 178, 170, 0.15) !important;
    border: 1px solid rgba(32, 178, 170, 0.3);
}

.tech-option-active::before {
    opacity: 1 !important;
}

.tech-option-radio {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    border: 2px solid #20b2aa;
    margin-right: 15px;
    margin-top: 3px;
    flex-shrink: 0;
    display: flex;
    justify-content: center;
    align-items: center;
    transition: all 0.3s ease;
}

.tech-option-radio-inner {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: #20b2aa;
    animation: scaleIn 0.3s ease forwards;
}

@keyframes scaleIn {
    from {
        transform: scale(0);
    }
    to {
        transform: scale(1);
    }
}

.tech-option-content {
    flex-grow: 1;
    text-align: left;
}

.tech-option-title {
    font-size: 16px;
    font-weight: 500;
    color: #333;
    margin-bottom: 5px;
}

.tech-option-details {
    font-size: 13px;
    color: #666;
}

.tech-warning {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-top: 5px;
    margin-left: 4px;
    padding: 12px 15px;
    background: rgba(230, 162, 60, 0.08);
    border-radius: 8px;
    border-left: 3px solid #E6A23C;
    animation: fadeIn 0.5s ease 0.3s forwards;
    opacity: 0;
}

.tech-warning .el-icon {
    color: #E6A23C;
    font-size: 18px;
}

.tech-warning span {
    color: #E6A23C;
    font-size: 13px;
}

.tech-dialog-footer {
    display: flex;
    justify-content: flex-end;
    padding: 20px 30px;
    background: #f5f7fa;
    border-top: 1px solid rgba(32, 178, 170, 0.1);
    gap: 15px;
}

.tech-cancel-btn {
    border: 1px solid #d9ecff;
    background-color: white;
    color: #606266;
    transition: all 0.3s ease;
}

.tech-cancel-btn:hover {
    color: #20b2aa;
    border-color: rgba(32, 178, 170, 0.3);
    background-color: rgba(32, 178, 170, 0.03);
}

.tech-confirm-btn {
    background: linear-gradient(135deg, #2580bf, #20b2aa) !important;
    border: none !important;
    box-shadow: 0 4px 10px rgba(32, 178, 170, 0.3) !important;
    padding: 10px 20px !important;
    overflow: hidden;
    position: relative;
    transition: all 0.3s ease !important;
    display: flex;
    align-items: center;
    gap: 8px;
}

.tech-confirm-btn::before {
    display: none;
}

.tech-confirm-btn:hover {
    transform: translateY(-2px) !important;
    box-shadow: 0 6px 15px rgba(32, 178, 170, 0.4) !important;
}

.tech-confirm-btn:hover::before {
    display: none;
}

.tech-btn-text {
    position: relative;
    z-index: 2;
}

.tech-btn-icon {
    position: relative;
    z-index: 2;
    opacity: 0;
    transform: translateX(-5px);
    transition: all 0.3s ease;
}

.tech-confirm-btn:hover .tech-btn-icon {
    opacity: 1;
    transform: translateX(0);
}

/* 恢复文件上传对话框新布局样式，并更新为蓝绿色主题 */
/* 圆角对话框样式 */
:deep(.rounded-dialog) {
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 10px 30px rgba(37, 128, 191, 0.15), 
                0 1px 8px rgba(32, 178, 170, 0.1);
}

:deep(.rounded-dialog .el-dialog__header) {
    padding: 12px 15px;
    text-align: right;
    border-bottom: none;
    margin-right: 0;
    background: linear-gradient(to right, #e0f5fa, #edfcfb);
}

:deep(.rounded-dialog .el-dialog__title) {
    display: none;
}

:deep(.rounded-dialog .el-dialog__headerbtn) {
    top: 12px;
    right: 15px;
}

:deep(.rounded-dialog .el-dialog__headerbtn .el-dialog__close) {
    color: #20b2aa;
    transition: all 0.3s ease;
}

:deep(.rounded-dialog .el-dialog__headerbtn:hover .el-dialog__close) {
    transform: rotate(90deg);
    color: #2580bf;
}

:deep(.rounded-dialog .el-dialog__body) {
    padding: 20px;
    background: linear-gradient(145deg, #ffffff, #f6fdfc);
}

:deep(.rounded-dialog .el-dialog__footer) {
    border-top: 1px solid rgba(32, 178, 170, 0.1);
    padding: 14px 20px;
    background: #f5f7fa;
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    gap: 12px;
}

.dialog-footer .cancel-btn {
    border-color: #d9ecff;
    color: #606266;
    background: #fff;
    transition: all 0.3s;
    padding: 9px 15px;
}

.dialog-footer .cancel-btn:hover {
    color: #20b2aa;
    border-color: rgba(32, 178, 170, 0.3);
    background-color: rgba(32, 178, 170, 0.03);
}

.dialog-footer .confirm-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    background: linear-gradient(135deg, #08d5cf, #0886d5);
    border: none;
    box-shadow: 0 4px 10px rgba(8, 213, 207, 0.3);
    transition: all 0.3s ease;
    min-width: 100px;
    padding: 10px 16px;
    height: auto;
    border-radius: 6px;
    font-weight: 500;
    letter-spacing: 0.5px;
}

.dialog-footer .confirm-btn:hover {
    transform: translateY(-2px);
    background: linear-gradient(135deg, #20e2dc, #1a98e7);
    box-shadow: 0 6px 15px rgba(8, 213, 207, 0.4);
}

.dialog-footer .confirm-btn:active {
    transform: translateY(0);
    box-shadow: 0 2px 5px rgba(32, 178, 170, 0.3);
}

@keyframes glow {
    from {
        filter: drop-shadow(0 0 2px rgba(8, 213, 207, 0.6));
    }
    to {
        filter: drop-shadow(0 0 8px rgba(8, 213, 207, 0.8));
    }
}

/* 文件上传对话框新布局样式 */
.upload-dialog-container {
    display: flex;
    gap: 0;
    padding: 15px 0;
    min-height: 300px;
}

.upload-dialog-left {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 10px 20px 10px 10px;
}

.upload-dialog-divider {
    width: 1px;
    background: linear-gradient(to bottom, 
        rgba(32, 178, 170, 0), 
        rgba(32, 178, 170, 0.2) 15%, 
        rgba(32, 178, 170, 0.2) 85%, 
        rgba(32, 178, 170, 0));
    margin: 20px 0;
}

.upload-dialog-right {
    flex: 1.2;
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: 10px 10px 10px 20px;
}

.upload-icon-container {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
}

.upload-icon {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: linear-gradient(135deg, #e0f7f6, #d4f0fa);
    display: flex;
    justify-content: center;
    align-items: center;
    box-shadow: 0 4px 15px rgba(8, 213, 207, 0.2),
                inset 0 -2px 5px rgba(255, 255, 255, 0.8),
                inset 0 2px 5px rgba(8, 213, 207, 0.1);
    animation: float 3s ease-in-out infinite;
}

@keyframes float {
    0%, 100% {
        transform: translateY(0);
    }
    50% {
        transform: translateY(-6px);
    }
}

.upload-icon-svg {
    font-size: 36px;
    color: #08d5cf;
    filter: drop-shadow(0 2px 4px rgba(8, 213, 207, 0.3));
}

.upload-title {
    font-size: 20px;
    color: #303133;
    margin: 0 0 12px 0;
    font-weight: 600;
    text-align: center;
    letter-spacing: 0.5px;
}

.upload-subtitle {
    font-size: 14px;
    color: #606266;
    margin: 0 0 24px 0;
    line-height: 1.5;
    text-align: center;
}

.upload-container {
    width: 100%;
    border-radius: 12px;
    background: #f8fafc;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.05),
                inset 0 1px 1px rgba(255, 255, 255, 0.8);
    overflow: hidden;
    transition: all 0.3s ease;
    padding: 2px;
}

.upload-container:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(32, 178, 170, 0.1),
                inset 0 1px 1px rgba(255, 255, 255, 0.8);
}

.upload-excel {
    width: 100%;
}

.upload-excel :deep(.el-upload-dragger) {
    width: 100%;
    height: 180px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background: #ffffff;
    border: 1px dashed rgba(32, 178, 170, 0.4);
    border-radius: 10px;
    transition: all 0.3s;
}

.upload-excel :deep(.el-upload-dragger:hover) {
    border-color: #20b2aa;
    background: linear-gradient(145deg, #fafcff, #f0f8f8);
    box-shadow: inset 0 2px 8px rgba(32, 178, 170, 0.08);
}

.upload-excel :deep(.el-upload-dragger .el-icon--upload) {
    font-size: 38px;
    color: #20b2aa;
    margin-bottom: 12px;
    filter: drop-shadow(0 2px 4px rgba(32, 178, 170, 0.2));
}

.upload-excel :deep(.el-upload__text) {
    color: #606266;
    font-size: 14px;
    line-height: 1.6;
}

.upload-excel :deep(.el-upload__text em) {
    color: #20b2aa;
    font-style: normal;
    font-weight: 600;
}

.upload-excel :deep(.el-upload__tip) {
    text-align: center;
    color: #909399;
    line-height: 1.5;
    margin-top: 8px;
}

.upload-tips {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: #909399;
    margin-top: auto;
    margin-bottom: 18px;
    padding: 10px 12px;
    background-color: rgba(32, 178, 170, 0.05);
    border-left: 3px solid #20b2aa;
    border-radius: 4px;
}

.upload-tips .el-icon {
    color: #08d5cf;
    font-size: 16px;
    flex-shrink: 0;
}

.template-download-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    background: linear-gradient(135deg, #08d5cf, #0886d5);
    border: none;
    border-radius: 6px;
    box-shadow: 0 4px 10px rgba(8, 213, 207, 0.3);
    transition: all 0.3s ease;
    margin-top: 0;
    padding: 10px 16px;
    height: auto;
    font-weight: 500;
    letter-spacing: 0.5px;
}

.template-download-btn:hover {
    transform: translateY(-2px);
    background: linear-gradient(135deg, #20e2dc, #1a98e7);
    box-shadow: 0 6px 15px rgba(8, 213, 207, 0.4);
}

.template-download-btn:active {
    transform: translateY(0);
    box-shadow: 0 2px 5px rgba(8, 213, 207, 0.3);
}

/* 编辑弹窗样式 */
:deep(.tech-edit-dialog) {
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15), 
                0 5px 15px rgba(0, 0, 0, 0.08);
}

:deep(.tech-edit-dialog .el-dialog__header) {
    padding: 0;
    margin: 0;
    height: 0;
}

:deep(.tech-edit-dialog .el-dialog__title) {
    display: none;
}

:deep(.tech-edit-dialog .el-dialog__headerbtn) {
    top: 16px;
    right: 16px;
    z-index: 10;
}

:deep(.tech-edit-dialog .el-dialog__headerbtn .el-dialog__close) {
    color: #fff;
    font-size: 20px;
    transition: all 0.3s ease;
}

:deep(.tech-edit-dialog .el-dialog__headerbtn:hover .el-dialog__close) {
    color: #fff;
    transform: rotate(90deg);
}

:deep(.tech-edit-dialog .el-dialog__body) {
    padding: 0;
    margin: 0;
}

:deep(.tech-edit-dialog .el-dialog__footer) {
    padding: 0;
    margin: 0;
}

.edit-dialog-container {
    position: relative;
    border-radius: 4px;
    overflow: hidden;
}

.edit-header {
    background: linear-gradient(135deg, #2580bf 0%, #20b2aa 100%);
    color: #fff;
    padding: 25px 30px;
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

.edit-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
    position: relative;
}

.edit-title span {
    font-size: 22px;
    font-weight: 600;
    /* Add subtle text shadow for better readability */
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
    /* Add subtle animation on load */
    animation: fadeInLeft 0.5s ease-out;
}

.edit-tag {
    font-weight: 500;
    font-size: 14px;
    transition: all 0.3s ease;
    transform-origin: right;
    animation: scaleIn 0.4s ease-out 0.2s both;
    background-color: rgba(255, 255, 255, 0.2);
    border: none;
    color: #fff;
    padding: 4px 10px;
}

.edit-subtitle {
    font-size: 14px;
    opacity: 0.9;
    animation: fadeInUp 0.5s ease-out 0.1s both;
}

.edit-content {
    padding: 0;
    background-color: #f8f9fa;
    animation: fadeIn 0.5s ease-out 0.1s both;
}

/* Tabs 样式覆盖 */
.tech-tabs {
    border: none !important;
    box-shadow: none !important;
    background-color: transparent !important;
}

:deep(.tech-tabs .el-tabs__header) {
    background: linear-gradient(to right, #f0f7f7, #e8f6fa);
    border-bottom: 1px solid rgba(32, 178, 170, 0.1) !important;
    padding: 10px 20px 0;
    margin: 0;
}

:deep(.tech-tabs .el-tabs__nav) {
    border: none !important;
}

:deep(.tech-tabs .el-tabs__item) {
    height: 40px;
    line-height: 40px;
    color: #606266;
    font-size: 14px;
    font-weight: 500;
    border: 1px solid transparent !important;
    border-bottom: none !important;
    border-radius: 8px 8px 0 0;
    transition: all 0.3s ease;
    position: relative;
    padding: 0 20px;
    margin-right: 5px;
}

:deep(.tech-tabs .el-tabs__item:hover) {
    color: #20b2aa;
}

:deep(.tech-tabs .el-tabs__item.is-active) {
    color: #2580bf;
    background: #fff;
    border-color: rgba(32, 178, 170, 0.1) !important;
}

:deep(.tech-tabs .el-tabs__item.is-active::after) {
    content: '';
    position: absolute;
    bottom: -1px;
    left: 0;
    right: 0;
    height: 2px;
    background-color: #fff;
}

:deep(.tech-tabs .el-tabs__content) {
    padding: 25px 20px;
    background-color: #fff;
}

:deep(.tech-tabs .el-tabs__nav-wrap::after) {
    display: none;
}

/* Form styles */
.tech-form {
    padding: 0;
}

.tech-form :deep(.el-form-item__label) {
    color: #606266;
    font-weight: 500;
    line-height: 1.5;
}

.tech-form :deep(.el-input__inner) {
    border-color: #dcdfe6;
    border-radius: 4px;
    transition: all 0.3s ease;
}

.tech-form :deep(.el-input__inner:hover) {
    border-color: #c0c4cc;
}

.tech-form :deep(.el-input__inner:focus) {
    border-color: #20b2aa;
    box-shadow: 0 0 0 2px rgba(32, 178, 170, 0.1);
}

.tech-form :deep(.el-select .el-input.is-focus .el-input__inner) {
    border-color: #20b2aa;
}

/* Dialog footer */
.tech-dialog-footer {
    display: flex;
    justify-content: flex-end;
    padding: 20px 30px;
    background: rgba(240, 246, 252, 0.5);
    border-top: 1px solid rgba(8, 213, 207, 0.1);
    gap: 15px;
}

.tech-cancel-btn {
    border: 1px solid #d9ecff;
    background-color: white;
    color: #606266;
    transition: all 0.3s ease;
}

.tech-cancel-btn:hover {
    color: #20b2aa;
    border-color: rgba(32, 178, 170, 0.3);
    background-color: rgba(32, 178, 170, 0.03);
}

.tech-confirm-btn {
    background: linear-gradient(135deg, #2580bf, #20b2aa) !important;
    border: none !important;
    box-shadow: 0 4px 10px rgba(32, 178, 170, 0.3) !important;
    padding: 10px 20px !important;
    overflow: hidden;
    position: relative;
    transition: all 0.3s ease !important;
    display: flex;
    align-items: center;
    gap: 8px;
}

.tech-confirm-btn::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, 
        rgba(255, 255, 255, 0) 0%, 
        rgba(255, 255, 255, 0.2) 50%, 
        rgba(255, 255, 255, 0) 100%);
    transition: all 0.6s ease;
}

.tech-confirm-btn:hover {
    transform: translateY(-2px) !important;
    box-shadow: 0 6px 15px rgba(32, 178, 170, 0.4) !important;
}

.tech-confirm-btn:hover::before {
    left: 100%;
}

.tech-btn-text {
    position: relative;
    z-index: 2;
}

.tech-btn-icon {
    position: relative;
    z-index: 2;
    opacity: 0;
    transform: translateX(-5px);
    transition: all 0.3s ease;
}

.tech-confirm-btn:hover .tech-btn-icon {
    opacity: 1;
    transform: translateX(0);
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

@keyframes scaleIn {
    from {
        opacity: 0;
        transform: scale(0.8);
    }
    to {
        opacity: 1;
        transform: scale(1);
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

/* 追溯对话框样式 */
.trace-dialog-container {
    position: relative;
    border-radius: 4px;
    overflow: hidden;
}

.trace-header {
    background: linear-gradient(135deg, #2580bf 0%, #20b2aa 100%);
    color: #fff;
    padding: 25px 30px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    position: relative;
    overflow: hidden;
}

.trace-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
    position: relative;
}

.trace-title span {
    font-size: 22px;
    font-weight: 600;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
    animation: fadeInLeft 0.5s ease-out;
}

.trace-tag {
    font-weight: 500;
    font-size: 14px;
    transition: all 0.3s ease;
    transform-origin: right;
    animation: scaleIn 0.4s ease-out 0.2s both;
    background-color: rgba(255, 255, 255, 0.2);
    border: none;
    color: #fff;
    padding: 4px 10px;
}

.trace-subtitle {
    font-size: 14px;
    opacity: 0.9;
    animation: fadeInUp 0.5s ease-out 0.1s both;
}

.trace-content {
    padding: 20px;
    background-color: #f8f9fa;
    animation: fadeIn 0.5s ease-out 0.1s both;
}

.trace-pagination {
    margin-top: 20px;
    text-align: right;
}

/* 记录详情样式 */
.record-details {
    max-height: 400px;
    overflow-y: auto;
    padding: 10px 0;
}

.record-detail-row {
    padding: 8px 0;
    border-bottom: 1px dashed #eee;
    display: flex;
}

.record-detail-row:last-child {
    border-bottom: none;
}

.record-detail-label {
    font-weight: 500;
    color: #606266;
    width: 100px;
    flex-shrink: 0;
}

.record-detail-value {
    color: #333;
    flex-grow: 1;
    word-break: break-all;
}

/* 追溯加载样式 */
.trace-loading {
    padding: 20px 0;
}

/* 表格样式增强 */
.trace-content .el-table {
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    border-radius: 4px;
}

/* 状态标签样式增强 */
.trace-content .el-tag {
    font-weight: 500;
    padding: 4px 8px;
    min-width: 70px;
    text-align: center;
}

.current-properties {
    margin: 20px 20px;
    padding: 15px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.property-header {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 15px;
    color: #333;
    padding-bottom: 8px;
    border-bottom: 1px solid #eee;
}

.property-content {
    display: flex;
    flex-direction: column;
    gap: 15px;
    padding: 0 5px;
}

.property-row {
    display: flex;
    width: 100%;
    gap: 20px;
}

.property-item {
    flex: 1;
    display: flex;
    align-items: center;
    margin-bottom: 0;
}

.property-label {
    font-weight: 500;
    margin-right: 10px;
    color: #666;
    width: 100px;
    flex-shrink: 0;
}

.property-value {
    flex-grow: 1;
}

.more-details-btn {
    margin: 5px 0;
    background: #ecf5ff;
    border: 1px solid #d9ecff;
    color: #409eff;
    border-radius: 4px;
    padding: 5px 15px;
    cursor: pointer;
}

.trace-history-header {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 10px;
    color: #333;
    padding-left: 5px;
}

.property-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 10px;
    padding-top: 10px;
    border-top: 1px dashed #eee;
}

/* 归还对话框样式 */
.return-dialog-content {
    display: flex;
    align-items: flex-start;
    padding: 20px 10px;
}

.return-icon {
    font-size: 24px;
    color: #E6A23C;
    margin-right: 16px;
    margin-top: 4px;
}

.return-text {
    flex: 1;
}

.return-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 16px 0;
}

.return-desc {
    font-size: 14px;
    color: #606266;
    margin: 8px 0;
    line-height: 1.5;
}

.text-danger {
    color: #F56C6C;
}

.text-primary {
    color: #409EFF;
}

/* 新的归还弹框详情样式 */
.return-details {
    padding: 25px 20px;
    display: flex;
    flex-direction: column;
    gap: 16px;
    animation: fadeIn 0.5s ease;
}

.return-detail-item {
    display: flex;
    align-items: center;
    border-bottom: 1px dashed rgba(37, 128, 191, 0.1);
    padding-bottom: 12px;
}

.return-detail-label {
    font-weight: 500;
    color: #606266;
    width: 100px;
    flex-shrink: 0;
}

.return-detail-value {
    flex: 1;
    color: #303133;
    font-weight: 500;
}

.return-note {
    margin-top: 10px;
    padding: 12px 15px;
    display: flex;
    align-items: flex-start;
    gap: 10px;
    border-radius: 6px;
    background-color: rgba(32, 178, 170, 0.05);
}

.note-icon {
    color: #20b2aa;
    font-size: 16px;
    margin-top: 2px;
}

.note-text {
    flex: 1;
    font-size: 13px;
    line-height: 1.5;
    color: #606266;
}

/* 为 Webkit 浏览器自定义滚动条样式 */
.user-results-dropdown::-webkit-scrollbar {
    width: 8px;
}

.user-results-dropdown::-webkit-scrollbar-track {
    background: #f4f4f5;
    border-radius: 4px;
}

.user-results-dropdown::-webkit-scrollbar-thumb {
    background-color: #909399;
    border-radius: 4px;
    border: 2px solid #f4f4f5;
}

.user-result-item {
    padding: 12px 15px;
    cursor: pointer;
    border-bottom: 1px solid #ebeef5;
    white-space: nowrap; /* 防止文本换行 */
    overflow: hidden; /* 隐藏溢出部分 */
    text-overflow: ellipsis; /* 显示省略号 */
    line-height: 1.4; /* 增加行高，提高可读性 */
    font-size: 14px; /* 调整字体大小 */
}

.user-result-item:last-child {
    border-bottom: none;
}

.user-result-item:hover {
    background-color: #f5f7fa;
}

.no-results {
    padding: 15px 12px;
    color: #909399;
    text-align: center;
    font-size: 14px;
}

.query-form {
  width: 100%;
  margin: 5px auto;
  padding: 0;
  background: none;
  display: flex;
  flex-wrap: wrap;
  position: relative;
  z-index: 10; /* 确保查询表单在较高层级 */
}

.form-item {
  margin-right: 10px;
  margin-bottom: 8px;
  width: auto;
  position: relative;
  z-index: 20; /* 确保表单项在更高层级 */
}
</style>
