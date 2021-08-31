package com.step.pdf.demo.screen;

import com.step.pdf.demo.screen.param.ScreenshotParam;
import com.step.pdf.demo.screen.param.ScreenshotResult;
import com.step.pdf.demo.screen.provider.ScreenshotProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/8/24
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/8/24
 */
@Component
public class ScreenshotManager {

    @Autowired
    public List<ScreenshotProvider> providers = new ArrayList<ScreenshotProvider>();

    /**
     * 执行屏幕裁剪
     *
     * @throws Exception
     */
    public ScreenshotResult clip(ScreenshotParam param) throws Exception {
        for (ScreenshotProvider provider : providers) {
            if (!provider.support(param)) {
                continue;
            }
            return provider.clip(param);
        }
        return null;
    }
}
