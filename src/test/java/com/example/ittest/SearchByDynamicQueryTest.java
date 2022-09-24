package com.example.ittest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.example.common.constant.CollName.CUSTOMER;
import static com.example.helper.DataUtil.insertJsonDataToMongo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class SearchByDynamicQueryTest extends BaseItTest{

    @Autowired
    MongoTemplate mongoTemplate;

    String baseUrl = "/customers?";

    @BeforeEach
    void setup() throws IOException {
        insertJsonDataToMongo("src/test/resources/json/crm_customer.json", mongoTemplate, CUSTOMER);
    }

    @AfterEach
    void clear() {
        mongoTemplate.dropCollection(CUSTOMER);
    }

    @Test
    void test_basic_time_range_query_should_return_right() throws Exception {
        MockHttpServletRequestBuilder get = getQuery(timeRangeQuery());
        mockMvc.perform(get)
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.total").value(5))
                .andExpect(jsonPath("data.content").isArray())
                .andExpect(jsonPath("data.size").value(50));
    }

    @Test
    void test_basic_time_range_query_with_page_size_two_should_return_right() throws Exception {
        MockHttpServletRequestBuilder get = getQuery(timeRangeQuery() + pageSizeTwo());
        mockMvc.perform(get)
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.total").value(5))
                .andExpect(jsonPath("data.totalPages").value(3))
                .andExpect(jsonPath("data.size").value(2));
    }

    @Test
    void test_in_query_in_sub_search_object_field_should_also_work() throws Exception {
        MockHttpServletRequestBuilder get = getQuery(dynamicQuery());
        MvcResult result = mockMvc.perform(get)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.total").value(1))
                .andExpect(jsonPath("data.totalPages").value(1))
                .andExpect(jsonPath("data.content").isNotEmpty())
                .andReturn();
        String res = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(res.contains("鸡西"));
    }

    @Test
    void test_dynamic_query_not_in_search_object_field_should_also_work() throws Exception {
        MockHttpServletRequestBuilder get = getQuery(inQuery());
        MvcResult result = mockMvc.perform(get)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.total").value(9))
                .andExpect(jsonPath("data.content").isNotEmpty())
                .andReturn();
        String res = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertFalse(res.contains("跟进结束"));
    }

    MockHttpServletRequestBuilder getQuery(String queries) {
        return MockMvcRequestBuilders.get(baseUrl + queries);
    }

    String inQuery() {
        return "跟进状态=待跟进&跟进状态=跟进中";
    }

    String dynamicQuery() {
        return "归属地市=鸡西";
    }

    String timeRangeQuery() {
        return "录入时间=2022-09-13 00:25:01&录入时间=2022-09-15 23:19:38";
    }

    String pageSizeTwo() {
        return "&pageSize=2";
    }
}
