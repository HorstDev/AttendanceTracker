package org.astu.attendancetracker.presentation.services.impl;

import jakarta.transaction.Transactional;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableTimetableItem;
import org.astu.attendancetracker.core.application.schedule.ScheduleFetcher;
import org.astu.attendancetracker.core.domain.TeacherProfile;
import org.astu.attendancetracker.persistence.repositories.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ProfileService {
    private final ScheduleFetcher scheduleFetcher;
    private final ProfileRepository profileRepository;

    public ProfileService(ScheduleFetcher scheduleFetcher, ProfileRepository teacherRepository) {
        this.scheduleFetcher = scheduleFetcher;
        this.profileRepository = teacherRepository;
    }

    @Transactional
    public CompletableFuture<Void> uploadAllTeachersFromApiTable() {
        CompletableFuture<List<ApiTableTimetableItem>> teachersFuture = scheduleFetcher.getAllTeachers();
        return teachersFuture.thenAccept(teachersApiTable -> {
           var teacherProfiles = new ArrayList<TeacherProfile>();
           teachersApiTable.forEach(teacherApiTable -> {
              var teacherProfile = new TeacherProfile();
              teacherProfile.setApiTableId(teacherApiTable.id());
              teacherProfile.setName(teacherApiTable.name());
              teacherProfiles.add(teacherProfile);
           });
            profileRepository.saveAll(teacherProfiles);
        });
    }

    public List<TeacherProfile> getAllTeachers() {
        return profileRepository.findAllTeacherProfiles();
    }
}
