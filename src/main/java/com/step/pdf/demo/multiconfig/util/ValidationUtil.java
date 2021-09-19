package com.step.pdf.demo.multiconfig.util;

import java.util.Collection;
import java.util.Map;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/9/14
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/14
 */
public class ValidationUtil {

    /**
     * String是否为null或者为""
     * @param obj String
     * @return 是否为空
     */
    public static boolean isEmpty(String obj){
        return obj == null || "".equals(obj);
    }

    /**
     * Array是否为null或者length为0
     * @param obj Array
     * @return 是否为空
     */
    public static boolean isEmpty(Object[] obj){
        return obj == null || obj.length == 0;
    }

    /**
     * Collection是否为null或者size为0
     * @param obj Collection
     * @return 是否为空
     */
    public static boolean isEmpty(Collection<?> obj){
        return obj == null || obj.isEmpty();
    }

    /**
     * Map是否为null或者size为0
     * @param obj Map
     * @return 是否为空
     */
    public static boolean isEmpty(Map<?,?> obj){
        return obj == null || obj.isEmpty();
    }
}
