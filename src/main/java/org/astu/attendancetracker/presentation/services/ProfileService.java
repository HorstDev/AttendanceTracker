package org.astu.attendancetracker.presentation.services;

import org.astu.attendancetracker.core.application.common.viewModels.profile.UpdateProfileVm;
import org.astu.attendancetracker.core.domain.Group;
import org.astu.attendancetracker.core.domain.Profile;
import org.astu.attendancetracker.core.domain.StudentProfile;
import org.astu.attendancetracker.core.domain.TeacherProfile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ProfileService {
    CompletableFuture<Void> uploadAllTeachersFromApiTable();
    List<TeacherProfile> getAllTeachers();
    List<TeacherProfile> getTeachersWithPartOfName(String partOfName);
    StudentProfile addStudentToGroup(Group group, String studentName);
    Profile getProfileById(UUID id);
    Profile updateProfile(UUID id, UpdateProfileVm updateProfileVm);
}
