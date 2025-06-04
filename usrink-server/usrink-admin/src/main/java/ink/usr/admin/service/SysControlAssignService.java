package ink.usr.admin.service;

import ink.usr.admin.dao.DTO.SysAllocateDeviceDTO;
import ink.usr.common.model.mysql.SysControlAssignModel;

import java.util.List;

public interface SysControlAssignService {
    List<SysControlAssignModel> getControlAssignList();
    
    /**
     * 获取控制分配列表(带查询条件)
     * @param queryModel 查询条件模型
     * @return 符合条件的分配记录列表
     */
    List<SysControlAssignModel> getControlAssignList(SysControlAssignModel queryModel);

    boolean allocateDevice(SysAllocateDeviceDTO sysAllocateDeviceDTO);
    
    /**
     * 设备领取
     * @param receiveDeviceDTO 设备领取信息
     * @return 是否成功
     */
    boolean receiveDevice(SysAllocateDeviceDTO receiveDeviceDTO);
    
    /**
     * 删除订单（将状态改为已关闭）
     * @param approvalId 订单号
     * @return 是否成功
     */
    boolean deleteOrder(Long approvalId);
}
