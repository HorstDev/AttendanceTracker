package org.astu.attendancetracker.core.application.auth;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface CustomUserDetails extends UserDetails {
    UUID getId();
}
