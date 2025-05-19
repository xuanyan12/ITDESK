package ink.usr.admin.service.Impl;

import ink.usr.admin.dao.DTO.SysAllocateDeviceDTO;
import ink.usr.admin.dao.DTO.SysControlAssignDTO;
import ink.usr.admin.dao.DTO.SysControlRecordDTO;
import ink.usr.admin.dao.DTO.SysControlRecordQueryDTO;
import ink.usr.admin.mapper.SysApplyMapper;
import ink.usr.admin.mapper.SysControlAssignMapper;
import ink.usr.admin.mapper.SysControlMapper;
import ink.usr.admin.mapper.SysUserMapper;
import ink.usr.admin.service.SysControlService;
import ink.usr.common.core.domain.Dict;
import ink.usr.common.model.mysql.SysApprovalRequestModel;
import ink.usr.common.model.mysql.SysControlAssignModel;
import ink.usr.common.model.mysql.SysControlModel;
import ink.usr.common.model.mysql.SysUserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SysControlServiceImpl implements SysControlService {

    @Autowired
    private SysControlMapper sysControlMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysControlAssignMapper sysControlAssignMapper;
    @Autowired
    private SysApplyMapper sysApplyMapper;

    public List<SysControlModel> selectSysControlList(SysControlModel sysControlModel) {
        List<SysControlModel> controlList = sysControlMapper.selectSysControlList(sysControlModel);
        return controlList;
    }

    public int selectCountNum(SysControlModel sysControlModel) {
        int num = sysControlMapper.selectCountNum(sysControlModel);
        return num;
    }

    public boolean updateSysControl(SysControlModel sysControlModel) {
        // 1.先记录原有的数据
        LocalDateTime now = LocalDateTime.now();
        // 格式化为字符串：yyyy-MM-dd HH:mm:ss
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = now.format(formatter);

        SysControlRecordDTO sysControlRecordDTO = new SysControlRecordDTO();
        BeanUtils.copyProperties(sysControlModel, sysControlRecordDTO);
        sysControlRecordDTO.setUpdateTime(formattedTime);
        sysControlMapper.updateSysControlRecord(sysControlRecordDTO);
        // 2.再更新新的数据
        boolean flag = sysControlMapper.updateSysControl(sysControlModel);
        return flag;
    }

    public boolean deleteSysControl(long id) {
        boolean flag = sysControlMapper.deleteSysControl(id);
        return flag;
    }

    @Override
    public SysControlModel getInternalComputerByUserName(String userName) {
        SysControlModel sysControlModel = sysControlMapper.getInternalComputerByUserName(userName);
        return sysControlModel;
    }

    @Override
    public List getComputerListByUserName(String userName) {
        List<SysControlModel> sysControlModelList = sysControlMapper.getComputerListByUserName(userName);
        List controlNameList = new ArrayList<>();
        String controlName = null;
        for(SysControlModel model : sysControlModelList){
            controlName = model.getCiName();
            controlNameList.add(controlName);
        }
        return controlNameList;
    }

    @Override
    public SysControlModel getComputerInfoByCiName(String ciName) {
        SysControlModel sysControlModel = sysControlMapper.getComputerInfoByCiName(ciName);
        return sysControlModel;
    }

    @Override
    public List<SysControlModel> selectNonScrappedComputersByCiName(String ciName) {
        return sysControlMapper.selectNonScrappedComputersByCiName(ciName);
    }

    @Override
    public boolean insertSysControl(SysControlModel sysControlModel) {
        return sysControlMapper.insertSysControl(sysControlModel);
    }

    @Override
    public List<SysControlModel> getComputerByInfo(SysControlAssignDTO sysControlAssignModel) {
        List<SysControlModel> sysControlModel = sysControlMapper.getComputerByInfo(sysControlAssignModel);
        return sysControlModel;
    }

    @Override
    public boolean allocateDevice(SysAllocateDeviceDTO sysAllocateDeviceDTO) {
        // 1.电脑表sys_control进行更改,根据ciName找到对应的需要修改的行数据
        SysControlModel sysControlModel = sysControlMapper.getComputerInfoByCiName(sysAllocateDeviceDTO.getCiName());
        SysControlModel originalSysControlModel = new SysControlModel();

        if(sysControlModel!=null){
            BeanUtils.copyProperties(sysControlModel, originalSysControlModel);
            LocalDateTime now = LocalDateTime.now();
            // 格式化为字符串：yyyy-MM-dd HH:mm:ss
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = now.format(formatter);
            // pcStatus修改为In Use
            sysControlModel.setPcStatus("In Use");
            // 通过applicant找到ntAccount并赋值进去
            String userNtAccount = sysUserMapper.getUserInfoByUserName(sysUserMapper.getNameByUserId(sysAllocateDeviceDTO.getApplicant())).getResponsibility();
            sysControlModel.setNtAccount(userNtAccount);
            // 如果isTemp为1，将temp置为1，否则置为0。
            if(sysAllocateDeviceDTO.getIsTemp() != null){
                if(sysAllocateDeviceDTO.getIsTemp() == 1){
                    sysControlModel.setTemp(1);
                }
                else {
                    sysControlModel.setTemp(0);
                }
            }
            // 通过ntAccount找到对应的数据，将用户字段赋值进去
            SysUserModel userInfo = sysUserMapper.getUserInfoByUserName(userNtAccount);
            if(userInfo!=null){

                // 解析userNick设置firstName和lastName
                if (userInfo.getUserNick() != null && !userInfo.getUserNick().isEmpty()) {
                    String userNick = userInfo.getUserNick();

                    // 处理FIXED-TERM前缀
                    if (userNick.startsWith("FIXED-TERM ")) {
                        userNick = userNick.substring("FIXED-TERM ".length()).trim();
                    }
                    // 处理EXTERNAL前缀，格式如: "EXTERNAL Chen, Zhanjun (XXXX)"
                    else if (userNick.startsWith("EXTERNAL ")) {
                        userNick = userNick.substring("EXTERNAL ".length()).trim();
                    }
                    // 处理[Blue color]前缀，格式如: "[Blue color] CHENG Xiaoyu (SES-MFO-STG3)"
                    else if (userNick.startsWith("[Blue color] ")) {
                        userNick = userNick.substring("[Blue color] ".length()).trim();
                    }

                    // 先分离出括号部分
                    int bracketIndex = userNick.indexOf(" (");
                    if (bracketIndex > 0) {
                        userNick = userNick.substring(0, bracketIndex);
                    }

                    // 分离姓和名
                    if (userInfo.getUserNick().startsWith("[Blue color] ")) {
                        // [Blue color] 格式使用空格分隔，FirstName LastName形式
                        String[] nameParts = userNick.split(" ", 2);
                        if (nameParts.length == 2) {
                            // 姓氏是第二部分
                            sysControlModel.setLastName(nameParts[1]);
                            // 名字是第一部分
                            sysControlModel.setFirstName(nameParts[0]);
                        } else {
                            // 如果不符合预期格式，使用整个userNick作为lastName
                            sysControlModel.setLastName(userNick);
                        }
                    } else {
                        // 普通格式使用逗号分隔，LastName, FirstName形式
                        String[] nameParts = userNick.split(", ");
                        if (nameParts.length == 2) {
                            // 姓氏是第一部分
                            sysControlModel.setLastName(nameParts[0]);
                            // 名字是第二部分
                            sysControlModel.setFirstName(nameParts[1]);
                        } else {
                            // 如果不符合预期格式，使用整个userNick作为lastName
                            sysControlModel.setLastName(userNick);
                        }
                    }
                }
                if(userInfo.getEmail()!=null){
                    sysControlModel.setEmailAddress(userInfo.getEmail());
                }
                if(userInfo.getPhone()!=null){
                    sysControlModel.setTelephone(userInfo.getPhone());
                }
                if(userInfo.getDepartment()!=null){
                    sysControlModel.setDepartment(userInfo.getDepartment());
                }
                if(userInfo.getCostCenter()!=null){
                    sysControlModel.setCostCenter(userInfo.getCostCenter());
                }
            }
            // 如果申请前有电脑且为In Use，需要置为Waiting for Return，且新电脑归属情况与之前保持一致
            Long approvalId = sysAllocateDeviceDTO.getApprovalId();
            if(approvalId!=null){
                SysApprovalRequestModel approvalRequestModel = sysApplyMapper.getByApprovalId(approvalId);
                String ciName = approvalRequestModel.getCiName();

                SysControlModel computerInfo = null;
                SysControlModel originalComputerInfo = new SysControlModel();
                // 如果之前有电脑，且申请的电脑归属情况是Internal User的话，原来的电脑要设置为Waiting for Return
                // 加一个，且不是其他申请
                if(ciName!=null && !"申请新电脑".equals(ciName)){
                    computerInfo = sysControlMapper.getComputerInfoByCiName(ciName);
                    // 之前有电脑且申请的电脑归属情况是Internal User时进行修改
                    if(computerInfo!=null){
                        BeanUtils.copyProperties(computerInfo, originalComputerInfo);
                        // 1.先将老归属情况赋值给新电脑
                        String pcClass = computerInfo.getPcClass();
                        if(pcClass!=null){
                            sysControlModel.setPcClass(pcClass);
                        }
                        // 2.再将原电脑使用状态为In Use且电脑归属情况为Internal User的电脑设置为Waiting for Return
                        if("In Use".equals(computerInfo.getPcStatus()) && "Internal User".equals(pcClass)){
                            computerInfo.setPcClass("Waiting for Return");
                        }
                    }

                } else {
                    // 原来没有电脑时
                    // 实习生，蓝领，外服直接设置为对应的标识，如果不是的话（正职）就设置为Internal User

                }
                if(computerInfo!=null){
                    // 1.记录原数据
                    SysControlRecordDTO sysControlRecordDTO = new SysControlRecordDTO();
                    BeanUtils.copyProperties(originalComputerInfo, sysControlRecordDTO);
                    sysControlRecordDTO.setUpdateTime(formattedTime);
                    sysControlMapper.updateSysControlRecord(sysControlRecordDTO);
                    // 2.更新新的数据
                    sysControlMapper.updateSysControl(computerInfo);
                }
            }
            SysControlRecordDTO sysControlRecordDTO2 = new SysControlRecordDTO();
            BeanUtils.copyProperties(originalSysControlModel, sysControlRecordDTO2);
            sysControlRecordDTO2.setUpdateTime(formattedTime);
            sysControlMapper.updateSysControlRecord(sysControlRecordDTO2);
            boolean flag = sysControlMapper.updateSysControl(sysControlModel);
            return flag;
        }
        return false;
    }

    @Override
    public Dict getControlRecordList(SysControlRecordQueryDTO queryDTO) {
        // 检查并设置分页偏移量
        long offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        queryDTO.setPageNum(offset);
        
        // 获取记录列表
        List<SysControlRecordDTO> recordList = sysControlMapper.getControlRecordList(queryDTO);
        
        // 获取记录总数
        int totalCount = sysControlMapper.getControlRecordCount(queryDTO);
        
        // 构建返回对象
        Dict result = Dict.create()
            .set("list", recordList)
            .set("total", totalCount)
            .set("pageSize", queryDTO.getPageSize())
            .set("pageNum", queryDTO.getPageNum() / queryDTO.getPageSize() + 1);
            
        return result;
    }
}
