package com.busapp.infra.monitoring;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ApiTimingInterceptor implements HandlerInterceptor {
    private static final String START_TIME_ATTR = "ApiTimingInterceptor.startTime";
    private final PerformanceMonitor performanceMonitor;
    private final List<Long> recentLatencies = new CopyOnWriteArrayList<>();

    public ApiTimingInterceptor(PerformanceMonitor performanceMonitor) {
        this.performanceMonitor = performanceMonitor;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            recentLatencies.add(duration);

            if (recentLatencies.size() >= 100) {
                List<Long> latenciesToProcess;
                synchronized (this) {
                    latenciesToProcess = new ArrayList<>(recentLatencies);
                    recentLatencies.clear();
                }
                performanceMonitor.analyzeLatency(latenciesToProcess);
            }
        }
    }
}
