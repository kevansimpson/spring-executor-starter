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

package org.spring.ext;

import org.spring.ext.task.MultipleExecutorAutoConfiguration;
import org.spring.ext.task.MultipleExecutorProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Spring Boot application for {@link MultipleExecutorAutoConfiguration} integration tests.
 *
 * @author Kevan Simpson
 */
@SpringBootApplication
@EnableConfigurationProperties
@EnableAsync
public class SpringExecutorStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringExecutorStarterApplication.class, args);
    }
}
