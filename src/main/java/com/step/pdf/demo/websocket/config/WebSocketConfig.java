package com.step.pdf.demo.websocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @description: 类描述
 * @author: Fesine
 * @createTime:2018/9/14
 * @update:修改内容
 * @author: Fesine
 * @updateTime:2018/9/14
 */
@Configuration
public class WebSocketConfig {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
