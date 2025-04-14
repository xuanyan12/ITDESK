package ink.usr.admin.dao.VO;

import ink.usr.common.model.mysql.SysControlModel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

}
