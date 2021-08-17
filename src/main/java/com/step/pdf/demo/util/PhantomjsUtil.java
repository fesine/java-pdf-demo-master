package com.step.pdf.demo.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/8/11
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/8/11
 */
@Slf4j
public class PhantomjsUtil {

    /**
     * phantomjs 本地执行工具类，需要服务端安装phantomjs服务
     * @param jsFile 待执行js
     * @param params 参数
     * @return
     * @throws IOException
     */
    public static String exec(String jsFile,String params) throws IOException {
        //String cmd = "phantomjs " + PATH + "pdf/js/code.js " + fileName;
        String cmd = "phantomjs " + jsFile +" " + params;
        log.debug("---->phantomjs exec cmd:{}", cmd);
        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec(cmd);
        InputStream is = p.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuffer sbf = new StringBuffer();
        String tmp;
        while ((tmp = br.readLine()) != null) {
            sbf.append(tmp);
        }
        String result = sbf.toString();
        log.debug("---->after phantomjs exec.result:{}", result);
        return result;
    }
}
