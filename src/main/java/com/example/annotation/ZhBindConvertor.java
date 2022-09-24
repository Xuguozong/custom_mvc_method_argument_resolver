package com.example.annotation;

import java.lang.annotation.*;

/**
 * 中文请求参数转换,请求实体字段配合 {@link ZhBindAlias} 使用
 * 实现原理：自定义实现 mvc 参数转换器 {@link org.springframework.web.method.support.HandlerMethodArgumentResolver}
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ZhBindConvertor {

    boolean required() default true;
}
