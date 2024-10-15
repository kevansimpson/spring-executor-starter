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

import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;
import org.springframework.web.context.request.RequestAttributes;

import static org.springframework.web.context.request.RequestContextHolder.*;

/**
 * A {@link TaskDecorator} which passes thread-local attributes via
 * {@link org.springframework.web.context.request.RequestContextHolder}.
 *
 * @author Kevan Simpson
 */
public class ContextAwareDecorator implements TaskDecorator {
    @Override @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
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
