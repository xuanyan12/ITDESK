package ink.usr.admin.controller;

import com.github.pagehelper.Page;
import ink.usr.admin.dao.DTO.SysAllocateDeviceDTO;
import ink.usr.admin.dao.VO.SysControlAssignListVO;
import ink.usr.admin.dao.VO.SysControlAssignVO;
import ink.usr.admin.mapper.SysControlMapper;
import ink.usr.admin.mapper.SysUserMapper;
import ink.usr.admin.service.SysControlAssignService;
import ink.usr.admin.service.SysControlService;
import ink.usr.common.core.domain.Dict;
import ink.usr.common.core.domain.Res;
import ink.usr.common.core.utils.PageUtil;
import ink.usr.common.model.mysql.SysControlAssignModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/sysControlAssign")
public class SysControlAssignController {
    @Autowired
    private SysControlAssignService sysControlAssignService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysControlService sysControlService;

    /**
     * 获取订单列表
     * @param sysControlAssignVO
     * @return
     */
    @RequestMapping("/getControlAssignList")
    public Res getControlAssignList(@RequestBody SysControlAssignVO sysControlAssignVO){
        log.info("Received request for control assign list with params: {}", sysControlAssignVO);
        Page<Object> pages = PageUtil.startPage();
        
        // 创建查询模型对象
        SysControlAssignModel queryModel = new SysControlAssignModel();
        
        // 设置查询字段，让SQL层处理非空验证
        queryModel.setCiName(sysControlAssignVO.getCiName());
        queryModel.setDeviceType(sysControlAssignVO.getDeviceType());
        queryModel.setDeviceSituation(sysControlAssignVO.getDeviceSituation());
        queryModel.setCompany(sysControlAssignVO.getCompany());
        queryModel.setAssignStatus(sysControlAssignVO.getAssignStatus());
        queryModel.setDeviceCategory(sysControlAssignVO.getDeviceCategory());
        queryModel.setReason(sysControlAssignVO.getReason());
        // 设置NT账号，但需要使用setLastLeastUserNtAccount
        if (sysControlAssignVO.getNtAccount() != null) {
            queryModel.setLastLeastUserNtAccount(sysControlAssignVO.getNtAccount());
        }
        
        // 调用服务层的查询方法
        List<SysControlAssignListVO> sysControlAssignListVOList = sysControlAssignService.getControlAssignListWithApprovalInfo(queryModel);
        for(SysControlAssignListVO singleVO : sysControlAssignListVOList){
            if(singleVO.getApplicant() != null){
                singleVO.setUser(sysUserMapper.getUserNickNameByUserId(singleVO.getApplicant()));
            }
            if(singleVO.getLastLeastUserNtAccount() != null){
                singleVO.setLastLeastUser(sysUserMapper.getUserInfoByUserName(singleVO.getLastLeastUserNtAccount()).getUserNick());
            }

            if(singleVO.getAssignor() != null){
                singleVO.setAssigner(sysUserMapper.getUserInfoByUserName(singleVO.getAssignor()).getUserNick());
            }
        }
        Dict result = Dict.create()
                .set("list", sysControlAssignListVOList)
                .set("total", pages.getTotal());
                
        log.info("Returning control assign list with total count: {}", pages.getTotal());
        return Res.success(result);
    }

    /**
     * 电脑分配，修改电脑表与订单表信息
     * @return
     */
    @RequestMapping("/allocateDevice")
    @Transactional
    public Res allocateDevice(@RequestBody SysAllocateDeviceDTO sysAllocateDeviceDTO){

        // 1.订单表sys_control_assign进行更改
        boolean controlAssignFlag = sysControlAssignService.allocateDevice(sysAllocateDeviceDTO);
        // 2.电脑表sys_control进行更改,根据ciName找到对应的需要修改的行数据
        boolean controlFlag = sysControlService.allocateDevice(sysAllocateDeviceDTO);

        if(controlFlag && controlAssignFlag){
            return Res.success();
        }
        return Res.error();
    }

