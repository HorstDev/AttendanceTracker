package org.astu.attendancetracker.presentation.mappers;

import org.astu.attendancetracker.core.domain.StudentProfile;
import org.astu.attendancetracker.presentation.viewModels.StudentProfileDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StudentProfileMapper {
    StudentProfileDto toDto(StudentProfile studentProfile);

    List<StudentProfileDto> toDto(List<StudentProfile> studentProfileList);
}
