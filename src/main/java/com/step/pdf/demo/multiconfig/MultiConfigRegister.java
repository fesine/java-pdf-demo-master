package com.step.pdf.demo.multiconfig;

import com.step.pdf.demo.multiconfig.annotation.EnableMultiConfig;
import com.step.pdf.demo.multiconfig.annotation.MultiConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.Arrays;
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
@Slf4j
public class MultiConfigRegister implements ImportBeanDefinitionRegistrar, EnvironmentAware,
        ResourceLoaderAware {

    private Environment environment;

    private ResourceLoader resourceLoader;


    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {
        log.info(">>>>>>>>>>>>>>>>start register multiConfig.");
        //获取主启动类上的注解
        AnnotationAttributes enableMultiConfig = AnnotationAttributes.fromMap(importingClassMetadata
                .getAnnotationAttributes(EnableMultiConfig.class.getName()));
        if (enableMultiConfig != null) {
            String[] basePackages = (String[]) enableMultiConfig.get("basePackages");
            if (basePackages == null || basePackages.length == 0) {
                throw new IllegalArgumentException("@EnableMultiConfig not set basePackages value" +
                        " please check.");
            }
            String enableMultiConfigName = EnableMultiConfig.class.getName();
            BeanDefinitionBuilder scanPackageBuilder =
                    BeanDefinitionBuilder.genericBeanDefinition(String[].class, () -> basePackages);
            registry.registerBeanDefinition(enableMultiConfigName,
                    scanPackageBuilder.getRawBeanDefinition());
            log.info(">>>>>>>>>>>>>>>>>register enableMultiConfigName {} with {}.",
                    enableMultiConfigName, Arrays.toString(basePackages));
            //注册multiConfig中的config类
            // 1. 获取标记注解配置类
            Class<? extends MultiBaseConfig>[] baseConfigClasses =
                    (Class<? extends MultiBaseConfig>[]) enableMultiConfig.get("configs");
            //7. 暂存配置文件
            Map<String, MultiBaseConfig> groupMap = new HashMap<>();
            for (Class<? extends MultiBaseConfig> baseConfigClass : baseConfigClasses) {
                if (!baseConfigClass.isAnnotationPresent(MultiConfig.class)) {
                    throw new BeanCreationException(baseConfigClass.getName() + " not add " +
                            "@MultiConfig annotation, please check.");
                }
                //2. 获取MultiConfig注解
                MultiConfig multiConfig = baseConfigClass.getAnnotation(MultiConfig.class);
                String prefix = multiConfig.prefix();
                //3. 根据配置的前缀读取配置内容
                BindResult bindResult = Binder.get(environment).bind(prefix, baseConfigClass);
                if (!bindResult.isBound()) {
                    log.error(">>>>>>>>>>>>>>>>{} not set properties value,please check.",
                            baseConfigClass.getSimpleName());
                    throw new BeanCreationException(baseConfigClass.getName() + " not set " +
                            "properties " +
                            "value, please check.");
                }
                MultiBaseConfig baseConfig = (MultiBaseConfig) bindResult.get();
                groupMap.put(baseConfig.getConfigMap().values().iterator().next().getClass().getName(), baseConfig);
                //4. 判断主配置的值
                if (StringUtils.isEmpty(baseConfig.getPrimary())) {
                    throw new BeanCreationException(baseConfigClass.getName() + " not set primary" +
                            " " +
                            "value, please check.");
                } else {
                    //5. 遍历map
                    Map<String, Object> configMap = baseConfig.getConfigMap();
                    if (configMap == null || configMap.size() == 0) {
                        throw new BeanCreationException(baseConfigClass.getName() + " not set " +
                                "configMap value, please check.");
                    }

                    registerConfigMap(registry, baseConfig, configMap);
                }
            }

            MultiBeanDefinitionScanner scanner = new MultiBeanDefinitionScanner(registry, false,
                    groupMap);
            if (resourceLoader != null) {
                scanner.setResourceLoader(resourceLoader);
            }
            scanner.registerFilters();
            scanner.scan(basePackages);
        }

    }

    /**
     * 注册配置文件bean
     *
     * @param registry
     * @param baseConfig
     * @param configMap
     */
    public void registerConfigMap(BeanDefinitionRegistry registry, MultiBaseConfig baseConfig,
                                  Map<String, Object> configMap) {
        for (Map.Entry<String, Object> entry : configMap.entrySet()) {
            Object value = entry.getValue();
            Class<? extends MultiBaseConfig> configClass =
                    (Class<? extends MultiBaseConfig>) value.getClass();
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(Object.class, () -> value);
            if (entry.getKey().equals(baseConfig.getPrimary())) {
                beanDefinitionBuilder.setPrimary(true);
            }
            registry.registerBeanDefinition(entry.getKey()+ "#" + configClass.getName(),
                    beanDefinitionBuilder.getRawBeanDefinition());
            log.info(">>>>>>>>>>>>>>>>>register multiConfig {}.",
                    entry.getKey() + "#" + configClass.getName());
        }
        //6. 注册配置文件的key group，便于在后期对使用MultiService注解的服务注册多group服务
        BeanDefinitionBuilder groupBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(Object.class, () -> baseConfig);
        registry.registerBeanDefinition(baseConfig.getClass().getName(),
                groupBuilder.getRawBeanDefinition());
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
