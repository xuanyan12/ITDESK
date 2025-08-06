package ink.usr.admin.controller;

import com.github.pagehelper.Page;
import ink.usr.admin.dao.DTO.SysMaintenanceApprovalDTO;
import ink.usr.admin.dao.VO.SysMaintenanceApprovalVO;
import ink.usr.admin.service.SysMaintenanceApprovalService;
import ink.usr.admin.service.SysApproverService;
import ink.usr.admin.service.SysUserService;
import ink.usr.common.core.domain.Dict;
import ink.usr.common.core.domain.Res;
import ink.usr.common.core.utils.PageUtil;
import ink.usr.framework.shiro.domain.ShiroUserInfo;
import ink.usr.framework.shiro.utils.ShiroUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/sysMaintenanceApproval")
public class SysMaintenanceApprovalController {

    @Autowired
    private SysMaintenanceApprovalService sysMaintenanceApprovalService;

    @Autowired
    private SysApproverService sysApproverService;

    @Autowired
    private SysUserService sysUserService;

    /**
     * 创建维修审批
     * @param approvalDTO 维修审批DTO
     * @return 创建结果
     */
    @RequestMapping("/createApproval")
    public Res createApproval(@RequestBody SysMaintenanceApprovalDTO approvalDTO) {
        try {
            boolean result = sysMaintenanceApprovalService.createApproval(approvalDTO);
            if (result) {
                return Res.success("维修审批创建成功");
            } else {
                return Res.error("维修审批创建失败");
            }
        } catch (Exception e) {
            log.error("创建维修审批失败", e);
            return Res.error("创建维修审批失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询维修审批
     * @param approvalId 审批ID
     * @return 维修审批信息
     */
    @RequestMapping("/getApprovalById")
    public Res getApprovalById(@RequestParam("approvalId") Long approvalId) {
        try {
            SysMaintenanceApprovalVO approvalVO = sysMaintenanceApprovalService.getApprovalById(approvalId);
            if (approvalVO != null) {
                return Res.success(approvalVO);
            } else {
                return Res.error("维修审批不存在");
            }
        } catch (Exception e) {
            log.error("查询维修审批失败", e);
            return Res.error("查询维修审批失败: " + e.getMessage());
        }
    }

    /**
     * 根据订单ID查询维修审批
     * @param orderId 订单ID
     * @return 维修审批列表
     */
    @RequestMapping("/getApprovalsByOrderId")
    public Res getApprovalsByOrderId(@RequestParam("orderId") Long orderId) {
        try {
            List<SysMaintenanceApprovalVO> approvalList = sysMaintenanceApprovalService.getApprovalsByOrderId(orderId);
            return Res.success(approvalList);
        } catch (Exception e) {
            log.error("根据订单ID查询维修审批失败", e);
            return Res.error("查询维修审批失败: " + e.getMessage());
        }
    }

    /**
     * 根据审批人查询维修审批
     * @param approvalType 审批类型：0-待办审批，1-审批历史
     * @return 维修审批列表
     */
    @RequestMapping("/getApprovalsByApprover")
    public Res getApprovalsByApprover(@RequestParam(value = "approvalType", defaultValue = "0") Long approvalType,
                                    @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                    @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        try {
            ShiroUserInfo shiroUserInfo = ShiroUtil.getShiroUserInfo();
            if (shiroUserInfo == null) {
                return Res.error("用户未登录或会话已过期");
            }
            
            Long userId = shiroUserInfo.getUserId();
            // 通过userId获得approverId
            String costCenter = sysUserService.getUserInfoByUserName(sysUserService.getNameByUserId(userId)).getCostCenter();
            Long approverId = sysApproverService.getApproverIdUseCostCenter(userId, costCenter);
            
            Page<Object> pages = PageUtil.startPage(pageNum, pageSize);
            List<SysMaintenanceApprovalVO> approvalList = sysMaintenanceApprovalService.getApprovalsWithOrderDetailsByApproverId(approverId, approvalType);
            
            // 添加响应数据日志以确认字段
            for (SysMaintenanceApprovalVO vo : approvalList) {
                log.info("返回审批数据 - approvalId: {}, status1Signal: {}, approvalStatus: {}, responsiblePerson: {}", 
                    vo.getApprovalId(), vo.getStatus1Signal(), vo.getApprovalStatus(), vo.getResponsiblePerson());
            }
            
            Dict result = Dict.create()
                    .set("list", approvalList)
                    .set("total", pages.getTotal());
            return Res.success(result);
        } catch (Exception e) {
            log.error("根据审批人查询维修审批失败", e);
            return Res.error("查询维修审批失败: " + e.getMessage());
        }
    }

    /**
     * 根据订单ID和审批级别查询维修审批
     * @param orderId 订单ID
     * @param approvalLevel 审批级别
     * @return 维修审批信息
     */
    @RequestMapping("/getApprovalByOrderIdAndLevel")
    public Res getApprovalByOrderIdAndLevel(@RequestParam("orderId") Long orderId,
                                           @RequestParam("approvalLevel") Integer approvalLevel) {
        try {
            SysMaintenanceApprovalVO approvalVO = sysMaintenanceApprovalService.getApprovalByOrderIdAndLevel(orderId, approvalLevel);
            if (approvalVO != null) {
                return Res.success(approvalVO);
            } else {
                return Res.error("维修审批不存在");
            }
        } catch (Exception e) {
            log.error("根据订单ID和审批级别查询维修审批失败", e);
            return Res.error("查询维修审批失败: " + e.getMessage());
        }
    }

    /**
     * 查询所有维修审批
     * @return 维修审批列表
     */
    @RequestMapping("/getAllApprovals")
    public Res getAllApprovals() {
        try {
            Page<Object> pages = PageUtil.startPage();
            List<SysMaintenanceApprovalVO> approvalList = sysMaintenanceApprovalService.getAllApprovals();
            
            Dict result = Dict.create()
                    .set("list", approvalList)
                    .set("total", pages.getTotal());
            return Res.success(result);
        } catch (Exception e) {
            log.error("查询所有维修审批失败", e);
            return Res.error("查询维修审批失败: " + e.getMessage());
        }
    }

    /**
     * 根据状态查询维修审批
     * @param approvalStatus 审批状态
     * @return 维修审批列表
     */
    @RequestMapping("/getApprovalsByStatus")
    public Res getApprovalsByStatus(@RequestParam("approvalStatus") String approvalStatus) {
        try {
            Page<Object> pages = PageUtil.startPage();
            List<SysMaintenanceApprovalVO> approvalList = sysMaintenanceApprovalService.getApprovalsByStatus(approvalStatus);
            
            Dict result = Dict.create()
                    .set("list", approvalList)
                    .set("total", pages.getTotal());
            return Res.success(result);
        } catch (Exception e) {
            log.error("根据状态查询维修审批失败", e);
            return Res.error("查询维修审批失败: " + e.getMessage());
        }
    }

    /**
     * 处理审批
     * @param orderId 订单ID
     * @param approvalLevel 审批级别
     * @param approvalStatus 审批状态
     * @param approvalReason 审批理由
     * @return 处理结果
     */
    @RequestMapping("/processApproval")
    public Res processApproval(@RequestParam("orderId") Long orderId,
                              @RequestParam("approvalLevel") Integer approvalLevel,
                              @RequestParam("approvalStatus") String approvalStatus,
                              @RequestParam(value = "approvalReason", required = false) String approvalReason) {
        try {
            ShiroUserInfo shiroUserInfo = ShiroUtil.getShiroUserInfo();
            if (shiroUserInfo == null) {
                return Res.error("用户未登录或会话已过期");
            }
            
            String result = sysMaintenanceApprovalService.processApproval(orderId, approvalLevel, approvalStatus, approvalReason);
            if (result.contains("成功") || result.contains("通过") || result.contains("等待")) {
                return Res.success(result);
            } else {
                return Res.error(result);
            }
        } catch (Exception e) {
            log.error("处理审批失败", e);
            return Res.error("处理审批失败: " + e.getMessage());
        }
    }

    /**
     * 删除维修审批
     * @param approvalId 审批ID
     * @return 删除结果
     */
    @RequestMapping("/deleteApproval")
    public Res deleteApproval(@RequestParam("approvalId") Long approvalId) {
        try {
            boolean result = sysMaintenanceApprovalService.deleteApproval(approvalId);
            if (result) {
                return Res.success("维修审批删除成功");
            } else {
                return Res.error("维修审批删除失败");
            }
        } catch (Exception e) {
            log.error("删除维修审批失败", e);
            return Res.error("删除维修审批失败: " + e.getMessage());
        }
    }

    /**
     * 根据订单ID删除维修审批
     * @param orderId 订单ID
     * @return 删除结果
     */
    @RequestMapping("/deleteApprovalsByOrderId")
    public Res deleteApprovalsByOrderId(@RequestParam("orderId") Long orderId) {
        try {
            boolean result = sysMaintenanceApprovalService.deleteApprovalsByOrderId(orderId);
            if (result) {
                return Res.success("维修审批删除成功");
            } else {
                return Res.error("维修审批删除失败");
            }
        } catch (Exception e) {
            log.error("根据订单ID删除维修审批失败", e);
            return Res.error("删除维修审批失败: " + e.getMessage());
        }
    }

    /**
     * 获取审批级别列表
     * @return 审批级别列表
     */
    @RequestMapping("/getApprovalLevels")
    public Res getApprovalLevels() {
        try {
            Dict result = Dict.create()
                    .set("levels", new Object[][]{
                            {1, "成本负责人"},
                            {2, "IT负责人"}
                    });
            return Res.success(result);
        } catch (Exception e) {
            log.error("获取审批级别列表失败", e);
            return Res.error("获取审批级别列表失败: " + e.getMessage());
        }
    }
} 