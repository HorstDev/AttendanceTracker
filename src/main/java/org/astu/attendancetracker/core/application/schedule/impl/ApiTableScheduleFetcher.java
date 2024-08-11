package org.astu.attendancetracker.core.application.schedule.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableGlobalData;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableGroupSchedule;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableTimetableItem;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableTimetable;
import org.astu.attendancetracker.core.application.schedule.ScheduleFetcher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ApiTableScheduleFetcher implements ScheduleFetcher {
    private final HttpClient httpClient;

    public ApiTableScheduleFetcher(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public CompletableFuture<ApiTableGroupSchedule> getGroupSchedule(String groupName) {
        return CompletableFuture.supplyAsync(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://apitable.astu.org/search/get?q=ДИПРБ_41/1&t=group"))
                .GET()
                .build();

            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                return new ObjectMapper().readValue(response.body(), ApiTableGroupSchedule.class);
            } catch(IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Integer> getCurrentWeekNumber() {
        return CompletableFuture.supplyAsync(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://apitable.astu.org/download?api-version=2.0"))
                    .GET()
                    .build();

            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                ApiTableGlobalData globalData = new ObjectMapper().readValue(response.body(), ApiTableGlobalData.class);
                return globalData.config().weekOverride();
            } catch(IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private CompletableFuture<ApiTableTimetable> getAllTimetableItems() {
        return CompletableFuture.supplyAsync(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://apitable.astu.org/download?api-version=2.0"))
                    .GET()
                    .build();

            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                return new ObjectMapper().readValue(response.body(), ApiTableTimetable.class);
            } catch(IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<List<ApiTableTimetableItem>> getAllTeachers() {
        return getAllTimetableItems().thenApplyAsync(timetableResponse ->
                timetableResponse.timetable().stream().filter(item -> item.type().equals("teacher")).toList());
    }
}
