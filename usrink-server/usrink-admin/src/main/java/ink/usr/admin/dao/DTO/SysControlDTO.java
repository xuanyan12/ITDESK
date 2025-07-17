package ink.usr.admin.dao.DTO;

import ink.usr.common.model.mysql.SysControlModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SysControlDTO extends SysControlModel {
    // 分页的第几页
    private int pageNum;
    // 一页的大小
    private int pageSize;
    private String ciName;
    private String ntAccount;
    private String department;
    private String wbsNum;
    // 出厂的终止时间，用于条件查询
    private String lifeCycleEnd;
    // WBS号列表，用于IN查询
    private List<String> wbsNumList;
    // NT账号列表，用于IN查询
    private List<String> ntAccountList;
    // 残值状态查询条件
    private String residualValueStatus;
}
