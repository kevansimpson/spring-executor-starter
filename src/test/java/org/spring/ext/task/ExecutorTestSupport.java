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

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import org.springframework.boot.task.ThreadPoolTaskExecutorCustomizer;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Supporting classes to test {@link MultipleExecutorAutoConfiguration} capabilities.
 *
 * @author Kevan Simpson
 */
public class ExecutorTestSupport {
    /**
     * Utility with test method annotated with {@link Async} and configured to use &quot;testPoolOne&quot;.
     */
    public static class PoolHolder {
        @Async("testPoolOne")
        public CompletableFuture<String> doSomethingAsync(AtomicBoolean flag) throws InterruptedException {
            Thread.sleep(250);
            flag.set(true);
            return completedFuture("done");
        }
    }

    /**
     * Test customizer which sets {@link java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy} on the executor.
     */
    public static class TestExecutorCustomizer implements ThreadPoolTaskExecutorCustomizer {
        @Override
        public void customize(ThreadPoolTaskExecutor taskExecutor) {
            taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        }
    }

    /**
     * Test task decorator which increments a {@link AtomicInteger counter}.
     */
    @AllArgsConstructor
    public static class TestTaskDecorator implements TaskDecorator {
        private final AtomicInteger counter;
        @Override @Nonnull
        public Runnable decorate(@Nonnull Runnable runnable) {
            counter.incrementAndGet();
            return runnable;
        }
    }
}
