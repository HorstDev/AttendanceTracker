package org.astu.attendancetracker.presentation.mappers;

import org.astu.attendancetracker.core.domain.TeacherProfile;
import org.astu.attendancetracker.presentation.viewModels.TeacherProfileDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TeacherProfileMapper {
    TeacherProfileDto toDto(TeacherProfile studentProfile);

    List<TeacherProfileDto> toDto(List<TeacherProfile> studentProfileList);
}
