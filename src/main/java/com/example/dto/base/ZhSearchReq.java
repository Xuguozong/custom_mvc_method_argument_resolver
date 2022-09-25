package com.example.dto.base;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSONObject;
import com.example.annotation.ZhBindAlias;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;

import javax.validation.constraints.Min;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

/**
 * 公共查询参数
 */
@Data
@Accessors(chain = true)
public class ZhSearchReq {

    @ZhBindAlias("录入时间")
    private String startTime;

    @ZhBindAlias(value = "录入时间", index = 1)
    private String endTime;

    /** 自定义查询字段(对应变化的部分) */
    @ZhBindAlias(value = "extra", includeQuery = false)
    private Map<String, List<String>> extras;

    /**
     * 排序字段, json 字符串，示例：
     * {“录入时间”: "ascend", "分配时间": "descend"}
     * 按录入时间升序，分配时间降序排列，默认升序
     */
    private String sorter;

    @Min(1)
    @ZhBindAlias(value = "current", includeQuery = false)
    private Integer current = 1;

    @Min(1)
    @ZhBindAlias(value = "pageSize", includeQuery = false)
    private Integer pageSize = 50;

    /** 默认按录入时间降序排列 */
    private Sort defaultSort() {
        return Sort.by("录入时间").descending();
    }

    /**
     * 获取子类字段的值,子类需要有 getter 方法
     * @deprecated
     */
    private void filedCriteriaForSubClass(Criteria criteria, Field f) {
        Method[] methods = this.getClass().getDeclaredMethods();
        String name = f.getName();
        Stream.of(methods).filter(m -> m.getName().startsWith("get") && m.getName().toLowerCase().contains(name.toLowerCase()))
                .findAny()
                .ifPresent(m -> {
                    try {
                        Object value = m.invoke(this, null);
                        // filedCriteria(criteria, f, value);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });
    }

    public Sort sort() {
        String sorter = getSorter();
        if (StringUtils.isEmpty(sorter)) return defaultSort();
        HashMap sorterMap = JSONObject.parseObject(sorter, HashMap.class);
        if (sorterMap.isEmpty()) return defaultSort();
        List<Sort.Order> orders = new ArrayList<>(sorterMap.size());
        sorterMap.forEach((k, v) -> {
            Sort.Order order;
            if (v.equals("descend")) {
                order = Sort.Order.desc(k.toString());
            } else {
                order = Sort.Order.asc(k.toString());
            }
            orders.add(order);
        });
        if (orders.isEmpty()) return defaultSort();
        return Sort.by(orders);
    }

    /**
     * 由于数据库选型原因，与 MongoDB 查询条件强绑定
     */
    public Criteria getQueryCriteria() {
        Criteria criteria = new Criteria();
        Class<? extends ZhSearchReq> reqClass = this.getClass();
        Class<?> superclass = reqClass.getSuperclass();
        Field[] superFields = new Field[0];
        if (!superclass.equals(Object.class)) {
            superFields = superclass.getDeclaredFields();
        }
        Field[] fields = reqClass.getDeclaredFields();
        Arrays.stream(fields).forEach(f -> filedCriteria(criteria, f));
        Arrays.stream(superFields).forEach(f -> filedCriteria(criteria, f));
        if (StringUtils.isNotBlank(getStartTime()) && StringUtils.isNotBlank(getEndTime())) {
            criteria.and("录入时间").gte(getStartTime()).lte(getEndTime());
        }
        // 对于动态参数的处理
        if (MapUtil.isNotEmpty(extras)) {
            extras.forEach((k, v) -> {
                if (CollUtil.isNotEmpty(v)) {
                    // warning 与具体业务逻辑相关,可以前端确定公用的处理模型
                    if (!v.contains("全选")) {
                        if (v.size() == 1) {
                            // 根据业务逻辑确定单值的查询定义
                            criteria.and(k).is(v.get(0));
                        } else {
                            // 根据业务逻辑确定多值的查询定义
                            criteria.and(k).in(v);
                        }
                    }
                }
            });
        }
        return criteria;
    }

    /**
     * 对于单个查询参数的处理
     */
    protected void filedCriteria(Criteria criteria, Field f) {
        String fName = f.getName();
        if ("sorter".equals(fName)) return;
        String typeName = f.getGenericType().getTypeName();
        ZhBindAlias alias = f.getAnnotation(ZhBindAlias.class);
        Object value = null;
        try {
            f.setAccessible(true);
            value = f.get(this);
        } catch (IllegalAccessException e) {
            // 内部调用，不会有问题
            e.printStackTrace();
        }
        if (Objects.isNull(value)) return;
        if (Objects.nonNull(alias)) {
            if (!alias.includeQuery()) return;
            String where = alias.value();
            if ("java.lang.String".equals(typeName) && !where.contains("时间")) {
                // 单值采用前缀匹配查询
                criteria.and(where).regex("^" + value);
            } else if ("java.util.List<java.lang.String>".equals(typeName) && !where.contains("时间")) {
                List<String> values = (List<String>) value;
                if (!values.contains("全选")) {
                    // 多值采用 $in 查询
                    criteria.and(where).in(values);
                }
            }
        } else {
            // 等值查询
            criteria.and(fName).is(value);
        }
    }
}
