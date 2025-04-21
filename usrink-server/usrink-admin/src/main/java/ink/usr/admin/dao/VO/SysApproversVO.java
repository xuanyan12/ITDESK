package ink.usr.admin.dao.VO;

import ink.usr.common.model.mysql.SysControlModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysApproversVO {
    // username用户名
    private String username;
    // aprrover1(假设仅有两个审批人的情况下)
    private String approver1;
    // approver2
    private String approver2;
    // status1
    private String status1;
    // status2
    private String status2;

}
