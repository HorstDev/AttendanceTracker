package org.astu.attendancetracker.core.application.schedule.impl;

import org.astu.attendancetracker.core.application.schedule.GroupScheduleBuilder;
import org.astu.attendancetracker.core.application.schedule.ScheduleFetcher;
import org.astu.attendancetracker.core.application.schedule.ScheduleManager;
import org.astu.attendancetracker.core.domain.TeacherProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ScheduleManagerImpl implements ScheduleManager {
    private final ScheduleFetcher scheduleFetcher;

    public ScheduleManagerImpl(ScheduleFetcher scheduleFetcher) {
        this.scheduleFetcher = scheduleFetcher;
    }

    public CompletableFuture<List<TeacherProfile>> getAllTeacherProfiles() {
        List<TeacherProfile> teacherProfiles = new ArrayList<>();

        return scheduleFetcher.getAllTeachers().thenApply(teachersApiTable -> {
            teachersApiTable.forEach(teacherApiTable -> {
                TeacherProfile profile = new TeacherProfile();
                profile.setApiTableId(teacherApiTable.id());
                profile.setName(teacherApiTable.name());
                teacherProfiles.add(profile);
            });
            return teacherProfiles;
        });
    }

    public GroupScheduleBuilder groupScheduleBuilder() {
        return new GroupScheduleBuilderImpl();
    }
}
