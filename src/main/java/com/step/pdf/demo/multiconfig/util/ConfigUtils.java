package com.step.pdf.demo.multiconfig.util;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/9/26
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/26
 */
@Slf4j
public class ConfigUtils {

    private ConfigUtils(){}

    public static List<Properties> loadProperties(String fileName) {
        List<Properties> propertiesList = new ArrayList<>();
        List<URL> list = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ConfigUtils.class.getClassLoader();
        }
        try {
            Enumeration<URL> urls = classLoader.getResources(fileName);
            while (urls.hasMoreElements()) {
                list.add(urls.nextElement());
            }
        } catch (Throwable e) {
            log.warn("Fail to load {} file: {}.", fileName, e.getMessage(), e);
        }
        if (ValidationUtil.isEmpty(list)) {
            return propertiesList;
        }
        for (URL url : list) {
            Properties p = new Properties();
            try {
                InputStream is = url.openStream();
                try {
                    p.load(is);
                    propertiesList.add(p);
                    log.info("Load {} file from {} success.", fileName, url);
                } finally {
                    try {
                        is.close();
                    } catch (Throwable t) {
                    }
                }
            } catch (Throwable e) {
                log.warn("Fail to load {} file from {}(ignore this file).{}", fileName, url, e.getMessage(), e);
            }
        }
        return propertiesList;
    }
}
