package org.astu.attendancetracker.presentation.mappers;

import org.astu.attendancetracker.core.domain.Lesson;
import org.astu.attendancetracker.presentation.viewModels.LessonViewModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LessonMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "discipline.name", target = "subjectName")
    @Mapping(source = "discipline.group.name", target = "groupName")
    @Mapping(source = "startDt", target = "start")
    @Mapping(source = "endDt", target = "end")
    @Mapping(source = "realStartDt", target = "realStart")
    @Mapping(source = "realEndDt", target = "realEnd")
    @Mapping(source = "lessonType.type", target = "type")
    @Mapping(source = "started", target = "started")
    LessonViewModel toViewModel(Lesson lesson);
}
