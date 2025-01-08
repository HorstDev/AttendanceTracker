package org.astu.attendancetracker.presentation.controllers;

import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableGroupSchedule;
import org.astu.attendancetracker.core.domain.Group;
import org.astu.attendancetracker.presentation.services.ScheduleService;
import org.astu.attendancetracker.presentation.services.impl.ScheduleServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/schedule")
public class ScheduleController {
    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleServiceImpl scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping("save-group")
    public ResponseEntity<Group> saveGroup(@RequestParam String groupName) {
        var savedGroup = scheduleService.saveGroup(groupName);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedGroup);
    }

    @PostMapping("upload-group")
    public CompletableFuture<Void> uploadDataForGroup(@RequestParam UUID groupId, @RequestParam boolean isEvenSemester) {

        Group group = scheduleService.findGroupById(groupId);
        int currentSemester = group.currentSemester(isEvenSemester);
        CompletableFuture<ApiTableGroupSchedule> scheduleFuture = scheduleService.getApiTableGroupSchedule(group.getName());
        CompletableFuture<Integer> currentWeekFuture = scheduleService.getCurrentWeekNumber();

        return scheduleFuture.thenCombine(currentWeekFuture, (apiTableGroupSchedule, currentWeek) -> {
            scheduleService.uploadGroupScheduleData(group, apiTableGroupSchedule, currentWeek, currentSemester);
            return null;
        });
    }
}
