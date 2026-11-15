package com.example.refactored_student_housing.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Week 12 — enables Spring's caching abstraction (used to cache the student
 * search) and the asynchronous execution model (used by the CSV bulk-import
 * feature). A dedicated thread pool keeps CSV imports off the HTTP request
 * threads so the browser never has to wait for processing to finish.
 */
@Configuration
@EnableAsync
@EnableCaching
public class AsyncCachingConfig {

    @Bean(name = "csvImportExecutor")
    public Executor csvImportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("csv-import-");
        executor.initialize();
        return executor;
    }
}
