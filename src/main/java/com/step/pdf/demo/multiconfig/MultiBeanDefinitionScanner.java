package com.step.pdf.demo.multiconfig;

import com.step.pdf.demo.multiconfig.annotation.MultiService;
import com.step.pdf.demo.multiconfig.util.ClassUtil;
import com.step.pdf.demo.multiconfig.util.ValidationUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
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

    private Map<String, Object> BEAN_MAP = new ConcurrentHashMap<>();

    private Map<String, Object> GROUP_BEAN_MAP = new ConcurrentHashMap<>();

    private Map<String, Resource> RESOURCE_MAP = new ConcurrentHashMap<>();

    private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

    /**
     * 1、存放所有被配置标记的目标对象的Map
     */
    private final Map<Class<?>, Object> beanMap = new ConcurrentHashMap<>();

    private final Map<Class<?>, List<BeanDefinitionHolder>> BEAN_HOLDER = new ConcurrentHashMap<>();


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
            //2. 将分组bean，按config#group#beanName组成key，添加到全局GROUP_BEAN_MAP中
            //doAddBeanMap(map);
            ////遍历GROUP_BEAN_MAP进行依赖注入
            //doIocGroupBean();
            ////遍历GROUP_BEAN_MAP注册bean
            //doRegisterBean();
            //Set<BeanDefinitionHolder> set = super.doScan(basePackages);
            Set<BeanDefinitionHolder> holders  = doAddBeanHolderSet(map);
            return holders;
        } catch (Exception e) {
            log.error("register multi service failed.{}.", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * 注册bean
     * beanName = GROUP_BEAN_MAP.key
     * bean = GROUP_BEAN_MAP.get(key)
     */
    private void doRegisterBean() {
        for (String s : GROUP_BEAN_MAP.keySet()) {
            registerToBeanContainer(s, GROUP_BEAN_MAP.get(s));
        }
        //注册配置文件
        for (String configName : groupMap.keySet()) {
            MultiBaseConfig config = groupMap.get(configName);
            Map<String, Object> configMap = config.getConfigMap();
            for (String subKey : configMap.keySet()) {
                String key = subKey + GROUP_SEPARATOR + configName;
                registerToBeanContainer(config.getPrimary(), subKey, key, configMap.get(subKey));
            }
        }
    }

    private void registerToBeanContainer(String s, Object o) {
        String[] keyArr = s.split(GROUP_SEPARATOR);
        MultiBaseConfig config = groupMap.get(keyArr[0]);
        List<Class<?>> allSuperClass = getAllSuperClass(o.getClass());
        for (Class<?> superClass : allSuperClass) {
            //config#iis#className#MultiService.name
            String key = keyArr[1] + GROUP_SEPARATOR + superClass.getName();
            if (keyArr.length == 4) {
                //有分组
                key = key + GROUP_SEPARATOR + keyArr[3];
            }
            registerToBeanContainer(config.getPrimary(), keyArr[1], key, o);
        }
    }


    private void registerToBeanContainer(String primary, String group, String key,
                                         Object object) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(Object.class, () -> object);
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(object.getClass());
        if (group.equals(primary)) {
            builder.setPrimary(true);
        }
        registry.registerBeanDefinition(key, builder.getRawBeanDefinition());
        BEAN_MAP.put(key, object);
    }

    /**
     * 初始化分组bean
     *
     * @param map
     */
    private void doAddBeanMap(Map<String, List<Class<?>>> map) {
        for (String key : map.keySet()) {
            MultiBaseConfig config = groupMap.get(key);
            Map<String, BaseConfig> configMap = config.getConfigMap();
            //遍历group组
            for (String group : configMap.keySet()) {
                for (Class<?> clazz : map.get(key)) {
                    MultiService annotation = clazz.getAnnotation(MultiService.class);
                    //groupKey = config#iis#className#MultiService.name
                    String groupKey =
                            key + GROUP_SEPARATOR + group + GROUP_SEPARATOR + clazz.getName();
                    if (!ValidationUtil.isEmpty(annotation.name())) {
                        groupKey = groupKey + GROUP_SEPARATOR + annotation.name();
                    }
                    if (!ValidationUtil.isEmpty(annotation.group())) {
                        if (contains(annotation.group(), group)) {
                            Object o = ClassUtil.newInstance(clazz, true);
                            GROUP_BEAN_MAP.put(groupKey, o);
                        }
                    } else {
                        Object o = ClassUtil.newInstance(clazz, true);
                        GROUP_BEAN_MAP.put(groupKey, o);
                    }
                }
            }
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
                    //groupKey = config#iis#className#MultiService.name
                    //String groupKey =
                    //        key + GROUP_SEPARATOR + group + GROUP_SEPARATOR + clazz.getName();
                    String beanName = group + GROUP_SEPARATOR + clazz.getName();
                    if (!ValidationUtil.isEmpty(annotation.name())) {
                        //groupKey = groupKey + GROUP_SEPARATOR + annotation.name();
                        beanName = beanName + GROUP_SEPARATOR + annotation.name();
                    }
                    boolean addFlag = false;
                    if (!ValidationUtil.isEmpty(annotation.group())) {
                        if (contains(annotation.group(), group)) {
                            //Object o = ClassUtil.newInstance(clazz, true);
                            //GROUP_BEAN_MAP.put(groupKey, o);
                            addFlag = true;
                        }
                    } else {
                        //Object o = ClassUtil.newInstance(clazz, true);
                        //GROUP_BEAN_MAP.put(groupKey, o);
                        addFlag = true;
                    }
                    if (addFlag) {
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

    private boolean contains(String[] groupArr, String group) {
        for (String s : groupArr) {
            if (s.equals(group)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 分组GROUP_BEAN_MAP注入依赖
     */
    private void doIocGroupBean() {
        if (ValidationUtil.isEmpty(GROUP_BEAN_MAP)) {
            log.warn("empty classSet in GROUP_BEAN_MAP.");
            return;
        }
        //1.遍历bean容器中所有的class对象
        for (String key : GROUP_BEAN_MAP.keySet()) {
            //2.遍历clazz所有的成员变量
            Object targetBean = GROUP_BEAN_MAP.get(key);
            Class<?> clazz = targetBean.getClass();
            //根据group注入对应的依赖
            Field[] fields = clazz.getDeclaredFields();
            if (ValidationUtil.isEmpty(fields)) {
                continue;
            }
            for (Field field : fields) {
                //3.遍历所有的field，找出被标记为@MultiService注解的成员变量
                if (field.isAnnotationPresent(MultiService.class)) {
                    MultiService multiService = field.getAnnotation(MultiService.class);
                    //4.获取成员变量的类型
                    Class<?> fieldClass = field.getType();
                    //5.获取这些成员变量的类型在容器中的实例
                    Object fieldValue = getFieldInstance(key, fieldClass, multiService.name());
                    if (fieldValue == null) {
                        throw new RuntimeException("unable to inject relevant type, target " +
                                "fieldClass is:" + fieldClass.getName());
                    } else {
                        //6.通过反射将成员变量实例注入到成员变量所在类的实例里
                        ClassUtil.setFieldValue(field, targetBean, fieldValue, true);

                    }
                }
            }
        }
    }

    private Object getFieldInstance(String key, Class<?> fieldClass, String name) {
        if (BaseConfig.class.isAssignableFrom(fieldClass)) {
            //是配置文件，则获取对应的配置文件
            String[] keyArr = key.split(GROUP_SEPARATOR);
            MultiBaseConfig multiBaseConfig = groupMap.get(keyArr[0]);
            return multiBaseConfig.getConfigMap().get(keyArr[1]);
        }
        String prefix = key.substring(0, key.lastIndexOf(GROUP_SEPARATOR) + 1);
        Object fieldValue = GROUP_BEAN_MAP.get(prefix + fieldClass.getName());
        //如果存在实例，直接返回
        if (fieldValue != null) {
            return fieldValue;
        } else {
            Class<?> implementClass = getImplementedClass(fieldClass, name);
            if (implementClass != null) {
                return GROUP_BEAN_MAP.get(prefix + implementClass.getName());
            } else {
                return null;
            }
        }
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
                MetadataReader metadataReader = null;
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

    /**
     * 根据类获取其父类及接口
     *
     * @param clazz
     * @return
     */
    private static List<Class<?>> getAllSuperClass(Class<?> clazz) {
        List<Class<?>> list = new ArrayList<>();
        Class<?> temp = clazz;
        while (temp != null && temp != Object.class) {
            list.addAll(new ArrayList<>(Arrays.asList(temp)));
            temp = temp.getSuperclass();
        }
        getAllInterfaces(clazz.getInterfaces(), list);
        return list;
    }

    private static void getAllInterfaces(Class<?>[] interfaces, List<Class<?>> list) {
        //获取所有接口
        while (interfaces.length > 0) {
            for (Class<?> anInterface : interfaces) {
                if (!list.contains(anInterface)) {
                    list.add(anInterface);
                }
                interfaces = anInterface.getInterfaces();
                getAllInterfaces(interfaces, list);

            }
        }
    }


    private ResourcePatternResolver getResourcePatternResolver() {
        if (this.resourcePatternResolver == null) {
            this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
        }
        return this.resourcePatternResolver;
    }


    public void registerFilters() {
        this.addIncludeFilter(new AnnotationTypeFilter(MultiService.class));
        //this.addIncludeFilter(new AnnotationTypeFilter(MultiConfig.class));
    }
}
