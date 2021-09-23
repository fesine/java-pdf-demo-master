package com.step.pdf.demo;

import com.step.pdf.demo.config.EsMultiConfig;
import com.step.pdf.demo.config.RedisMultiConfig;
import com.step.pdf.demo.multiconfig.annotation.EnableMultiConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableMultiConfig(basePackages = {"com.step.pdf.demo.controller", "com.step.pdf.demo.service"}, configs = {EsMultiConfig.class,
        RedisMultiConfig.class})
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
