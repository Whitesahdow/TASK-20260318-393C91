package com.busapp.infra.monitoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PerformanceMonitor {
    private static final Logger log = LoggerFactory.getLogger(PerformanceMonitor.class);

    public double analyzeLatency(List<Long> latencies) {
        double p95 = calculateP95(latencies);
        if (p95 > 500) {
            generateDiagnosticReport(p95);
        }
        return p95;
    }

    public double calculateP95(List<Long> latencies) {
        if (latencies == null || latencies.isEmpty()) {
            return 0.0;
        }
        List<Long> sorted = new ArrayList<>(latencies);
        Collections.sort(sorted);
        int idx = (int) Math.ceil(sorted.size() * 0.95) - 1;
        idx = Math.max(0, Math.min(idx, sorted.size() - 1));
        return sorted.get(idx);
    }

    private void generateDiagnosticReport(double p95) {
        log.warn("Diagnostic Report Generated: API P95 latency {}ms exceeds threshold 500ms.", p95);
    }
}
