package org.astu.attendancetracker.presentation.services.impl;

import org.astu.attendancetracker.core.domain.*;
import org.astu.attendancetracker.persistence.repositories.LessonOutcomeRepository;
import org.astu.attendancetracker.persistence.repositories.LessonRepository;
import org.astu.attendancetracker.persistence.repositories.ProfileRepository;
import org.astu.attendancetracker.presentation.mappers.LessonMapper;
import org.astu.attendancetracker.presentation.services.LessonService;
import org.astu.attendancetracker.presentation.viewModels.LessonUserStatusViewModel;
import org.astu.attendancetracker.presentation.viewModels.LessonUserStatusWithUserViewModel;
import org.astu.attendancetracker.presentation.viewModels.LessonUserStatusesDataViewModel;
import org.astu.attendancetracker.presentation.viewModels.LessonViewModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LessonServiceImpl implements LessonService {
    private final LessonRepository lessonRepository;
    private final LessonOutcomeRepository lessonOutcomeRepository;
    private final ProfileRepository profileRepository;
    private final LessonMapper lessonMapper;

    public LessonServiceImpl(LessonRepository lessonRepository, LessonOutcomeRepository lessonOutcomeRepository,
                             ProfileRepository profileRepository, LessonMapper lessonMapper) {
        this.lessonRepository = lessonRepository;
        this.lessonOutcomeRepository = lessonOutcomeRepository;
        this.profileRepository = profileRepository;
        this.lessonMapper = lessonMapper;
    }

    @Override
    public Lesson findLessonById(UUID lessonId) {
        return lessonRepository.findById(lessonId).orElseThrow(() ->
                new IllegalArgumentException("Занятие с id " + lessonId + " не найдено"));
    }

    @Override
    @Transactional
    public List<LessonViewModel> startLessons(List<UUID> lessonId) {
        List<LessonViewModel> lessonViewModels = new ArrayList<>();
        lessonId.forEach(lesson ->{
            lessonViewModels.add(startLesson(lesson));
        });
        return lessonViewModels;
    }

    @Override
    @Transactional
    public LessonViewModel startLesson(UUID lessonId) {
        Lesson lesson = findLessonById(lessonId);
        lesson.start();
        lessonRepository.save(lesson);

        List<StudentProfile> studentProfiles = lessonRepository.findStudentsParticipatingInLesson(lessonId);
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
        return lessonMapper.toViewModel(lesson);
    }

    // todo Тут добавить ViewModel, преобразовать в нее
    @Transactional(readOnly = true)
    public List<LessonViewModel> findLessonsByDayForTeacher(UUID userId, LocalDate date) {
        // Проблема в том, что я передаю userId, а предполагается profileId
        TeacherProfile profile = profileRepository.findTeacherProfileByUserId(userId).orElseThrow(() ->
                new NullPointerException("Профиль преподавателя для пользователя с id = " + userId + " не найден"));
        List<Lesson> lessons = lessonRepository.findLessonsByDateAndTeacher(profile.getId(), date);
//        String disciplineName = lessons.getFirst().getDiscipline().getName();
        return lessons.stream()
                .map(lessonMapper::toViewModel)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LessonViewModel> findCurrentLessonsForTeacher(UUID userId) {
        TeacherProfile profile = profileRepository.findTeacherProfileByUserId(userId).orElseThrow(() ->
                new NullPointerException("Профиль преподавателя для пользователя с id = " + userId + " не найден"));
        List<Lesson> lessons = lessonRepository.findCurrentLessonsForTeacher(profile.getId());

        return lessons.stream()
                .map(lessonMapper::toViewModel)
                .toList();
    }

    @Override
    @Transactional
    public List<LessonUserStatusViewModel> updateLessonStatuses(List<LessonUserStatusViewModel> statuses) {
        List<UUID> ids = statuses.stream().map(LessonUserStatusViewModel::getId).toList();
        List<LessonOutcome> outcomes = lessonOutcomeRepository.findAllById(ids);

        Map<UUID, LessonUserStatusViewModel> statusById = new LinkedHashMap<>();
        for (LessonUserStatusViewModel s : statuses) {
            statusById.put(s.getId(), s);
        }

        for (LessonOutcome outcome : outcomes) {
            LessonUserStatusViewModel incoming = statusById.get(outcome.getId());
            if (incoming != null) {
                outcome.setVisited(incoming.isVisited());
            }
        }
        lessonOutcomeRepository.saveAll(outcomes);

        return outcomes.stream()
                .map(o -> new LessonUserStatusViewModel(o.getId(), o.isVisited(), null))
                .toList();
    }

    @Override
    @Transactional
    public List<LessonViewModel> stopLessons(List<UUID> lessonsId) {
        List<LessonViewModel> result = new ArrayList<>();
        for (UUID id : lessonsId) {
            Lesson lesson = findLessonById(id);
            lesson.stop();
            lessonRepository.save(lesson);
            result.add(lessonMapper.toViewModel(lesson));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public LessonUserStatusViewModel getActiveLessonStatusForStudent(UUID userId) {
        StudentProfile profile = profileRepository.findStudentProfileByUserId(userId).orElseThrow(() ->
                new IllegalArgumentException("Профиль студента для пользователя с id = " + userId + " не найден"));
        LessonOutcome outcome = lessonOutcomeRepository.findActiveOutcomeByStudentId(profile.getId())
                .orElseThrow(() -> new IllegalStateException("Активное занятие для студента не найдено"));
        return new LessonUserStatusViewModel(outcome.getId(), outcome.isVisited(), null);
    }

    @Override
    @Transactional
    public LessonUserStatusViewModel markStatusVisited(UUID statusId) {
        LessonOutcome outcome = lessonOutcomeRepository.findById(statusId)
                .orElseThrow(() -> new IllegalArgumentException("Статус с id = " + statusId + " не найден"));
        outcome.setVisited(true);
        lessonOutcomeRepository.save(outcome);
        return new LessonUserStatusViewModel(outcome.getId(), outcome.isVisited(), null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonUserStatusesDataViewModel> getLessonUserStatusesInProgress(UUID userId) {
        TeacherProfile profile = profileRepository.findTeacherProfileByUserId(userId).orElseThrow(() ->
                new NullPointerException("Профиль преподавателя для пользователя с id = " + userId + " не найден"));

        List<LessonOutcome> outcomes = lessonOutcomeRepository
                .findOutcomesForInProgressLessonsByTeacher(profile.getId());

        Map<UUID, LessonUserStatusesDataViewModel> groupedByLesson = new LinkedHashMap<>();
        for (LessonOutcome outcome : outcomes) {
            UUID lessonId = outcome.getLesson().getId();
            groupedByLesson.computeIfAbsent(lessonId, id -> new LessonUserStatusesDataViewModel(
                    lessonMapper.toViewModel(outcome.getLesson()),
                    new ArrayList<>()
            ));
            LessonUserStatusViewModel statusVm = new LessonUserStatusViewModel(
                    outcome.getId(),
                    outcome.isVisited(),
                    null
            );
            groupedByLesson.get(lessonId).getLessonUserStatusesWithUsers()
                    .add(new LessonUserStatusWithUserViewModel(outcome.getStudentProfile().getName(), statusVm));
        }
        return new ArrayList<>(groupedByLesson.values());
    }
}
