package org.astu.attendancetracker.core.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;

@Component
public class AppConfig {

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
    }
}
