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
        
        // 设置NT账号，但需要使用setLastLeastUserNtAccount
        if (sysControlAssignVO.getNtAccount() != null) {
            queryModel.setLastLeastUserNtAccount(sysControlAssignVO.getNtAccount());
        }
        
        // 调用服务层的查询方法
        List<SysControlAssignModel> sysControlAssignModelList = sysControlAssignService.getControlAssignList(queryModel);
        List<SysControlAssignListVO> sysControlAssignListVOList = new ArrayList<SysControlAssignListVO>();
        for(SysControlAssignModel singleVO : sysControlAssignModelList){
            SysControlAssignListVO sysControlAssignListVO = new SysControlAssignListVO();
            BeanUtils.copyProperties(singleVO, sysControlAssignListVO);
            if(singleVO.getApplicant() != null){
                sysControlAssignListVO.setUser(sysUserMapper.getUserNickNameByUserId(singleVO.getApplicant()));
            }
            if(singleVO.getLastLeastUserNtAccount() != null){
                sysControlAssignListVO.setLastLeastUser(sysUserMapper.getUserInfoByUserName(singleVO.getLastLeastUserNtAccount()).getUserNick());
            }

            if(singleVO.getAssignor() != null){
                sysControlAssignListVO.setAssigner(sysUserMapper.getUserInfoByUserName(singleVO.getAssignor()).getUserNick());
            }
            sysControlAssignListVOList.add(sysControlAssignListVO);
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
}
