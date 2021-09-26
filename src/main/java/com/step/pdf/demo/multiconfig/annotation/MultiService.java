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
@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MultiService {

    /**
     * 对于同一接口，多种实现，可以通过name来指定名称
     * @return
     */
    String name() default "";

    /**
     * 需要注册的服务组，默认空，注册全部组
     * @return
     */
    String[] group() default {};

    /**
     * 配置文件，通过指定配置文件，将服务注册成对应多组实例
     * @return
     */
    Class<?> config() default Object.class;

    /**
     * 实例完成依赖注入后，可通过此标记，执行初始化方法
     * 暂时只支持无参方法
     * @return
     */
    String initMethod() default "";

}
