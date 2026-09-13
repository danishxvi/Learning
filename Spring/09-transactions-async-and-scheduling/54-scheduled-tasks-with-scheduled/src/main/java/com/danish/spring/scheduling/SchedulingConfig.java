package com.danish.spring.scheduling;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    // Spring Boot's default TaskScheduler (when none is defined) runs EVERY @Scheduled
    // method on a SINGLE thread - see the .md for the real, measured contention that
    // causes between fastTask and slowTask without this bean. A pool-based scheduler
    // gives each concurrently-due task its own thread instead of queuing behind
    // whichever task happens to be running.
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("scheduling-pool-");
        scheduler.initialize();
        return scheduler;
    }
}
