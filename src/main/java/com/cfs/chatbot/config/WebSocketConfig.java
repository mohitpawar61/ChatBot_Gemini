package com.cfs.chatbot.config;

import com.cfs.chatbot.controller.GeminiWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GeminiWebSocketHandler geminiWebSocketHandler;

    public WebSocketConfig(GeminiWebSocketHandler geminiWebSocketHandler) {
        this.geminiWebSocketHandler = geminiWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(geminiWebSocketHandler,"/chat").setAllowedOrigins("*");
    }
}
