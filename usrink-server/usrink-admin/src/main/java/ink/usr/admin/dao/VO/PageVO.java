package ink.usr.admin.dao.VO;

import lombok.Data;

import java.util.List;

@Data
public class PageVO<T> {
    private List<T> list;
    private long total;
    private int pageNum;
    private int pageSize;

    public static <T> PageVO<T> build(List<T> list, long total, int pageNum, int pageSize) {
        PageVO<T> pageVO = new PageVO<>();
        pageVO.setList(list);
        pageVO.setTotal(total);
        pageVO.setPageNum(pageNum);
        pageVO.setPageSize(pageSize);
        return pageVO;
    }
} 