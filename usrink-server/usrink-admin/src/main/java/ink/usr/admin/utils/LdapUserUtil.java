package ink.usr.admin.utils;

import ink.usr.common.model.mysql.SysLadpUserModel;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.ArrayList;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

public class LdapUserUtil {
    /* Attributes --> SysLadpUserModel */
    public static SysLadpUserModel copy2LdapUserDO (Attributes userAttrs) throws NamingException {
        SysLadpUserModel ldapUserDO = new SysLadpUserModel();

        for (NamingEnumeration ne = userAttrs.getAll(); ne.hasMore();) {
            // 得到下一个属性
            Attribute Attr = (Attribute) ne.next();
            String key = Attr.getID().toString();
            // 读取属性值
            for (NamingEnumeration e = Attr.getAll(); e.hasMore();) {
                String val = e.next().toString();

                if(key.equals("name")){
                    ldapUserDO.setName(val);
                }
                if(key.equals("department")){
                    ldapUserDO.setDepartment(val);
                }
                if(key.equals("mail")){
                    ldapUserDO.setMail(val);
                }
                if(key.equals("displayName")){
                    ldapUserDO.setDisplayName(val);
                }
                if(key.equals("description")){
                    ldapUserDO.setDescription(val);
                }
                if(key.equals("telephoneNumber")){
                    ldapUserDO.setTelephoneNumber(val);
                }
                if(key.equals("title")){
                    ldapUserDO.setTitle(val);
                }
                if(key.equals("manager")){
                    ldapUserDO.setManager(val);
                }
                if(key.equals("l")){
                    ldapUserDO.setL(val);
                }
                if(key.equals("userAccountControl")){
                    ldapUserDO.setUserAccountControl(val);
                }
                if(key.equals("whenCreated")){
                    ldapUserDO.setWhenCreated(val);
                }
            }
        }
        return ldapUserDO;
    }
}
