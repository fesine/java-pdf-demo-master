package com.step.pdf.demo.multiconfig;

import com.step.pdf.demo.multiconfig.annotation.MultiService;
import com.step.pdf.demo.multiconfig.util.ClassUtil;
import com.step.pdf.demo.multiconfig.util.ValidationUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.step.pdf.demo.multiconfig.constant.Constants.DEFAULT_RESOURCE_PATTERN;
import static com.step.pdf.demo.multiconfig.constant.Constants.GROUP_SEPARATOR;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/9/18
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/18
 */
@Slf4j
public class MultiBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {
    private ResourcePatternResolver resourcePatternResolver;

    private Map<String, MultiBaseConfig> groupMap;

    private BeanDefinitionRegistry registry;

    /**
     * 存放自定义扫描包资源
     */
    private final Map<String, Resource> RESOURCE_MAP = new ConcurrentHashMap<>();

    private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

    /**
     * 1、存放所有被配置标记的目标对象的Map
     */
    private final Map<Class<?>, Object> beanMap = new ConcurrentHashMap<>();


    public MultiBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters,
                                      Map<String, MultiBaseConfig> groupMap) {
        super(registry, useDefaultFilters);
        this.groupMap = groupMap;
        this.registry = registry;
    }

    @SneakyThrows
    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        try {
            //1. 扫描配置包
            scanMultiService(basePackages);
            Map<String, List<Class<?>>> map = new HashMap<>();
            //2. 将bean按配置文件分组
            map = getAllClassByMultiConfig(map);
            if (ValidationUtil.isEmpty(map)) {
                return null;
            }
            Set<BeanDefinitionHolder> holders  = doAddBeanHolderSet(map);
            return holders;
        } catch (Exception e) {
            log.error("register multi service failed.{}.", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private Set<BeanDefinitionHolder> doAddBeanHolderSet(Map<String, List<Class<?>>> map) {
        Set<BeanDefinitionHolder> beanDefinitionHolderSet = new LinkedHashSet<>();
        for (String key : map.keySet()) {
            MultiBaseConfig config = groupMap.get(key);
            Map<String, BaseConfig> configMap = config.getConfigMap();
            //遍历group组
            for (String group : configMap.keySet()) {
                for (Class<?> clazz : map.get(key)) {
                    MultiService annotation = clazz.getAnnotation(MultiService.class);
                    String beanName = group + GROUP_SEPARATOR + clazz.getName();
                    if (!ValidationUtil.isEmpty(annotation.name())) {
                        beanName = beanName + GROUP_SEPARATOR + annotation.name();
                    }
                    boolean addFlag = false;
                    if (!ValidationUtil.isEmpty(annotation.group())) {
                        if (contains(annotation.group(), group)) {
                            addFlag = true;
                        }
                    } else {
                        addFlag = true;
                    }
                    if (addFlag) {
                        //参考spring 创建并注册bean定义，同时添加到holder中
                        Resource resource = RESOURCE_MAP.get(clazz.getName());
                        try {
                            MetadataReader metadataReader =
                                    getMetadataReaderFactory().getMetadataReader(resource);
                            ScannedGenericBeanDefinition candidate =
                                    new ScannedGenericBeanDefinition(metadataReader);
                            candidate.setResource(resource);
                            candidate.setSource(resource);
                            if (group.equals(config.getPrimary())) {
                                candidate.setPrimary(true);
                            }
                            ScopeMetadata scopeMetadata =
                                    this.scopeMetadataResolver.resolveScopeMetadata(candidate);
                            candidate.setScope(scopeMetadata.getScopeName());
                            postProcessBeanDefinition(candidate,beanName);
                            AnnotationConfigUtils.processCommonDefinitionAnnotations(candidate);
                            if (checkCandidate(beanName, candidate)) {
                                BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(candidate, beanName);
                                beanDefinitionHolderSet.add(definitionHolder);
                                registerBeanDefinition(definitionHolder, this.registry);
                            }
                        } catch (IOException e) {
                            log.warn("error get metadataReader for class {}",clazz.getName());
                        }
                    }

                }
            }

        }
        return beanDefinitionHolderSet;
    }

    /**
     * 配置组名是否包含在配置文件配置的组中
     * @param groupArr
     * @param group
     * @return
     */
    private boolean contains(String[] groupArr, String group) {
        for (String s : groupArr) {
            if (s.equals(group)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 扫描包，加载到bean到allClassByMultiConfigMap
     *
     * @param basePackages
     * @throws Exception
     */
    public void scanMultiService(String... basePackages) throws Exception {
        for (String basePackage : basePackages) {
            //包扫描
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    resolveBasePackage(basePackage) + '/' + DEFAULT_RESOURCE_PATTERN;
            //扫描包下所有的资源
            Resource[] resources = new Resource[0];
            try {
                resources = getResourcePatternResolver().getResources(packageSearchPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //遍历资源
            for (Resource resource : resources) {
                MetadataReader metadataReader;
                try {
                    metadataReader = getMetadataReaderFactory().getMetadataReader(resource);
                } catch (IOException e) {
                    log.warn("get meta data by resource {} failed.", resource.getFilename(), e);
                    continue;
                }
                //获取标记MultiService注解类
                AnnotationAttributes multiService =
                        AnnotationAttributes.fromMap(metadataReader.getAnnotationMetadata()
                                .getAnnotationAttributes(MultiService.class.getName()));
                if (multiService != null) {
                    //初始化实例
                    String className = metadataReader.getClassMetadata().getClassName();
                    Class<?> c = Class.forName(className);
                    Object o = ClassUtil.newInstance(c, true);
                    beanMap.put(c, o);
                    RESOURCE_MAP.put(className, resource);
                }
            }
        }
    }

    /**
     * 递归获取同一配置的所有class类，并添加到对应的map中
     */
    private Map<String, List<Class<?>>> getAllClassByMultiConfig(Map<String, List<Class<?>>> multiServiceMap) {
        List<Class<?>> tempList = new ArrayList<>();
        Set<Class<?>> classSet = beanMap.keySet();
        for (String key : groupMap.keySet()) {
            List<Class<?>> list = new ArrayList<>();
            for (Class<?> clazz : classSet) {
                boolean addFlag = getAllClassAddOneGroup(key, clazz, list);
                if (!list.contains(clazz) && addFlag) {
                    list.add(clazz);
                }
            }
            if (multiServiceMap.get(key) != null) {
                multiServiceMap.get(key).addAll(list);
            } else {
                multiServiceMap.put(key, list);
            }
            //过滤已经处理的class
            tempList.addAll(list);
            classSet = new HashSet<>();
            for (Class<?> c : beanMap.keySet()) {
                if (!tempList.contains(c)) {
                    classSet.add(c);
                }
            }
        }
        return multiServiceMap;
    }

    /**
     * 将相同配置文件的bean引用添加到同一组里面
     * @param key
     * @param clazz
     * @param list
     * @return
     */
    private boolean getAllClassAddOneGroup(String key, Class<?> clazz, List<Class<?>> list) {
        //2.遍历clazz所有的成员变量
        Field[] fields = clazz.getDeclaredFields();
        if (ValidationUtil.isEmpty(fields)) {
            return false;
        }
        boolean flag = false;
        for (Field field : fields) {
            if (list.contains(field.getType()) && field.isAnnotationPresent(MultiService.class)) {
                return true;
            }
            if (BaseConfig.class.isAssignableFrom(field.getType())
                    && !key.equals(field.getType().getName())) {
                return false;
            }
            if (key.equals(field.getType().getName())) {
                flag = true;
            } else if (BaseConfig.class.isAssignableFrom(field.getType()) && flag) {
                throw new IllegalArgumentException("duplicate config file " + key + " and " + field.getType().getName() + " in " + clazz.getName());
            } else if (field.isAnnotationPresent(MultiService.class)) {
                //如果变量中包含MultiService注解，继续查找递归
                MultiService multiService = field.getAnnotation(MultiService.class);
                //4.获取成员变量的类型
                Class<?> fieldClass = field.getType();
                Object fieldInstance = getFieldInstance(fieldClass, multiService.name());
                if (fieldInstance != null) {
                    if (list.contains(fieldInstance.getClass())) {
                        return true;
                    } else {
                        return getAllClassAddOneGroup(key, fieldInstance.getClass(), list);
                    }
                }
            }
        }
        return flag;
    }

    /**
     * 根据成员变量类名获取类实例
     *
     * @param fieldClass
     * @return
     */
    private Object getFieldInstance(Class<?> fieldClass, String annotationValue) {
        Object fieldValue = beanMap.get(fieldClass);
        //如果存在实例，直接返回
        if (fieldValue != null) {
            return fieldValue;
        } else {
            Class<?> implementClass = getImplementedClass(fieldClass, annotationValue);
            if (implementClass != null) {
                return beanMap.get(implementClass);
            } else {
                return null;
            }
        }
    }

    /**
     * 获取所有接口或父类实现类
     * @param fieldClass
     * @param annotationValue
     * @return
     */
    private Class<?> getImplementedClass(Class<?> fieldClass, String annotationValue) {
        //如果不存在，则通过父类或接口查询实例对象
        Set<Class<?>> classSet = getClassesBySuper(fieldClass);
        if (!ValidationUtil.isEmpty(classSet)) {
            if (ValidationUtil.isEmpty(annotationValue)) {
                if (classSet.size() == 1) {
                    return classSet.iterator().next();
                } else {
                    throw new RuntimeException("multiple implemented classes for "
                            + fieldClass.getName()
                            + ", please set @MultiService's value to pick one.");
                }
            } else {
                //如果有多个实例，则需要通过value匹配
                for (Class<?> clazz : classSet) {
                    if (annotationValue.equals(clazz.getAnnotation(MultiService.class).name())) {
                        return clazz;
                    }
                }
            }
        }
        return null;
    }


    /**
     * 通过接口或者父类获取实现类或子类的class集合，不包含其本身
     *
     * @param interfaceOrClass 接口或父类
     * @return class集合
     */
    public Set<Class<?>> getClassesBySuper(Class<?> interfaceOrClass) {
        //1.获取beanMap的所有class对象
        Set<Class<?>> keySet = beanMap.keySet();
        if (ValidationUtil.isEmpty(keySet)) {
            log.warn("nothing in beanMap");
            return null;
        }
        //2.判断keySet里面的元素是否是传入的接口实现类或类的子类，如果是，则添加到classSet中
        Set<Class<?>> classSet = new HashSet<>();
        for (Class<?> clazz : keySet) {
            //判断keySet里面的元素是否是传入的接口实现类或类的子类
            if (interfaceOrClass.isAssignableFrom(clazz) && !clazz.equals(interfaceOrClass)) {
                classSet.add(clazz);
            }
        }
        return classSet.size() > 0 ? classSet : null;
    }




    private ResourcePatternResolver getResourcePatternResolver() {
        if (this.resourcePatternResolver == null) {
            this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
        }
        return this.resourcePatternResolver;
    }


    /**
     * 创建MultiService注解过滤器
     */
    public void registerFilters() {
        this.addIncludeFilter(new AnnotationTypeFilter(MultiService.class));
    }
}
