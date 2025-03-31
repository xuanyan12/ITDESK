package ink.usr.common.model.mysql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysApprovalTokenModel implements Serializable {
    // tokenId
    private Long id;
    // 审批流程id
    private Long flowId;
    // token
    private String token;
    // token创建时间
    private String createdAt;
    // token失效时间
    private String expiredAt;
    // token的使用状态
    private boolean used;
    
}
