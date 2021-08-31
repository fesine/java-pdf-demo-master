package com.step.pdf.demo.screen.provider;

import com.step.pdf.demo.screen.param.ScreenshotParam;
import com.step.pdf.demo.screen.param.ScreenshotResult;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/8/24
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/8/24
 */
public interface ScreenshotProvider {
    /**
     * 根据参数判断是否支持
     */
    boolean support(ScreenshotParam param);

    /**
     * 执行屏幕裁剪
     */
    ScreenshotResult clip(ScreenshotParam param) throws Exception;
}
