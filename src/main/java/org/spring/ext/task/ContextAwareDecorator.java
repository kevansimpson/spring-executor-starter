package org.spring.ext.task;

import org.springframework.core.task.TaskDecorator;
import org.springframework.web.context.request.RequestAttributes;

import static org.springframework.web.context.request.RequestContextHolder.*;

/**
 * A {@link TaskDecorator} which passes thread-local attributes via
 * {@link org.springframework.web.context.request.RequestContextHolder}.
 */
public class ContextAwareDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        RequestAttributes attributes = getRequestAttributes();
        return () -> {
            try {
                setRequestAttributes(attributes, true);
                runnable.run();
            }
            finally {
                resetRequestAttributes();
            }
        };
    }
}
