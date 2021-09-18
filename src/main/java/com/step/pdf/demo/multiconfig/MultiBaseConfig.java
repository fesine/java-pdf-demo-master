package com.step.pdf.demo.multiconfig;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/9/17
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/17
 */
public abstract  class MultiBaseConfig<T> {
    /**
     * 主实例组名，未配置时默认使用第一组配置
     */
    private String primary;
    /**
     * 配置map
     * 每一个key对应一个T对象
     */
    Map<String, T> configMap = new HashMap<>();

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public Map<String, T> getConfigMap() {
        return configMap;
    }

    public void setConfigMap(Map<String, T> configMap) {
        this.configMap = configMap;
    }

    @Override
    public String toString() {
        return "MultiBaseConfig{" + "primary='" + primary + '\'' + ", configMap=" + configMap + '}';
    }
}
