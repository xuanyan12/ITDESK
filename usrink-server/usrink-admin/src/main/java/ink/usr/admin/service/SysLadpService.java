package ink.usr.admin.service;

import ink.usr.admin.dao.DTO.SysAutoLoginUserDto;
import ink.usr.common.model.mysql.SysLadpUserModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SysLadpService {

    void initConnect();

    void disConnect();

    void connect(String username, String password);

    SysLadpUserModel authenticate(String loginName, String password);

    List<SysLadpUserModel> searchLdapUser(String username);

    String linkLDAPRefreshAllInfo();

    boolean updateApprover(MultipartFile file);
    
    /**
     * 获取或重新生成用户的备用密码
     * @param userName 用户名
     * @return 明文UUID备用密码
     */
    String getUserBackupPassword(String userName);
}
