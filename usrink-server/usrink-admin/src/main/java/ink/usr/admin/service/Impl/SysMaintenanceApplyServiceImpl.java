package ink.usr.admin.service.Impl;



import ink.usr.admin.dao.DTO.SysMaintenanceApplyRequestDTO;
import ink.usr.admin.dao.DTO.SysMaintenanceOrderDTO;
import ink.usr.admin.dao.VO.SysApplyListVO;
import ink.usr.admin.dao.VO.SysMaintenanceApplyVO;
import ink.usr.admin.mapper.SysMaintenanceApplyMapper;
import ink.usr.admin.mapper.SysApplyMapper;
import ink.usr.admin.mapper.SysApproverMapper;
import ink.usr.admin.mapper.SysMaintenanceOrderMapper;
import ink.usr.admin.service.SysMaintenanceApplyService;
import ink.usr.admin.service.SysMaintenanceOrderService;
import ink.usr.admin.service.SysUserService;


import ink.usr.common.model.mysql.SysMaintenanceApplyModel;
import ink.usr.common.model.mysql.SysApprovalFlowModel;
import ink.usr.common.model.mysql.SysMaintenanceOrderModel;
import ink.usr.common.model.mysql.SysApprovalTokenModel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SysMaintenanceApplyServiceImpl implements SysMaintenanceApplyService {
    
    @Autowired
    private SysMaintenanceApplyMapper sysMaintenanceApplyMapper;

    @Autowired
    private SysMaintenanceOrderService sysMaintenanceOrderService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysApplyMapper sysApplyMapper;

    @Autowired
    private SysApproverMapper sysApproverMapper;

    @Autowired
    private SysMaintenanceOrderMapper sysMaintenanceOrderMapper;

    @Value("${frontend.url}")
    private String frontendUrl;



    @Override
    public List<SysApplyListVO> getApplyList(Long userId) {
        try {
            // 获取用户的维修申请列表
            List<SysMaintenanceApplyModel> applyModels = sysMaintenanceApplyMapper.selectAppliesByApplicant(userId);
            
            // 转换为SysApplyListVO格式（兼容原有接口）
            return applyModels.stream().map(this::convertToSysApplyListVO).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取维修申请列表失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public String addApply(SysMaintenanceApplyRequestDTO sysMaintenanceApplyRequestDTO) {
        try {
            // 1. 将用户名转换为用户ID
            Long applicantId = sysUserService.getUserIdByUserName(sysMaintenanceApplyRequestDTO.getUserName());
            if (applicantId == null) {
                return "用户不存在：" + sysMaintenanceApplyRequestDTO.getUserName();
            }
            
            // 2. 创建维修申请模型
            SysMaintenanceApplyModel applyModel = new SysMaintenanceApplyModel();
            BeanUtils.copyProperties(sysMaintenanceApplyRequestDTO, applyModel);
            
            // 3. 设置用户ID、电脑名称和初始状态
            applyModel.setApplicant(applicantId);
            applyModel.setCiName(sysMaintenanceApplyRequestDTO.getComputerName()); // 设置电脑名称
            applyModel.setApplyStatus("待处理");
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            applyModel.setCreateTime(currentTime);
            applyModel.setUpdateTime(currentTime);
            
            // 4. 保存维修申请到数据库
            int result = sysMaintenanceApplyMapper.insertApply(applyModel);
            if (result <= 0) {
                return "保存维修申请失败";
            }
            
            Long maintenanceId = applyModel.getMaintenanceId();
            
            // 5. 根据维修类别确定订单类型和审批流程
            String fixCategory = sysMaintenanceApplyRequestDTO.getFixCategory();
            String orderType = determineOrderType(fixCategory);
            
            // 6. 根据维修类别更新申请状态和创建审批流程
            if ("qualityIssueRepair".equals(fixCategory) || "质量问题维修".equals(fixCategory)) {
                // 质量问题维修：自动审批通过
                sysMaintenanceApplyMapper.updateApplyStatus(maintenanceId, "审批通过");
                
                // 创建维修订单（质量问题维修自动通过，直接创建订单）
                SysMaintenanceOrderDTO orderDTO = new SysMaintenanceOrderDTO();
                orderDTO.setMaintenanceId(maintenanceId);
                orderDTO.setOrderType(orderType);
                orderDTO.setCostCenter(sysMaintenanceApplyRequestDTO.getCostCenter());
                orderDTO.setApplicant(String.valueOf(applicantId));
                String ciName = sysMaintenanceApplyRequestDTO.getComputerName();
                orderDTO.setCiName(ciName);
                orderDTO.setProblemDescription(sysMaintenanceApplyRequestDTO.getProblemDescription());
                
                String orderResult = sysMaintenanceOrderService.createOrder(orderDTO);
                if (orderResult.contains("成功")) {
                    // 对维修订单执行自动审批
                    SysMaintenanceOrderModel order = sysMaintenanceOrderMapper.selectOrderByMaintenanceId(maintenanceId);
                    if (order != null) {
                        sysMaintenanceOrderService.processAutoApproval(order.getOrderId());
                    }
                    return "维修申请提交成功，质量问题维修已自动审批通过";
                } else {
                    return "维修申请提交成功，但订单创建失败：" + orderResult;
                }
            } else {
                // 人为问题维修：需要审批，创建审批流程
                sysMaintenanceApplyMapper.updateApplyStatus(maintenanceId, "审批中");
                
                // 创建审批流程和审批URL
                String approvalUrl = createMaintenanceApprovalFlow(maintenanceId, sysMaintenanceApplyRequestDTO.getCostCenter());
                if (approvalUrl == null) {
                    return "创建审批流程失败";
                }
                
                return "维修申请提交成功，人为问题维修已提交审批|APPROVAL_URL:" + approvalUrl;
            }
        } catch (Exception e) {
            log.error("提交维修申请失败", e);
            return "提交维修申请失败: " + e.getMessage();
        }
    }

    @Override
    public SysMaintenanceApplyVO getApplyById(Long maintenanceId) {
        try {
            SysMaintenanceApplyModel applyModel = sysMaintenanceApplyMapper.selectApplyById(maintenanceId);
            if (applyModel != null) {
                return convertToVO(applyModel);
            }
            return null;
        } catch (Exception e) {
            log.error("查询维修申请失败", e);
            return null;
        }
    }

    @Override
    public List<SysMaintenanceApplyVO> getAppliesByApplicant(Long applicant) {
        try {
            List<SysMaintenanceApplyModel> applyModels = sysMaintenanceApplyMapper.selectAppliesByApplicant(applicant);
            return applyModels.stream().map(this::convertToVO).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据申请人查询维修申请失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<SysMaintenanceApplyVO> getAllApplies() {
        try {
            List<SysMaintenanceApplyModel> applyModels = sysMaintenanceApplyMapper.selectAllApplies();
            return applyModels.stream().map(this::convertToVO).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询所有维修申请失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<SysMaintenanceApplyVO> getAppliesByStatus(String applyStatus) {
        try {
            List<SysMaintenanceApplyModel> applyModels = sysMaintenanceApplyMapper.selectAppliesByStatus(applyStatus);
            return applyModels.stream().map(this::convertToVO).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据状态查询维修申请失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public boolean updateApply(SysMaintenanceApplyModel applyModel) {
        try {
            applyModel.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            int result = sysMaintenanceApplyMapper.updateApply(applyModel);
            return result > 0;
        } catch (Exception e) {
            log.error("更新维修申请失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateApplyStatus(Long maintenanceId, String applyStatus) {
        try {
            int result = sysMaintenanceApplyMapper.updateApplyStatus(maintenanceId, applyStatus);
            return result > 0;
        } catch (Exception e) {
            log.error("更新申请状态失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteApply(Long maintenanceId) {
        try {
            int result = sysMaintenanceApplyMapper.deleteApply(maintenanceId);
            return result > 0;
        } catch (Exception e) {
            log.error("删除维修申请失败", e);
            return false;
        }
    }

    @Override
    public String getFixCategoryName(String fixCategory) {
        switch (fixCategory) {
            case "qualityIssueRepair":
            case "质量问题维修":
                return "质量问题维修";
            case "humanIssueRepair":
            case "人为问题维修":
                return "人为问题维修";
            default:
                return "未知类别";
        }
    }
    
    /**
     * 根据维修类别确定订单类型
     * @param fixCategory 维修类别
     * @return 订单类型
     */
    private String determineOrderType(String fixCategory) {
        // 根据维修类别确定订单类型
        switch (fixCategory) {
            case "qualityIssueRepair":
            case "质量问题维修":
                // 质量问题维修
                return "1";
            case "humanIssueRepair":
            case "人为问题维修":
                // 人为问题维修
                return "3";
            default:
                return "1"; // 默认保修期内质量问题
        }
    }

    /**
     * 将Model转换为VO
     */
    private SysMaintenanceApplyVO convertToVO(SysMaintenanceApplyModel model) {
        SysMaintenanceApplyVO vo = new SysMaintenanceApplyVO();
        BeanUtils.copyProperties(model, vo);
        vo.setFixCategoryName(getFixCategoryName(model.getFixCategory()));
        
        // 获取申请人姓名
        try {
            String applicantName = sysUserService.getUserNickNameByUserId(model.getApplicant());
            vo.setApplicantName(applicantName != null ? applicantName : "未知用户");
        } catch (Exception e) {
            vo.setApplicantName("未知用户");
        }
        
        return vo;
    }

    /**
     * 转换为SysApplyListVO（兼容原有接口）
     */
    private SysApplyListVO convertToSysApplyListVO(SysMaintenanceApplyModel model) {
        SysApplyListVO vo = new SysApplyListVO();
        vo.setApprovalId(model.getMaintenanceId());
        vo.setApplicant(model.getApplicant());
        vo.setDeviceCategory("维修申请");
        vo.setDeviceType(getFixCategoryName(model.getFixCategory()));
        vo.setCostCenter(model.getCostCenter());
        vo.setCompany(model.getCompany());
        // 注意：responsibility字段是Long类型，这里暂时设置为null
        vo.setResponsibility(null);
        vo.setDeviceSituation(model.getProblemDescription());
        vo.setReason(model.getProblemDescription());
        vo.setStatus(model.getApplyStatus());
        vo.setCreatedAt(model.getCreateTime());
        vo.setUpdatedAt(model.getUpdateTime());
        vo.setCiName(model.getCiName());
        
        // 设置维修类别到fixCategory字段
        vo.setFixCategory(model.getFixCategory());
        
        // 添加申请人姓名
        try {
            String applicantName = sysUserService.getUserNickNameByUserId(model.getApplicant());
            vo.setUserName(applicantName != null ? applicantName : "未知用户");
        } catch (Exception e) {
            vo.setUserName("未知用户");
        }
        
        // 添加责任人姓名（责任人字段是String类型，直接使用）
        if (model.getResponsiblility() != null && !model.getResponsiblility().trim().isEmpty()) {
            vo.setResponsibilityName(model.getResponsiblility());
        } else {
            vo.setResponsibilityName("-");
        }
        
        return vo;
    }
    
    /**
     * 创建维修申请审批流程并生成审批URL
     * @param maintenanceId 维修申请ID
     * @param costCenter 成本中心
     * @return 审批URL
     */
    private String createMaintenanceApprovalFlow(Long maintenanceId, String costCenter) {
        try {
            String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String expireTime = LocalDateTime.now().plusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // 获取成本中心审批人
            Long approverId = sysApplyMapper.getApproverIdByCostCenter(costCenter);
            if (approverId == null) {
                log.error("未找到成本中心审批人: {}", costCenter);
                return null;
            }
            
            // 创建一级审批流（成本中心审批人）
            SysApprovalFlowModel sysApprovalFlowModel = new SysApprovalFlowModel();
            sysApprovalFlowModel.setApprovalId(maintenanceId);
            sysApprovalFlowModel.setApproverId(approverId);
            sysApprovalFlowModel.setStage(1);
            sysApprovalFlowModel.setStatus("审批中");
            sysApprovalFlowModel.setCreatedAt(createTime);
            sysApprovalFlowModel.setUpdatedAt(createTime);
            sysApplyMapper.addApplyFlow(sysApprovalFlowModel);
            Long flow1Id = sysApprovalFlowModel.getFlowId();
            
            // 创建二级审批流（IT审批人）
            Long itApprover = sysApproverMapper.getITApprover();
            if (itApprover != null) {
                SysApprovalFlowModel sysApprovalFlowModel4ITApprover = new SysApprovalFlowModel();
                sysApprovalFlowModel4ITApprover.setApprovalId(maintenanceId);
                sysApprovalFlowModel4ITApprover.setApproverId(itApprover);
                sysApprovalFlowModel4ITApprover.setStage(2);
                sysApprovalFlowModel4ITApprover.setStatus("审批中");
                sysApprovalFlowModel4ITApprover.setCreatedAt(createTime);
                sysApprovalFlowModel4ITApprover.setUpdatedAt(createTime);
                sysApplyMapper.addApplyFlow(sysApprovalFlowModel4ITApprover);
                Long flow2Id = sysApprovalFlowModel4ITApprover.getFlowId();
                
                // 为二级审批流生成token
                SysApprovalTokenModel sysApprovalTokenModel2 = new SysApprovalTokenModel();
                sysApprovalTokenModel2.setFlowId(flow2Id);
                String token2 = UUID.randomUUID().toString().replace("-", "");
                sysApprovalTokenModel2.setToken(token2);
                sysApprovalTokenModel2.setCreatedAt(createTime);
                sysApprovalTokenModel2.setExpireTime(expireTime);
                sysApprovalTokenModel2.setUpdatedAt(createTime);
                sysApprovalTokenModel2.setUsed(0);
                sysApplyMapper.InsertToken(sysApprovalTokenModel2);
            }
            
            // 为一级审批流生成token
            SysApprovalTokenModel sysApprovalTokenModel = new SysApprovalTokenModel();
            sysApprovalTokenModel.setFlowId(flow1Id);
            String token = UUID.randomUUID().toString().replace("-", "");
            sysApprovalTokenModel.setToken(token);
            sysApprovalTokenModel.setCreatedAt(createTime);
            sysApprovalTokenModel.setExpireTime(expireTime);
            sysApprovalTokenModel.setUpdatedAt(createTime);
            sysApprovalTokenModel.setUsed(0);
            sysApplyMapper.InsertToken(sysApprovalTokenModel);
            
            // 生成审批URL
            String url = frontendUrl + "/public-page?flowId=" + flow1Id + "&token=" + token;
            return url;
            
        } catch (Exception e) {
            log.error("创建维修申请审批流程失败", e);
            return null;
        }
    }
}
