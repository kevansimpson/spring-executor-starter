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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.task.ThreadPoolTaskExecutorCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

import static org.spring.ext.task.MultipleExecutorSupport.createMultipleTaskExecutors;

/**
 * Auto-configuration to provide configuration-defined {@link AsyncTaskExecutor} beans.
 *
 * @author Kevan Simpson
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MultipleExecutorProperties.class)
@ConditionalOnClass(MultipleExecutorProperties.class)
public class MultipleExecutorAutoConfiguration {

    /**
     * Injects a {@link TaskDecorator} which passes thread-local attributes via
     * {@link org.springframework.web.context.request.RequestContextHolder}
     * using the name &quot;contextAwareDecorator&quot;.
     *
     * @return a <code>TaskDecorator</code> bean.
     */
    @Bean
    public ContextAwareDecorator contextAwareDecorator() {
        return new ContextAwareDecorator();
    }

    /**
     * Injects map of configuration-defined {@link AsyncTaskExecutor} beans.
     *
     * @param properties Configuration properties for executor beans.
     * @param taskExecutorCustomizer Optionally provided executor customizer.
     * @param taskDecorator Optionally provided task decorator.
     * @param registry Bean definition registry.
     * @param applicationContext Eponymous application context.
     * @return a map of executor beans mapped by configured name.
     */
    @Bean
    public Map<String, AsyncTaskExecutor> multipleTaskExecutors(
            MultipleExecutorProperties properties,
            ObjectProvider<ThreadPoolTaskExecutorCustomizer> taskExecutorCustomizer,
            ObjectProvider<TaskDecorator> taskDecorator,
            DefaultListableBeanFactory registry,
            ApplicationContext applicationContext) {

        return createMultipleTaskExecutors(
                properties,
                taskExecutorCustomizer,
                taskDecorator,
                registry,
                applicationContext);
    }
}
