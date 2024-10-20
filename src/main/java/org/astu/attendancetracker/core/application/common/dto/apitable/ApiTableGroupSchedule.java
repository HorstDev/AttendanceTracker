package org.astu.attendancetracker.core.application.common.dto.apitable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.astu.attendancetracker.core.domain.TeacherProfile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiTableGroupSchedule(String name, List<ApiTableLesson> lessons) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiTableLesson(List<ApiTableEntry> entries, int dayId, int lessonOrderId) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record ApiTableEntry(String teacher, String discipline, String type, String audience) {

        }
    }

    public Set<String> getTeacherNames() {
        Set<String> teacherNames = new HashSet<>();
        lessons().forEach(lesson -> lesson.entries().forEach(entry -> teacherNames.add(entry.teacher())));
        return teacherNames;
    }
}
