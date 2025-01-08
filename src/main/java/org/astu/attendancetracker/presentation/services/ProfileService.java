package org.astu.attendancetracker.presentation.services;

import org.astu.attendancetracker.core.domain.Group;
import org.astu.attendancetracker.core.domain.StudentProfile;
import org.astu.attendancetracker.core.domain.TeacherProfile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ProfileService {
    CompletableFuture<Void> uploadAllTeachersFromApiTable();
    List<TeacherProfile> getAllTeachers();
    StudentProfile addStudentToGroup(Group group, String studentName);
}
