package com.step.pdf.demo.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * @description: freemarker渲染工具类
 * @author: fesine
 * @createTime:2021/8/9
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/8/9
 */
@Slf4j
public class FreemarkerUtil {

    /**
     * 将freemarker模板渲染返回string
     * @param templateDirectory 模板绝对完整目录
     * @param templateFileName 模板名称带后缀
     * @param datas 渲染数据
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    public static String generateStringInDirectory(String templateDirectory, String templateFileName,
                                                   Map<String, Object> datas)
            throws IOException, TemplateException {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_0);

        // 设置默认编码
        configuration.setDefaultEncoding("UTF-8");

        // 设置模板所在文件夹
        configuration.setDirectoryForTemplateLoading(new File(templateDirectory));
        return processByConfiguration(configuration, templateFileName, datas);
    }

    /**
     * 将freemarker模板渲染返回string
     * @param templateDirectory 模板所有资源目录
     * @param templateFileName 模板名称带后缀
     * @param datas 渲染数据
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    public static String generateStringInClassLoader(String templateDirectory, String templateFileName,
                                                     Map<String, Object> datas)
            throws IOException, TemplateException {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_0);

        // 设置默认编码
        configuration.setDefaultEncoding("UTF-8");

        // 设置模板所在文件夹
        configuration.setClassLoaderForTemplateLoading(FreemarkerUtil.class.getClassLoader(), templateDirectory);
        return processByConfiguration(configuration, templateFileName, datas);
    }

    /**
     * 将freemarker模板渲染返回string
     * @param templateFileName 模板名称带后缀
     * @param datas 渲染数据
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    public static String processByConfiguration(Configuration configuration, String templateFileName,
                                        Map<String, Object> datas)
            throws IOException, TemplateException {
        // 生成模板对象
        Template template = configuration.getTemplate(templateFileName);

        // 将datas写入模板并返回
        try (StringWriter stringWriter = new StringWriter()) {
            template.process(datas, stringWriter);
            stringWriter.flush();
            String result = stringWriter.getBuffer().toString();
            log.debug("---->after freemarker process.result:{}", result);
            return result;
        }
    }


}
