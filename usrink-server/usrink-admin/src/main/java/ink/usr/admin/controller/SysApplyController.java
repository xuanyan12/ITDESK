package ink.usr.admin.controller;

import com.github.pagehelper.Page;
import ink.usr.admin.dao.DTO.SysApplyDTO;
import ink.usr.admin.service.SysApplyService;
import ink.usr.common.core.constants.Constants;
import ink.usr.common.core.domain.Dict;
import ink.usr.common.core.domain.Res;
import ink.usr.common.core.utils.DateUtil;
import ink.usr.common.core.utils.PageUtil;
import ink.usr.common.core.utils.ServletUtil;
import ink.usr.common.model.mysql.SysApprovalRequestModel;
import ink.usr.framework.shiro.domain.ShiroUserInfo;
import ink.usr.framework.shiro.utils.ShiroUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/sysApply")
public class SysApplyController {

    @Autowired
    private SysApplyService sysApplyService;

    /**
     * 根据id获取对应的设备申请列表
     * @return
     */
    @RequestMapping("/getApplyList")
    public Res getApplyList(){
        Page<Object> pages = PageUtil.startPage();
        ShiroUserInfo shiroUserInfo = ShiroUtil.getShiroUserInfo();
        Long userId = shiroUserInfo.getUserId();
        List<SysApprovalRequestModel> applyList = sysApplyService.getApplyList(userId);

        Dict result = Dict.create()
                .set("list", applyList)
                .set("total", pages.getTotal());
        return Res.success(result);
    }

    /**
     * 提交设备申请，并生成审批流程
     * @param sysApprovalRequestModel
     * @return
     */
    @RequestMapping("/submitApply")
    public Res submitApply(@RequestBody SysApprovalRequestModel sysApprovalRequestModel){

        //  1.先创建一个request
        sysApplyService.addApply(sysApprovalRequestModel);
        //  2.创建一个属于部门leader的一级工作流
        //  3.再创建一个属于IT部门审批者的二级工作流
        //  4.再通过邮件发送链接给对应的审批者进行审批(一级工作流审批者),一级通过后触发邮件给二级审批者
        return Res.success();
    }
}
