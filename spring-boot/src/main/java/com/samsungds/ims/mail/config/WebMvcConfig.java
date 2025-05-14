package com.samsungds.ims.mail.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // Spring MVC 비동기 요청 처리를 위한 ThreadPoolTaskExecutor 구성
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("mvc-async-");
        executor.setAllowCoreThreadTimeOut(true);
        executor.initialize();
        
        // 비동기 요청 타임아웃 설정 (60초)
        configurer.setDefaultTimeout(60000);
        configurer.setTaskExecutor(executor);
    }
}
