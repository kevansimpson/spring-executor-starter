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

import lombok.Data;

import java.time.Duration;

/**
 * Executor pool configuration properties, analogous to
 * {@link org.springframework.boot.autoconfigure.task.TaskExecutionProperties.Pool}
 * and using the same default values.
 *
 * @author Kevan Simpson
 */
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
