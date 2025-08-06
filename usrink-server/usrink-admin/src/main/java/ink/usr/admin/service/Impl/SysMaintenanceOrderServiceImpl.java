package ink.usr.admin.service.Impl;

import ink.usr.admin.dao.DTO.SysMaintenanceOrderDTO;
import ink.usr.admin.dao.VO.SysMaintenanceOrderVO;
import ink.usr.admin.mapper.SysMaintenanceOrderMapper;
import ink.usr.admin.service.SysMaintenanceOrderService;
import ink.usr.admin.service.SysControlMaintenanceService;
import ink.usr.common.model.mysql.SysMaintenanceOrderModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@Slf4j
public class SysMaintenanceOrderServiceImpl implements SysMaintenanceOrderService {

    @Autowired
    private SysMaintenanceOrderMapper sysMaintenanceOrderMapper;
    
    @Autowired
    private SysControlMaintenanceService sysControlMaintenanceService;

    @Override
    @Transactional
    public String createOrder(SysMaintenanceOrderDTO orderDTO) {
        try {
            // 创建维修订单模型
            SysMaintenanceOrderModel orderModel = new SysMaintenanceOrderModel();
            BeanUtils.copyProperties(orderDTO, orderModel);
            
            // 生成订单编号
            orderModel.setOrderNumber(generateOrderNumber());
            
            // 设置初始状态 - 审批通过后创建的订单应该是待维修状态
            orderModel.setOrderStatus("待维修");
            
            // 设置时间
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            orderModel.setCreateTime(currentTime);
            orderModel.setUpdateTime(currentTime);
            
            // 插入数据库
            int result = sysMaintenanceOrderMapper.insertOrder(orderModel);
            
            if (result > 0) {
                return "维修订单创建成功";
            } else {
                return "创建维修订单失败";
            }
        } catch (Exception e) {
            log.error("创建维修订单失败", e);
            return "创建维修订单失败: " + e.getMessage();
        }
    }

    @Override
    public SysMaintenanceOrderVO getOrderById(Long orderId) {
        try {
            SysMaintenanceOrderModel orderModel = sysMaintenanceOrderMapper.selectOrderById(orderId);
            if (orderModel != null) {
                return convertToVO(orderModel);
            }
            return null;
        } catch (Exception e) {
            log.error("查询维修订单失败", e);
            return null;
        }
    }

    @Override
    public SysMaintenanceOrderVO getOrderByMaintenanceId(Long maintenanceId) {
        try {
            SysMaintenanceOrderModel orderModel = sysMaintenanceOrderMapper.selectOrderByMaintenanceId(maintenanceId);
            if (orderModel != null) {
                return convertToVO(orderModel);
            }
            return null;
        } catch (Exception e) {
            log.error("根据维修申请ID查询维修订单失败", e);
            return null;
        }
    }

    @Override
    public List<SysMaintenanceOrderVO> getAllOrders() {
        try {
            List<SysMaintenanceOrderModel> orderModels = sysMaintenanceOrderMapper.selectAllOrders();
            return orderModels.stream().map(this::convertToVO).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询所有维修订单失败", e);
            return null;
        }
    }

    @Override
    public List<SysMaintenanceOrderVO> getOrdersByStatus(String orderStatus) {
        try {
            List<SysMaintenanceOrderModel> orderModels = sysMaintenanceOrderMapper.selectOrdersByStatus(orderStatus);
            return orderModels.stream().map(this::convertToVO).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据状态查询维修订单失败", e);
            return null;
        }
    }

    @Override
    public List<SysMaintenanceOrderVO> getOrdersByApprover(Long approverId) {
        try {
            List<SysMaintenanceOrderModel> orderModels = sysMaintenanceOrderMapper.selectOrdersByApprover(approverId);
            return orderModels.stream().map(this::convertToVO).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据审批人查询维修订单失败", e);
            return null;
        }
    }

