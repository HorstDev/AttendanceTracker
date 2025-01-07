package org.astu.attendancetracker.presentation.services.impl;

import lombok.RequiredArgsConstructor;
import org.astu.attendancetracker.core.application.auth.AuthenticationResponse;
import org.astu.attendancetracker.core.application.auth.JwtService;
import org.astu.attendancetracker.core.application.common.viewModels.auth.AuthenticationRequest;
import org.astu.attendancetracker.core.application.common.viewModels.auth.RegisterRequest;
import org.astu.attendancetracker.core.domain.Profile;
import org.astu.attendancetracker.core.domain.Role;
import org.astu.attendancetracker.core.domain.User;
import org.astu.attendancetracker.persistence.repositories.UserRepository;
import org.astu.attendancetracker.presentation.services.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    public User getUserForProfile(Profile profile) {
        String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        return User
                .builder()
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
}
