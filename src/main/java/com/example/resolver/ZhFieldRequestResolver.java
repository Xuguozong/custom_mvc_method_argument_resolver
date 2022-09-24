package com.example.resolver;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.example.annotation.ZhBindAlias;
import com.example.annotation.ZhBindConvertor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class ZhFieldRequestResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 有 ZhBindConvertor 注解的参数才进行解析转换
        return parameter.hasParameterAnnotation(ZhBindConvertor.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        assert request != null;
        // 获取参数类型，根据参数类型反射创建类型实例
        Class<?> resultType = parameter.getParameterType();
        return buildResultObject(resultType, request);
    }

    private Object buildResultObject(Class<?> resultType, HttpServletRequest request) throws InvocationTargetException,
            NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        String method = request.getMethod();
        // 根据不同的 http 方法，使用不同的参数构建模式
        return switch (method) {
            case "POST" -> buildResultObjectForPost(resultType, request);
            case "GET" -> buildResultObjectForGet(resultType, request);
            default -> throw new IllegalStateException("不支持的 http 方法类型: " + method);
        };
    }

    /**
     * GET 方法直接通过 {@link ServletRequest#getParameterMap()} 方法获取请求参数
     */
    private Object buildResultObjectForGet(Class<?> resultType, HttpServletRequest request) throws NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, List<String>> pMap = new HashMap<>();
        parameterMap.forEach((k, v) -> pMap.put(k, List.of(v)));
        Field[] fields = resultType.getDeclaredFields();
        Class<?> superclass = resultType.getSuperclass();
        Field[] superFields = null;
        if (!superclass.equals(Object.class)) {
            superFields = superclass.getDeclaredFields();
        }
        Object instance = resultType.getDeclaredConstructor(null).newInstance(null);
        setFieldsForGet(fields, instance, parameterMap, pMap);
        if (superFields != null) {
            setFieldsForGet(superFields, instance, parameterMap, pMap);
        }
        return instance;
    }

    private void setFieldsForGet(Field[] fields, Object instance, Map<String, String[]> parameterMap, Map<String,
            List<String>> pMap) throws IllegalAccessException {
        for (Field f : fields) {
            f.setAccessible(true);
            String typeName = f.getGenericType().getTypeName();
            ZhBindAlias bindAlias = f.getAnnotation(ZhBindAlias.class);
            if (Objects.nonNull(bindAlias)) {
                String name = bindAlias.value();
                int index = bindAlias.index();
                String[] values = parameterMap.get(name);
                if (values != null && values.length > 0) {
                    Object convertValue = FieldTypeConvertor.FieldType.of(typeName).convert(values, index);
                    f.set(instance, convertValue);
                }
                pMap.remove(name);
                if ("extras".equals(f.getName()) && !pMap.isEmpty()) {
                    f.set(instance, pMap);
                }
            } else {
                String[] values = parameterMap.get(f.getName());
                if (values != null && values.length > 0) {
                    Object convertValue = FieldTypeConvertor.FieldType.of(typeName).convertFirst(values);
                    f.set(instance, convertValue);
                }
                pMap.remove(f.getName());
            }
        }
    }

    /**
     * POST 方法通过获取请求体 json 字符串转请求对象的方式获取参数信息
     */
    private Object buildResultObjectForPost(Class<?> resultType, HttpServletRequest request) throws NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException, IOException {
        // 验证 header 的 Content-Type 为 application/json 才能进行后续操作
        String contentTypeHeader = request.getHeader(HttpHeaders.CONTENT_TYPE);
        if (StringUtils.isBlank(contentTypeHeader) || !contentTypeHeader.equals(MediaType.APPLICATION_JSON_VALUE)) {
            throw new IllegalStateException("请设置 Content-Type 值为 application/json");
        }
        Field[] fields = resultType.getDeclaredFields();
        Class<?> superclass = resultType.getSuperclass();
        Field[] superFields = null;
        if (!superclass.equals(Object.class)) {
            superFields = superclass.getDeclaredFields();
        }
        Object instance = resultType.getDeclaredConstructor(null).newInstance(null);
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line = reader.readLine();
            while (StringUtils.isNotBlank(line)) {
                sb.append(line);
                line = reader.readLine();
            }
            log.info("uri:{},请求体参数:{}", request.getRequestURI(), sb);
            JSONObject body = JSON.parseObject(sb.toString());
            setFields(fields, body, instance);
            setFields(superFields, body, instance);
        }
        return instance;
    }

    private void setFields(Field[] fields, JSONObject source, Object target) throws IllegalAccessException {
        if (Objects.isNull(fields) || fields.length == 0) return;
        for (Field f : fields) {
            f.setAccessible(true);
            String fName = f.getName();
            String typeName = f.getGenericType().getTypeName();
            ZhBindAlias bindAlias = f.getAnnotation(ZhBindAlias.class);
            if (Objects.nonNull(bindAlias)) {
                String name = bindAlias.value();
                int index = bindAlias.index();
                Object o = source.get(name);
                if ("extras".equals(fName)) {
                    f.set(target, jsonObjectToMap(source));
                }
                if (Objects.isNull(o)) continue;
                Object convertValue = FieldTypeConvertor.FieldType.of(typeName).convertJsonObject(o, index, name);
                f.set(target, convertValue);
                source.remove(name);
            } else {
                Object o = source.get(fName);
                if (Objects.isNull(o)) continue;
                Object convertValue = FieldTypeConvertor.FieldType.of(typeName).convertJsonObject(o, 0, fName);
                f.set(target, convertValue);
                source.remove(fName);
            }
        }
    }

    private Map<String, List<String>> jsonObjectToMap(JSONObject object) {
        TypeReference<List<String>> type = new TypeReference<>(){};
        Map<String, List<String>> value = new HashMap<>();
        object.forEach((k, v) -> {
            List<String> list = JSON.parseObject(v.toString(), type);
            value.put(k, list);
        });
        return value;
    }
}
