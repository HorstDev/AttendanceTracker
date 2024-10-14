package org.astu.attendancetracker.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.astu.attendancetracker.core.application.schedule.ScheduleFetcher;
import org.astu.attendancetracker.core.domain.TeacherProfile;
import org.astu.attendancetracker.persistence.repositories.ProfileRepository;
import org.astu.attendancetracker.presentation.services.impl.ProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/swagger-test")
public class TestSwaggerController {
    private final ScheduleFetcher scheduleFetcher;
    private final ProfileRepository repository;
    private final ProfileService profileService;

    public TestSwaggerController(ScheduleFetcher scheduleFetcher, ProfileRepository repository, ProfileService profileService) {
        this.scheduleFetcher = scheduleFetcher;
        this.repository = repository;
        this.profileService = profileService;
    }

    @Operation(summary = "Возвращает рандомное число в определенном диапазоне")
    @ApiResponse(responseCode = "200", description = "Рандомное число успешно вернулось")
    @GetMapping("random-in-range")
    public CompletableFuture<Void> returnIntInRange() {
        return profileService.uploadAllTeachersFromApiTable();
    }

    @Operation(summary = "Возвращает рандомное число")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Рандомное число успешно вернулось"),
            @ApiResponse(responseCode = "404", description = "Рандомное число не найдено")
    })
    @GetMapping("random")
    public List<TeacherProfile> returnInt() {
        Random rand = new Random();
        return profileService.getAllTeachers();
    }

    @GetMapping
    public String testHttp() {
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("https://apitable.astu.org/search/get?q=ДИПРБ_41/1&t=group"))
//                .GET()
//                .build();
//
//        try {
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//            return response.body();
//        } catch(IOException | InterruptedException e) {
//            return e.getMessage();
//        }
        return null;
    }
}
