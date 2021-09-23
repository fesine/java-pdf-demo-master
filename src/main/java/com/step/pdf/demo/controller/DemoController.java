package com.step.pdf.demo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.step.pdf.demo.config.PdfConfig;
import com.step.pdf.demo.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class DemoController {
    private static final String URL = "http://localhost:6666";

    private static final ConcurrentHashMap<String, String> HTML_MAP = new ConcurrentHashMap<>();

    @Autowired
    private PdfConfig pdfConfig;

    @GetMapping("init")
    public void init() throws IOException {
        pdfConfig.init();
    }

    @GetMapping("pdf")
    public void test(HttpServletResponse response) throws Exception {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "表格标题");
        variables.put("array", new String[]{"土豆", "番茄", "白菜", "芹菜", "土豆", "番茄", "白菜", "芹菜", "芹菜", "土豆",
                "芹菜", "土豆", "番茄", "白菜", "芹菜", "土豆", "番茄", "白菜", "芹菜", "土豆", "番茄", "白菜", "芹菜", "土豆",
                "芹菜", "土豆", "番茄", "白菜", "芹菜", "土豆", "番茄", "白菜", "芹菜", "土豆", "番茄", "白菜", "芹菜", "土豆",
                "芹菜", "土豆", "番茄", "白菜", "芹菜", "土豆", "番茄", "白菜", "芹菜", "土豆", "番茄", "白菜", "芹菜", "土豆",
                "芹菜", "土豆", "番茄", "白菜", "芹菜", "土豆", "番茄", "白菜", "芹菜", "土豆", "番茄", "白菜", "芹菜", "土豆",
                "番茄", "白菜", "芹菜"});
        //PdfUtil.buildPdf(response, "用于测试的pdf", "example", variables);
    }

    @GetMapping("temp/static/{template}")
    public void getHtml(HttpServletResponse response, @PathVariable("template")String template) throws IOException {
        response.setHeader("Content-type", "text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String s = HTML_MAP.get(template);
        response.getOutputStream().write(s.getBytes());
    }

    @GetMapping("echarts")
    public void echarts(HttpServletResponse response) throws Exception {
        //初始化临时目录
        pdfConfig.init();
        // 变量
        String title = "水果";
        String[] categories = new String[]{"苹果", "香蕉", "西瓜"};
        int[] values = new int[]{3000, 2000, 1000};
        // 模板参数
        HashMap<String, Object> datas = new HashMap<>();
        datas.put("categories", JSON.toJSONString(categories));
        datas.put("values", JSON.toJSONString(values));
        datas.put("title", title);

        // 1、生成option字符串
        String option = FreemarkerUtil.generateStringInClassLoader(pdfConfig.getTemplateDir(),
                "option.ftl", datas);
        // 2、生成图片码
        String base64 = EchartsUtil.generateEchartsBase64(URL,option);
        Map<String, Object> variables = new HashMap<>();
        variables.put("imageSrc", "data:image/png;base64,"+base64);
        //variables.put("content", FileUtil.getStringByJarFileName(pdfConfig.getTemplateDir()+"/content.txt"));
        variables.put("content", "");
        variables.put("width",440);
        variables.put("height",900);
        //3、使用freemarker渲染模板
        String html = FreemarkerUtil.generateStringInDirectory(pdfConfig.getTmpTemplateDir(), pdfConfig.getTemplateName(), variables);
        HTML_MAP.put(pdfConfig.getTemplateName(),html);
        //获取文件高度
        String result = PhantomjsUtil.exec(pdfConfig.getTmpJsFile(), pdfConfig.getServerTemplate());
        System.out.println(result);
        JSONObject jsonObject = JSONObject.parseObject(result);
        Integer scrollHeight = (Integer) jsonObject.get("document.documentElement.scrollHeight");
        variables.put("height", scrollHeight+"");
        variables.put("width", jsonObject.get("document.documentElement.scrollWidth")+"");
        html = FreemarkerUtil.generateStringInDirectory(pdfConfig.getTmpTemplateDir(), pdfConfig.getTemplateName(), variables);
        System.out.println(html);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "filename=" + new String(("test.pdf").getBytes(), "iso8859-1"));
        PdfUtil.buildPdf(response.getOutputStream(), html, pdfConfig.getTmpFontDir(), pdfConfig.getTmpImageDir());
        //generateImage(base64, "target/test.png");
    }

}
