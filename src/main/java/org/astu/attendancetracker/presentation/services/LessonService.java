package org.astu.attendancetracker.presentation.services;

import org.astu.attendancetracker.core.domain.Lesson;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface LessonService {
    Lesson findLessonById(UUID lessonId);
    void startLesson(UUID lessonId);
    List<Lesson> findLessonsByDayForTeacher(UUID teacherId, LocalDate date);
}
