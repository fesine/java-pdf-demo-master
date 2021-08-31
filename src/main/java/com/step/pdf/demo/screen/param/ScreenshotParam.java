package com.step.pdf.demo.screen.param;

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
public class ScreenshotParam {
    /**
     * 访问的url地址；格式如：
     * 1：about:blank  <br />
     * 2：file///:/path/to/demo.html <br />
     * 3：https://www.baidu.com <br />
     */
    private String url;
    // 剪裁宽度
    private Integer clipWidth = 500;
    // 剪裁高度
    private Integer clipHeight = 400;
}
