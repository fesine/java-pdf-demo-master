package com.step.pdf.demo.screen.param;

import com.step.pdf.demo.screen.DriverTypeEnum;
import lombok.Data;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/8/24
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/8/24
 */
@Data
public class FreemarkerSeleniumScreenshotParam extends ScreenshotParam {

    public FreemarkerSeleniumScreenshotParam(DriverTypeEnum driverType) {
        this.driverType = driverType;
    }

    /**
     * 使用的驱动器的类型
     */
    private DriverTypeEnum driverType;

    /**
     * 模板路径
     */
    private String templatePath;
}
