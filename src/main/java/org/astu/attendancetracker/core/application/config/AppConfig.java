package org.astu.attendancetracker.core.application.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;

@Component
@EnableCaching
@EnableScheduling
public class AppConfig {

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
    }
}
