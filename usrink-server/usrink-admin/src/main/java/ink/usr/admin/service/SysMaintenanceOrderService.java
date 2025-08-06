package ink.usr.admin.service;

import ink.usr.admin.dao.DTO.SysMaintenanceOrderDTO;
import ink.usr.admin.dao.VO.SysMaintenanceOrderVO;
import ink.usr.common.model.mysql.SysMaintenanceOrderModel;

import java.util.List;
import java.util.Map;

public interface SysMaintenanceOrderService {
    
    /**
     * 创建维修订单
     * @param orderDTO 维修订单DTO
     * @return 创建结果
     */
    String createOrder(SysMaintenanceOrderDTO orderDTO);
    
    /**
     * 根据ID查询维修订单
     * @param orderId 订单ID
     * @return 维修订单VO
     */
    SysMaintenanceOrderVO getOrderById(Long orderId);
    
    /**
     * 根据维修申请ID查询维修订单
     * @param maintenanceId 维修申请ID
     * @return 维修订单VO
     */
    SysMaintenanceOrderVO getOrderByMaintenanceId(Long maintenanceId);
    
    /**
     * 查询所有维修订单
     * @return 维修订单VO列表
     */
    List<SysMaintenanceOrderVO> getAllOrders();
    
    /**
     * 根据状态查询维修订单
     * @param orderStatus 订单状态
     * @return 维修订单VO列表
     */
    List<SysMaintenanceOrderVO> getOrdersByStatus(String orderStatus);
    
    /**
     * 根据审批人查询维修订单
     * @param approverId 审批人ID
     * @return 维修订单VO列表
     */
    List<SysMaintenanceOrderVO> getOrdersByApprover(Long approverId);
    
    /**
     * 更新维修订单
     * @param orderModel 维修订单模型
     * @return 更新结果
     */
    boolean updateOrder(SysMaintenanceOrderModel orderModel);
    
    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param orderStatus 订单状态
     * @return 更新结果
     */
    boolean updateOrderStatus(Long orderId, String orderStatus);
    

    
    /**
     * 删除维修订单
     * @param orderId 订单ID
     * @return 删除结果
     */
    boolean deleteOrder(Long orderId);
    
    /**
     * 生成订单编号
     * @return 订单编号
     */
    String generateOrderNumber();
    
    /**
     * 根据订单类型获取订单类型名称
     * @param orderType 订单类型
     * @return 订单类型名称
     */
    String getOrderTypeName(String orderType);
    
    /**
     * 处理自动审批逻辑
     * @param orderId 订单ID
     * @return 处理结果
     */
    String processAutoApproval(Long orderId);
    
    /**
     * 获取维修订单列表
     * @param dto 查询条件
     * @return 维修订单列表
     */
    List<Map<String, Object>> getOrderList(SysMaintenanceOrderDTO dto);
    
    /**
     * 获取维修订单总数
     * @param dto 查询条件
     * @return 总数
     */
    int getOrderCount(SysMaintenanceOrderDTO dto);
    
    /**
     * 维修审批
     * @param orderId 订单ID
     * @param status 审批状态
     * @param reason 审批理由
     * @return 审批结果
     */
    boolean approveOrder(Long orderId, String status, String reason);
    
    /**
     * 维修操作
     * @param orderId 订单ID
     * @param action 维修操作
     * @param result 维修结果
     * @param cost 维修费用
     * @return 操作结果
     */
    boolean operateMaintenance(Long orderId, String action, String result, Double cost);
    
    /**
     * 完成维修
     * @param orderId 订单ID
     * @param maintenanceAction 维修操作
     * @param maintenanceResult 维修结果
     * @param maintenanceCost 维修费用
     * @param maintenanceRemark 维修备注
     * @param orderStatus 订单状态
     * @return 操作结果
     */
    boolean completeMaintenance(Long orderId, String maintenanceAction, String maintenanceResult, Double maintenanceCost, String maintenanceRemark, String orderStatus);
} 