package com.step.pdf.demo.util;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.Map;

@Slf4j
public class PdfUtil {

    /**
     * 通过thymeleaf渲染模板生成PDF
     * @param os
     * @param templateName
     * @param variables
     * @param fontDirectory
     * @param imgDirectory
     * @throws Exception
     */
    public static void buildPdfByThymeleaf(OutputStream os, String prefix, String templateName,String suffix, Map<String, Object> variables, String fontDirectory, String imgDirectory) throws Exception {
        //OutputStream os = response.getOutputStream();
        //OutputStream os = new FileOutputStream("target/test.pdf");
        String example = ThymeleafUtil.generateString(prefix, templateName, suffix, variables);
        buildPdf(os, example,fontDirectory,imgDirectory);
    }

    /**
     * 通过freemarker渲染模板生成PDF
     * @param os
     * @param templateDirectory
     * @param templateName
     * @param variables
     * @param fontDirectory
     * @param imgDirectory
     * @throws Exception
     */
    public static void buildPdfByFreemarkerInDirectory(OutputStream os, String templateDirectory, String templateName,
                                            Map<String, Object> variables, String fontDirectory, String imgDirectory) throws Exception {
        String html = FreemarkerUtil.generateStringInDirectory(templateDirectory, templateName,  variables);
        buildPdf(os, html,fontDirectory,imgDirectory);
    }

    /**
     * 通过freemarker渲染模板生成PDF
     * @param os
     * @param templateDirectory
     * @param templateName
     * @param variables
     * @param fontDirectory
     * @param imgDirectory
     * @throws Exception
     */
    public static void buildPdfByFreemarkerInClassLoader(OutputStream os, String templateDirectory, String templateName,
                                            Map<String, Object> variables, String fontDirectory, String imgDirectory) throws Exception {
        String html = FreemarkerUtil.generateStringInClassLoader(templateDirectory, templateName,  variables);
        buildPdf(os, html,fontDirectory,imgDirectory);
    }

    /**
     * 使用openhtmltopdf根据html内容生成pdf文件
     * @param os
     * @param html
     * @param fontDirectory
     * @param imgDirectory
     * @throws Exception
     */
    public static void buildPdf(OutputStream os, String html,String fontDirectory,String imgDirectory) throws Exception {
        PdfRendererBuilder builder = new PdfRendererBuilder();
        addFont(builder,fontDirectory);
        builder.useFastMode();
        builder.withHtmlContent(html, ResourceUtils.getURL(imgDirectory).toString());
        builder.toStream(os);
        builder.run();
    }


    /**
     * 添加字体库
     *
     * @param builder
     * @param dir
     */
    private static void addFont(PdfRendererBuilder builder, String dir) {
        File f = new File(dir);
        if (f.isDirectory()) {
            File[] files = f.listFiles((dir1, name) -> {
                String lower = name.toLowerCase();
                    //lower.endsWith(".otf") ||  对otf库的字体支持有问题，暂时屏蔽
                return lower.endsWith(".ttf") || lower.endsWith(".ttc");
            });
            for (File subFile : files) {
                String fontFamily = subFile.getName().substring(0, subFile.getName().lastIndexOf("."));
                builder.useFont(subFile, fontFamily);
            }
        }
    }
}
