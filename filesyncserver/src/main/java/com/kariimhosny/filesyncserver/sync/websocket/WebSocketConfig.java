package com.kariimhosny.filesyncserver.sync.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final EventsHandler webSocketHandler;
    private final AuthHandshakeInterceptor authInterceptor;

    public WebSocketConfig(EventsHandler webSocketHandler, AuthHandshakeInterceptor authInterceptor) {
        this.webSocketHandler = webSocketHandler;
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws")
        .addInterceptors(authInterceptor)
                .setAllowedOrigins("*"); // allow all origins for now
    }
}
