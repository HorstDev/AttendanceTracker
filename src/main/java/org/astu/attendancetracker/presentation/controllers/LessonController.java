package org.astu.attendancetracker.presentation.controllers;

import org.astu.attendancetracker.core.application.auth.CustomUserDetails;
import org.astu.attendancetracker.core.domain.Lesson;
import org.astu.attendancetracker.presentation.services.LessonService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
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

    @GetMapping("lessons-in-day")
    public UUID getLessonsInDay() {
        Object userDetailsObj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userDetailsObj instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        return null;
    }
}
