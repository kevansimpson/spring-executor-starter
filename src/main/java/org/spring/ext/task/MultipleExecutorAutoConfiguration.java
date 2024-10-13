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

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MultipleExecutorProperties.class)
@ConditionalOnClass(MultipleExecutorProperties.class)
public class MultipleExecutorAutoConfiguration {

    @Bean
    public ContextAwareDecorator contextAwareDecorator() {
        return new ContextAwareDecorator();
    }

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
