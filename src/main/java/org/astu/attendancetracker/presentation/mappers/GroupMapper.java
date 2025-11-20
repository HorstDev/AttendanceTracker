package org.astu.attendancetracker.presentation.mappers;

import org.astu.attendancetracker.core.domain.Group;
import org.astu.attendancetracker.presentation.viewModels.GroupDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    GroupDto toDto(Group group);

    List<GroupDto> toDto(List<Group> groups);
}
