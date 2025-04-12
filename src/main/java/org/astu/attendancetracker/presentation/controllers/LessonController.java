package org.astu.attendancetracker.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.astu.attendancetracker.core.domain.Lesson;
import org.astu.attendancetracker.presentation.services.AuthService;
import org.astu.attendancetracker.presentation.services.LessonService;
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

    @PostMapping("start-lesson")
    public void startLesson(@RequestParam UUID lessonId) {
        lessonService.startLesson(lessonId);
    }

    @Operation(description = "Доступ: преподаватели", summary = "Возвращает занятия преподавателя в определенный день")
    @GetMapping("lessons-in-day")
    public List<Lesson> getLessonsInDay(@RequestParam LocalDate date) {
        UUID userId = authService.getCurrentUserId();
        return lessonService.findLessonsByDayForTeacher(userId, date);
    }
}
