package org.spring.ext.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.spring.ext.task.ExecutorTestSupport.PoolHolder;
import org.spring.ext.task.ExecutorTestSupport.TestExecutorCustomizer;
import org.spring.ext.task.ExecutorTestSupport.TestRequestAttributes;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.request.RequestAttributes;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.web.context.request.RequestContextHolder.getRequestAttributes;
import static org.springframework.web.context.request.RequestContextHolder.setRequestAttributes;

@SuppressWarnings("unchecked")
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
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
        public AtomicInteger counter() {
            return new AtomicInteger(0);
        }

        @Bean
        public TaskDecorator testTaskDecorator(AtomicInteger counter) {
            return new TestTaskDecorator(counter);
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

    @Test @DirtiesContext
    public void testExecutorAutoConfigure() throws Exception {
        Map<String, AsyncTaskExecutor> executorsMap =
                (Map<String, AsyncTaskExecutor>) applicationContext.getBean("multipleTaskExecutors");
        assertThat(executorsMap).isNotNull();
        assertThat(executorsMap.size()).isEqualTo(6);
        Stream.of("One", "Two", "Three", "Four", "Five", "Six")
                .forEach(name -> {
                    String poolName = "testPool"+ name;
                    assertThat(executorsMap.containsKey(poolName)).isTrue();
                    assertThat(executorsMap.get(poolName)).isNotNull();
                });
        // default + 6 yaml-defined
        assertThat(applicationContext.getBeansOfType(AsyncTaskExecutor.class).size()).isEqualTo(7);

        AtomicInteger counter = applicationContext.getBean("counter", AtomicInteger.class);
        PoolHolder holder = applicationContext.getBean(PoolHolder.class);
        assertThat(holder).isNotNull();
        assertThat(counter.get()).isEqualTo(0);
        AtomicBoolean isAsync = new AtomicBoolean(false);
        CompletableFuture<String> future = holder.doSomethingAsync(isAsync);
        assertThat(isAsync.get()).isFalse();
        assertThat(future).isNotNull();
        assertThat(future.get()).isEqualTo("done");
        assertThat(isAsync.get()).isTrue();
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test @DirtiesContext
    public void testRequestAttributes() throws Exception {
        AsyncTaskExecutor one = applicationContext.getBean("testPoolOne", AsyncTaskExecutor.class);
        AsyncTaskExecutor two = applicationContext.getBean("testPoolTwo", AsyncTaskExecutor.class);
        AsyncTaskExecutor three = applicationContext.getBean("testPoolThree", AsyncTaskExecutor.class);
        assertThat(one).isNotNull();
        assertThat(two).isNotNull();
        assertThat(three).isNotNull();

        Runnable hasAttributes = () -> assertThat(getRequestAttributes()).isNotNull();
        Runnable noAttributes = () -> {
            RequestAttributes attributes = getRequestAttributes();
            if (attributes != null)
                throw new IllegalStateException();
        };

        setRequestAttributes(null);
        assertDoesNotThrow(() -> one.submit(noAttributes).get());
        assertDoesNotThrow(() -> two.submit(noAttributes).get());
        assertDoesNotThrow(() -> three.submit(noAttributes).get());
        assertThrows(ExecutionException.class, () -> one.submit(hasAttributes).get());
        assertThrows(ExecutionException.class, () -> two.submit(hasAttributes).get());
        assertThrows(ExecutionException.class, () -> three.submit(hasAttributes).get());

        setRequestAttributes(new TestRequestAttributes());
        assertThrows(ExecutionException.class, () -> one.submit(hasAttributes).get());
        assertThrows(ExecutionException.class, () -> two.submit(hasAttributes).get());
        assertDoesNotThrow(() -> three.submit(hasAttributes));
        assertDoesNotThrow(() -> one.submit(noAttributes).get());
        assertDoesNotThrow(() -> two.submit(noAttributes).get());
        assertThrows(ExecutionException.class, () -> three.submit(noAttributes).get());
    }
}
