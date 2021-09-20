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
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;

import static com.step.pdf.demo.multiconfig.constant.Constants.GROUP_SEPARATOR;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/9/2
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/2
 */
@Component
@Slf4j
public class MultiBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private String[] scanPackage;

    private final static String AOP_CGLIB_TARGET_FIELD = "CGLIB$CALLBACK_0";

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Object target = getCglibObject(bean);
        if(beanName.contains("#")){
            log.info(">>>>>>>>>>>>>>>>>multiService bean {}",beanName);
        }
        //1、获取类上面的ServiceGroup注解
        ServiceGroup serviceGroup = bean.getClass().getAnnotation(ServiceGroup.class);
        if (serviceGroup != null) {
            //获取失败是普通bean
            //1.1、获取所有包含注解的属性
            Field[]  fields = target.getClass().getDeclaredFields();
            for (Field field : fields) {
                //1.2 获取MultiService注解
                MultiService s = field.getAnnotation(MultiService.class);
                if (s != null) {
                    //1.3 重新注入
                    setBeanFieldValue(target, field, serviceGroup.value(),s.name());
                    continue;
                }
                ////1.4 获取Resource和Autowired注解
                //Resource r = field.getAnnotation(Resource.class);
                //Autowired a = field.getAnnotation(Autowired.class);
                //if (r != null || a != null) {
                //    setBeanFieldValue(target, field, serviceGroup.value());
                //}
            }
        //}else if(scanPackage != null && scanPackage.length != 0){
        //    for (String sp : scanPackage) {
        //        if (StringUtils.isEmpty(sp)) {
        //            continue;
        //        }
        //        //在此包中
        //        if (target.getClass().getName().startsWith(sp+".")) {
        //            setBeanFieldValue(target);
        //        }
        //    }
        //}else{
        //    //3、没有配置scanPackage，全部扫描处理，建议配置scanPackage
        //    setBeanFieldValue(target);
        }
        return bean;
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
            if (target == null){
                return bean;
            }
            log.info(">>>>>>>>>>>>>>>>>{} is proxy by cglib [{}] .",target.getClass().getName(),
                    bean.getClass().getName());
            return target;
        } catch (Exception e) {
        }
        return bean;
    }

    //private void setBeanFieldValue(Object bean) {
    //    //2 类上面没有ServiceGroup注解
    //    //2.1、获取所有包含注解的属性
    //    Field[] fields = bean.getClass().getDeclaredFields();
    //    for (Field field : fields) {
    //        //2.2 获取ServiceGroup注解
    //        ServiceGroup s = field.getAnnotation(ServiceGroup.class);
    //        if (s != null) {
    //            //2.3 重新注入
    //            setBeanFieldValue(bean, field, s.value());
    //        }
    //    }
    //}

    /**
     * 重新设值
     * @param bean
     * @param field
     * @param group
     * @param name
     */
    private void setBeanFieldValue(Object bean, Field field, String group,String name) {
        try {
            String beanName = group+ GROUP_SEPARATOR + field.getType().getName();
            if(!ValidationUtil.isEmpty(name)){
                beanName = beanName +GROUP_SEPARATOR + name;
            }
            Object value = applicationContext.getBean(beanName);
            field.setAccessible(true);
            field.set(bean, value);
            log.info(">>>>>>>>>>>>>>>>>{}#{} set service group [{}] use reference [{}] success.",
                    bean.getClass().getName(), field.getName(), group,beanName);
        } catch (Exception e) {
            //忽略异常
        }
    }

    @SneakyThrows
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        try{
            scanPackage = applicationContext.getBean(EnableMultiConfig.class.getName(), String[].class);
            log.info(">>>>>>>>>>>>>>>>>scanPackage={}", Arrays.toString(scanPackage));
        }catch (BeansException e){
            log.info(">>>>>>>>>>>>>>>>>scanPackage not configured，will scan all package.");

        }
    }
}
