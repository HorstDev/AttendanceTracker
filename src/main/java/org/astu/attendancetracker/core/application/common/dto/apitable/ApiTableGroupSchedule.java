package org.astu.attendancetracker.core.application.common.dto.apitable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiTableGroupSchedule(String name, List<ApiTableLesson> lessons) {

    public Set<String> getTeacherNames() {
        Set<String> teacherNames = new HashSet<>();
        lessons().forEach(lesson -> lesson.entries().forEach(entry -> teacherNames.add(entry.teacher())));
        return teacherNames;
    }

    public HashMap<Integer, List<ApiTableLesson>> getLessonsByDays() {
        HashMap<Integer, List<ApiTableLesson>> lessonsByDays = new HashMap<>();

        // Инициализируем
        for (int i = 0; i < 12; i++)
            lessonsByDays.put(i, new ArrayList<>());

        // Распределяем занятия по dayId
        for (ApiTableLesson lesson : lessons())
            lessonsByDays.get(lesson.dayId()).add(lesson);

        return lessonsByDays;
    }
}
