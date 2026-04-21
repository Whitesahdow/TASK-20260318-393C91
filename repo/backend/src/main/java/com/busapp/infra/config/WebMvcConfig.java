package com.busapp.infra.config;

import com.busapp.infra.monitoring.ApiTimingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final ApiTimingInterceptor apiTimingInterceptor;

    public WebMvcConfig(ApiTimingInterceptor apiTimingInterceptor) {
        this.apiTimingInterceptor = apiTimingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiTimingInterceptor).addPathPatterns("/api/**");
    }
}
