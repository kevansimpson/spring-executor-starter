/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/**
 * Factory methods for creating {@link AsyncTaskExecutor} beans from {@link MultipleExecutorProperties} configuration.
 *
 * @author Kevan Simpson
 */
@Log4j2
public class MultipleExecutorSupport {
    /**
     * Creates and customizes {@link AsyncTaskExecutor} beans.
     *
     * @param properties Configuration properties for executor beans.
     * @param taskExecutorCustomizer Optionally provided executor customizer.
     * @param taskDecorator Optionally provided task decorator.
     * @param registry Bean definition registry.
     * @param applicationContext Eponymous application context.
     * @return a map of executor beans mapped by configured name.
     */
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

    /**
     * Customizes a single {@link ThreadPoolTaskExecutor} from configuration.
     *
     * @param poolName The name of the executor pool to be customized.
     * @param poolConfig The pool configuration and source of customization.
     * @param executor The executor to be customized.
     * @param applicationContext Eponymous application context.
     */
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

    /**
     * Creates an executor builder using provided configuration.
     *
     * @param name The name of the executor pool to be customized.
     * @param pool The pool configuration and source of customization.
     * @param shutdown The shutdown configuration and source of customization.
     * @return the configured executor builder.
     */
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

    /**
     * Configures the {@link RejectedExecutionHandler} for the given executor.
     *
     * @param executor The executor to be customized.
     * @param policy The policy to apply to the executor.
     * @param applicationContext Eponymous application context.
     */
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
