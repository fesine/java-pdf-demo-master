package com.step.pdf.demo.websocket;

import com.step.pdf.demo.websocket.config.WebSocketConfig;
import com.step.pdf.demo.websocket.server.WebSocketServer;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/10/15
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/10/15
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({WebSocketConfig.class, WebSocketServer.class})
public @interface EnableWebscoket {
}
