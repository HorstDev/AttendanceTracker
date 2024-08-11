package org.astu.attendancetracker.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.transaction.Transactional;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableGroupSchedule;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableTimetableItem;
import org.astu.attendancetracker.core.application.schedule.ScheduleFetcher;
import org.astu.attendancetracker.core.domain.TeacherProfile;
import org.astu.attendancetracker.persistence.repositories.impl.TeacherRepositoryImpl;
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
    private final TeacherRepositoryImpl repository;

    public TestSwaggerController(ScheduleFetcher scheduleFetcher, TeacherRepositoryImpl repository) {
        this.scheduleFetcher = scheduleFetcher;
        this.repository = repository;
    }

    @Operation(summary = "Возвращает рандомное число в определенном диапазоне")
    @ApiResponse(responseCode = "200", description = "Рандомное число успешно вернулось")
    @GetMapping("random-in-range")
    @Transactional
    public CompletableFuture<List<ApiTableTimetableItem>> returnIntInRange() {
        TeacherProfile profile = new TeacherProfile();
        profile.setApiTableId("авыва");
        repository.addTeacher(profile);
        List<TeacherProfile> profiles = repository.getAllTeachers();


        CompletableFuture<ApiTableGroupSchedule> schedule = scheduleFetcher.getGroupSchedule("авыа");
        CompletableFuture<List<ApiTableTimetableItem>> teachers = scheduleFetcher.getAllTeachers();

        return teachers;

//        return CompletableFuture.supplyAsync(() -> {
//            return 1;
//        });
    }

    @Operation(summary = "Возвращает рандомное число")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Рандомное число успешно вернулось"),
            @ApiResponse(responseCode = "404", description = "Рандомное число не найдено")
    })
    @GetMapping("random")
    public int returnInt() {
        Random rand = new Random();
        return rand.nextInt();
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
