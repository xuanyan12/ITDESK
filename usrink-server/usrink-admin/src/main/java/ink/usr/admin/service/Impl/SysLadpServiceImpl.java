package ink.usr.admin.service.Impl;

import com.google.protobuf.ServiceException;
import ink.usr.admin.dao.DTO.SysAutoLoginUserDto;
import ink.usr.admin.mapper.SysApproverMapper;
import ink.usr.admin.mapper.SysLadpMapper;
import ink.usr.admin.mapper.SysUserMapper;
import ink.usr.admin.service.SysLadpService;
import ink.usr.admin.utils.LdapUserUtil;
import ink.usr.common.core.constants.Constants;
import ink.usr.common.core.domain.Res;
import ink.usr.common.core.utils.Md5Util;
import ink.usr.common.model.mysql.SysApproverModel;
import ink.usr.common.model.mysql.SysLadpUserModel;
import ink.usr.common.model.mysql.SysUserModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.AuthenticationException;
import javax.naming.Context;

import javax.annotation.Resource;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SysLadpServiceImpl implements SysLadpService {

    @Autowired
    private SysLadpMapper sysLadpMapper;
    @Autowired
    private SysApproverMapper sysApproverMapper;
    @Autowired
    private SysUserMapper sysUserMapper;

    @Resource
    private ink.usr.admin.config.LDAPConfig ldapConfig;

    LdapContext ctx = null;
    String msg = null;

    @Override
    public void initConnect() {
        this.connect(ldapConfig.getNtUserName(), ldapConfig.getNtPassword());
    }

    @Override
    public void disConnect() {
        try {
            ctx.close();
        } catch (Exception e) {
            ctx=null;
        }
        ctx=null;
        this.msg = "链接断开";
    }

    @Override
    public void connect(String username, String password) {
        // 断开连接
        this.disConnect();

        // 创建一个Hashtable对象，用于存储LDAP连接参数
        Hashtable<String,String> HashEnv = new Hashtable<String,String>();
        HashEnv.put(Context.SECURITY_AUTHENTICATION, "simple"); 							// LDAP访问安全级别(none,simple,strong)
        HashEnv.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory"); 	// LDAP工厂类
        HashEnv.put("com.sun.jndi.ldap.connect.timeout", "20000");							// 连接超时设置为10秒
        HashEnv.put(Context.PROVIDER_URL, ldapConfig.getLaps());			                                // AD域服务器地址
        HashEnv.put(Context.SECURITY_PRINCIPAL, "SG\\"+username); 								    // AD的用户名
        HashEnv.put(Context.SECURITY_CREDENTIALS, password);

        // 设置系统属性
        Properties systemProps = System.getProperties();
        systemProps.put( "javax.net.ssl.trustStore",ldapConfig.getTrustStore());
        systemProps.put( "javax.net.ssl.trustStorePassword", ldapConfig.getTrustStorePassword());
        System.setProperties(systemProps);

        try {
            // 创建一个InitialLdapContext对象，用于连接LDAP服务器
            this.ctx =  new InitialLdapContext(HashEnv, null);
            this.msg = "连接成功";
        } catch (Exception e) {
            this.msg = "AD域身份验证失败";
            throw new RuntimeException(e);
        }
    }

    @Override
    public SysLadpUserModel authenticate(String loginName, String password) {
        this.disConnect();
        int count = 0;
        do{
            count ++;
            this.connect(loginName, password);
        }while (this.ctx != null && count < 3);
        if( this.ctx != null ){
            List<SysLadpUserModel> ldapUserDOS = this.searchLdapUser(loginName);
            if( ldapUserDOS.size() == 1 ){
                return ldapUserDOS.get(0);
            }
        }
        return null;
    }

    @Override
    public List<SysLadpUserModel> searchLdapUser(String username) {
        int count = 0;
        while(this.ctx==null && count < 3 ){
            count++;
            this.initConnect();
        }
        if(this.ctx==null){
            throw new IllegalStateException("LDAP 连接失败，ctx 为空！");
        }

        List<SysLadpUserModel> ldapUserDOList = new ArrayList<SysLadpUserModel>();

        //String searchBase = "CN=liiccng,OU=L,OU=USERACCOUNTS,OU=Bosch-Accounts,OU=Applications,DC=sg,DC=lan";
        String searchBase = "OU=USERACCOUNTS,OU=Bosch-Accounts,OU=Applications,DC=sg,DC=lan";
        if (username != null && !username.isEmpty()) {
            searchBase = "CN=" + username + ",OU=" + username.substring(0, 1) + "," + searchBase;
        }
        /*LDAP搜索过滤器类*/
        String searchFilter = "objectClass=User";

        /*搜索控制器*/
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        // 1.添加了title来标识性别 Mr. Ms.
        // 2.添加了manager来标识mentor（fix-term才需要换成mentor，默认为Name，即用户自己）
        // 3.添加了l来标识地点（区分长沙与长春）
        // 4.添加了userAccountControl来标识账号的启停情况
        // 512：启用的普通账号（默认值）；544：启用，密码永不过期；514：账号被禁用；66050：被禁用 + 密码永不过期。
        // 5.添加了whenCreated，标识用户的创建时间
        String[] attributes = {"Name","Department","mail","DisplayName","Department","Description","telephoneNumber","title","manager","l","userAccountControl","whenCreated"};
        searchCtls.setReturningAttributes(attributes);   // 设置返回属性集

        try {
            //分页
            int pageSize = 500;
            byte[] cookie = null;
            ctx.setRequestControls(new Control[] { new PagedResultsControl(  pageSize, Control.CRITICAL) });
            int totalResults = 0;
            do {
                /*根据设置的域节点、过滤器类和搜索控制器搜索LDAP得到结果*/
                NamingEnumeration<SearchResult> results = ctx.search(searchBase, searchFilter,searchCtls);

                while (null != results && results.hasMoreElements()) {//结果不为空且有值
                    SearchResult sr = results.next();	// 得到符合搜索条件的DN
                    Attributes attrs = sr.getAttributes();		// 得到符合条件的属性集
                    totalResults ++;
                    if (attrs != null) {
                        SysLadpUserModel ldapUserDO = LdapUserUtil.copy2LdapUserDO(attrs);
                        ldapUserDOList.add(ldapUserDO);
                    }
                }
                //cookie是一个字节数组，包含了通过PagedResultsControl下一次调用服务器时所需的信息
                cookie = parseControls(ctx.getResponseControls());
                ctx.setRequestControls(new Control[] { new PagedResultsControl(  pageSize, cookie, Control.CRITICAL) });
            } while ((cookie != null) && (cookie.length != 0));
            System.out.println("Total = " + totalResults);

        } catch (Exception e) {
            this.disConnect();
            throw new IllegalStateException("检索域用户时发生未知错误", e);
        }
        return ldapUserDOList;

    }

    /**
     * 通过调用AD域中数据对当前数据库中数据作更新
     * @return
     */
    @Override
    @Transactional
    public String linkLDAPRefreshAllInfo() {
        List<SysLadpUserModel> persons = this.searchLdapUser(null);
        // 1.仅仅筛选出l为changsha 或 changchun的用户数据，即filteredPersons
        List<SysLadpUserModel> filteredPersons = persons.stream()
                .filter(p -> {
                    String city = p.getL(); // 或 p.getCity()，看你在 model 中字段叫什么
                    return "Changsha".equalsIgnoreCase(city) || "Changchun".equalsIgnoreCase(city);
                })
                .collect(Collectors.toList());

        List<SysUserModel> newList = new ArrayList<SysUserModel>();

        for(SysLadpUserModel singleLadpUserModel : filteredPersons){
            SysUserModel sysUserModel = new SysUserModel();
            sysUserModel.setUserName(singleLadpUserModel.getName()); // 设置nt账号
            String password = Md5Util.md5(Constants.DEFAULT_PASSWORD);
            sysUserModel.setUserPassword(Md5Util.md5(password + Constants.SALT)); // 设置初始密码（实际上用不到）
            // 添加逻辑：所有审批人表中的数据角色不能被设置为2
            sysUserModel.setUserRoleId(2L); // 设置初始用户角色（与用户能看到的内容有关）

            String nickname = singleLadpUserModel.getDisplayName();
            if (nickname != null && nickname.contains("[offboarded]")) {
                nickname = nickname.replace("[offboarded] ", "").trim();
            }
            if (nickname != null) {
                sysUserModel.setUserNick(nickname); // 设置处理后的用户昵称
            } else {
                sysUserModel.setUserNick("");
            }

            sysUserModel.setAvatar(null); // 设置头像（空）
            sysUserModel.setEmail(singleLadpUserModel.getMail()); // 设置邮箱
            sysUserModel.setPhone(singleLadpUserModel.getTelephoneNumber()); // 设置手机号

            String whenCreated = singleLadpUserModel.getWhenCreated(); // 获取用户创建时间
            if (whenCreated != null && !whenCreated.isEmpty() && whenCreated.length() >= 8) {
                try {
                    // 提取前8位：即日期部分（yyyyMMdd）
                    String datePart = whenCreated.substring(0, 8);

                    // 拼接成 "yyyy-MM-dd" 格式
                    String formattedDate = datePart.substring(0, 4) + "-" + datePart.substring(4, 6) + "-" + datePart.substring(6, 8);

                    // 设置到 sysUserModel 中
                    sysUserModel.setCreateTime(formattedDate); // 设置用户创建时间
                } catch (Exception e) {
                    e.printStackTrace(); // 处理异常
                    sysUserModel.setCreateTime(null); // 转换失败时可以设置为 null 或其他值
                }
            } else {
                sysUserModel.setCreateTime(null); // 如果 whenCreated 为 null 或空，设置为 null
            }

            sysUserModel.setUpdateTime(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))); // 设置更新时间
            // 性别
            String title = singleLadpUserModel.getTitle();
            if (title != null) {
                if (title.equals("Mr.")) {
                    sysUserModel.setSex(1); // 男
                } else if (title.equals("Ms.")) {
                    sysUserModel.setSex(0); // 女
                } else {
                    sysUserModel.setSex(-1); // 未知
                }
            } else {
                sysUserModel.setSex(-1); // 未知
            }

            // 账号启停状态
            String uac = singleLadpUserModel.getUserAccountControl(); // 获取状态字段
            // 如果 uac 字符串不为空且可以转换为数字
            if (uac != null && !uac.isEmpty()) {
                try {
                    int uacValue = Integer.parseInt(uac);  // 将 uac 字符串转换为整数

                    // 使用按位与运算判断是否启用
                    if ((uacValue & 0x2) != 0) {
                        // 账号禁用 - 当第2位标志被设置时表示禁用
                        sysUserModel.setStatus(-1); // 禁用时设置状态为 -1
                    } else {
                        // 账号启用
                        sysUserModel.setStatus(0); // 启用时设置状态为 0
                    }
                } catch (NumberFormatException e) {
                    // 如果无法转换为整数，处理异常
                    sysUserModel.setStatus(-1);  // 如果 uac 格式不正确，则默认禁用
                    System.err.println("无法解析 UAC 值: " + uac);
                }
            } else {
                sysUserModel.setStatus(-1);  // 如果 uac 为空，默认禁用
            }

            sysUserModel.setDepartment(singleLadpUserModel.getDepartment()); // 设置部门

            // 判断singleLadpUserModel.displayName中是否含有FIXED-TERM，如果没有则直接赋值Name，否则从Manager里获取CN的数据
            String displayName = singleLadpUserModel.getDisplayName();
            String manager = singleLadpUserModel.getManager();
            String name = singleLadpUserModel.getName();

            if (displayName != null && displayName.contains("FIXED-TERM")) {
                String managerCN = (manager != null) ? extractCN(manager) : "";
                sysUserModel.setResponsibility(managerCN);
            } else {
                sysUserModel.setResponsibility(name != null ? name : "");
            }

            // 判断公司
            // l为Changchun直接赋值为SGCC
            // 其余情况下（1）department中包含SES时，赋值SES，其余情况赋值SGCS
            String l = singleLadpUserModel.getL();
            String department = singleLadpUserModel.getDepartment();

            if ("Changchun".equalsIgnoreCase(l)) {
                // 忽略大小写，判断是否是 Changchun
                sysUserModel.setCompany("SGCC");
            } else if (department != null && department.contains("SES")) {
                sysUserModel.setCompany("SES");
            } else {
                sysUserModel.setCompany("SGCS");
            }


            sysUserModel.setCostCenter(singleLadpUserModel.getDescription()); // 设置成本中心
            newList.add(sysUserModel);
        }

        // 对比当前数据库中的数据与新获取的数据的差别
        // 1.先前表里有且现在也有时：一致：跳过   不一样：update
        // 2.先前表里没有时：insert
        // 3.先前表里有但现在没有：delete
        //新增代码
        // 获取数据库中现有的所有LDAP用户
        List<SysUserModel> existingUsers = sysLadpMapper.getAllLadpUsers();
        
        // 创建一个Map来存储现有用户，以userName为键
        Map<String, SysUserModel> existingUserMap = new HashMap<>();
        for (SysUserModel user : existingUsers) {
            existingUserMap.put(user.getUserName(), user);
        }
        
        // 创建一个Map来存储新获取的用户，以userName为键
        Map<String, SysUserModel> newUserMap = new HashMap<>();
        for (SysUserModel user : newList) {
            newUserMap.put(user.getUserName(), user);
        }
        
        // 处理更新和新增
        List<SysUserModel> toUpdate = new ArrayList<>();
        List<SysUserModel> toInsert = new ArrayList<>();
        
        for (SysUserModel newUser : newList) {
            String userName = newUser.getUserName();
            if (existingUserMap.containsKey(userName)) {
                // 用户已存在，检查是否需要更新
                SysUserModel existingUser = existingUserMap.get(userName);
                // 自定义比较逻辑，忽略userPassword字段
                if (!compareUsersIgnoringPassword(newUser, existingUser)) {
                    // 数据不一致，需要更新
                    toUpdate.add(newUser);
                }
                // 如果一致，跳过
            } else {
                // 用户不存在，需要新增
                toInsert.add(newUser);
            }
        }
        
        // 处理删除
        List<SysUserModel> toDelete = new ArrayList<>();
        for (SysUserModel existingUser : existingUsers) {
            String userName = existingUser.getUserName();
            if (!newUserMap.containsKey(userName)) {
                // 现有用户不在新数据中，需要删除
                toDelete.add(existingUser);
            }
        }
        
        // 执行批量操作
        if (!toUpdate.isEmpty()) {
            // 使用单用户更新方法逐个更新用户
            for (SysUserModel user : toUpdate) {
                sysLadpMapper.updateLadpUser(user);
            }
        }
        
        if (!toInsert.isEmpty()) {
            sysLadpMapper.batchInsertLadpUsers(toInsert);
        }
        
        if (!toDelete.isEmpty()) {
            List<String> userNamesToDelete = toDelete.stream()
                .map(SysUserModel::getUserName)
                .collect(Collectors.toList());
            sysLadpMapper.batchDeleteLadpUsers(userNamesToDelete);
        }
        
        // 记录操作结果
        String instructResult = String.format("LDAP用户同步完成: 更新 %d 条, 新增 %d 条, 删除 %d 条",
                toUpdate.size(), toInsert.size(), toDelete.size());
        //新增代码

        // 更新user表： XJU1CS,TII2CS,YSG1CNG,YIL2CS,PEV2CS -> role = 1 (加if条件,存在时才进行修改)
        String[] arr = {"XJU1CS", "TII2CS", "YSG1CNG", "YIL2CS", "PEV2CS"};
        for(String singleArr : arr){
            SysUserModel user4Approver = sysUserMapper.getUserInfoByUserName(singleArr);
            // 用户非空时role=1(admin权限)
            if(user4Approver!=null){
                sysUserMapper.updateUserRole2AdminByName(singleArr);
            }
            // approver表： YSG1CNG -> ITApprover
            // 修改一下，前端留一个弹窗：是否以当前服务器存储的文件为基础进行审批人表更新（user表更新的同时，approver表也需要更新，因为userId可能变了）
            // 是 -> 删除所有的数据再插入
            // 否 -> 删除所有数据，并存储新文件，以新文件为基础插入
            if(singleArr.equals("YSG1CNG")){
                // 如果sysapprover表里本来就有，要删除掉
                Long userid = sysApproverMapper.getITApprover();
                if(userid!=null){
                    sysApproverMapper.deleteByNTAccount("YSG1CNG");
                }
                Long approverIdByUserId = sysApproverMapper.getApproverIdByUserId(user4Approver.getUserId());
                if(approverIdByUserId==null){
                    SysApproverModel sysApproverModel = new SysApproverModel();
                    sysApproverModel.setName(singleArr);
                    sysApproverModel.setEmail(user4Approver.getEmail());
                    sysApproverModel.setRole("ITApprover");
                    sysApproverModel.setCostCenter(user4Approver.getCostCenter());
                    sysApproverModel.setCreatedAt(user4Approver.getCreateTime());
                    sysApproverModel.setUpdatedAt(user4Approver.getUpdateTime());
                    sysApproverModel.setUserId(user4Approver.getUserId());
                    sysApproverMapper.insertITApprover(sysApproverModel);
                }
            }
        }
        // 更新一次，从approver表里拿数据，如果拿到的话，就遍历name来匹配user表里的username，并将userRoleId置为5（YSG1CNG除外）
        List<SysApproverModel> approvers = sysApproverMapper.getAllApprovers();
        if (approvers != null && !approvers.isEmpty()) {
            int roleUpdateCount = 0;
            for (SysApproverModel approver : approvers) {
                String approverName = approver.getName();
                // 跳过YSG1CNG，因为它已经设置为ITApprover (role=1)
                if ("YSG1CNG".equals(approverName)) {
                    continue;
                }
                
                SysUserModel user = sysUserMapper.getUserInfoByUserName(approverName);
                if (user != null) {
                    try {
                        // 将userRoleId设置为5
                        sysUserMapper.updateUserRoleById(user.getUserId(), 5L);
                        roleUpdateCount++;
                        log.info("从approver表更新用户角色: userId={}, name={}, roleId=5", user.getUserId(), approverName);
                    } catch (Exception e) {
                        log.error("更新用户角色失败: userId={}, name={}", user.getUserId(), approverName, e);
                    }
                } else {
                    log.warn("无法在用户表中找到审批人: {}", approverName);
                }
            }
            log.info("从approver表更新用户角色完成: 共更新 {} 条记录", roleUpdateCount);
        }

        return instructResult;
    }

    @Override
    @Transactional
    public boolean updateApprover(MultipartFile file) {
        try {
            // 使用POI读取Excel文件
            Workbook workbook;
            if (file.getOriginalFilename().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(file.getInputStream());
            } else if (file.getOriginalFilename().endsWith(".xls")) {
                workbook = new HSSFWorkbook(file.getInputStream());
            } else {
                throw new IllegalArgumentException("不支持的文件格式，请上传.xlsx或.xls格式的Excel文件");
            }

            // 获取Sheet1
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalStateException("Excel文件中不存在Sheet1");
            }

            // 获取列索引
            int costCtrIndex = -1;
            int approverIndex = -1;
            Row headerRow = sheet.getRow(0);
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String cellValue = cell.getStringCellValue().trim();
                    if ("Cost Ctr".equals(cellValue)) {
                        costCtrIndex = i;
                    } else if ("Select 1.approver".equals(cellValue)) {
                        approverIndex = i;
                    }
                }
            }

            if (costCtrIndex == -1 || approverIndex == -1) {
                throw new IllegalStateException("Excel文件中缺少必要的列：Cost Ctr 或 Select 1.approver");
            }

            // 处理数据行
            Map<String, String> excelDataMap = new HashMap<>(); // 存储Excel中的costCenter -> approver的映射
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // 从第二行开始（索引1）
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell costCtrCell = row.getCell(costCtrIndex);
                Cell approverCell = row.getCell(approverIndex);

                if (costCtrCell != null && approverCell != null) {
                    String costCtr = "";
                    // 处理不同类型的Cell
                    if (costCtrCell.getCellType() == CellType.NUMERIC) {
                        costCtr = String.valueOf((long) costCtrCell.getNumericCellValue());
                    } else {
                        costCtr = costCtrCell.getStringCellValue().trim();
                    }

                    // 处理Cost Ctr前四位为0的情况
                    if (costCtr.length() > 4 && costCtr.substring(0, 4).matches("0000")) {
                        costCtr = costCtr.substring(4);
                    }

                    String approver = "";
                    if (approverCell.getCellType() == CellType.NUMERIC) {
                        approver = String.valueOf((long) approverCell.getNumericCellValue());
                    } else {
                        approver = approverCell.getStringCellValue().trim();
                    }

                    if (!costCtr.isEmpty() && !approver.isEmpty()) {
                        excelDataMap.put(costCtr, approver);
                        log.info("读取到审批人数据: Cost Ctr={}, Approver={}", costCtr, approver);
                    }
                }
            }

            workbook.close();

            if (excelDataMap.isEmpty()) {
                log.warn("未从Excel中读取到有效的审批人数据");
                return false;
            }

            log.info("成功从Excel中读取了{}条审批人数据", excelDataMap.size());

            // 获取数据库中现有的审批人数据
            List<SysApproverModel> existingApprovers = sysApproverMapper.getAllApprovers();
            
            // 创建一个Map来存储现有审批人，以costCenter为键
            Map<String, SysApproverModel> existingApproverMap = new HashMap<>();
            for (SysApproverModel approver : existingApprovers) {
                existingApproverMap.put(approver.getCostCenter(), approver);
            }
            
            // 处理更新和新增
            List<SysApproverModel> toUpdate = new ArrayList<>();
            List<SysApproverModel> toInsert = new ArrayList<>();
            
            // 用于记录Excel中包含的costCenter，以便后续删除不在Excel中的记录
            Set<String> excelCostCenters = new HashSet<>(excelDataMap.keySet());
            
            // 处理Excel中的每一条记录
            for (Map.Entry<String, String> entry : excelDataMap.entrySet()) {
                String costCenter = entry.getKey();
                String approverName = entry.getValue();
                
                if (existingApproverMap.containsKey(costCenter)) {
                    // 已存在的costCenter - 检查name是否需要更新
                    SysApproverModel existingApprover = existingApproverMap.get(costCenter);
                    if (!approverName.equals(existingApprover.getName())) {
                        // 名称不同，需要更新
                        SysUserModel user = sysUserMapper.getUserInfoByUserName(approverName);
                        if (user != null) {
                            existingApprover.setName(approverName);
                            existingApprover.setEmail(user.getEmail());
                            existingApprover.setUserId(user.getUserId());
                            existingApprover.setUpdatedAt(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                            toUpdate.add(existingApprover);
                            log.info("需要更新的审批人: costCenter={}, 旧name={}, 新name={}", 
                                    costCenter, existingApprover.getName(), approverName);
                        } else {
                            log.warn("无法在用户表中找到审批人: {}", approverName);
                        }
                    }
                } else {
                    // 不存在的costCenter - 新增记录
                    SysUserModel user = sysUserMapper.getUserInfoByUserName(approverName);
                    if (user != null) {
                        SysApproverModel newApprover = new SysApproverModel();
                        newApprover.setName(approverName);
                        newApprover.setEmail(user.getEmail());
                        if(!"YSG1CNG".equals(user.getUserName())){
                            newApprover.setRole("manager"); // 默认设置为manager
                        } else {
                            newApprover.setRole("ITApprover");
                        }
                        newApprover.setCostCenter(costCenter);
                        newApprover.setCreatedAt(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        newApprover.setUpdatedAt(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        newApprover.setUserId(user.getUserId());
                        toInsert.add(newApprover);
                        log.info("需要新增的审批人: costCenter={}, name={}", costCenter, approverName);
                    } else {
                        log.warn("无法在用户表中找到审批人: {}", approverName);
                    }
                }
            }
            
            // 处理删除 - 找出数据库中存在但Excel中不存在的记录
            List<SysApproverModel> toDelete = new ArrayList<>();
            for (SysApproverModel approver : existingApprovers) {
                String costCenter = approver.getCostCenter();
                String role = approver.getRole();
                // 跳过role为admin和ITApprover的记录
                if ("admin".equals(role) || "ITApprover".equals(role)) {
                    log.info("跳过删除特殊角色审批人: costCenter={}, name={}, role={}", 
                            costCenter, approver.getName(), role);
                    continue;
                }
                
                if (!excelCostCenters.contains(costCenter)) {
                    toDelete.add(approver);
                    log.info("需要删除的审批人: costCenter={}, name={}", costCenter, approver.getName());
                }
            }
            
            // 执行批量操作
            int updateCount = 0;
            int insertCount = 0;
            int deleteCount = 0;
            
            if (!toUpdate.isEmpty()) {
                for (SysApproverModel approver : toUpdate) {
                    sysApproverMapper.updateApprover(approver);
                    updateCount++;
                }
            }
            
            if (!toInsert.isEmpty()) {
                for (SysApproverModel approver : toInsert) {
                    sysApproverMapper.insertApprover(approver);
                    insertCount++;
                }
            }
            
            if (!toDelete.isEmpty()) {
                for (SysApproverModel approver : toDelete) {
                    sysApproverMapper.deleteApproverById(approver.getApproverId());
                    deleteCount++;
                }
            }
            
            // 将所有manager角色的审批人对应的用户角色更新为5
            List<SysApproverModel> managerApprovers = sysApproverMapper.getApproversByRole("manager");
            if (managerApprovers != null && !managerApprovers.isEmpty()) {
                int userRoleUpdateCount = 0;
                for (SysApproverModel approver : managerApprovers) {
                    Long userId = approver.getUserId();
                    if (userId != null) {
                        try {
                            // 更新用户角色为5
                            sysUserMapper.updateUserRoleById(userId, 5L);
                            userRoleUpdateCount++;
                            log.info("更新用户角色: userId={}, name={}, roleId=5", userId, approver.getName());
                        } catch (Exception e) {
                            log.error("更新用户角色失败: userId={}, name={}", userId, approver.getName(), e);
                        }
                    }
                }
                log.info("审批人对应用户角色更新完成: 共更新 {} 条记录", userRoleUpdateCount);
            }
            
            log.info("审批人信息更新完成: 更新 {} 条, 新增 {} 条, 删除 {} 条", updateCount, insertCount, deleteCount);
            
            return true;
        } catch (Exception e) {
            log.error("处理审批人Excel文件时发生错误", e);
            return false;
        }
    }

    /*下次查询要用的cookie*/
    private static byte[] parseControls(Control[] controls) throws NamingException {
        byte[] cookie = null;
        if (controls != null) {
            for (int i = 0; i < controls.length; i++) {
                if (controls[i] instanceof PagedResultsResponseControl) {
                    PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[i];
                    cookie = prrc.getCookie();
                    System.out.println(">>Next Page \n");
                }
            }
        }
        return (cookie == null) ? new byte[0] : cookie;
    }

    // 提取CN后的字符串
    public static String extractCN(String dn) {
        if (dn != null && dn.startsWith("CN=")) {
            String[] parts = dn.split(",");
            return parts[0].substring(3); // 去掉 CN= 前缀
        }
        return null;
    }

    // 自定义比较逻辑，忽略userPassword字段
    private boolean compareUsersIgnoringPassword(SysUserModel user1, SysUserModel user2) {
        if (user1 == null || user2 == null) {
            return user1 == user2;
        }

        // 比较用户名（必须相等）
        if (!Objects.equals(user1.getUserName(), user2.getUserName())) {
            return false;
        }

        // 比较用户角色ID
        if (!Objects.equals(user1.getUserRoleId(), user2.getUserRoleId())) {
            return false;
        }

        // 比较昵称
        if (!Objects.equals(user1.getUserNick(), user2.getUserNick())) {
            return false;
        }

        // 比较头像
        if (!Objects.equals(user1.getAvatar(), user2.getAvatar())) {
            return false;
        }

        // 比较邮箱
        if (!Objects.equals(user1.getEmail(), user2.getEmail())) {
            return false;
        }

        // 比较电话
        if (!Objects.equals(user1.getPhone(), user2.getPhone())) {
            return false;
        }

        // 比较创建时间
        if (!Objects.equals(user1.getCreateTime(), user2.getCreateTime())) {
            return false;
        }

        // 比较更新时间
        if (!Objects.equals(user1.getUpdateTime(), user2.getUpdateTime())) {
            return false;
        }

        // 比较性别
        if (!Objects.equals(user1.getSex(), user2.getSex())) {
            return false;
        }

        // 比较状态
        if (!Objects.equals(user1.getStatus(), user2.getStatus())) {
            return false;
        }

        // 比较部门
        if (!Objects.equals(user1.getDepartment(), user2.getDepartment())) {
            return false;
        }

        // 比较责任人
        if (!Objects.equals(user1.getResponsibility(), user2.getResponsibility())) {
            return false;
        }

        // 比较公司
        if (!Objects.equals(user1.getCompany(), user2.getCompany())) {
            return false;
        }

        // 比较成本中心
        if (!Objects.equals(user1.getCostCenter(), user2.getCostCenter())) {
            return false;
        }

        // 所有比较都相等
        return true;
    }
}
