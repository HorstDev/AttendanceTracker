package org.astu.attendancetracker.presentation.services.impl;

import org.astu.attendancetracker.core.domain.*;
import org.astu.attendancetracker.persistence.repositories.LessonOutcomeRepository;
import org.astu.attendancetracker.persistence.repositories.LessonRepository;
import org.astu.attendancetracker.presentation.services.LessonService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LessonServiceImpl implements LessonService {
    private final LessonRepository lessonRepository;
    private final LessonOutcomeRepository lessonOutcomeRepository;

    public LessonServiceImpl(LessonRepository lessonRepository, LessonOutcomeRepository lessonOutcomeRepository) {
        this.lessonRepository = lessonRepository;
        this.lessonOutcomeRepository = lessonOutcomeRepository;
    }

    @Override
    public Lesson findLessonById(UUID lessonId) {
        return lessonRepository.findById(lessonId).orElseThrow(() ->
                new IllegalArgumentException("Занятие с id " + lessonId + " не найдено"));
    }

    // Старт занятия
    @Override
    public void startLesson(UUID lessonId) {
        Lesson lesson = findLessonById(lessonId);
        // Обновляем статус занятия
        lesson.start();

        // Находим студентов, участвующих в данном занятии
        List<StudentProfile> studentProfiles = lessonRepository.findStudentsParticipatingInLesson(lessonId);
        // Для каждого студента добавляем исход занятия
        List<LessonOutcome> lessonOutcomes = new ArrayList<>();
        for (StudentProfile studentProfile : studentProfiles) {
            lessonOutcomes.add(LessonOutcome
                    .builder()
                            .lesson(lesson)
                            .studentProfile(studentProfile)
                            .isVisited(false)
                    .build());
        }
        lessonOutcomeRepository.saveAll(lessonOutcomes);
    }
}
