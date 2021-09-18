package com.step.pdf.demo;

import com.step.pdf.demo.config.MyMultiConfig;
import com.step.pdf.demo.config.MyMultiConfig2;
import com.step.pdf.demo.multiconfig.annotation.EnableMultiConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableMultiConfig(basePackages = {"com.step.pdf.demo"}, configs = {MyMultiConfig.class,
        MyMultiConfig2.class})
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
