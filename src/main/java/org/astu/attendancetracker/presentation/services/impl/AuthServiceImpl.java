package org.astu.attendancetracker.presentation.services.impl;

import lombok.RequiredArgsConstructor;
import org.astu.attendancetracker.core.application.auth.AuthenticationResponse;
import org.astu.attendancetracker.core.application.auth.CustomUserDetails;
import org.astu.attendancetracker.core.application.auth.JwtService;
import org.astu.attendancetracker.core.application.common.viewModels.auth.AuthenticationRequest;
import org.astu.attendancetracker.core.application.common.viewModels.auth.RegisterRequest;
import org.astu.attendancetracker.core.domain.*;
import org.astu.attendancetracker.persistence.repositories.UserRepository;
import org.astu.attendancetracker.presentation.services.AuthService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Deprecated
    public AuthenticationResponse register(RegisterRequest request) {
        var user = User
                .builder()
                .login(request.getLogin())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STUDENT)
                .build();

        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public TeacherProfile createTeacherProfile(String name, String apiTableId) {
        TeacherProfile teacher = new TeacherProfile(name, apiTableId);
        teacher.setUser(getUserForProfile(teacher));
        return teacher;
    }

    public StudentProfile createStudentProfile(String name, Group group) {
        StudentProfile student = new StudentProfile(group, name);
        student.setUser(getUserForProfile(student));
        return student;
    }

    public User getUserForProfile(Profile profile) {
        String randomPassword = passwordEncoder.encode(profile.getName());

        return User
                .builder()
                // todo Потом изменить установку логина, чтобы он был рандомным, т.к. если брать имя, то при одинаковых именах будет одинаковый логин и будет ошибка
                .login(profile.getName())
                .password(randomPassword)
                .role(profile.getRole())
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getLogin(),
                        request.getPassword())
        );
        var user = userRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь " + request.getLogin() + " не найден"));

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object userDetailsObj = authentication.getPrincipal();
            if (userDetailsObj instanceof CustomUserDetails userDetails) {
                return userDetails.getId();
            }
        }

        throw new AccessDeniedException("Пользователь не аутентифицирован");
    }
}
