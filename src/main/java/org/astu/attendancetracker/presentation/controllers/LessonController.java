package org.astu.attendancetracker.presentation.controllers;

import org.astu.attendancetracker.presentation.services.LessonService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lesson")
public class LessonController {
    private final LessonService lessonService;

    public LessonController(final LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @PostMapping("start-lesson")
    public void startLesson(@RequestParam UUID lessonId) {
        lessonService.startLesson(lessonId);
    }
}
