package com.step.pdf.demo.screen.param;

import com.github.abel533.echarts.Option;
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
public class EchartsScreenshotParam extends ScreenshotParam {

    // echart的Option
    public Option option;

    public String type = "file";

}
