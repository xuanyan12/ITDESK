package ink.usr.admin.service;

import ink.usr.admin.dao.DTO.SysAutoLoginUserDto;
import ink.usr.common.model.mysql.SysLadpUserModel;

import java.util.List;

public interface SysLadpService {

    void initConnect();

    void disConnect();

    void connect(String username, String password);

    SysLadpUserModel authenticate(String loginName, String password);

    List<SysLadpUserModel> searchLdapUser(String username);

    boolean linkLDAPRefreshAllInfo();
}
