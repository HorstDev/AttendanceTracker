package org.astu.attendancetracker.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.astu.attendancetracker.presentation.services.AuthService;
import org.astu.attendancetracker.presentation.services.LessonService;
import org.astu.attendancetracker.presentation.viewModels.LessonUserStatusViewModel;
import org.astu.attendancetracker.presentation.viewModels.LessonUserStatusesDataViewModel;
import org.astu.attendancetracker.presentation.viewModels.LessonViewModel;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lesson")
public class LessonController {
    private final LessonService lessonService;
    private final AuthService authService;

    public LessonController(final LessonService lessonService, AuthService authService) {
        this.lessonService = lessonService;
        this.authService = authService;
    }

    @PutMapping("start-lessons")
    public List<LessonViewModel> startLessons(@RequestBody List<UUID> lessonsId) {
        return lessonService.startLessons(lessonsId);
    }

    @PutMapping("stop-lessons")
    public List<LessonViewModel> stopLessons(@RequestBody List<UUID> lessonsId) {
        return lessonService.stopLessons(lessonsId);
    }

    @PutMapping("update-lesson-statuses")
    public List<LessonUserStatusViewModel> updateLessonStatuses(@RequestBody List<LessonUserStatusViewModel> statuses) {
        return lessonService.updateLessonStatuses(statuses);
    }

    @Operation(description = "Доступ: студенты", summary = "Возвращает статус студента для текущего активного занятия (для генерации QR)")
    @GetMapping("active-lesson-status")
    public LessonUserStatusViewModel getActiveLessonStatus() {
        UUID userId = authService.getCurrentUserId();
        return lessonService.getActiveLessonStatusForStudent(userId);
    }

    @Operation(description = "Доступ: преподаватели", summary = "Отмечает студента как присутствующего по QR-коду")
    @PutMapping("check-lesson-status-visited/{statusId}")
    public LessonUserStatusViewModel checkLessonStatusVisited(@PathVariable UUID statusId) {
        return lessonService.markStatusVisited(statusId);
    }

    @PostMapping("start-lesson")
    public void startLesson(@RequestParam UUID lessonId) {
        lessonService.startLesson(lessonId);
    }

    @Operation(description = "Доступ: преподаватели", summary = "Возвращает занятия преподавателя в определенный день")
    @GetMapping("lessons-in-day")
    public List<LessonViewModel> getLessonsInDay(@RequestParam LocalDate date) {
        UUID userId = authService.getCurrentUserId();
        return lessonService.findLessonsByDayForTeacher(userId, date);
    }

    @Operation(description = "Доступ: преподаватели", summary = "Возвращает занятия преподавателя, идущие в текущий момент")
    @GetMapping("current-lessons")
    public List<LessonViewModel> getCurrentLessons() {
        UUID userId = authService.getCurrentUserId();
        return lessonService.findCurrentLessonsForTeacher(userId);
    }

    @Operation(description = "Доступ: преподаватели", summary = "Возвращает статусы студентов для занятий, проводимых прямо сейчас")
    @GetMapping("lessons-in-progress-user-statuses")
    public List<LessonUserStatusesDataViewModel> getLessonUserStatusesInProgress() {
        UUID userId = authService.getCurrentUserId();
        return lessonService.getLessonUserStatusesInProgress(userId);
    }
}
