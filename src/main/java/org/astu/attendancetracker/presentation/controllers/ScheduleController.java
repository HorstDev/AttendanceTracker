package org.astu.attendancetracker.presentation.controllers;

import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableGroupSchedule;
import org.astu.attendancetracker.presentation.services.impl.ScheduleServiceImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/schedule")
public class ScheduleController {
    private final ScheduleServiceImpl scheduleService;

    public ScheduleController(ScheduleServiceImpl scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping("upload-group")
    public CompletableFuture<Void> uploadDataForGroup(@RequestParam String groupName, @RequestParam boolean isEvenSemester) {

        CompletableFuture<ApiTableGroupSchedule> scheduleFuture = scheduleService.getApiTableGroupSchedule(groupName);
        CompletableFuture<Integer> currentWeekFuture = scheduleService.getCurrentWeekNumber();

        return scheduleFuture.thenCombine(currentWeekFuture, (apiTableGroupSchedule, currentWeek) -> {
            scheduleService.uploadGroupScheduleData(apiTableGroupSchedule, currentWeek, isEvenSemester);
            return null;
        });
    }
}
