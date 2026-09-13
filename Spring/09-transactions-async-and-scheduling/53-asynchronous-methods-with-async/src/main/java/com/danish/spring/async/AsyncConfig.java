package com.danish.spring.async;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

// @EnableAsync is what turns @Async from an inert annotation into a real proxy that
// intercepts the call and hands it to an Executor instead of running it on the calling
// thread. Implementing AsyncConfigurer lets us supply BOTH the thread pool @Async uses
// and the handler for exceptions thrown by void @Async methods.
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private final CapturingAsyncExceptionHandler capturingAsyncExceptionHandler;

    public AsyncConfig(CapturingAsyncExceptionHandler capturingAsyncExceptionHandler) {
        this.capturingAsyncExceptionHandler = capturingAsyncExceptionHandler;
    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setThreadNamePrefix("async-demo-");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return capturingAsyncExceptionHandler;
    }
}
