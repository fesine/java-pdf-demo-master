package com.step.pdf.demo.websocket.server;


import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.websocket.WsSession;
import org.springframework.util.CollectionUtils;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@ServerEndpoint("/websocket/{userId}")
//@Component
public class WebSocketServer {
    /**
     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static int onlineCount = 0;
    /**
     * 存放userId对应的MyWebSocket对象列表。
     */
    private static final ConcurrentHashMap<String, CopyOnWriteArrayList<WebSocketServer>> USER_WS_LIST_MAP =
            new ConcurrentHashMap();

    /**
     * 存放每个userId对应的session列表
     */
    private static final ConcurrentHashMap<String, CopyOnWriteArrayList<Session>> USER_SESSION_LIST_MAP =
            new ConcurrentHashMap();

    /**
     * 存放每个sessionId对应的userId
     */
    private static final ConcurrentHashMap<String, String> SESSION_USER_MAP = new ConcurrentHashMap();

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;


    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(
            @PathParam("userId") String userId,
            Session session) {
        this.session = session;
        String sessionId = getSessionId(session);
        //用于断开时，根据sessionId找到对应的userId，关闭对应的webSocket及session
        SESSION_USER_MAP.put(sessionId, userId);
        CopyOnWriteArrayList<Session> sessionList = USER_SESSION_LIST_MAP.get(userId);
        if (CollectionUtils.isEmpty(sessionList)) {
            sessionList = new CopyOnWriteArrayList<>();
        }
        sessionList.add(session);
        USER_SESSION_LIST_MAP.put(userId, sessionList);
        CopyOnWriteArrayList<WebSocketServer> webSocketList= USER_WS_LIST_MAP.get(userId);
        if (CollectionUtils.isEmpty(webSocketList)) {
            webSocketList = new CopyOnWriteArrayList<>();
        }
        webSocketList.add(this);
        USER_WS_LIST_MAP.put(userId, webSocketList);
        //在线数加1
        addOnlineCount();
        log.info(">>>>>>>>>【WS消息通知系统】有新用户[{}]加入！当前在线人数为[{}]", userId, getOnlineCount());
        try {
            sendMessage("连接成功");
        } catch (IOException e) {
            log.error("websocket IO异常");
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        String sessionId = getSessionId(session);
        //根据sessionId找到userId
        if (!SESSION_USER_MAP.containsKey(sessionId)) {
            return;
        }
        String userId = SESSION_USER_MAP.get(sessionId);
        //关闭对应的session
        List<Session> sessionList = USER_SESSION_LIST_MAP.get(userId);
        if (!CollectionUtils.isEmpty(sessionList)) {
            for (Session s : sessionList) {
                String closeId = getSessionId(s);
                if (sessionId.equals(closeId)) {
                    try {
                        s.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }

            }
        }
        //移除ws
        List<WebSocketServer> wsList = USER_WS_LIST_MAP.get(userId);
        if (!CollectionUtils.isEmpty(wsList)) {
            wsList.remove(this);
        }
        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //在线数减1
        subOnlineCount();
        log.info(">>>>>>>>>【WS消息通知系统】有用户[{}]断开连接！当前在线人数为[{}]", userId, getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     * @param session session
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        String sessionId = getSessionId(session);
        log.info(">>>>>>>>>【WS消息通知系统】来自sessionId=[{}]的消息:[{}]",sessionId,message);
        if (!SESSION_USER_MAP.containsKey(sessionId)) {
            return;
        }
        String userId = SESSION_USER_MAP.get(sessionId);
        if (USER_WS_LIST_MAP.containsKey(userId)) {
            //群发消息
            for (WebSocketServer item : USER_WS_LIST_MAP.get(userId)) {
                try {
                    item.sendMessage(message);
                } catch (IOException e) {
                    log.error(">>>>>>>>>【WS消息通知系统】来自userId=[{}]的消息发送异常:{}", userId, e);
                }
            }
        }
    }

    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error(">>>>>>>>>【WS消息通知系统】来自sessionId=[{}]的连接发生异常:{}", getSessionId(session),error);
    }

    /**
     * 给某一用户发送自定义消息
     * @param userId
     * @param message
     */
    public static void sendInfo(String userId, String message) {
        log.info(">>>>>>>>>【WS消息通知系统】待发送给userId=[{}]的消息:[{}]", userId, message);
        if(USER_WS_LIST_MAP.containsKey(userId)){
            //群发消息
            for (WebSocketServer item : USER_WS_LIST_MAP.get(userId)) {
                try {
                    item.sendMessage(message);
                } catch (IOException e) {
                    log.error(">>>>>>>>>【WS消息通知系统】发送给userId=[{}]的消息，异常:{}", userId, e);
                }
            }
        }else{
            log.info(">>>>>>>>>【WS消息通知系统】没有找到待发送的用户。userId=[{}]", userId);
        }
    }


    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }


    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        onlineCount--;
    }

    /**
     * 获取sessonId
     *
     * @param session
     * @return
     */
    public String getSessionId(Session session) {
        WsSession ws = (WsSession)session;
        String sessionId = ws.getHttpSessionId();
        if (sessionId == null) {
            sessionId = ws.getId();
        }
        return sessionId;
    }
}
