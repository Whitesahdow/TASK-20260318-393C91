package com.busapp;

import com.busapp.infra.monitoring.PerformanceMonitor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ObservabilityModuleTest {
    @Test
    void p95CalculationShouldBeCorrect() {
        PerformanceMonitor monitor = new PerformanceMonitor();
        double p95 = monitor.calculateP95(List.of(100L, 120L, 200L, 600L, 900L));
        assertTrue(p95 >= 600);
    }
}
