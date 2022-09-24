package com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ZhBindAlias {
    /** 字段的中文别名 */
    String value();

    /** 同名的情况下绑定到第几个参数 */
    int index() default 0;

    /** 是否默认添加到 query 条件构建中 */
    boolean includeQuery() default true;
}
