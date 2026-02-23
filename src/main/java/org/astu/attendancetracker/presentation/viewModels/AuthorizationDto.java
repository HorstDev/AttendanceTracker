package org.astu.attendancetracker.presentation.viewModels;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthorizationDto {
    private String login;
    private String password;
}
