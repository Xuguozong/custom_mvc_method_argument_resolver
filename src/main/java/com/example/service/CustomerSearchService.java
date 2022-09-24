package com.example.service;

import cn.hutool.extra.servlet.ServletUtil;
import com.example.common.rest.PageResult;
import com.example.dto.customer.CustomerSearchReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

import static com.example.common.constant.CollName.CUSTOMER;

@Service
@RequiredArgsConstructor
public class CustomerSearchService {

    private final MongoTemplate mongoTemplate;

    public PageResult<LinkedHashMap> search(CustomerSearchReq searchReq) {
        Criteria criteria = searchReq.getQueryCriteria();
        Query query = Query.query(criteria);
        long total = mongoTemplate.count(query.skip(-1).limit(-1), CUSTOMER);
        if (total < 1) return PageResult.empty();

        // 根据实际业务返回有限个字段
        // query.fields().include(selectors.toArray(new String[0]));

        // page
        PageRequest pageRequest = PageRequest.of(searchReq.getCurrent() - 1, searchReq.getPageSize());
        query.with(pageRequest);

        // sort
        query.with(searchReq.sort());

        // 因为实际业务返回字段不定(由用户自定义)，所以采用 LinkedHashMap 接收
        List<LinkedHashMap> content = mongoTemplate.find(query, LinkedHashMap.class, CUSTOMER);
        return PageResult.of(total, searchReq.getCurrent(), searchReq.getPageSize(), content);
    }
}
