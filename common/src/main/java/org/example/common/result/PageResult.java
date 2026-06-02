package org.example.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

/**
 * 分页响应封装
 */
@Data
public class PageResult<T> {

    private long total;
    private long pages;
    private long current;
    private long size;
    private List<T> records;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.total = page.getTotal();
        result.pages = page.getPages();
        result.current = page.getCurrent();
        result.size = page.getSize();
        result.records = page.getRecords();
        return result;
    }

    public static <T> PageResult<T> of(long total, long current, long size, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.total = total;
        result.current = current;
        result.size = size;
        result.pages = (total + size - 1) / size;
        result.records = records;
        return result;
    }
}
