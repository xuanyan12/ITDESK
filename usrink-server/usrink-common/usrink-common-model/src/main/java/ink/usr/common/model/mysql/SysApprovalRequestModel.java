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
public class SysApprovalRequestModel implements Serializable {
    // 审批请求id
    private Long approvalId;
    // 申请人id
    private Long applicant;
    // 设备大类
    private String deviceCategory;
    // 设备小类
    private String deviceType;
    // 设备名称
    private String deviceName;
    // 设备数量
    private String quantity;
    // 申请理由
    private String reason;
    // 请求有效期（过期时间）
    private String timestamp;
    // 当前审批状态
    private String status;
    // 审批请求时间
    private String createdAt;
    // 审批请求更新时间
    private String updatedAt;

}