    @Override
    @Transactional
    public boolean updateOrder(SysMaintenanceOrderModel orderModel) {
        try {
            orderModel.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            int result = sysMaintenanceOrderMapper.updateOrder(orderModel);
            return result > 0;
        } catch (Exception e) {
            log.error("更新维修订单失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateOrderStatus(Long orderId, String orderStatus) {
        try {
            int result = sysMaintenanceOrderMapper.updateOrderStatus(orderId, orderStatus);
            return result > 0;
        } catch (Exception e) {
            log.error("更新订单状态失败", e);
            return false;
        }
    }







    @Override
    @Transactional
    public boolean deleteOrder(Long orderId) {
        try {
            int result = sysMaintenanceOrderMapper.deleteOrder(orderId);
            return result > 0;
        } catch (Exception e) {
            log.error("删除维修订单失败", e);
            return false;
        }
    }

    @Override
    public String generateOrderNumber() {
        // 生成订单编号：MO + 年月日 + 4位序号
        String prefix = "MO";
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequence = String.format("%04d", (int)(Math.random() * 10000));
        return prefix + date + sequence;
    }

    @Override
    public String getOrderTypeName(String orderType) {
        switch (orderType) {
            case "1":
                return "员工电脑保修期内(1-3年)质量问题";
            case "2":
                return "员工电脑保修期外(3-6年)质量问题";
            case "3":
                return "员工电脑保修期内(1-3年)人为损坏";
            case "4":
                return "员工电脑保修期外(3-6年)人为损坏";
            default:
                return "未知类型";
        }
    }

    @Override
    @Transactional
    public String processAutoApproval(Long orderId) {
        try {
            SysMaintenanceOrderModel order = sysMaintenanceOrderMapper.selectOrderById(orderId);
            if (order == null) {
                return "维修订单不存在";
            }

            String orderType = order.getOrderType();

            // 根据订单类型处理自动审批逻辑
            switch (orderType) {
                case "1": // 质量问题维修 - 自动通过
                    updateOrderStatus(orderId, "维修中");
                    return "申请已自动审批通过，进入维修中状态";
                    
                case "3": // 人为问题维修 - 需审批
                    updateOrderStatus(orderId, "审批中");
                    return "申请已提交，等待审批";
                    
                default:
                    return "未知的订单类型";
            }
        } catch (Exception e) {
            log.error("处理自动审批失败", e);
            return "处理自动审批失败: " + e.getMessage();
        }
    }

    /**
     * 将Model转换为VO
     */
    private SysMaintenanceOrderVO convertToVO(SysMaintenanceOrderModel model) {
        SysMaintenanceOrderVO vo = new SysMaintenanceOrderVO();
        BeanUtils.copyProperties(model, vo);
        vo.setOrderTypeName(getOrderTypeName(model.getOrderType()));
        return vo;
    }

    @Override
    public List<Map<String, Object>> getOrderList(SysMaintenanceOrderDTO dto) {
        try {
            // 计算偏移量
            if (dto.getPageNum() != null && dto.getPageSize() != null) {
                int offset = (dto.getPageNum() - 1) * dto.getPageSize();
                dto.setPageNum(offset);
            }
            return sysMaintenanceOrderMapper.selectOrderList(dto);
        } catch (Exception e) {
            log.error("查询维修订单列表失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public int getOrderCount(SysMaintenanceOrderDTO dto) {
        try {
            return sysMaintenanceOrderMapper.selectOrderCount(dto);
        } catch (Exception e) {
            log.error("查询维修订单总数失败", e);
            return 0;
        }
    }

    @Override
    @Transactional
    public boolean approveOrder(Long orderId, String status, String reason) {
        try {
            SysMaintenanceOrderModel order = sysMaintenanceOrderMapper.selectOrderById(orderId);
            if (order == null) {
                log.error("维修订单不存在: {}", orderId);
                return false;
            }

            if ("已通过".equals(status)) {
                // 审批通过，进入维修中状态
                updateOrderStatus(orderId, "维修中");
            } else if ("已拒绝".equals(status)) {
                // 拒绝申请
                updateOrderStatus(orderId, "已拒绝");
            }
            
            return true;
        } catch (Exception e) {
            log.error("审批维修订单失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean operateMaintenance(Long orderId, String action, String result, Double cost) {
        try {
            SysMaintenanceOrderModel order = sysMaintenanceOrderMapper.selectOrderById(orderId);
            if (order == null) {
                log.error("维修订单不存在: {}", orderId);
                return false;
            }

            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // 更新维修时间
            order.setMaintenanceTime(currentTime);
            order.setUpdateTime(currentTime);
            
            int updateResult = sysMaintenanceOrderMapper.updateOrder(order);
            
            if (updateResult > 0) {
                // 更新订单状态为维修中
                updateOrderStatus(orderId, "维修中");
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("执行维修操作失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean completeMaintenance(Long orderId, String maintenanceAction, String maintenanceResult, Double maintenanceCost, String maintenanceRemark, String orderStatus) {
        try {
            SysMaintenanceOrderModel order = sysMaintenanceOrderMapper.selectOrderById(orderId);
            if (order == null) {
                log.error("维修订单不存在: {}", orderId);
                return false;
            }

            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // 更新维修订单状态和时间
            order.setMaintenanceTime(currentTime);
            order.setMaintenanceRemark(maintenanceRemark);
            order.setOrderStatus(orderStatus);
            order.setUpdateTime(currentTime);
            
            int updateResult = sysMaintenanceOrderMapper.updateOrder(order);
            
            if (updateResult > 0) {
                // 将维修记录保存到维修记录表
                // maintenanceRecord字段只存储维修操作内容，不重复其他字段信息
                String maintenanceRecordContent = maintenanceAction; // 只保存维修操作内容
                
                // 需要从订单信息中获取维修类别，如果没有，使用默认值
                String maintenanceCategory = "qualityIssueRepair"; // 默认质量问题维修
                if (order.getOrderType() != null) {
                    switch (order.getOrderType()) {
                        case "1":
                        case "2":
                            maintenanceCategory = "qualityIssueRepair";
                            break;
                        case "3":
                        case "4":
                            maintenanceCategory = "damageRepair";
                            break;
                        default:
                            maintenanceCategory = "qualityIssueRepair";
                    }
                }
                
                boolean recordSaved = sysControlMaintenanceService.saveMaintenanceRecord(
                    order.getOrderNumber(),           // 维修订单号
                    order.getCiName(),               // 电脑名
                    order.getApplicant(),            // 申请人
                    maintenanceCategory,             // 维修类别
                    order.getProblemDescription(),   // 故障描述
                    maintenanceResult,               // 维修结果
                    maintenanceRecordContent,        // 完整维修记录
                    maintenanceRemark,               // 维修备注
                    currentTime                      // 维修时间
                );
                if (recordSaved) {
                    log.info("维修订单 {} 完成维修，状态更新为: {}, 维修记录已保存", orderId, orderStatus);
                } else {
                    log.warn("维修订单 {} 完成维修，状态更新为: {}, 但维修记录保存失败", orderId, orderStatus);
                }
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("完成维修失败", e);
            return false;
        }
    }
} 