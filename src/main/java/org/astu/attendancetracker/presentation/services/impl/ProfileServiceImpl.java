package org.astu.attendancetracker.presentation.services.impl;

import jakarta.transaction.Transactional;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableTimetableItem;
import org.astu.attendancetracker.core.application.schedule.ScheduleManager;
import org.astu.attendancetracker.core.domain.Group;
import org.astu.attendancetracker.core.domain.Profile;
import org.astu.attendancetracker.core.domain.StudentProfile;
import org.astu.attendancetracker.core.domain.TeacherProfile;
import org.astu.attendancetracker.persistence.repositories.GroupRepository;
import org.astu.attendancetracker.persistence.repositories.ProfileRepository;
import org.astu.attendancetracker.presentation.services.AuthService;
import org.astu.attendancetracker.presentation.services.ProfileService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ProfileServiceImpl implements ProfileService {
    private final ScheduleManager scheduleFetcher;
    private final ProfileRepository profileRepository;
    private final AuthService authService;

    public ProfileServiceImpl(
            ScheduleManager scheduleFetcher, ProfileRepository teacherRepository,
            AuthService authService) {
        this.scheduleFetcher = scheduleFetcher;
        this.profileRepository = teacherRepository;
        this.authService = authService;
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
                   var newTeacher = new TeacherProfile();
                   newTeacher.setName(teacherApiTable.name());
                   newTeacher.setApiTableId(teacherApiTable.id());
                   newTeacher.setUser(authService.getUserForProfile(newTeacher));
                   databaseTeacherProfiles.add(newTeacher);
               }
           });

           profileRepository.saveAll(databaseTeacherProfiles);
        });
    }

    // Возвращает всех студентов в базе данных
    public List<TeacherProfile> getAllTeachers() {
        return profileRepository.findAllTeacherProfiles();
    }

    // Добавление студента в группу
    public StudentProfile addStudentToGroup(Group group, String studentName) {
        StudentProfile studentProfile = new StudentProfile(group, studentName);
        studentProfile.setUser(authService.getUserForProfile(studentProfile));
        return profileRepository.save(studentProfile);
    }
}
