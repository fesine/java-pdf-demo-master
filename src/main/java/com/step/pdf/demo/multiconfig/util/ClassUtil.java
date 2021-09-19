package com.step.pdf.demo.multiconfig.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/9/13
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/13
 */
@Slf4j
public class ClassUtil {

    public static final String FILE_PROTOCOL = "file";
    public static final String CLASS_FILE = ".class";

    /**
     * 根据包名获取所有class文件
     * @param packageName
     * @return
     */
   public static Set<Class<?>> extractPackageClass(String packageName){
       //1、获取到类的加载器
       ClassLoader classLoader = getClassLoader();
       //2、通过类加载器获取到加载的资源
       URL url = classLoader.getResource(packageName.replace(".", "/"));
       if (url == null) {
           log.warn("unable to retrieve anything from package: {}",packageName);
           return null;
       }
       //3、依据不同的资源类型，采取不同的方式获取资源的集合
       Set<Class<?>> classSet = null;
       // 过滤出文件类型的资源
       if(url.getProtocol().equalsIgnoreCase(FILE_PROTOCOL)){
            classSet = new HashSet<Class<?>>();
           File packageDirectory = new File(url.getPath());
           extractClassFile(classSet,packageDirectory,packageName);
       }
       //TODO 此处可加入针对其他类型资源的处理
       return classSet;
   }

    /**
     * 递归获取目标package里面所有的class文件(包括子package里面的class文件)
     * @param emptyClassSet 装载目标类的集合
     * @param fireSource 文件或者目录
     * @param packageName 包名
     */
    private static void extractClassFile(Set<Class<?>> emptyClassSet, File fireSource, String packageName) {
        if (!fireSource.isDirectory()) {
            return;
        }
        //如果是一个文件夹，则调用listFiles方法获取文件夹下面的文件或文件夹
        File[] files = fireSource.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }else{
                    String absoluteFilePath = file.getAbsolutePath();
                    if (absoluteFilePath.endsWith(CLASS_FILE)) {
                        //若是class文件，直接加载
                        addToClassSet(absoluteFilePath);
                    }
                }
                return false;
            }

            /**
             * 根据class文件的绝对值路径，获取并生成class对象，并放入到classSet中
             * @param absoluteFilePath
             */
            private void addToClassSet(String absoluteFilePath) {
                //1.从class文件的绝对值路径里面提取包含package的类名
                absoluteFilePath = absoluteFilePath.replace(File.separator, ".");
                String className = absoluteFilePath.substring(absoluteFilePath.indexOf(packageName));
                className = className.substring(0, className.lastIndexOf("."));
                //2.通过反射机制获取对应的class对象并放入到classSet里
                Class<?> targetClass = loadClass(className);
                emptyClassSet.add(targetClass);

            }
        });
        if (files != null) {
            for (File f : files) {
                //递归调用
                extractClassFile(emptyClassSet, f, packageName);
            }
        }


    }

    /**
     * 实例化class
     * @param clazz Class
     * @param <T> class的类型
     * @param accessible 是否支持创建私有class对象的实例
     * @return 类的实例化
     */
    public static <T> T newInstance(Class<?> clazz, boolean accessible){
        try {
            Constructor constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(accessible);
            return (T)constructor.newInstance();
        } catch (Exception e) {
            log.error("newInstance error",e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取class对象
     * @param className class全名=package+类名
     * @return
     */
    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            log.error("load class error.", e);
            throw new RuntimeException(e);
        }
    }


    /**
     * 获取classLoader
     * @return 当前的ClassLoader
     */
   public static ClassLoader getClassLoader(){
       return Thread.currentThread().getContextClassLoader();
   }

    /**
     * 设置类的属性值
     * @param field 成员变量
     * @param target 类实例
     * @param value 成员变量的值
     * @param accessible 是否允许设置私有属性
     */
    public static void setFieldValue(Field field, Object target, Object value, boolean accessible) {
        field.setAccessible(accessible);
        try {
            field.set(target,value);
        } catch (IllegalAccessException e) {
            log.error("set field value failed.", e);
            throw new RuntimeException(e);
        }
    }
}
