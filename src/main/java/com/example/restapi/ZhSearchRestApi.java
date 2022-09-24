package com.example.restapi;

import com.example.annotation.ZhBindConvertor;
import com.example.common.rest.PageResult;
import com.example.common.rest.R;
import com.example.dto.customer.CustomerSearchReq;
import com.example.service.CustomerSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;


@Tag(name = "客户")
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class ZhSearchRestApi {

    private final CustomerSearchService customerSearchService;

    @GetMapping()
    @Operation(summary = "线索列表")
    public R<PageResult<LinkedHashMap>> searchPage(@ZhBindConvertor CustomerSearchReq searchReq) {
        return R.ok(customerSearchService.search(searchReq));
    }
}
