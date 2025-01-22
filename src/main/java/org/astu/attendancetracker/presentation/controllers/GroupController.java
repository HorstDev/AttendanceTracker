package org.astu.attendancetracker.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.apache.coyote.BadRequestException;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableGroupSchedule;
import org.astu.attendancetracker.core.domain.Group;
import org.astu.attendancetracker.presentation.services.GroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/group")
public class GroupController {
    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("save-group")
    public ResponseEntity<Group> saveGroup(@RequestParam String groupName) {
        var savedGroup = groupService.saveGroup(groupName);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedGroup);
    }

    @PostMapping("upload-semester")
    public CompletableFuture<Void> uploadSemesterForGroup(@RequestParam UUID groupId, @RequestParam boolean isEvenSemester) {

        Group group = groupService.findGroupById(groupId);
        int currentSemester = group.currentSemester(isEvenSemester);
        CompletableFuture<ApiTableGroupSchedule> scheduleFuture = groupService.getApiTableGroupSchedule(group.getName());
        CompletableFuture<Integer> currentWeekFuture = groupService.getCurrentWeekNumber();

        return scheduleFuture.thenCombine(currentWeekFuture, (apiTableGroupSchedule, currentWeek) -> {
            groupService.uploadSemesterForGroup(group, apiTableGroupSchedule, currentWeek, currentSemester);
            return null;
        });
    }

    @PostMapping(path = "upload-curriculum", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Добавляет учебный план для группы")
    public ResponseEntity<Void> uploadCurriculumForGroup(@RequestParam UUID groupId, @RequestPart MultipartFile curriculumFile) {
        groupService.uploadCurriculumForGroup(groupId, curriculumFile);
        return ResponseEntity.ok().build();
    }
}
