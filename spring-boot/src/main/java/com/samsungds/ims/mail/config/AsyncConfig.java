package com.samsungds.ims.mail.config;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 이메일 처리 전용 스레드풀 설정
     */
    @Bean(name = "emailExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 코어 스레드 크기
        executor.setCorePoolSize(5);
        // 최대 스레드 크기
        executor.setMaxPoolSize(10);
        // 대기열 크기
        executor.setQueueCapacity(25);
        // 스레드 이름 접두사
        executor.setThreadNamePrefix("email-task-");
        // 종료 시 대기 중인 작업 처리
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 종료 대기 시간 (초)
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }

    // 비동기 작업 예외 처리를 위한 빈
    @Bean
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}