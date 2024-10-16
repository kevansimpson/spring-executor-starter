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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration test to verify behavior of {@link MultipleExecutorAutoConfiguration}
 * when {@link MultipleExecutorProperties configuration} is absent.
 *
 * @author Kevan Simpson
 */
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
    }

    @Test @DirtiesContext
    public void testExecutorAutoConfigureShouldNotOccur() {
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

    @Test
    public void testSupportClassFailsInstantiation() {
        assertThrows(UnsupportedOperationException.class, MultipleExecutorSupport::new);
    }
}
