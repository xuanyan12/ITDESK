package ink.usr.common.core.utils;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import ink.usr.common.core.constants.Constants;

public class PageUtil {

    /**
     * 开始分页
     *
     * @param <E> 分页对象
     * @return 分页对象
     */
    public static <E> Page<E> startPage() {

        // 获取分页参数
        String pageNum = ServletUtil.getParameter(Constants.PAGE_NUM);
        // 获取分页大小
        String pageSize = ServletUtil.getParameter(Constants.PAGE_SIZE);

        // 设置默认值，如果参数为null或空字符串
        int pageNumInt = 1;
        int pageSizeInt = 10;

        // 尝试解析页码，如果解析失败则使用默认值
        if (pageNum != null && !pageNum.trim().isEmpty()) {
            try {
                pageNumInt = Integer.parseInt(pageNum);
            } catch (NumberFormatException e) {
                // 解析失败时使用默认值
            }
        }

        // 尝试解析每页大小，如果解析失败则使用默认值
        if (pageSize != null && !pageSize.trim().isEmpty()) {
            try {
                pageSizeInt = Integer.parseInt(pageSize);
            } catch (NumberFormatException e) {
                // 解析失败时使用默认值
            }
        }

        return PageHelper.startPage(pageNumInt, pageSizeInt);
    }

    /**
     * 开始分页（直接传递参数）
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param <E> 分页对象
     * @return 分页对象
     */
    public static <E> Page<E> startPage(int pageNum, int pageSize) {
        return PageHelper.startPage(pageNum, pageSize);
    }

}
