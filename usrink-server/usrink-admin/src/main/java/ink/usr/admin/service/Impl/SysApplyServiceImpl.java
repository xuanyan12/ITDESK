package ink.usr.admin.service.Impl;

import ink.usr.admin.dao.DTO.SysApplyRequestDTO;
import ink.usr.admin.dao.VO.SysApplyListVO;
import ink.usr.admin.mapper.SysApplyMapper;
import ink.usr.admin.mapper.SysApproverMapper;
import ink.usr.admin.mapper.SysControlAssignMapper;
import ink.usr.admin.service.SysApplyService;
import ink.usr.admin.service.SysUserService;
import ink.usr.common.model.mysql.SysApprovalFlowModel;
import ink.usr.common.model.mysql.SysApprovalRequestModel;
import ink.usr.common.model.mysql.SysApprovalTokenModel;
import ink.usr.common.model.mysql.SysControlAssignModel;
import ink.usr.framework.shiro.domain.ShiroUserInfo;
import ink.usr.framework.shiro.utils.ShiroUtil;
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

@Service
public class SysApplyServiceImpl implements SysApplyService {

    @Autowired
    private SysApplyMapper sysApplyMapper;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysApproverMapper sysApproverMapper;

    @Autowired
    private SysControlAssignMapper sysControlAssignMapper;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public List<SysApplyListVO> getApplyList(Long userId) {
        List<SysApplyListVO> sysApplyListVOList = new ArrayList<>();

        List<SysApprovalRequestModel> applyList = sysApplyMapper.getApplyList(userId);
        for(SysApprovalRequestModel singleList : applyList){
            SysApplyListVO object = new SysApplyListVO();
            String userName = sysUserService.getUserNickNameByUserId(singleList.getApplicant());
            String responsibilityName = sysUserService.getUserNickNameByUserId(singleList.getResponsibility());
            object.setUserName(userName);
            object.setResponsibilityName(responsibilityName);
            BeanUtils.copyProperties(singleList, object);
            
            // 获取分配状态信息
            if (singleList.getStatus() != null && 
                (singleList.getStatus().equals("审批通过") || singleList.getStatus().equals("已通过"))) {
                // 从sys_control_assign表获取分配状态
                SysControlAssignModel assignInfo = 
                    sysControlAssignMapper.getControlAssign(singleList.getApprovalId());
                if (assignInfo != null && assignInfo.getAssignStatus() != null) {
                    object.setAssignStatus(assignInfo.getAssignStatus());
                } else {
                    object.setAssignStatus("分配中");
                }
            }
            
            sysApplyListVOList.add(object);
        }
        return sysApplyListVOList;
    }

