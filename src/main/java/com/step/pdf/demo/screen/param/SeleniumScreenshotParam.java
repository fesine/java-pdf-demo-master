package com.step.pdf.demo.screen.param;

import com.step.pdf.demo.screen.DriverTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/8/24
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/8/24
 */
@Getter
@AllArgsConstructor
public class SeleniumScreenshotParam extends ScreenshotParam {

    /**
     * 使用的驱动器的类型
     *
     */
    private DriverTypeEnum driverType;

}
