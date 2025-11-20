package org.astu.attendancetracker.presentation.services.impl;

import jakarta.transaction.Transactional;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableTimetableItem;
import org.astu.attendancetracker.core.application.common.viewModels.profile.UpdateProfileVm;
import org.astu.attendancetracker.core.application.schedule.ScheduleManager;
import org.astu.attendancetracker.core.domain.*;
import org.astu.attendancetracker.persistence.repositories.GroupRepository;
import org.astu.attendancetracker.persistence.repositories.LessonOutcomeRepository;
import org.astu.attendancetracker.persistence.repositories.LessonRepository;
import org.astu.attendancetracker.persistence.repositories.ProfileRepository;
import org.astu.attendancetracker.presentation.mappers.StudentProfileMapper;
import org.astu.attendancetracker.presentation.services.AuthService;
import org.astu.attendancetracker.presentation.services.GroupService;
import org.astu.attendancetracker.presentation.services.ProfileService;
import org.astu.attendancetracker.presentation.viewModels.StudentProfileDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ProfileServiceImpl implements ProfileService {
    private final ScheduleManager scheduleFetcher;
    private final ProfileRepository profileRepository;
    private final AuthService authService;
    private final LessonRepository lessonRepository;
    private final GroupService groupService;
    private final StudentProfileMapper studentProfileMapper;

    public ProfileServiceImpl(
            ScheduleManager scheduleFetcher, ProfileRepository teacherRepository,
            AuthService authService, LessonRepository lessonRepository, GroupService groupService,
            StudentProfileMapper studentProfileMapper) {
        this.scheduleFetcher = scheduleFetcher;
        this.profileRepository = teacherRepository;
        this.authService = authService;
        this.lessonRepository = lessonRepository;
        this.groupService = groupService;
        this.studentProfileMapper = studentProfileMapper;
    }

    @Transactional
    public CompletableFuture<Void> uploadAllTeachersFromApiTable() {
        // Преподаватели из ApiTable АГТУ
        CompletableFuture<List<ApiTableTimetableItem>> teachersFuture = scheduleFetcher.getAllApiTableItemsLikeTeachers();
        // Преподаватели из базы данных (уже ранее загруженные)
        List<TeacherProfile> databaseTeacherProfiles = profileRepository.findAllTeacherProfiles();

        return teachersFuture.thenAccept(teachersApiTable -> {
           teachersApiTable.forEach(teacherApiTable -> {

               // Извлекаем преподавателя из БД (такого же, как в ApiTable)
               Optional<TeacherProfile> profileInDb = databaseTeacherProfiles.stream()
                       .filter(x -> x.getApiTableId().equals(teacherApiTable.id()))
                       .findFirst();

               // Если он есть, обновляем его имя, если нет, добавляем нового преподавателя в БД
               if (profileInDb.isPresent()) {
                   profileInDb.get().setName(teacherApiTable.name());
               } else {
                   TeacherProfile newTeacher = authService.createTeacherProfile(teacherApiTable.name(), teacherApiTable.id());
                   databaseTeacherProfiles.add(newTeacher);
               }
           });

           profileRepository.saveAll(databaseTeacherProfiles);
        });
    }

    public List<TeacherProfile> getTeachersWithPartOfName(String partOfName) {
        return profileRepository.findAllTeacherProfilesByPartOfName(partOfName)
                .orElseThrow(() -> new NullPointerException("Преподавателей не найдено"));
    }

    // Возвращает всех преподавателей в базе данных
    public List<TeacherProfile> getAllTeachers() {
        return profileRepository.findAllTeacherProfiles();
    }

    // Добавление студента в группу
    public StudentProfile addStudentToGroup(UUID groupId, String studentName) {
        Group group = groupService.findGroupById(groupId);
        StudentProfile studentProfile = authService.createStudentProfile(studentName, group);
        // Для каждого занятия, которое уже было начато в группе, в которую добавляется студент, добавляем статус занятия
        // (отмечаем, что его не было на этих занятиях)
        List<Lesson> startedLessons = lessonRepository.findStartedLessonsInGroup(group.getId());
        for (Lesson startedLesson : startedLessons) {
            var lessonOutcome = LessonOutcome
                    .builder()
                    .lesson(startedLesson)
                    .studentProfile(studentProfile)
                    .isVisited(false)
                    .build();
            studentProfile.getLessonOutcomes().add(lessonOutcome);
        }

        return profileRepository.save(studentProfile);
    }

    public Profile getProfileById(UUID id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Не найдено профиля с id = " + id));
    }

    public Profile updateProfile(UUID id, UpdateProfileVm updateProfileVm) {
        Profile profileToUpdate = getProfileById(id);
        profileToUpdate.setName(updateProfileVm.getName());
        profileToUpdate.setEmail(updateProfileVm.getEmail());
        return profileRepository.save(profileToUpdate);
    }

    public List<StudentProfileDto> getStudentProfilesByGroupId(UUID groupId) {
        List<StudentProfile> studentProfiles = profileRepository.findStudentProfilesByGroupId(groupId);
        return studentProfileMapper.toDto(studentProfiles);
    }
}
