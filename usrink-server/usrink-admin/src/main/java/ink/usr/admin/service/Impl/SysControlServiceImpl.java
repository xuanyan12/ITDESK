package ink.usr.admin.service.Impl;

import ink.usr.admin.dao.DTO.SysAllocateDeviceDTO;
import ink.usr.admin.dao.DTO.SysControlAssignDTO;
import ink.usr.admin.mapper.SysApplyMapper;
import ink.usr.admin.mapper.SysControlAssignMapper;
import ink.usr.admin.mapper.SysControlMapper;
import ink.usr.admin.mapper.SysUserMapper;
import ink.usr.admin.service.SysControlService;
import ink.usr.common.model.mysql.SysApprovalRequestModel;
import ink.usr.common.model.mysql.SysControlAssignModel;
import ink.usr.common.model.mysql.SysControlModel;
import ink.usr.common.model.mysql.SysUserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        if(sysControlModel!=null){
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
                // 如果之前有电脑的话，原来的电脑要设置为Waiting for Return
                if(ciName!=null && !"申请新电脑".equals(ciName)){
                    computerInfo = sysControlMapper.getComputerInfoByCiName(ciName);
                    if(computerInfo!=null){
                        if("In Use".equals(computerInfo.getPcStatus())){
                            computerInfo.setPcClass("Waiting for Return");
                        }
                        String pcClass = computerInfo.getPcClass();
                        if(pcClass!=null){
                            sysControlModel.setPcClass(pcClass);
                        }
                    }

                } else {
                    // 原来没有电脑，设置新的pcClass为Internal User
//                    sysControlModel.setPcClass("Internal User");
                }
                if(computerInfo!=null){
                    sysControlMapper.updateSysControl(computerInfo);
                }
            }
            boolean flag = sysControlMapper.updateSysControl(sysControlModel);
            return flag;
        }
        return false;
    }
}
