package com.devreport.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    private static final Logger log = LoggerFactory.getLogger(OpenAiConfig.class);

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        log.info("ChatClient configured for OpenAI");
        return builder.build();
    }
}
