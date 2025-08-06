package ink.usr.admin.service.Impl;

import ink.usr.admin.dao.DTO.SysMaintenanceApprovalDTO;
import ink.usr.admin.dao.VO.SysMaintenanceApprovalVO;
import ink.usr.admin.mapper.SysMaintenanceApprovalMapper;
import ink.usr.admin.mapper.SysMaintenanceOrderMapper;
import ink.usr.admin.mapper.SysApplyMapper;
import ink.usr.admin.mapper.SysMaintenanceApplyMapper;
import ink.usr.admin.service.SysMaintenanceApprovalService;
import ink.usr.admin.service.SysApprovalFlowService;
import ink.usr.admin.service.SysUserService;
import ink.usr.common.model.mysql.SysMaintenanceApprovalModel;
import ink.usr.common.model.mysql.SysMaintenanceOrderModel;

import ink.usr.common.model.mysql.SysMaintenanceApplyModel;
import ink.usr.common.model.mysql.SysApprovalRequestModel;
import ink.usr.admin.dao.VO.SysApprovalFlowVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SysMaintenanceApprovalServiceImpl implements SysMaintenanceApprovalService {

    @Autowired
    private SysMaintenanceApprovalMapper sysMaintenanceApprovalMapper;

    @Autowired
    private SysMaintenanceOrderMapper sysMaintenanceOrderMapper;

    @Autowired
    private SysApplyMapper sysApplyMapper;

    @Autowired
    private SysApprovalFlowService sysApprovalFlowService;

    @Autowired
    private SysMaintenanceApplyMapper sysMaintenanceApplyMapper;

    @Autowired
    private SysUserService sysUserService;

    @Override
    @Transactional
    public boolean createApproval(SysMaintenanceApprovalDTO approvalDTO) {
        try {
            // 创建维修审批模型
            SysMaintenanceApprovalModel approvalModel = new SysMaintenanceApprovalModel();
            BeanUtils.copyProperties(approvalDTO, approvalModel);
            
            // 设置初始状态
            approvalModel.setApprovalStatus("待审批");
            
            // 设置时间
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            approvalModel.setCreateTime(currentTime);
            approvalModel.setUpdateTime(currentTime);
            
            // 插入数据库
            int result = sysMaintenanceApprovalMapper.insertApproval(approvalModel);
            return result > 0;
        } catch (Exception e) {
            log.error("创建维修审批失败", e);
            return false;
        }
    }

    @Override
    public SysMaintenanceApprovalVO getApprovalById(Long approvalId) {
        try {
            SysMaintenanceApprovalModel approvalModel = sysMaintenanceApprovalMapper.selectApprovalById(approvalId);
            if (approvalModel != null) {
                return convertToVO(approvalModel);
            }
            return null;
        } catch (Exception e) {
            log.error("查询维修审批失败", e);
            return null;
        }
    }

    @Override
    public List<SysMaintenanceApprovalVO> getApprovalsByOrderId(Long orderId) {
        try {
            List<SysMaintenanceApprovalModel> approvalModels = sysMaintenanceApprovalMapper.selectApprovalsByOrderId(orderId);
            return approvalModels.stream().map(this::convertToVO).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据订单ID查询维修审批失败", e);
            return null;
        }
    }

    @Override
    public List<SysMaintenanceApprovalVO> getApprovalsByApproverId(Long approverId) {
        try {
            List<SysMaintenanceApprovalModel> approvalModels = sysMaintenanceApprovalMapper.selectApprovalsByApproverId(approverId);
            return approvalModels.stream().map(this::convertToVO).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据审批人ID查询维修审批失败", e);
            return null;
        }
    }

    @Override
    public List<SysMaintenanceApprovalVO> getApprovalsWithOrderDetailsByApproverId(Long approverId, Long approvalType) {
        try {
            // 使用sys_approval_flow表查询审批记录
            // 根据approvalType参数决定查询待办审批(0)还是审批历史(1)
            List<SysApprovalFlowVO> allApprovalFlows = sysApprovalFlowService.getApprovalFlowListByApproverId(approverId, approvalType);
            List<SysMaintenanceApprovalVO> result = new ArrayList<>();
            
            for (SysApprovalFlowVO flowModel : allApprovalFlows) {
                // 检查是否是维修申请（通过查询维修申请表确认）
                Long maintenanceId = flowModel.getApprovalId();
                
                // 先检查是否是电脑申请，如果是就跳过
                SysApprovalRequestModel computerApply = sysApplyMapper.getByApprovalId(maintenanceId);
                if (computerApply != null) {
                    // 这是电脑申请，跳过
                    log.debug("跳过电脑申请记录，approvalId: {}", maintenanceId);
                    continue;
                }
                
                // 从维修申请表获取基本信息
                SysMaintenanceApplyModel applyModel = sysMaintenanceApplyMapper.selectApplyById(maintenanceId);
                if (applyModel != null && ("humanIssueRepair".equals(applyModel.getFixCategory()) || "人为问题维修".equals(applyModel.getFixCategory()))) {
                    // 只处理人为问题维修，质量问题维修应该自动审批，不应该出现在审批列表中
                    // 这是维修申请，创建维修审批VO
                    SysMaintenanceApprovalVO vo = new SysMaintenanceApprovalVO();
                    
                    // 设置审批流程信息
                    vo.setApprovalId(flowModel.getFlowId());
                    vo.setOrderId(maintenanceId);
                    vo.setApproverId(flowModel.getApproverId());
                    vo.setApprovalLevel(flowModel.getStage());
                    vo.setApprovalStatus(flowModel.getStatus());
                    vo.setApprovalTime(flowModel.getUpdatedAt());
                    vo.setApprovalReason(flowModel.getApprovalReason());
                    vo.setCreateTime(flowModel.getCreatedAt());
                    vo.setUpdateTime(flowModel.getUpdatedAt());
                    
                    // 设置维修申请信息
                    vo.setOrderNumber("MA" + maintenanceId); // 使用maintenanceId生成订单编号
                    vo.setCiName(applyModel.getCiName());
                    vo.setProblemDescription(applyModel.getProblemDescription());
                    vo.setCostCenter(applyModel.getCostCenter());
                    vo.setApplicant(String.valueOf(applyModel.getApplicant())); // 修正：使用真正的申请人ID
                    vo.setOrderType(applyModel.getFixCategory());
                    vo.setOrderStatus(applyModel.getApplyStatus());
                    
                    // 获取申请人信息
                    String applicantName = sysUserService.getUserNickNameByUserId(applyModel.getApplicant());
                    vo.setApplicantName(applicantName != null ? applicantName : "申请人-" + maintenanceId);
                    
                    // 设置责任人信息 - 注意字段名的拼写错误(responsiblility)
                    vo.setResponsiblePerson(applyModel.getResponsiblility());
                    
                    // 设置流程信号：直接使用SysApprovalFlowVO中已经计算好的status1Signal
                    vo.setStatus1Signal(flowModel.getStatus1Signal());
                    
                    result.add(vo);
                }
                // 如果不是维修申请（即applyModel为null），则跳过，不添加到结果中
            }
            
            return result;
        } catch (Exception e) {
            log.error("根据审批人ID查询维修审批（包含订单详情）失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public SysMaintenanceApprovalVO getApprovalByOrderIdAndLevel(Long orderId, Integer approvalLevel) {
        try {
            SysMaintenanceApprovalModel approvalModel = sysMaintenanceApprovalMapper.selectApprovalByOrderIdAndLevel(orderId, approvalLevel);
            if (approvalModel != null) {
                return convertToVO(approvalModel);
            }
            return null;
        } catch (Exception e) {
            log.error("根据订单ID和审批级别查询维修审批失败", e);
            return null;
        }
    }

    @Override
    public List<SysMaintenanceApprovalVO> getAllApprovals() {
        try {
            List<SysMaintenanceApprovalModel> approvalModels = sysMaintenanceApprovalMapper.selectAllApprovals();
            return approvalModels.stream().map(this::convertToVO).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询所有维修审批失败", e);
            return null;
        }
    }

    @Override
    public List<SysMaintenanceApprovalVO> getApprovalsByStatus(String approvalStatus) {
        try {
            List<SysMaintenanceApprovalModel> approvalModels = sysMaintenanceApprovalMapper.selectApprovalsByStatus(approvalStatus);
            return approvalModels.stream().map(this::convertToVO).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据状态查询维修审批失败", e);
            return null;
        }
    }

    @Override
    @Transactional
    public boolean updateApproval(SysMaintenanceApprovalModel approvalModel) {
        try {
            approvalModel.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            int result = sysMaintenanceApprovalMapper.updateApproval(approvalModel);
            return result > 0;
        } catch (Exception e) {
            log.error("更新维修审批失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateApprovalStatus(Long approvalId, String approvalStatus, String approvalReason) {
        try {
            String approvalTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            int result = sysMaintenanceApprovalMapper.updateApprovalStatus(approvalId, approvalStatus, approvalTime, approvalReason);
            return result > 0;
        } catch (Exception e) {
            log.error("更新审批状态失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteApproval(Long approvalId) {
        try {
            int result = sysMaintenanceApprovalMapper.deleteApproval(approvalId);
            return result > 0;
        } catch (Exception e) {
            log.error("删除维修审批失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteApprovalsByOrderId(Long orderId) {
        try {
            int result = sysMaintenanceApprovalMapper.deleteApprovalsByOrderId(orderId);
            return result > 0;
        } catch (Exception e) {
            log.error("根据订单ID删除维修审批失败", e);
            return false;
        }
    }

    @Override
    public String getApprovalLevelName(Integer approvalLevel) {
        switch (approvalLevel) {
            case 1:
                return "成本负责人";
            case 2:
                return "IT负责人";
            default:
                return "未知级别";
        }
    }

    @Override
    @Transactional
    public String processApproval(Long orderId, Integer approvalLevel, String approvalStatus, String approvalReason) {
        try {
            // 获取订单信息
            SysMaintenanceOrderModel order = sysMaintenanceOrderMapper.selectOrderById(orderId);
            if (order == null) {
                return "维修订单不存在";
            }

            // 获取当前审批记录
            SysMaintenanceApprovalModel approval = sysMaintenanceApprovalMapper.selectApprovalByOrderIdAndLevel(orderId, approvalLevel);
            if (approval == null) {
                return "审批记录不存在";
            }

            // 更新审批状态
            boolean updateResult = updateApprovalStatus(approval.getApprovalId(), approvalStatus, approvalReason);
            if (!updateResult) {
                return "更新审批状态失败";
            }

            // 更新订单状态
            if (approvalLevel == 1) {
                // 成本负责人审批
                if ("已通过".equals(approvalStatus)) {
                    // 成本负责人通过，检查是否需要IT负责人审批
                    if ("3".equals(order.getOrderType()) || "4".equals(order.getOrderType())) {
                        // 人为损坏需要IT负责人审批
                        return "成本负责人审批通过，等待IT负责人审批";
                    } else {
                        // 质量问题，直接进入维修中状态
                        sysMaintenanceOrderMapper.updateOrderStatus(orderId, "维修中");
                        return "成本负责人审批通过，进入维修中状态";
                    }
                } else if ("已驳回".equals(approvalStatus)) {
                    // 成本负责人驳回，订单结束
                    sysMaintenanceOrderMapper.updateOrderStatus(orderId, "已取消");
                    return "成本负责人审批驳回，维修订单已取消";
                }
            } else if (approvalLevel == 2) {
                // IT负责人审批
                if ("已通过".equals(approvalStatus)) {
                    // IT负责人通过，进入维修中状态
                    sysMaintenanceOrderMapper.updateOrderStatus(orderId, "维修中");
                    return "IT负责人审批通过，进入维修中状态";
                } else if ("已驳回".equals(approvalStatus)) {
                    // IT负责人驳回，订单结束
                    sysMaintenanceOrderMapper.updateOrderStatus(orderId, "已取消");
                    return "IT负责人审批驳回，维修订单已取消";
                }
            }

            return "审批处理成功";
        } catch (Exception e) {
            log.error("处理审批流程失败", e);
            return "处理审批流程失败: " + e.getMessage();
        }
    }

    /**
     * 将Model转换为VO
     */
    private SysMaintenanceApprovalVO convertToVO(SysMaintenanceApprovalModel model) {
        SysMaintenanceApprovalVO vo = new SysMaintenanceApprovalVO();
        BeanUtils.copyProperties(model, vo);
        vo.setApprovalLevelName(getApprovalLevelName(model.getApprovalLevel()));
        return vo;
    }
} 