package org.astu.attendancetracker.presentation.services;

import org.astu.attendancetracker.core.domain.TeacherProfile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ProfileService {
    CompletableFuture<Void> uploadAllTeachersFromApiTable();
    List<TeacherProfile> getAllTeachers();
}