    /**
     * 设备领取
     * @param sysAllocateDeviceDTO
     * @return
     */
    @RequestMapping("/receiveDevice")
    @Transactional
    public Res receiveDevice(@RequestBody SysAllocateDeviceDTO sysAllocateDeviceDTO){
        log.info("设备领取请求, 订单号: {}, 电脑归属情况: {}", 
                sysAllocateDeviceDTO.getApprovalId(), 
                sysAllocateDeviceDTO.getPcClass());

        // 1. 更新订单表状态为已领取
        boolean controlAssignFlag = sysControlAssignService.receiveDevice(sysAllocateDeviceDTO);
        
        // 2. 更新电脑表的归属情况
        boolean controlFlag = true;
        if (sysAllocateDeviceDTO.getCiName() != null && sysAllocateDeviceDTO.getPcClass() != null) {
            controlFlag = sysControlService.updateComputerOwnership(
                sysAllocateDeviceDTO.getCiName(), 
                sysAllocateDeviceDTO.getPcClass()
            );
        }

        if(controlFlag && controlAssignFlag){
            log.info("设备领取成功, 订单号: {}", sysAllocateDeviceDTO.getApprovalId());
            return Res.success("设备领取成功");
        } else {
            log.error("设备领取失败, 订单号: {}", sysAllocateDeviceDTO.getApprovalId());
            return Res.error("设备领取失败");
        }
    }

    /**
     * 删除订单（设置状态为已关闭）
     * @param request
     * @return
     */
    @RequestMapping("/deleteOrder")
    @Transactional
    public Res deleteOrder(@RequestBody Map<String, Object> request){
        Long approvalId = Long.valueOf(request.get("approvalId").toString());
        log.info("删除订单请求, 订单号: {}", approvalId);

        boolean flag = sysControlAssignService.deleteOrder(approvalId);

        if(flag){
            log.info("订单删除成功, 订单号: {}", approvalId);
            return Res.success("订单删除成功");
        } else {
            log.error("订单删除失败, 订单号: {}", approvalId);
            return Res.error("订单删除失败");
        }
    }

    /**
     * 关闭分配完成状态的订单并更新电脑状态
     * @param request
     * @return
     */
    @RequestMapping("/deleteOrderWithComputerUpdate")
    @Transactional
    public Res deleteOrderWithComputerUpdate(@RequestBody Map<String, Object> request){
        Long approvalId = Long.valueOf(request.get("approvalId").toString());
        String ciName = (String) request.get("ciName");
        String pcStatus = (String) request.get("pcStatus");
        String pcClass = (String) request.get("pcClass");
        
        log.info("关闭订单并更新电脑状态请求, 订单号: {}, 电脑名: {}, 电脑状态: {}, 电脑归属: {}", 
                approvalId, ciName, pcStatus, pcClass);

        // 1. 关闭订单
        boolean deleteFlag = sysControlAssignService.deleteOrder(approvalId);
        
        // 2. 更新电脑状态和归属情况
        boolean updateFlag = true;
        if (ciName != null && pcStatus != null && pcClass != null) {
            updateFlag = sysControlService.updateComputerStatusAndOwnership(ciName, pcStatus, pcClass);
        }

        if(deleteFlag && updateFlag){
            log.info("订单关闭并电脑状态更新成功, 订单号: {}", approvalId);
            return Res.success("订单关闭成功，电脑状态已更新");
        } else {
            log.error("订单关闭或电脑状态更新失败, 订单号: {}", approvalId);
            return Res.error("订单关闭失败");
        }
    }

    /**
     * 归还共享电脑
     * @param request
     * @return
     */
    @RequestMapping("/returnSharedComputer")
    @Transactional
    public Res returnSharedComputer(@RequestBody Map<String, Object> request){
        Long approvalId = Long.valueOf(request.get("approvalId").toString());
        String ciName = (String) request.get("ciName");
        
        log.info("归还共享电脑请求, 订单号: {}, 电脑名: {}", approvalId, ciName);

        boolean flag = sysControlAssignService.returnSharedComputer(approvalId, ciName);

        if(flag){
            log.info("共享电脑归还成功, 订单号: {}, 电脑名: {}", approvalId, ciName);
            return Res.success("归还成功");
        } else {
            log.error("共享电脑归还失败, 订单号: {}, 电脑名: {}", approvalId, ciName);
            return Res.error("归还失败");
        }
    }
}
