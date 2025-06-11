package ink.usr.common.model.mysql;

import lombok.*;

import java.io.Serializable;

/**
 * OneLink系统管理实体类
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysOnelinkSystemModel implements Serializable {
    
    /**
     * 系统ID，主键
     */
    private Long systemId;
    
    /**
     * 系统名称
     */
    private String systemName;
    
    /**
     * 系统描述
     */
    private String systemDescription;
    
    /**
     * 系统链接
     */
    private String systemUrl;
    
    /**
     * 系统图标
     */
    private String systemIcon;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 是否启用
     */
    private Boolean isActive;
    
    /**
     * 创建时间
     */
    private String createdAt;
    
    /**
     * 更新时间
     */
    private String updatedAt;
} 