package com.step.pdf.demo.screen.param;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/8/24
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/8/24
 */
public class ScreenshotResult {
    // 截图保存路径
    private String screenshotPath;

    public ScreenshotResult() {
    }

    public ScreenshotResult(ScreenshotResult result) {
        if (result == null) {
            return;
        }
        this.screenshotPath = result.getScreenshotPath();
    }

    public ScreenshotResult(String screenshotPath) {
        this.screenshotPath = screenshotPath;
    }

    public String getScreenshotPath() {
        return screenshotPath;
    }

    public void setScreenshotPath(String screenshotPath) {
        this.screenshotPath = screenshotPath;
    }
}
