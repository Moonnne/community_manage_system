package edu.tut.cms.common.result;

import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private Integer pages;
    private List<T> records;

    public static <T> PageResult<T> of(List<T> list) {
        PageInfo<T> pageInfo = new PageInfo<>(list);
        PageResult<T> result = new PageResult<>();
        result.setTotal(pageInfo.getTotal());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setPages(pageInfo.getPages());
        result.setRecords(pageInfo.getList());
        return result;
    }
}
