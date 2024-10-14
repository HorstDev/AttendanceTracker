package org.astu.attendancetracker.core.application.schedule;

import org.astu.attendancetracker.core.domain.TeacherProfile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ScheduleManager {
    CompletableFuture<List<TeacherProfile>> getAllTeacherProfiles();
    GroupScheduleBuilder groupScheduleBuilder();
}
