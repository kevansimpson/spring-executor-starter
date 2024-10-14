package org.spring.ext.task;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import org.springframework.boot.task.ThreadPoolTaskExecutorCustomizer;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.RequestAttributes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class ExecutorTestSupport {
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

    @AllArgsConstructor
    public static class TestTaskDecorator implements TaskDecorator {
        private final AtomicInteger counter;
        @Override @Nonnull
        public Runnable decorate(@Nonnull Runnable runnable) {
            counter.incrementAndGet();
            return runnable;
        }
    }

    public static class TestRequestAttributes implements RequestAttributes {
        private final Map<String, Object> attributeMap = new LinkedHashMap<>();

        @Override
        public Object getAttribute(@Nonnull String name, int scope) {
            return (scope == SCOPE_REQUEST) ? attributeMap.get(name) : null;
        }

        @Override
        public void setAttribute(@Nonnull String name, @Nonnull Object value, int scope) {
            if (scope == SCOPE_REQUEST)
                attributeMap.put(name, value);
        }

        @Override
        public void removeAttribute(@Nonnull String name, int scope) {
            if (scope == SCOPE_REQUEST)
                attributeMap.remove(name);
        }

        @Override @Nonnull
        public String[] getAttributeNames(int scope) {
            return (scope == SCOPE_REQUEST) ? attributeMap.keySet().toArray(new String[0]) : new String[0];
        }

        @Override
        public void registerDestructionCallback(@Nonnull String name, @Nonnull Runnable callback, int scope) {
            // no op
        }

        @Override
        public Object resolveReference(@Nonnull String key) {
            return null;
        }

        @Override @Nonnull
        public String getSessionId() {
            return "There is no Session, only Zuul";
        }

        @Override @Nonnull
        public Object getSessionMutex() {
            return this;
        }
    }
}
