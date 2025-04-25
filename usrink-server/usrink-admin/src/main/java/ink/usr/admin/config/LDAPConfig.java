package ink.usr.admin.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Getter
@Configuration
@PropertySource("classpath:ldap.properties")   //导入配置文件
public class LDAPConfig {

    @Value("${laps}")
    private String laps;
    //数字证书
    @Value("${trustStore}")
    private String trustStore;
    //数字证书密码
    @Value("${trustStorePassword}")
    private String trustStorePassword;
    @Value("${ntUserName}")
    private String ntUserName;
    @Value("${ntPassword}")
    private String ntPassword;

}
