package com.example.dto.customer;

import com.example.annotation.ZhBindAlias;
import com.example.dto.base.ZhSearchReq;
import lombok.Data;
import java.util.List;

@Data
public class CustomerSearchReq extends ZhSearchReq {

    @ZhBindAlias("跟进状态")
    private List<String> followState;

    @ZhBindAlias("手机号")
    private String mobile;

    @ZhBindAlias("姓名")
    private String name;

}
