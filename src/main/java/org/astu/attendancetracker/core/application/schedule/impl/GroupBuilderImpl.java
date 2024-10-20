package org.astu.attendancetracker.core.application.schedule.impl;

import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableGroupSchedule;
import org.astu.attendancetracker.core.application.common.exceptions.InvalidMethodOrderException;
import org.astu.attendancetracker.core.application.schedule.GroupBuilder;
import org.astu.attendancetracker.core.domain.Discipline;
import org.astu.attendancetracker.core.domain.Group;
import org.astu.attendancetracker.core.domain.TeacherProfile;

import java.util.*;

public class GroupBuilderImpl implements GroupBuilder {
    private Group group;
    private ApiTableGroupSchedule apiTableGroupSchedule;

    public GroupBuilder setGroupSchedule(ApiTableGroupSchedule schedule) {
        this.apiTableGroupSchedule = schedule;
        return this;
    }

    public GroupBuilder setGroup() {
        if (apiTableGroupSchedule == null)
            throw new InvalidMethodOrderException("Расписание группы не было добавлено");

        this.group = new Group();
        group.setName(apiTableGroupSchedule.name());

        return this;
    }

    public GroupBuilder setDisciplines(int currentSemester) {
        if (group == null)
            throw new InvalidMethodOrderException("Группа не была установлена перед установкой дисциплин");

        var disciplines = new HashSet<Discipline>();
        apiTableGroupSchedule.lessons().forEach(lesson -> lesson.entries().forEach(entry -> {
           var discipline = new Discipline(entry.discipline(), currentSemester);
           disciplines.add(discipline);
           discipline.setGroup(group);
        }));
        group.setDisciplines(new ArrayList<>(disciplines));

        return this;
    }

    public GroupBuilder setTeachersForDisciplines(Set<TeacherProfile> teacherProfilesInGroup) {
        if (group.getDisciplines().isEmpty())
            throw new InvalidMethodOrderException("Не были загружены дисциплины перед установкой преподавателей");

        // Преподаватели и соответствующие им дисциплины
        Map<TeacherProfile, HashSet<Discipline>> teachersAndDisciplines = new HashMap<>();
        teacherProfilesInGroup.forEach(teacher -> teachersAndDisciplines.put(teacher, new HashSet<>()));

        // Заполняем дисциплины для каждого преподавателя (teachersAndDisciplines)
        group.getDisciplines().forEach(discipline -> apiTableGroupSchedule.lessons().forEach(lesson -> lesson.entries().forEach(entry -> {
            if (discipline.getName().equals(entry.discipline())) {
                // Нужен id преподавателя, соответственнно, тут сначала нужно брать всех преподавателей из бд
                teacherProfilesInGroup.stream()
                        .filter(profile -> profile.getName().equals(entry.teacher()))
                        .findFirst()
                        .ifPresent(teacher -> teachersAndDisciplines.get(teacher).add(discipline));
            }
        })));

        // Устанавливаем дисциплины у преподавателей и преподавателей у дисциплин
        teachersAndDisciplines.forEach((teacher, disciplines) -> {
            teacher.getDisciplines().addAll(disciplines);
            disciplines.forEach(discipline -> discipline.getTeacherProfiles().add(teacher));
        });

        return this;
    }

    public Group build() {
        return group;
    }
}
