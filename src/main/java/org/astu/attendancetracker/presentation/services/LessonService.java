package org.astu.attendancetracker.presentation.services;

import org.astu.attendancetracker.core.domain.Lesson;

import java.util.UUID;

public interface LessonService {
    Lesson findLessonById(UUID lessonId);
    void startLesson(UUID lessonId);
}
