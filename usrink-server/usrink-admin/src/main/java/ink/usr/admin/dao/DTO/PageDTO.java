package ink.usr.admin.dao.DTO;

import lombok.Data;

@Data
public class PageDTO {
    private int pageNum = 1;
    private int pageSize = 10;
} 