package org.spring.ext.task;

import lombok.Data;

import java.time.Duration;

@Data
public class PoolConfig {
    private int queueCapacity = Integer.MAX_VALUE;
    private int coreSize = 8;
    private int maxSize = Integer.MAX_VALUE;
    private boolean allowCoreThreadTimeout = true;
    private Duration keepAlive = Duration.ofSeconds(60L);

    private String threadFactory;
    private String threadGroupName;
    private String taskDecorator;
    private boolean waitForTasksToCompleteShutdown;
    private String rejectedExecutionHandler;
}
