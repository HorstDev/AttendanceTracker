package org.astu.attendancetracker.presentation.controllers;

import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableGroupSchedule;
import org.astu.attendancetracker.core.domain.Group;
import org.astu.attendancetracker.presentation.services.GroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
