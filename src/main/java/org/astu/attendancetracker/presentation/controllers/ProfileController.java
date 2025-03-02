package org.astu.attendancetracker.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.astu.attendancetracker.core.application.common.viewModels.profile.UpdateProfileVm;
import org.astu.attendancetracker.core.domain.Group;
import org.astu.attendancetracker.core.domain.Profile;
import org.astu.attendancetracker.core.domain.StudentProfile;
import org.astu.attendancetracker.presentation.services.GroupService;
import org.astu.attendancetracker.presentation.services.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {
    private final ProfileService profileService;
    private final GroupService groupService;

    public ProfileController(ProfileService profileService, GroupService groupService) {
        this.profileService = profileService;
        this.groupService = groupService;
    }

    @Operation(summary = "Загружает всех преподавателей АГТУ в базу данных")
    @ApiResponse(responseCode = "200", description = "Преподаватели успешно добавлены")
    @PostMapping("upload-all-teachers")
    public CompletableFuture<Void> uploadAllTeachers() {
        return profileService.uploadAllTeachersFromApiTable();
    }

    @Operation(summary = "Добавляет студента в группу")
    @ApiResponse(responseCode = "200", description = "Студент успешно добавлен в группу")
    @PostMapping("add-student")
    public ResponseEntity<StudentProfile> addStudent(@RequestParam UUID groupId, @RequestParam String studentName) {
        Group group = groupService.findGroupById(groupId);
        StudentProfile savedStudent = profileService.addStudentToGroup(group, studentName);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStudent);
    }

    //todo потом переделать, а то без viewModels кривовато
    @Operation(summary = "Обновление профиля")
    @PutMapping("/{id}")
    public ResponseEntity<Profile> updateProfile(@PathVariable UUID id, @RequestBody UpdateProfileVm updateProfileVm) {
        Profile updatedProfile = profileService.updateProfile(id, updateProfileVm);
        return ResponseEntity.status(HttpStatus.OK).body(updatedProfile);
    }
}
