package ink.usr.admin.controller;

import ink.usr.admin.dao.DTO.SysMaintenanceOrderDTO;
import ink.usr.admin.dao.VO.SysMaintenanceOrderVO;
import ink.usr.admin.service.SysMaintenanceOrderService;
import ink.usr.common.model.mysql.SysMaintenanceOrderModel;
import ink.usr.common.core.domain.Res;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 维修订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/sysMaintenanceOrder")
public class SysMaintenanceOrderController {

    @Autowired
    private SysMaintenanceOrderService sysMaintenanceOrderService;

    /**
     * 获取维修订单列表
     */
    @RequestMapping("/getOrderList")
    public Res getOrderList(@RequestParam Map<String, Object> params) {
        try {
            log.info("获取维修订单列表，参数：{}", params);
            
            // 构建查询参数
            SysMaintenanceOrderDTO dto = new SysMaintenanceOrderDTO();
            dto.setCiName((String) params.get("ciName"));
            dto.setFixCategory((String) params.get("fixCategory"));
            dto.setOrderStatus((String) params.get("orderStatus"));
            dto.setApplicant((String) params.get("applicant"));
            dto.setCostCenter((String) params.get("costCenter"));
            dto.setCompany((String) params.get("company"));
            
            // 分页参数
            String pageNumStr = (String) params.get("pageNum");
            String pageSizeStr = (String) params.get("pageSize");
            if (pageNumStr != null && pageSizeStr != null) {
                dto.setPageNum(Integer.parseInt(pageNumStr));
                dto.setPageSize(Integer.parseInt(pageSizeStr));
            }
            
            // 查询维修订单列表
            List<Map<String, Object>> list = sysMaintenanceOrderService.getOrderList(dto);
            int total = sysMaintenanceOrderService.getOrderCount(dto);
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", total);
            return Res.success().setData(result);
        } catch (Exception e) {
            log.error("获取维修订单列表失败", e);
            return Res.error("获取维修订单列表失败");
        }
    }

    /**
     * 维修审批
     */
    @RequestMapping("/approveOrder")
    public Res approveOrder(@RequestBody Map<String, Object> params) {
        try {
            log.info("维修审批，参数：{}", params);
            
            Long orderId = Long.parseLong(params.get("orderId").toString());
            String status = (String) params.get("status");
            String reason = (String) params.get("reason");
            
            boolean result = sysMaintenanceOrderService.approveOrder(orderId, status, reason);
            
            if (result) {
                return Res.success("审批提交成功");
            } else {
                return Res.error("审批提交失败");
            }
        } catch (Exception e) {
            log.error("维修审批失败", e);
            return Res.error("维修审批失败");
        }
    }

    /**
     * 维修操作
     */
    @RequestMapping("/operateMaintenance")
    public Res operateMaintenance(@RequestBody Map<String, Object> params) {
        try {
            log.info("维修操作，参数：{}", params);
            
            Long orderId = Long.parseLong(params.get("orderId").toString());
            String action = (String) params.get("action");
            String result = (String) params.get("result");
            Double cost = params.get("cost") != null ? Double.parseDouble(params.get("cost").toString()) : 0.0;
            
            boolean success = sysMaintenanceOrderService.operateMaintenance(orderId, action, result, cost);
            
            if (success) {
                return Res.success("维修操作提交成功");
            } else {
                return Res.error("维修操作提交失败");
            }
        } catch (Exception e) {
            log.error("维修操作失败", e);
            return Res.error("维修操作失败");
        }
    }

    /**
     * 获取维修订单详情
     */
    @RequestMapping("/getOrderDetail")
    public Res getOrderDetail(@RequestParam Long orderId) {
        try {
            log.info("获取维修订单详情，订单ID：{}", orderId);
            
            SysMaintenanceOrderVO order = sysMaintenanceOrderService.getOrderById(orderId);
            
            if (order != null) {
                return Res.success().setData(order);
            } else {
                return Res.error("维修订单不存在");
            }
        } catch (Exception e) {
            log.error("获取维修订单详情失败", e);
            return Res.error("获取维修订单详情失败");
        }
    }

    /**
     * 更新维修订单状态
     */
    @RequestMapping("/updateOrderStatus")
    public Res updateOrderStatus(@RequestBody Map<String, Object> params) {
        try {
            log.info("更新维修订单状态，参数：{}", params);
            
            Long orderId = Long.parseLong(params.get("orderId").toString());
            String orderStatus = (String) params.get("orderStatus");
            
            boolean result = sysMaintenanceOrderService.updateOrderStatus(orderId, orderStatus);
            
            if (result) {
                return Res.success("订单状态更新成功");
            } else {
                return Res.error("订单状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新维修订单状态失败", e);
            return Res.error("更新维修订单状态失败");
        }
    }

    /**
     * 完成维修
     */
    @RequestMapping("/completeMaintenance")
    public Res completeMaintenance(@RequestBody Map<String, Object> params) {
        try {
            log.info("完成维修，参数：{}", params);
            
            Long orderId = Long.parseLong(params.get("orderId").toString());
            String maintenanceAction = (String) params.get("maintenanceAction");
            String maintenanceResult = (String) params.get("maintenanceResult");
            Double maintenanceCost = params.get("maintenanceCost") != null ? Double.parseDouble(params.get("maintenanceCost").toString()) : 0.0;
            String maintenanceRemark = (String) params.get("maintenanceRemark");
            String orderStatus = (String) params.get("orderStatus");
            
            boolean result = sysMaintenanceOrderService.completeMaintenance(orderId, maintenanceAction, maintenanceResult, maintenanceCost, maintenanceRemark, orderStatus);
            
            if (result) {
                return Res.success("维修完成，订单状态已更新");
            } else {
                return Res.error("维修提交失败");
            }
        } catch (Exception e) {
            log.error("完成维修失败", e);
            return Res.error("完成维修失败");
        }
    }
} 