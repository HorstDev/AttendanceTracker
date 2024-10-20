package org.astu.attendancetracker.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.astu.attendancetracker.presentation.services.ProfileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Operation(summary = "Загружает всех преподавателей АГТУ в базу данных")
    @ApiResponse(responseCode = "200", description = "Преподаватели успешно добавлены")
    @PostMapping("upload-all-teachers")
    public CompletableFuture<Void> uploadAllTeachers() {
        return profileService.uploadAllTeachersFromApiTable();
    }
}
