package com.step.pdf.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/8/17
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/8/17
 */
@Controller
@RequestMapping("/th")
public class HtmlController {


    @GetMapping("/test")
    public String test(ModelMap map,@RequestParam("name")String name){
        map.put("name", name);
        return "/test";
    }


    @GetMapping("/test-freemarker")
    public String testFreemarker(Model map, @RequestParam("name")String name){
        map.addAttribute("name", name);
        return "/test-freemarker";
    }
}
