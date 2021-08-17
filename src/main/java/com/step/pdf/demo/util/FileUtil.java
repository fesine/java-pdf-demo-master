package com.step.pdf.demo.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;

import java.io.*;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/8/11
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/8/11
 */
@Slf4j
public class FileUtil {
    private static PathMatchingResourcePatternResolver resolver;

    static {
        resolver =
                new PathMatchingResourcePatternResolver();
    }

    /**
     * 生成html文件
     *
     * @param fileName 文件完整路径名称
     * @param context  内容
     * @throws Exception
     */
    public static void generatorFile(String fileName, String context) throws Exception {
        FileWriter writer = new FileWriter(fileName);
        //清空原文件内容
        writer.write("");
        writer.write(context);
        writer.flush();
        writer.close();
    }

    public static boolean mkdir(String parentPath, String... childPath) {
        File file = new File(parentPath);
        boolean b = true;
        if (!file.exists()) {
            b = file.mkdirs();
        }
        if (childPath == null || childPath.length == 0) {
            return b;
        }
        //父级目录创建成功或已存在，再创建子目录
        if (b) {
            for (String s : childPath) {
                b = mkdir(parentPath + File.separator + s);
            }
        }
        return b;

    }

    public static String getStringByJarFileName(String jarFileName) throws IOException {
        InputStream inputStream = getInputStreamByFileName(jarFileName);
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        return new String(bytes);
    }

    /**
     * 获取jar中资源目录
     *
     * @param jarFileName
     * @return
     * @throws IOException
     */
    public static InputStream getInputStreamByFileName(String jarFileName)  {
        return FileUtil.class.getClassLoader().getResourceAsStream(jarFileName);
    }

    /**
     * 获取jar中资源目录
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static Resource[] getResourceByPathPattern(String path) throws IOException {
        return getResourceByPathPattern(path, null);
    }

    /**
     * 根据后缀获取jar中资源
     *
     * @param path
     * @param suffix
     * @return
     * @throws IOException
     */
    public static Resource[] getResourceByPathPattern(String path, String suffix) throws IOException {
        String temp = ResourceUtils.CLASSPATH_URL_PREFIX + path + "/*";
        if (suffix != null) {
            temp += suffix;
        }
        return resolver.getResources(temp);
    }

    public static void copyResourceToFile(String resourcePath, String target) throws IOException {
        copyResourceToFile(resourcePath, null, target);
    }

    public static void copyResourceToFile(String resourcePath, String suffix, String target) throws IOException {
        Resource[] resource = getResourceByPathPattern(resourcePath, suffix);
        if (resource.length == 0) {
            return;
        }
        for (Resource r : resource) {
            //创建文件目录
            boolean b = mkdir(target + File.separator);
            if (b) {
                //创建目标文件
                File dest = new File(target + File.separator + r.getFilename());
                //复制文件
                FileCopyUtils.copy(r.getInputStream(), new FileOutputStream(dest));
            }

        }
    }

}
