package com.step.pdf.demo.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.ClientProtocolException;
import sun.misc.BASE64Decoder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/8/9
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/8/9
 */
@Slf4j
public class EchartsUtil {

    private static final String SUCCESS_CODE = "1";

    /**
     * 将echarts图表生成图片64位码
     * @param url
     * @param option
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static String generateEchartsBase64(String url,String option) throws ClientProtocolException,
            IOException {
        String base64 = "";
        if (option == null) {
            return base64;
        }
        //正则表达式中\s匹配任何空白字符，包括空格、制表符、换页符等等, 等价于[ \f\n\r\t\v]
        //替换所有的双引号为单引号
        //替换所有的%为转义%25
        option = option.replaceAll("\\s+", "")
                .replaceAll("\"", "'")
                .replaceAll("%","%25");

        // 将option字符串作为参数发送给echartsConvert服务器
        Map<String, String> params = new HashMap<>();
        params.put("opt", option);
        String response = HttpUtil.post(url, params, "utf-8");

        // 解析echartsConvert响应
        JSONObject responseJson = JSON.parseObject(response);
        String code = responseJson.getString("code");

        // 如果echartsConvert正常返回
        if (SUCCESS_CODE.equals(code)) {
            base64 = responseJson.getString("data");
        }
        // 未正常返回
        else {
            String string = responseJson.getString("msg");
            throw new RuntimeException(string);
        }

        return base64;
    }

    /**
     * base64生成图片
     * @param base64
     * @param path
     * @throws IOException
     */
    public static void generateImage(String base64, String path) throws IOException {
        BASE64Decoder decoder = new BASE64Decoder();
        try (OutputStream out = new FileOutputStream(path)) {
            // 解密
            byte[] b = decoder.decodeBuffer(base64);
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            out.write(b);
            out.flush();
        }
    }
}
