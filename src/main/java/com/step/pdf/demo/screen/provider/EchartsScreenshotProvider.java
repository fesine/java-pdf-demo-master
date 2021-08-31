package com.step.pdf.demo.screen.provider;

import com.google.gson.Gson;
import com.step.pdf.demo.screen.param.EchartsScreenshotParam;
import com.step.pdf.demo.screen.param.ScreenshotParam;
import com.step.pdf.demo.screen.param.ScreenshotResult;
import com.step.pdf.demo.screen.util.CommandExecUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Base64;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/8/24
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/8/24
 */
@Component
@Slf4j
public class EchartsScreenshotProvider extends PhantomjsScreenshotProvider {
    @PostConstruct
    @Override
    public void init() {
        if (StringUtils.isBlank(jsFilePath)) {
            try {
                // 执行的JS文件路径
                File jsFile = ResourceUtils.getFile("classpath:static/js/echarts-screenshot.js");
                // 生成的图片名称
                jsFilePath = jsFile.getAbsolutePath();
                log.info("jsFilePath:{}", jsFilePath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        super.init();
    }


    @Override
    public boolean support(ScreenshotParam param) {
        return param instanceof EchartsScreenshotParam;
    }

    @Override
    public ScreenshotResult clip(ScreenshotParam param) throws Exception {
        EchartsScreenshotParam echartsScreenshotParam = (EchartsScreenshotParam) param;
        if (echartsScreenshotParam == null || echartsScreenshotParam.getOption() == null) {
            throw new Exception("参数不能为空");
        }
        if (StringUtils.isBlank(jsFilePath)) {
            throw new Exception("执行的js文件不能为空");
        }
        //屏幕截图的保存位置
        String screenshotPath = getRandomImagePath();

        // 实例化
        Gson gson = new Gson();
        // 将map转成JSON
        String paramsJsonStr = gson.toJson(echartsScreenshotParam);
        Base64.Encoder encoder = Base64.getEncoder();
        // 转换为base64编码是为了防止执行指令的时候出问题
        String base64Str = encoder.encodeToString(paramsJsonStr.getBytes("UTF-8"));

        String[] args = new String[3];
        args[0] = jsFilePath;
        args[1] = base64Str;
        args[2] = screenshotPath;
        // 执行脚本
        try {
            String result = CommandExecUtil.execHasResult(phantomjsPath, args, null);
            return new ScreenshotResult(result);
        } catch (Exception e) {
            throw e;
        }
    }
}
