package org.spring.ext.task;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.boot.task.ThreadPoolTaskExecutorCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

@Log4j2
public class MultipleExecutorSupport {
    public static Map<String, AsyncTaskExecutor> createMultipleTaskExecutors(
            MultipleExecutorProperties properties,
            ObjectProvider<ThreadPoolTaskExecutorCustomizer> taskExecutorCustomizer,
            ObjectProvider<TaskDecorator> taskDecorator,
            DefaultListableBeanFactory registry,
            ApplicationContext applicationContext) {

        Map<String, AsyncTaskExecutor> executorMap = new LinkedHashMap<>();
        for (String poolName : properties.getPools().keySet()) {
            PoolConfig poolConfig = properties.getPools().get(poolName);
            log.info("Creating AsyncTaskExecutor {}: {}", poolName, poolConfig);
            ThreadPoolTaskExecutorBuilder builder = newBuilder(poolName, poolConfig, properties.getShutdown());
            taskExecutorCustomizer.orderedStream().forEach(builder::additionalCustomizers);
            builder.taskDecorator(taskDecorator.getIfUnique());

            ThreadPoolTaskExecutor pool = builder.build();
            // configure properties that Spring Boot (afaik) does not
            customizeExecutor(poolName, poolConfig, pool, applicationContext);
            // create + register executor as bean
            pool.initialize();
            log.info("Registering initialized pool: {} -> {}",
                    poolName, ToStringBuilder.reflectionToString(pool));
            registry.registerSingleton(poolName, pool);
            log.info("Registration for pool {} is complete!", poolName);
            executorMap.put(poolName, pool);
        }

        return executorMap;
    }

    static void customizeExecutor(
            String poolName,
            PoolConfig poolConfig,
            ThreadPoolTaskExecutor executor,
            ApplicationContext applicationContext) {

        log.debug("Customizing executor: {}", poolName);
        executor.setBeanName(poolName);
        if (StringUtils.isNotBlank(poolConfig.getThreadFactory())) {
            log.debug("Setting thread factory: {}", poolConfig.getThreadFactory());
            executor.setThreadFactory(applicationContext.getBean(poolConfig.getThreadFactory(), ThreadFactory.class));
        }
        if (StringUtils.isNotBlank(poolConfig.getTaskDecorator())) {
            log.debug("Setting task decorator: {}", poolConfig.getTaskDecorator());
            executor.setTaskDecorator(applicationContext.getBean(poolConfig.getTaskDecorator(), TaskDecorator.class));
        }
        executor.setWaitForTasksToCompleteOnShutdown(poolConfig.isWaitForTasksToCompleteShutdown());
        setRejectedExecutionHandler(executor, poolConfig.getRejectedExecutionHandler(), applicationContext);
    }

    static ThreadPoolTaskExecutorBuilder newBuilder(String name, PoolConfig pool, ShutdownConfig shutdown) {
        return new ThreadPoolTaskExecutorBuilder()
                .queueCapacity(pool.getQueueCapacity())
                .corePoolSize(pool.getCoreSize())
                .maxPoolSize(pool.getMaxSize())
                .allowCoreThreadTimeOut(pool.isAllowCoreThreadTimeout())
                .keepAlive(pool.getKeepAlive())
                .awaitTermination(shutdown.isAwaitTermination())
                .awaitTerminationPeriod(shutdown.getAwaitTerminationPeriod())
                .threadNamePrefix(String.format("%s-task-", name));
    }

    static void setRejectedExecutionHandler(
            ThreadPoolTaskExecutor executor,
            String policy,
            ApplicationContext applicationContext) {

        if (StringUtils.isNotBlank(policy)) {
            log.debug("Setting rejected execution handler: {}", policy);
            switch (policy) {
                case "AbortPolicy" ->
                        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
                case "CallerRunsPolicy" ->
                        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
                case "DiscardOldestPolicy" ->
                        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
                case "DiscardPolicy" ->
                        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
                default ->
                        executor.setRejectedExecutionHandler(
                                applicationContext.getBean(policy, RejectedExecutionHandler.class));
            }
        }
    }
}
