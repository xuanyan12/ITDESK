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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        HashEnv.put("com.sun.jndi.ldap.connect.timeout", "3000");							// 连接超时设置为3秒
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

    @Override
    @Transactional
    public boolean linkLDAPRefreshAllInfo() {
        List<SysLadpUserModel> persons = this.searchLdapUser(null);
        // 1.仅仅筛选出l为changsha 或 changchun的用户数据，即filteredPersons
        List<SysLadpUserModel> filteredPersons = persons.stream()
                .filter(p -> {
                    String city = p.getL(); // 或 p.getCity()，看你在 model 中字段叫什么
                    return "Changsha".equalsIgnoreCase(city) || "Changchun".equalsIgnoreCase(city);
                })
                .collect(Collectors.toList());

        List newList = new ArrayList<SysUserModel>();

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
        boolean isDeleted = sysLadpMapper.deleteAllInfos();
        boolean isInserted = sysLadpMapper.linkLDAPRefreshAllInfo(newList);

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
        return isDeleted && isInserted;
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
}
