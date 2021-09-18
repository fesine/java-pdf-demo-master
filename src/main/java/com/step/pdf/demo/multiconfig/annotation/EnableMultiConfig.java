package com.step.pdf.demo.multiconfig.annotation;

import com.step.pdf.demo.multiconfig.MultiBaseConfig;
import com.step.pdf.demo.multiconfig.MultiConfigRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/9/17
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/17
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(MultiConfigRegister.class)
public @interface EnableMultiConfig {
    /**
     * 扫描包路径
     * @return
     */
    String[] basePackages();

    /**
     * 配置文件入口
     * @return
     */
    Class<? extends MultiBaseConfig>[] configs();
}