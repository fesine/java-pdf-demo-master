package com.step.pdf.demo.multiconfig;

import com.step.pdf.demo.multiconfig.annotation.MultiService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.util.*;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/9/18
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/18
 */
public class MultiBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {
    static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
    private ResourcePatternResolver resourcePatternResolver;

    private List<Map<String, Object>> groupList;

    private BeanDefinitionRegistry registry;

    private static final TypeFilter tf =  new AnnotationTypeFilter(MultiService .class);

    public MultiBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters, List<Map<String, Object>> groupList) {
        super(registry, useDefaultFilters);
        this.groupList = groupList;
        this.registry = registry;
    }

    @SneakyThrows
    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        for (String basePackage : basePackages) {
            //包扫描
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    resolveBasePackage(basePackage) + '/' + DEFAULT_RESOURCE_PATTERN;
            //扫描包下所有的资源
            Resource[] resources = getResourcePatternResolver().getResources(packageSearchPath);
            List<MetadataReader> metadataReaderList = new LinkedList<>();
            for (Resource resource : resources) {
                MetadataReader metadataReader =
                        getMetadataReaderFactory().getMetadataReader(resource);
                //获取标记MultiService注解类
                AnnotationAttributes multiService =
                        AnnotationAttributes.fromMap(metadataReader.getAnnotationMetadata()
                        .getAnnotationAttributes(MultiService.class.getName()));
                if(multiService != null){
                    metadataReaderList.add(metadataReader);
                }
            }
            //注册resource bean
            if (metadataReaderList.size() > 0) {
                register(metadataReaderList);
            }
        }
        ////添加过滤条件
        //addIncludeFilter(new AnnotationTypeFilter(MultiService .class));
        ////调用spring的扫描
        Set<BeanDefinitionHolder> beanDefinitionHolders = new LinkedHashSet<>();
        if (beanDefinitionHolders.size() != 0) {
            //给扫描出来的接口添加上代理对象
            processBeanDefinitions(beanDefinitionHolders);
        }
        return beanDefinitionHolders;
    }

    @SneakyThrows
    private void register(List<MetadataReader> metadataReaderList) {
        //遍历 注册bean
        for (MetadataReader metadataReader : metadataReaderList) {
            String className = metadataReader.getClassMetadata().getClassName();
            Class<?> clazz = Class.forName(className);

        }
    }


    /**
     * 给扫描出来的接口添加上代理对象
     *
     * @param beanDefinitions
     */
    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        GenericBeanDefinition definition;
        for (BeanDefinitionHolder holder : beanDefinitions) {
            definition = (GenericBeanDefinition) holder.getBeanDefinition();
            //拿到接口的全路径名称
            String beanClassName = definition.getBeanClassName();
            //设置属性 即所对应的消费接口
            try {
                definition.getPropertyValues().add("interfaceClass", Class.forName(beanClassName));
                //设置Calss 即代理工厂
                //definition.setBeanClass(MethodProxyFactory.class);
                //按 照查找Bean的Class的类型
                definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    private ResourcePatternResolver getResourcePatternResolver() {
        if (this.resourcePatternResolver == null) {
            this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
        }
        return this.resourcePatternResolver;
    }
}
