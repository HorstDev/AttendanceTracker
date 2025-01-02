package org.astu.attendancetracker.core.application.schedule.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableGlobalData;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableGroupSchedule;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableTimetableItem;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableTimetable;
import org.astu.attendancetracker.core.application.schedule.ScheduleManager;
import org.astu.attendancetracker.core.domain.TeacherProfile;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ApiTableScheduleManager implements ScheduleManager {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiTableScheduleManager(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public CompletableFuture<ApiTableGroupSchedule> getGroupSchedule(String groupName) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://apitable.astu.org/search/get?q=" + groupName + "&t=group"))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.body(), ApiTableGroupSchedule.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public CompletableFuture<Integer> getCurrentWeekNumber() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://apitable.astu.org/download?api-version=2.0"))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        ApiTableGlobalData globalData = objectMapper.readValue(response.body(), ApiTableGlobalData.class);
                        return globalData.config().weekOverride();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private CompletableFuture<ApiTableTimetable> getAllTimetableItems() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://apitable.astu.org/download?api-version=2.0"))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.body(), ApiTableTimetable.class);
                    } catch(JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public CompletableFuture<List<ApiTableTimetableItem>> getAllApiTableItemsLikeTeachers() {
        return getAllTimetableItems().thenApply(timetableResponse ->
                timetableResponse.timetable().stream().filter(item -> item.type().equals("teacher")).toList());
    }

    public CompletableFuture<List<TeacherProfile>> getAllTeacherProfiles() {
        List<TeacherProfile> teacherProfiles = new ArrayList<>();

        return getAllApiTableItemsLikeTeachers().thenApply(teachersApiTable -> {
            teachersApiTable.forEach(teacherApiTable -> {
                TeacherProfile profile = new TeacherProfile();
                profile.setApiTableId(teacherApiTable.id());
                profile.setName(teacherApiTable.name());
                teacherProfiles.add(profile);
            });
            return teacherProfiles;
        });
    }
}
