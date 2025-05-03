package com.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 비동기 처리를 위한 Executor Bean
     * @return Executor
     */
    @Bean("chatPersistenceExecutor")
    public Executor chatPersistenceExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(2);
        ex.setMaxPoolSize(4);
        ex.setQueueCapacity(1000);
        ex.setThreadNamePrefix("ChatPersist-");
        ex.initialize();
        return ex;
    }
}