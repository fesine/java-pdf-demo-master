package com.step.pdf.demo.multiconfig;

import com.step.pdf.demo.multiconfig.annotation.EnableMultiConfig;
import com.step.pdf.demo.multiconfig.annotation.MultiService;
import com.step.pdf.demo.multiconfig.annotation.ServiceGroup;
import com.step.pdf.demo.multiconfig.util.ValidationUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import static com.step.pdf.demo.multiconfig.constant.Constants.AOP_CGLIB_TARGET_FIELD;
import static com.step.pdf.demo.multiconfig.constant.Constants.GROUP_SEPARATOR;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/9/2
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/2
 */
@Slf4j
public class MultiBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private String[] scanPackage;


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //1. 进行包配置判断
        if (scanPackage != null && scanPackage.length != 0) {
            for (String sp : scanPackage) {
                if (StringUtils.isEmpty(sp)) {
                    continue;
                }
                //在此包中,中断循环，处理bean
                if (bean.getClass().getName().startsWith(sp + ".")) {
                    break;
                }
            }
        }
        Object target = getCglibObject(bean);
        //未标记MultiService和ServiceGroup不处理
        if (!target.getClass().isAnnotationPresent(MultiService.class)
                && !target.getClass().isAnnotationPresent(ServiceGroup.class)) {
            return target;
        }
        //获取服务提供者bean
        String groupValue = null;
        //初始化方法
        String initMethod = null;
        if (target.getClass().isAnnotationPresent(ServiceGroup.class)) {
            //处理消费者引用
            ServiceGroup groupAnnotation =
                    target.getClass().getAnnotation(ServiceGroup.class);
            groupValue = groupAnnotation.value();
            if (ValidationUtil.isEmpty(groupValue)) {
                log.warn(">>>>>>>>>>>>>>>>>[IOC]{} marked @ServiceGroup, but didn't set value.",
                        beanName);
                return target;
            }
        } else if (target.getClass().isAnnotationPresent(MultiService.class)) {
            //处理消费者引用
            MultiService multiAnnotation =
                    target.getClass().getAnnotation(MultiService.class);
            initMethod = multiAnnotation.initMethod();
        }
        Field[] fields = target.getClass().getDeclaredFields();
        for (Field field : fields) {
            //只处理同样标记MultiService注解的属性
            //其他注解，spring已经完成注入
            if (!field.isAnnotationPresent(MultiService.class)) {
                continue;
            }
            MultiService fieldAnnotation = field.getAnnotation(MultiService.class);
            //如果属性上指定引用服务组，临时覆盖类上注解标记的组
            String tempGroup = null;
            String[] group = fieldAnnotation.group();
            if (!ValidationUtil.isEmpty(group)) {
                tempGroup = group[0];
            }
            //groupValue == null 说明是MultiService注解类
            if (ValidationUtil.isEmpty(groupValue)) {
                //分割beanName，组装field beanName
                String[] keyGroup = beanName.split(GROUP_SEPARATOR);
                groupValue = keyGroup[0];
            }
            String fieldBeanName =
                    (tempGroup == null ? groupValue : tempGroup) + GROUP_SEPARATOR + field.getType().getName();
            try {
                //处理配置文件引用
                if (applicationContext.containsBean(fieldBeanName)) {
                    Object value = applicationContext.getBean(fieldBeanName);
                    setBeanFieldValue(target, field, tempGroup == null ? groupValue : "field " +
                            "group " + tempGroup + " covered class group " + groupValue, value,
                            fieldBeanName);
                } else {
                    //不包含当前bean，可能是接口或父类，则通过类型寻找
                    Map<String, ?> fieldValueMap =
                            applicationContext.getBeansOfType(field.getType());
                    if (ValidationUtil.isEmpty(fieldValueMap)) {
                        log.warn(">>>>>>>>>>>>>>>>>>[IOC]{} field {} can not reference from " +
                                "spring bean container.", beanName, field.getType().getName());
                        continue;
                    }
                    for (String key : fieldValueMap.keySet()) {
                        if (!key.startsWith((tempGroup == null ? groupValue : tempGroup) + GROUP_SEPARATOR)) {
                            continue;
                        }
                        //处理注解中有name的属性值
                        if (!ValidationUtil.isEmpty(fieldAnnotation.name())) {
                            String[] arr = key.split(GROUP_SEPARATOR);
                            //判断注册bean的最后一个key是否等于name的值
                            if (arr[arr.length - 1].equals(fieldAnnotation.name())) {
                                setBeanFieldValue(target, field, tempGroup == null ? groupValue :
                                                "field group " + tempGroup + " covered class " +
                                                        "group " + groupValue,
                                        fieldValueMap.get(key), key);
                            }
                        } else {
                            setBeanFieldValue(target, field, tempGroup == null ? groupValue :
                                            "field group " + tempGroup + " covered class group " + groupValue,
                                    fieldValueMap.get(key), key);
                        }
                        break;
                    }
                }

            } catch (BeansException e) {
                log.warn(">>>>>>>>>>>>>>>>>>[IOC]error set field value.{}", e.getMessage());
            }
        }
        if (!ValidationUtil.isEmpty(initMethod)) {
            try {
                Method method = target.getClass().getDeclaredMethod(initMethod, null);
                method.setAccessible(true);
                method.invoke(target);
            } catch (Exception e) {
                log.warn(">>>>>>>>>>>>>>>>>>[IOC]{} error invoke initMethod {}.{}", beanName,
                        initMethod, e.getMessage());
            }
        }

        return target;
    }

    private Object getCglibObject(Object bean) {
        //需要判断bean是不是cglib代理类
        try {
            Field field = bean.getClass().getDeclaredField(AOP_CGLIB_TARGET_FIELD);
            field.setAccessible(true);
            Object d = field.get(bean);
            Field advised = d.getClass().getDeclaredField("advised");
            advised.setAccessible(true);
            Object target = ((AdvisedSupport) advised.get(d)).getTargetSource().getTarget();
            if (target == null) {
                return bean;
            }
            log.info(">>>>>>>>>>>>>>>>>>[IOC]{} is proxy by cglib [{}] .",
                    target.getClass().getName(),
                    bean.getClass().getName());
            return target;
        } catch (Exception e) {
        }
        return bean;
    }

    /**
     * 重新设值
     *
     * @param bean
     * @param field
     */
    private void setBeanFieldValue(Object bean, Field field, String group, Object value,
                                   String fieldBeanName) {
        try {
            field.setAccessible(true);
            field.set(bean, value);
            log.info(">>>>>>>>>>>>>>>>>>[IOC]{} field {} set service group [{}] use reference " +
                            "[{}] success.",
                    bean.getClass().getName(), field.getName(), group, fieldBeanName);
        } catch (Exception e) {
            //忽略异常
        }
    }

    @SneakyThrows
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        try {
            scanPackage = applicationContext.getBean(EnableMultiConfig.class.getName(),
                    String[].class);
            log.info(">>>>>>>>>>>>>>>>>>[IOC]scanPackage={}", Arrays.toString(scanPackage));
        } catch (BeansException e) {
            log.warn(">>>>>>>>>>>>>>>>>>[IOC]scanPackage not configured，will not invoke multi " +
                    "service Ioc.");

        }
    }

}
