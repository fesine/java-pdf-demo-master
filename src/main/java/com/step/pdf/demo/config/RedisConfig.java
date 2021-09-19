package com.step.pdf.demo.config;

import com.step.pdf.demo.multiconfig.BaseConfig;
import lombok.Data;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/9/17
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/17
 */
@Data
public class RedisConfig extends BaseConfig {

    private String url;

    private String username;

    private String password;
}
