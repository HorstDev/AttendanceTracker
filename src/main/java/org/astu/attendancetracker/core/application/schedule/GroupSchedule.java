package org.astu.attendancetracker.core.application.schedule;

import java.util.List;

public class GroupSchedule {
    private List<DisciplineTeacherRelationship> disciplineTeacherRelationships;

    static class DisciplineTeacherRelationship {
        private String discipline;
        private String teacher;
    }
}
