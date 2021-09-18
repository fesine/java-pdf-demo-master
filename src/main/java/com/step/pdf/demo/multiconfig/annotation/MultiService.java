package com.step.pdf.demo.multiconfig.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/9/17
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/17
 */
@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MultiService {
    /**
     * 需要注册的服务组，默认空，注册全部组
     * @return
     */
    @AliasFor("value")
    String[] group() default {};

    /**
     * 需要注册的服务组，默认空，注册全部组
     * @return
     */
    @AliasFor("group")
    String[] value() default {};
}
