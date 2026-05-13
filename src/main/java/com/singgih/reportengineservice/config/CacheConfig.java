package com.singgih.reportengineservice.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    /** Cache name untuk hasil lookup ReportTemplate dari DB. */
    public static final String REPORT_TEMPLATES = "reportTemplates";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(REPORT_TEMPLATES);
    }
}
