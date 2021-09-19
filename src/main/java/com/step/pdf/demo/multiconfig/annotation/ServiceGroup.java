package com.step.pdf.demo.multiconfig.annotation;

import java.lang.annotation.*;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/9/17
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/17
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ServiceGroup {

    String value();
}
