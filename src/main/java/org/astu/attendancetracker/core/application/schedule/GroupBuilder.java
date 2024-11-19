package org.astu.attendancetracker.core.application.schedule;

import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableGroupSchedule;
import org.astu.attendancetracker.core.domain.Group;
import org.astu.attendancetracker.core.domain.TeacherProfile;

import java.util.Set;

public interface GroupBuilder {
    GroupBuilder setGroupSchedule(ApiTableGroupSchedule schedule);
    GroupBuilder setGroup();
    GroupBuilder setDisciplines(int currentSemester);
    GroupBuilder setTeachersForDisciplines(Set<TeacherProfile> teacherProfilesInGroup);
    GroupBuilder setLessons(int currentWeekNumber);

    Group build();
}
