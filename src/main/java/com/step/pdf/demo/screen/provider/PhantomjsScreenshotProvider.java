package com.step.pdf.demo.screen.provider;

import com.step.pdf.demo.screen.util.OSUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @description: 通过Phantomjs来执行JavaScript文件
 * @author: fesine
 * @createTime:2021/8/24
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/8/24
 */
@Slf4j
public abstract class PhantomjsScreenshotProvider implements ScreenshotProvider {

    protected static String BLANK = " ";

    @Value("${screenshotDir:}")
    protected String screenshotDir;
    @Value("${phantomjsPath:}")
    protected String phantomjsPath;
    // rasterize.js
    protected String jsFilePath;

    @PostConstruct
    public void init() {
        if (StringUtils.isBlank(screenshotDir)) {
            try {
                File classPathDir = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX);
                File dir = new File(classPathDir.getAbsolutePath() + "/screenshot/phantomjs" +
                        "/echarts/images");
                if (!dir.exists() || !dir.isDirectory()) {
                    dir.mkdirs();
                }
                screenshotDir = dir.getAbsolutePath();
                log.info("screenshotDir:{}", screenshotDir);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (StringUtils.isBlank(phantomjsPath)) {
            try {
                File exeFile = null;
                if (OSUtil.isUnix() || OSUtil.isMac()) {
                    exeFile = ResourceUtils
                            .getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "drivers/phantomjs/v2.1" +
                                    ".1/linux64/phantomjs");
                } else {
                    exeFile = ResourceUtils.getFile(
                            ResourceUtils.CLASSPATH_URL_PREFIX + "drivers/phantomjs/v2.1" +
                                    ".1/window/phantomjs.exe");
                }
                phantomjsPath = exeFile.getAbsolutePath();
                log.info("phantomjsPath:{}", phantomjsPath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (StringUtils.isBlank(jsFilePath)) {
            try {
                // 执行的JS文件路径
                File jsFile = ResourceUtils.getFile("classpath:static/js/rasterize.js");
                // 生成的图片名称
                jsFilePath = jsFile.getAbsolutePath();
                log.info("jsFilePath:{}", jsFilePath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // 生成图片的所存放路径
    protected String getRandomImagePath() {
        String outPath =
                screenshotDir + File.separator + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + "-" + new Random().nextInt() + ".png";
        return outPath;
    }

    // 执行cmd命令
    protected String commandLine(String url, String screenshotPath) {
        return phantomjsPath + BLANK + jsFilePath + BLANK + url + BLANK + screenshotPath;
    }

    protected void exeCommandLine(String commandLine) throws IOException {
        // Java中使用Runtime和Process类运行外部程序
        Process process = Runtime.getRuntime().exec(commandLine);
        InputStream inputStream = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String tmp = "";
        StringBuffer sb = new StringBuffer();
        while ((tmp = reader.readLine()) != null) {
            sb.append(tmp);
        }
        close(process, reader);
    }

    // 关闭命令
    protected void close(Process process, BufferedReader bufferedReader) throws IOException {
        if (bufferedReader != null) {
            bufferedReader.close();
        }
        if (process != null) {
            process.destroy();
            process = null;
        }
    }

    public String getScreenshotDir() {
        return screenshotDir;
    }

    public void setScreenshotDir(String screenshotDir) {
        this.screenshotDir = screenshotDir;
    }

    public String getPhantomjsPath() {
        return phantomjsPath;
    }

    public void setPhantomjsPath(String phantomjsPath) {
        this.phantomjsPath = phantomjsPath;
    }

    public String getJsFilePath() {
        return jsFilePath;
    }

    public void setJsFilePath(String jsFilePath) {
        this.jsFilePath = jsFilePath;
    }
}
