package org.spring.ext.task;

import org.springframework.boot.task.ThreadPoolTaskExecutorCustomizer;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class ExecutorTestSupport {
    public static AtomicInteger COUNTER = new AtomicInteger(0);

    public static class PoolHolder {
        @Async("testPoolOne")
        public CompletableFuture<String> doSomethingAsync(AtomicBoolean flag) throws InterruptedException {
            Thread.sleep(250);
            flag.set(true);
            return completedFuture("done");
        }
    }

    public static class TestExecutorCustomizer implements ThreadPoolTaskExecutorCustomizer {
        @Override
        public void customize(ThreadPoolTaskExecutor taskExecutor) {
            taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        }
    }

    public static class TestTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            COUNTER.incrementAndGet();
            return runnable;
        }
    }
}
