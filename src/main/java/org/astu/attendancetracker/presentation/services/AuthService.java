package org.astu.attendancetracker.presentation.services;

import org.astu.attendancetracker.core.application.auth.AuthenticationResponse;
import org.astu.attendancetracker.core.application.common.viewModels.auth.AuthenticationRequest;
import org.astu.attendancetracker.core.application.common.viewModels.auth.RegisterRequest;
import org.astu.attendancetracker.core.domain.*;
import org.astu.attendancetracker.presentation.viewModels.AuthorizationDto;

import java.util.UUID;

public interface AuthService {
    AuthenticationResponse register(RegisterRequest request);
    User getUserForProfile(Profile profile);
    AuthenticationResponse authenticate(AuthenticationRequest request);
    TeacherProfile createTeacherProfile(String name, String apiTableId);
    StudentProfile createStudentProfile(String name, Group group);
    UUID getCurrentUserId();
    void changeAuthorizationData(UUID profileId, AuthorizationDto authorizationDto);
}
