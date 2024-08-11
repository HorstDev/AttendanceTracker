package org.astu.attendancetracker.core.application.schedule;

import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableGroupSchedule;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableTimetableItem;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ScheduleFetcher {
    CompletableFuture<ApiTableGroupSchedule> getGroupSchedule(String groupName);
    CompletableFuture<Integer> getCurrentWeekNumber();
    CompletableFuture<List<ApiTableTimetableItem>> getAllTeachers();
}
