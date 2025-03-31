package ink.usr.common.model.mysql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysApproverModel implements Serializable {
    // 审批人id
    private Long approverId;
    // 审批人姓名
    private String name;
    // 审批人邮箱地址
    private String email;
    // 审批人角色权限
    private String role;
    // 部门id
    private Long department;
    // 创建时间
    private String createdAt;
    // 更新时间
    private String updatedAt;
    // userId
    private Long userId;
    
}
