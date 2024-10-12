package org.spring.ext.task;

import lombok.Data;

import java.time.Duration;

@Data
public class ShutdownConfig {
    private boolean awaitTermination;
    private Duration awaitTerminationPeriod;
}
