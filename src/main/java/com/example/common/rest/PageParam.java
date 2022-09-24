package com.example.common.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "分页请求参数")
public final class PageParam {

    @Min(1)
    private Integer current = 1;

    @Min(1)
    private Integer size = 10;
}
