package org.astu.attendancetracker.presentation.services;

import org.astu.attendancetracker.core.domain.Lesson;
import org.astu.attendancetracker.presentation.viewModels.LessonUserStatusViewModel;
import org.astu.attendancetracker.presentation.viewModels.LessonUserStatusesDataViewModel;
import org.astu.attendancetracker.presentation.viewModels.LessonViewModel;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LessonService {
    Lesson findLessonById(UUID lessonId);
    LessonViewModel startLesson(UUID lessonId);
    List<LessonViewModel> startLessons(List<UUID> lessonsId);
    List<LessonViewModel> findLessonsByDayForTeacher(UUID teacherId, LocalDate date);
    List<LessonViewModel> findCurrentLessonsForTeacher(UUID userId);
    List<LessonUserStatusesDataViewModel> getLessonUserStatusesInProgress(UUID userId);
    List<LessonViewModel> stopLessons(List<UUID> lessonsId);
    List<LessonUserStatusViewModel> updateLessonStatuses(List<LessonUserStatusViewModel> statuses);
    LessonUserStatusViewModel getActiveLessonStatusForStudent(UUID userId);
    LessonUserStatusViewModel markStatusVisited(UUID statusId);
}
