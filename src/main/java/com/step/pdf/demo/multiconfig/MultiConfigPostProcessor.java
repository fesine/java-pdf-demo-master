package com.step.pdf.demo.multiconfig;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/9/17
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/17
 */
@Component
public class MultiConfigPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        //MyConfig iisConfig = (MyConfig) applicationContext.getBean("iis" + MyConfig.class.getName());
        //MyConfig jwlogConfig = (MyConfig) applicationContext.getBean("jwlog" + MyConfig.class.getName());
        //System.out.println(iisConfig);
        //System.out.println(jwlogConfig);
        //MyConfig2 iisConfig2 = (MyConfig2) applicationContext.getBean("iis" + MyConfig2.class.getName());
        //MyConfig2 jwlogConfig2 = (MyConfig2) applicationContext.getBean("jwlog" + MyConfig2.class.getName());
        //System.out.println(iisConfig2);
        //System.out.println(jwlogConfig2);
        //Set<String> configSet = (Set<String>) applicationContext.getBean(MyConfig.class.getName()+"#key");
        //Set<String> config2Set = (Set<String>) applicationContext.getBean(MyConfig2.class.getName() + "#key");
        //System.out.println(configSet);
        //System.out.println(config2Set);
    }


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println(bean);
        return null;
    }
}
