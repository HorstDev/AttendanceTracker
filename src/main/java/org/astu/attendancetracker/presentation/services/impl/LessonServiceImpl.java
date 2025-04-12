package org.astu.attendancetracker.presentation.services.impl;

import org.astu.attendancetracker.core.domain.*;
import org.astu.attendancetracker.persistence.repositories.LessonOutcomeRepository;
import org.astu.attendancetracker.persistence.repositories.LessonRepository;
import org.astu.attendancetracker.persistence.repositories.ProfileRepository;
import org.astu.attendancetracker.presentation.services.LessonService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LessonServiceImpl implements LessonService {
    private final LessonRepository lessonRepository;
    private final LessonOutcomeRepository lessonOutcomeRepository;
    private final ProfileRepository profileRepository;

    public LessonServiceImpl(LessonRepository lessonRepository, LessonOutcomeRepository lessonOutcomeRepository, ProfileRepository profileRepository) {
        this.lessonRepository = lessonRepository;
        this.lessonOutcomeRepository = lessonOutcomeRepository;
        this.profileRepository = profileRepository;
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

    // todo Тут добавить ViewModel, преобразовать в нее
    public List<Lesson> findLessonsByDayForTeacher(UUID userId, LocalDate date) {
        // Проблема в том, что я передаю userId, а предполагается profileId
        TeacherProfile profile = profileRepository.findTeacherProfileByUserId(userId).orElseThrow(() ->
                new NullPointerException("Профиль преподавателя для пользователя с id = " + userId + " не найден"));
        List<Lesson> lessons = lessonRepository.findLessonsByDateAndTeacher(profile.getId(), date);
//        String disciplineName = lessons.getFirst().getDiscipline().getName();
        return lessons;
    }
}
