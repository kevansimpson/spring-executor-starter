package org.spring.ext.task;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@ConfigurationProperties("base.task")
public class MultipleExecutorProperties {
    private Map<String, PoolConfig> pools = new LinkedHashMap<>();
    private ShutdownConfig shutdown;
}
