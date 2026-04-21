package com.busapp.infra;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class TraceIdFilter implements Filter {
    private static final String TRACE_ID_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRACE_ID_KEY, traceId);

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setHeader("X-Trace-ID", traceId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }
}
