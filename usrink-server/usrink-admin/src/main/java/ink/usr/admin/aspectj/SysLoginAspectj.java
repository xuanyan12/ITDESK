package ink.usr.admin.aspectj;

import ink.usr.common.core.utils.*;
import ink.usr.common.interfaces.admin.ISysLogLoginService;
import ink.usr.common.model.mysql.SysLogLoginModel;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class SysLoginAspectj {

    /**
     * 登录计时
     */
    private static final ThreadLocal<Long> Login_use_time = new ThreadLocal<>();


    /**
     * 登录切入点
     */
    @Pointcut("execution(public * ink.usr.admin.controller.SysLoginController.login(..))")
    public void loginPoint() {

    }

    /**
     * 登录前置通知
     */
    @Before("loginPoint()")
    public void doBeforeLogin() {
        Login_use_time.set(System.currentTimeMillis());
    }

    /**
     * 登录后置通知
     *
     * @param joinPoint 切点
     * @param result    返回结果
     */
    @AfterReturning(pointcut = "loginPoint()", returning = "result")
    public void doAfterLogin(JoinPoint joinPoint, Object result) {
        long useTime = System.currentTimeMillis() - Login_use_time.get();
        Login_use_time.remove();

        String jsonResult = JsonUtil.toJson(result);
        jsonResult = StringUtil.subString(jsonResult, 0, 1000);

        recordLoginInfo(useTime, 0, jsonResult);
    }

    /**
     * 登录异常通知
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(pointcut = "loginPoint()", throwing = "e")
    public void doAfterThrowingLogin(JoinPoint joinPoint, Throwable e) {
        long useTime = System.currentTimeMillis() - Login_use_time.get();
        Login_use_time.remove();

        String result = e.getMessage();
        result = StringUtil.subString(result, 0, 1000);

        recordLoginInfo(useTime, -1, result);
    }

    /**
     * 判断是否为本地或内网IP地址
     */
    private boolean isLocalOrPrivateIP(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return true;
        }
        
        // 本地回环地址
        if (ip.equals("127.0.0.1") || ip.equals("localhost") || ip.equals("::1") || ip.equals("0:0:0:0:0:0:0:1")) {
            return true;
        }
        
        // 私有网络地址段
        if (ip.startsWith("192.168.") || ip.startsWith("10.")) {
            return true;
        }
        
        // 172.16.0.0 - 172.31.255.255
        if (ip.startsWith("172.")) {
            try {
                String[] parts = ip.split("\\.");
                if (parts.length >= 2) {
                    int secondOctet = Integer.parseInt(parts[1]);
                    if (secondOctet >= 16 && secondOctet <= 31) {
                        return true;
                    }
                }
            } catch (NumberFormatException e) {
                // 解析失败，当作内网处理
                return true;
            }
        }
        
        return false;
    }

    /**
     * 记录登录日志
     *
     * @param costTime 接口耗时
     * @param status   接口状态，0成功，-1失败
     * @param result   返回结果
     */
    private void recordLoginInfo(Long costTime, int status, String result) {
        SysLogLoginModel sysLogLoginModel = new SysLogLoginModel();
        // 接口耗时
        sysLogLoginModel.setCostTime(costTime);
        // 接口状态，0成功，-1失败
        sysLogLoginModel.setStatus(status);
        // 返回结果
        sysLogLoginModel.setResult(result);

        HttpServletRequest request = ServletUtil.getHttpServletRequest();
        // 请求的IP地址
        sysLogLoginModel.setIpAddr(IpUtil.getClientIpAddr(request));

        String userAgent = request.getHeader("User-Agent");
        // 获取浏览器类型
        sysLogLoginModel.setBrowser(UserAgentUtil.getBrowser(userAgent));
        // 获取操作系统
        sysLogLoginModel.setOs(UserAgentUtil.getOs(userAgent));
        // 请求方法
        sysLogLoginModel.setReqType(request.getMethod());
        // 请求URI
        sysLogLoginModel.setReqUrl(request.getRequestURI());
        
        // 请求参数 - 过滤敏感信息
        Map<String, Object> parameterMap = ServletUtil.getAllParameters();
        // 创建新的Map来存储过滤后的参数
        Map<String, Object> filteredParams = new HashMap<>(parameterMap);
        // 移除密码字段，避免在日志中泄露敏感信息
        filteredParams.remove("password");
        sysLogLoginModel.setReqParam(JsonUtil.toJson(filteredParams));
        
        // 登录账号
        sysLogLoginModel.setUserName(ServletUtil.getParameter("userName"));

        // 执行异步任务
        AsyncUtil.executeAsyncTask(() -> {
            // 判断是否为本地或内网IP，如果是则跳过地理位置查询
            if (isLocalOrPrivateIP(sysLogLoginModel.getIpAddr())) {
                sysLogLoginModel.setLocation("内网地址");
                log.debug("检测到内网IP: {}, 跳过地理位置查询", sysLogLoginModel.getIpAddr());
            } else {
                // 外网IP，尝试查询地理位置
                try {
                    // 使用HTTP协议避免SSL证书问题
                    String locationJsonString = HttpUtil.get("http://whois.pconline.com.cn/ipJson.jsp?json=true&ip=" + sysLogLoginModel.getIpAddr());
                    if (locationJsonString != null && !locationJsonString.trim().isEmpty()) {
                        try {
                            Map<String, Object> locationMap = JsonUtil.parse(locationJsonString, Map.class);
                            Object addr = locationMap.get("addr");
                            if (addr != null) {
                                sysLogLoginModel.setLocation(addr.toString());
                            } else {
                                sysLogLoginModel.setLocation("未知地区");
                            }
                        } catch (Exception parseException) {
                            log.warn("解析地理位置信息失败: {}", parseException.getMessage());
                            sysLogLoginModel.setLocation("地理位置解析失败");
                        }
                    } else {
                        sysLogLoginModel.setLocation("地理位置查询无响应");
                    }
                } catch (Exception e) {
                    log.warn("查询IP地理位置失败: IP={}, 错误={}", sysLogLoginModel.getIpAddr(), e.getMessage());
                    sysLogLoginModel.setLocation("地理位置查询失败");
                }
            }

            // 保存登录日志
            try {
                SpringBeanUtil.getBean(ISysLogLoginService.class).insertSysLogLogin(sysLogLoginModel);
            } catch (Exception e) {
                log.error("保存登录日志失败", e);
            }
        });
    }

}
