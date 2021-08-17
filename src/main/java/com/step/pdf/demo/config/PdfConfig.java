package com.step.pdf.demo.config;

import com.step.pdf.demo.util.FileUtil;
import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * @description: 资源目录配置类，默认资源位置在resources下
 * pdf:
 *   static:
 *     template:
 *       dir: pdf/templates
 *       name: image_freemarker.html
 *     font:
 *       dir: pdf/fonts
 *     image:
 *       dir: pdf/img
 *     js:
 *       dir: pdf/js
 *       name: code.js
 *     tmp:
 *       dir: user.dir #java.io.tmpdir
 * @author: fesine
 * @createTime:2021/8/13
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/8/13
 */
@Component
@Data
@ToString
public class PdfConfig {

    /**
     * 访问服务端模板内容
     */
    @Value("${pdf.server.template}")
    private String serverTemplate;

    /**
     * 模板目录
     */
    @Value("${pdf.static.template.dir}")
    private String templateDir;

    /**
     * 模板名称
     */
    @Value("${pdf.static.template.name}")
    private String templateName;

    /**
     * 字体目录
     */
    @Value("${pdf.static.font.dir}")
    private String fontDir;

    /**
     * 字体目录
     */
    @Value("${pdf.static.image.dir}")
    private String imageDir;

    /**
     * js目录
     */
    @Value("${pdf.static.js.dir}")
    private String jsDir;

    /**
     * js文件
     */
    @Value("${pdf.static.js.name}")
    private String jsName;

    /**
     * 静态资源临时目录
     * 可以是java properties中配置或者绝对路径
     */
    @Value("${pdf.static.tmp.dir}")
    private String tmpDir;

    public String getTmpFontDir(){
        return tmpDir + File.separator + fontDir;
    }

    public String getTmpImageDir(){
        return tmpDir + File.separator + imageDir;
    }

    public String getTmpJsDir(){
        return tmpDir + File.separator + jsDir;
    }

    public String getTmpTemplateDir(){
        return tmpDir + File.separator + templateDir;
    }

    public String getTmpJsFile(){
        return getTmpJsDir() + File.separator + jsName;
    }

    /**
     * 初始化目录
     * @throws IOException
     */
    public void init() throws IOException {
        // 复制资源文件到临时目录
        FileUtil.copyResourceToFile(getFontDir(), getTmpFontDir());
        FileUtil.copyResourceToFile(getJsDir(), getTmpJsDir());
        FileUtil.copyResourceToFile(getImageDir(), getTmpImageDir());
        FileUtil.copyResourceToFile(getTemplateDir(), getTmpTemplateDir());
    }
}
