package org.astu.attendancetracker.presentation.viewModels;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class StudentProfileDto {
    // id профиля
    private UUID id;
    private String name;
}
