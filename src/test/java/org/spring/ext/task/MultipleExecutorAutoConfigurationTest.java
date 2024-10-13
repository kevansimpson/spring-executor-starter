package org.spring.ext.task;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.spring.ext.task.ExecutorTestSupport.PoolHolder;
import org.spring.ext.task.ExecutorTestSupport.TestExecutorCustomizer;
import org.spring.ext.task.ExecutorTestSupport.TestTaskDecorator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.task.ThreadPoolTaskExecutorCustomizer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = { MultipleExecutorAutoConfigurationTest.AutoTestConfig.class },
        properties = { "spring.main.allow-bean-definition-overriding=true" }
)
@ActiveProfiles("tasks")
public class MultipleExecutorAutoConfigurationTest {
    @Autowired
    private ApplicationContext applicationContext;

    @TestConfiguration
    @AutoConfigureAfter(MultipleExecutorAutoConfiguration.class)
    static class AutoTestConfig {
        @Bean
        public PoolHolder poolHolder() {
            return new PoolHolder();
        }

        @Bean
        public ThreadFactory testThreadFactory() {
            return Executors.defaultThreadFactory();
        }

        @Bean
        public TaskDecorator testTaskDecorator() {
            return new TestTaskDecorator();
        }

        @Bean
        public RejectedExecutionHandler testRejectedExecutionHandler() {
            return new ThreadPoolExecutor.AbortPolicy();
        }

        @Bean
        public ThreadPoolTaskExecutorCustomizer testTaskExecutorCustomizer() {
            return new TestExecutorCustomizer();
        }
    }

    @Test
    public void testExecutorAutoConfigure() {
        Map<String, AsyncTaskExecutor> executorsMap =
                (Map<String, AsyncTaskExecutor>) applicationContext.getBean("multipleTaskExecutors");
        assertThat(executorsMap).isNotNull();
        assertThat(executorsMap.size()).isEqualTo(6);
        Stream.of("One", "Two", "Three", "Four", "Five", "Six")
                .forEach(name -> assertThat(executorsMap.containsKey("testPool"+ name)).isTrue());
    }
}
