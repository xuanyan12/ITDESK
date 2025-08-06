package ink.usr.admin.mapper;

import ink.usr.common.model.mysql.SysMaintenanceOrderModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import ink.usr.admin.dao.DTO.SysMaintenanceOrderDTO;

@Mapper
public interface SysMaintenanceOrderMapper {
    
    /**
     * 插入维修订单
     * @param order 维修订单
     * @return 影响行数
     */
    int insertOrder(SysMaintenanceOrderModel order);
    
    /**
     * 根据ID查询维修订单
     * @param orderId 订单ID
     * @return 维修订单
     */
    SysMaintenanceOrderModel selectOrderById(@Param("orderId") Long orderId);
    
    /**
     * 根据维修申请ID查询维修订单
     * @param maintenanceId 维修申请ID
     * @return 维修订单
     */
    SysMaintenanceOrderModel selectOrderByMaintenanceId(@Param("maintenanceId") Long maintenanceId);
    
    /**
     * 查询所有维修订单
     * @return 维修订单列表
     */
    List<SysMaintenanceOrderModel> selectAllOrders();
    
    /**
     * 根据状态查询维修订单
     * @param orderStatus 订单状态
     * @return 维修订单列表
     */
    List<SysMaintenanceOrderModel> selectOrdersByStatus(@Param("orderStatus") String orderStatus);
    
    /**
     * 根据审批人查询维修订单
     * @param approverId 审批人ID
     * @return 维修订单列表
     */
    List<SysMaintenanceOrderModel> selectOrdersByApprover(@Param("approverId") Long approverId);
    
    /**
     * 更新维修订单
     * @param order 维修订单
     * @return 影响行数
     */
    int updateOrder(SysMaintenanceOrderModel order);
    
    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param orderStatus 订单状态
     * @return 影响行数
     */
    int updateOrderStatus(@Param("orderId") Long orderId, @Param("orderStatus") String orderStatus);
    

    
    /**
     * 删除维修订单
     * @param orderId 订单ID
     * @return 影响行数
     */
    int deleteOrder(@Param("orderId") Long orderId);
    
    /**
     * 根据条件查询维修订单列表（分页）
     * @param dto 查询条件
     * @return 维修订单列表
     */
    List<Map<String, Object>> selectOrderList(SysMaintenanceOrderDTO dto);
    
    /**
     * 根据条件查询维修订单总数
     * @param dto 查询条件
     * @return 总数
     */
    int selectOrderCount(SysMaintenanceOrderDTO dto);
    
} 