package org.astu.attendancetracker.presentation.services;

import org.astu.attendancetracker.core.application.auth.AuthenticationResponse;
import org.astu.attendancetracker.core.application.common.viewModels.auth.AuthenticationRequest;
import org.astu.attendancetracker.core.application.common.viewModels.auth.RegisterRequest;
import org.astu.attendancetracker.core.domain.Profile;
import org.astu.attendancetracker.core.domain.User;

public interface AuthService {
    AuthenticationResponse register(RegisterRequest request);
    User getUserForProfile(Profile profile);
    AuthenticationResponse authenticate(AuthenticationRequest request);
}
