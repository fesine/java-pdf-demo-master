package com.step.pdf.demo.util;

import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Map;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/8/11
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/8/11
 */
@Slf4j
public class ThymeleafUtil {
    /**
     * @param prefix 模板前缀，目录
     * @param templateName 模板文件名不带后缀
     * @param suffix 模板后缀
     * @param datas 渲染数据
     * @return
     */
    public static String generateString(String prefix, String templateName, String suffix,
                                        Map<String, Object> datas) {
        //构造模板引擎
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        //模板所在目录，相对于当前classloader的classpath。
        resolver.setPrefix(prefix);
        //模板文件后缀
        resolver.setSuffix(suffix);
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);
        //构造上下文(Model)
        Context context = new Context();
        context.setVariables(datas);
        //渲染模板
        String result = templateEngine.process(templateName, context);
        log.debug("---->after thymeleaf process.result:{}", result);
        return result;
    }
}
