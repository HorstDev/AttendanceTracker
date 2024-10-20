package org.astu.attendancetracker.presentation.controllers;

import org.astu.attendancetracker.presentation.services.impl.ScheduleServiceImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {
    private final ScheduleServiceImpl scheduleService;

    public ScheduleController(ScheduleServiceImpl scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping("upload-group")
    public CompletableFuture<Void> uploadDataForGroup() {
        String groupName = "ДИПРБ-41/1";
        return scheduleService.getApiTableGroupSchedule(groupName)
                .thenAccept(scheduleService::uploadGroupScheduleData);
    }
}
