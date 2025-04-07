package org.astu.attendancetracker.core.application.schedule.impl;

import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableGroupSchedule;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableLesson;
import org.astu.attendancetracker.core.application.common.enums.LessonType;
import org.astu.attendancetracker.core.application.common.exceptions.InvalidMethodOrderException;
import org.astu.attendancetracker.core.application.schedule.GroupBuilder;
import org.astu.attendancetracker.core.domain.Discipline;
import org.astu.attendancetracker.core.domain.Group;
import org.astu.attendancetracker.core.domain.Lesson;
import org.astu.attendancetracker.core.domain.TeacherProfile;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class GroupBuilderImpl implements GroupBuilder {
    private Group group;
    private ApiTableGroupSchedule apiTableGroupSchedule;

    private final Map<String, LessonType> lessonTypes = Map.of(
            "laboratory", LessonType.LABORATORY,
            "practice", LessonType.PRACTICE,
            "lecture", LessonType.LECTURE
    );

    private final Map<Integer, LocalTime> lessonStartTimesByOrderId = Map.of(
            0, LocalTime.of(8, 30),
            1, LocalTime.of(10, 15),
            2, LocalTime.of(12, 0),
            3, LocalTime.of(14, 0),
            4, LocalTime.of(15, 45),
            5, LocalTime.of(17, 30),
            6, LocalTime.of(19, 15)
    );

    private final Map<Integer, LocalTime> lessonEndTimesByOrderId = Map.of(
            0, LocalTime.of(10, 0),
            1, LocalTime.of(11, 45),
            2, LocalTime.of(13, 30),
            3, LocalTime.of(15, 30),
            4, LocalTime.of(17, 15),
            5, LocalTime.of(19, 0),
            6, LocalTime.of(20, 45)
    );

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

    public GroupBuilder setGroup(Group group) {
        if (apiTableGroupSchedule == null)
            throw new InvalidMethodOrderException("Расписание группы не было добавлено");

        this.group = group;

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

    public GroupBuilder setLessons(int currentWeekNumber) {
        if (group.getDisciplines().isEmpty())
            throw new InvalidMethodOrderException("Не были загружены дисциплины перед установкой занятий");

        // До какой даты загружаем занятия
        LocalDate uploadLessonsTill = LocalDate.now().plusMonths(6);
        // Занятия по дням (по dayId)
        HashMap<Integer, List<ApiTableLesson>> lessonsByDays = apiTableGroupSchedule.getLessonsByDays();
        // Названия дисциплин и соответствующие им дисциплины
        Map<String, Discipline> namesAndDisciplines = getMapWithNamesAndDisciplines();

        // Проходимся по датам, начиная с сегодняшней
        for (LocalDate currentDate = LocalDate.now(); currentDate.isBefore(uploadLessonsTill); currentDate = currentDate.plusDays(1)) {
            final LocalDate currentDateLocal = currentDate;
            int currentDayId = getCurrentDayId(currentWeekNumber, currentDate);
            // Если воскресенье, меняем неделю на следующую
            if (currentDayId == -1) {
                currentWeekNumber = (currentWeekNumber + 1) % 2;
                continue;
            }

            List<ApiTableLesson> lessonsInCurrentDay = lessonsByDays.get(currentDayId);
            lessonsInCurrentDay.forEach(lesson -> lesson.entries().forEach(entry -> {
                Lesson lessonToAdd = new Lesson();
                lessonToAdd.setLessonType(lessonTypes.get(entry.type()));
                lessonToAdd.setAudience(entry.audience());
                lessonToAdd.setStartDt(LocalDateTime.of(currentDateLocal, lessonStartTimesByOrderId.get(lesson.lessonOrderId())));
                lessonToAdd.setEndDt(LocalDateTime.of(currentDateLocal, lessonEndTimesByOrderId.get(lesson.lessonOrderId())));

                // Находим дисциплину с названием, как у entry, и устанавливаем дисциплине занятие
                Discipline disciplineForLesson = namesAndDisciplines.get(entry.discipline());
                disciplineForLesson.getLessons().add(lessonToAdd);
                lessonToAdd.setDiscipline(disciplineForLesson);
            }));
        }

        return this;
    }

    public Group build() {
        return group;
    }

    // Возвращает dayId для даты (dayId из предметной области ApiTable, который предоставляет АГТУ). Для воскресенья -1
    private int getCurrentDayId(int currentWeekNumber, LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY)
            return -1;

        return currentWeekNumber == 0
                ? date.getDayOfWeek().getValue() + 5
                : date.getDayOfWeek().getValue() - 1;
    }

    public Map<String, Discipline> getMapWithNamesAndDisciplines() {
        if (group.getDisciplines().isEmpty())
            throw new InvalidMethodOrderException("Не были загружены дисциплины");

        Map<String, Discipline> namesAndDisciplines = new HashMap<>();
        group.getDisciplines().forEach(discipline -> namesAndDisciplines.put(discipline.getName(), discipline));
        return namesAndDisciplines;
    }
}
