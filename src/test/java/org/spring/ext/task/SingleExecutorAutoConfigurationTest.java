package org.spring.ext.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.spring.ext.task.ExecutorTestSupport.PoolHolder;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("unchecked")
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = { SingleExecutorAutoConfigurationTest.AutoTestConfig.class },
        properties = { "spring.main.allow-bean-definition-overriding=true" }
)
@ActiveProfiles("test")
public class SingleExecutorAutoConfigurationTest {
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
        public AtomicInteger counter() {
            return new AtomicInteger(0);
        }
    }

    @Test @DirtiesContext
    public void testExecutorAutoConfigureShouldNotOccur() throws Exception {
        Map<String, AsyncTaskExecutor> executorsMap =
                (Map<String, AsyncTaskExecutor>) applicationContext.getBean("multipleTaskExecutors");
        assertThat(executorsMap).isNotNull();
        assertThat(executorsMap.size()).isEqualTo(0);
        // default only
        assertThat(applicationContext.getBeansOfType(AsyncTaskExecutor.class).size()).isEqualTo(1);
    }

    @Test @DirtiesContext
    public void testThereAreNoCustomPools() {
        Stream.of("One", "Two", "Three", "Four", "Five", "Six")
                .forEach(name ->
                    assertThrows(NoSuchBeanDefinitionException.class,
                            () -> applicationContext.getBean("testPool"+ name)));
    }

    @Test @DirtiesContext
    public void testPoolHolderFailsWithoutCustomPool() {
        assertThrows(NoSuchBeanDefinitionException.class, () -> {
            PoolHolder holder = applicationContext.getBean(PoolHolder.class);
            assertThat(holder).isNotNull();
            holder.doSomethingAsync(new AtomicBoolean(false));
        });
    }
}
