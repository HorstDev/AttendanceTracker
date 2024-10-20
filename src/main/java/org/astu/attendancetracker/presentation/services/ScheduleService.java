package org.astu.attendancetracker.presentation.services;

import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableGroupSchedule;
import org.astu.attendancetracker.core.application.schedule.GroupBuilder;

import java.util.concurrent.CompletableFuture;

public interface ScheduleService {
    CompletableFuture<ApiTableGroupSchedule> getApiTableGroupSchedule(String groupName);
    void uploadGroupScheduleData(ApiTableGroupSchedule apiTableGroupSchedule);
    GroupBuilder groupBuilder();
}