    @Override
    @Transactional
    public String addApply(SysApplyRequestDTO sysApplyRequestDTO) {
        //  1.新建设备申请订单
        Long userId = sysUserService.getUserIdByUserName(sysApplyRequestDTO.getUserName());
        sysApplyRequestDTO.setApplicant(userId);
        //  1.2 设置审批流有效期，默认值为999天
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = LocalDateTime.now().plusDays(999).format(formatter);
        String createTime = LocalDateTime.now().format(formatter);
        sysApplyRequestDTO.setTimestamp(timestamp);
        sysApplyRequestDTO.setCreatedAt(createTime);
        sysApplyRequestDTO.setUpdatedAt(createTime);
        SysApprovalRequestModel sysApprovalRequestModel = new SysApprovalRequestModel();
        
        // 检查申请类别，如果不是"办公电脑未超六年换新"或"其他用途电脑申请"，直接设置为审批通过
        String deviceCategory = sysApplyRequestDTO.getDeviceCategory();
        boolean needsApproval = "办公电脑未超六年换新".equals(deviceCategory) || "其他用途电脑申请".equals(deviceCategory);
        
        if (needsApproval) {
            sysApplyRequestDTO.setStatus("审批中");
        } else {
            sysApplyRequestDTO.setStatus("审批通过");
        }
        
        Long responsibilityId = sysUserService.getUserIdByUserName(sysApplyRequestDTO.getResponsibilityName());
        BeanUtils.copyProperties(sysApplyRequestDTO,sysApprovalRequestModel);
        sysApprovalRequestModel.setResponsibility(responsibilityId);
        sysApplyMapper.addApply(sysApprovalRequestModel);
        
        //  获取订单id
        Long approvalId = sysApprovalRequestModel.getApprovalId();
        
        if (needsApproval) {
            // 需要审批的流程：创建审批流程并发送邮件
            //  2.创建一个属于部门leader的一级审批工作流
            //  2.1 获取审批人id，审批人id需要根据申请人id去找到对应的部门号，再通过部门号在审批表里找到对应的审批者
            //  2.1.1 根据使用人id获取成本中心
            String costCenter = sysApplyMapper.getCostCenterByApplyUserId(userId);
            //  2.1.2 根据成本中心找到审批者id
            Long approverId = sysApplyMapper.getApproverIdByCostCenter(costCenter);
            //  2.2 构建一级审批流实体
            SysApprovalFlowModel sysApprovalFlowModel = new SysApprovalFlowModel();
            sysApprovalFlowModel.setApprovalId(approvalId);
            sysApprovalFlowModel.setApproverId(approverId);
            sysApprovalFlowModel.setStage(1);
            sysApprovalFlowModel.setStatus("审批中");
            sysApprovalFlowModel.setCreatedAt(createTime);
            sysApprovalFlowModel.setUpdatedAt(createTime);
            sysApplyMapper.addApplyFlow(sysApprovalFlowModel);
            Long flow1Id = sysApprovalFlowModel.getFlowId();
            //  3.创建一个属于IT Approver的二级审批工作流
            SysApprovalFlowModel sysApprovalFlowModel4ITApprover = new SysApprovalFlowModel();
            sysApprovalFlowModel4ITApprover.setApprovalId(approvalId);
            Long itApprover = sysApproverMapper.getITApprover();
            sysApprovalFlowModel4ITApprover.setApproverId(itApprover);
            sysApprovalFlowModel4ITApprover.setStage(2);
            sysApprovalFlowModel4ITApprover.setStatus("审批中");
            sysApprovalFlowModel4ITApprover.setCreatedAt(createTime);
            sysApprovalFlowModel4ITApprover.setUpdatedAt(createTime);
            sysApplyMapper.addApplyFlow(sysApprovalFlowModel4ITApprover);
            Long flow2Id = sysApprovalFlowModel4ITApprover.getFlowId();
            //  4.发送邮件给一级审批工作流用户，附带可不登陆直接审批的链接
            //  4.1 生成唯一token并存入
            SysApprovalTokenModel sysApprovalTokenModel = new SysApprovalTokenModel();
            sysApprovalTokenModel.setFlowId(flow1Id);
            String token = UUID.randomUUID().toString().replace("-", "");
            sysApprovalTokenModel.setToken(token);
            sysApprovalTokenModel.setCreatedAt(createTime);
            sysApprovalTokenModel.setExpireTime(timestamp);
            sysApprovalTokenModel.setUpdatedAt(createTime);
            sysApprovalTokenModel.setUsed(0);

            // 生成第二个token
            SysApprovalTokenModel sysApprovalTokenModel2 = new SysApprovalTokenModel();
            sysApprovalTokenModel2.setFlowId(flow2Id);
            String token2 = UUID.randomUUID().toString().replace("-", "");
            sysApprovalTokenModel2.setToken(token2);
            sysApprovalTokenModel2.setCreatedAt(createTime);
            sysApprovalTokenModel2.setExpireTime(timestamp);
            sysApprovalTokenModel2.setUpdatedAt(createTime);
            sysApprovalTokenModel2.setUsed(0);

            sysApplyMapper.InsertToken(sysApprovalTokenModel);
            sysApplyMapper.InsertToken(sysApprovalTokenModel2);
            //  4.2 发送邮件
            String url = frontendUrl + "/public-page" + "?flowId=" + flow1Id + "&token=" + token;
            return url;
        } else {
            // 不需要审批的流程：直接创建分配订单
            // 创建分配数据
            SysControlAssignModel sysControlAssignModel = new SysControlAssignModel();
            sysControlAssignModel.setDeviceType(sysApprovalRequestModel.getDeviceType());
            sysControlAssignModel.setDeviceSituation(sysApprovalRequestModel.getDeviceSituation());
            sysControlAssignModel.setApprovalId(approvalId);
            sysControlAssignModel.setCompany(sysApprovalRequestModel.getCompany());
            sysControlAssignModel.setApplicant(sysApprovalRequestModel.getApplicant());
            sysControlAssignModel.setAssignStatus("分配中");
            sysControlAssignModel.setStartTime(createTime);
            sysControlAssignMapper.addAssignInfo(sysControlAssignModel);
            
            return "申请已自动审批通过，已进入分配流程";
        }
    }
}
