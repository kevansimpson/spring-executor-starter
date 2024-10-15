# Spring Executor Starter

Provides configuration-defined
[AsyncTaskExecutor](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/task/AsyncTaskExecutor.html)
beans as a standard
[Spring Boot autoconfiguration](https://docs.spring.io/spring-boot/reference/using/auto-configuration.html).

## Import
Add to Maven POM as a dependency:
```xml
<dependency>
    <groupId>org.base</groupId>
    <artifactId>spring-executor-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Usage
Define executors in application configuration under `base.task`.
Shown with default values below.

### Yaml
```yaml
base:
  task:
    pools:
      nameOfExecutor:
        queueCapacity: Integer.MAX_VALUE
        coreSize: 8
        maxSize: Integer.MAX_VALUE
        allowCoreThreadTimeout: true
        keepAlive: 60s
        threadFactory: nameOfOptionalThreadFactory
        threadGroupName: nameOfOptionalThreadGroup
        waitForTasksToCompleteShutdown: false
        rejectedExecutionHandler: nameOfOptionalRejectedExecutionHandler
    shutdown:
      awaitTermination: false
      awaitTerminationPeriod: null
```