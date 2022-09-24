package com.example.common.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;

@Data
@Schema(name = "分页结果")
@Accessors(chain = true)
public final class PageResult<T> {

    private Long total;
    private Integer totalPages;
    private Integer current;
    private Integer size;
    private List<T> content;

    public static <T> PageResult<T> of(Page<T> page) {
        return new PageResult<T>()
                .setTotal(page.getTotalElements())
                .setTotalPages(page.getTotalPages())
                .setCurrent(page.getNumber() + 1)
                .setSize(page.getSize())
                .setContent(page.getContent());
    }

    public static <T> PageResult<T> of(long total, PageParam param, List<T> content) {
        return of(total, param.getCurrent(), param.getSize(), content);
    }

    public static <T> PageResult<T> of(long total, int current, int pageSize, List<T> content) {
        if (total < 1 || current < 0 || pageSize < 1) return empty();
        Integer size = pageSize;
        Integer pages;
        if (total <= size) {
            pages = 1;
        } else {
            pages = (int) total / size + 1;
        }
        return new PageResult<T>()
                .setTotal(total)
                .setTotalPages(pages)
                .setCurrent(current)
                .setSize(size)
                .setContent(content);
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<T>()
                .setTotal(0L)
                .setTotalPages(0)
                .setCurrent(0)
                .setSize(0)
                .setContent(Collections.emptyList());
    }
}
