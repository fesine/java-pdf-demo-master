package com.step.pdf.demo.controller;

import com.step.pdf.demo.multiconfig.annotation.MultiService;
import com.step.pdf.demo.multiconfig.annotation.ServiceGroup;
import com.step.pdf.demo.service.CombineMultiService;
import com.step.pdf.demo.service.ConsumerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/9/20
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/20
 */
@RestController
@ServiceGroup(value = "hangzhou")
public class MultiController {

    @Resource(name = "esService")
    private ConsumerService consumerEsService;

    @Resource(name = "redisService")
    private ConsumerService consumerRedisService;

    @MultiService
    private CombineMultiService combineMultiService;

    @GetMapping("/es/{msg}")
    public String es(@PathVariable String msg){
        return consumerEsService.consumer(msg);
    }

    @GetMapping("/redis/{msg}")
    public String redis(@PathVariable String msg){
        return consumerRedisService.consumer(msg);
    }

    @GetMapping("/comb/{msg}")
    public String combine(@PathVariable String msg){
        return combineMultiService.handler(msg);
    }

}
