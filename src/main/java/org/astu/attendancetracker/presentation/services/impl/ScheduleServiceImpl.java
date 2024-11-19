package org.astu.attendancetracker.presentation.services.impl;

import jakarta.transaction.Transactional;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableGroupSchedule;
import org.astu.attendancetracker.core.application.schedule.GroupBuilder;
import org.astu.attendancetracker.core.application.schedule.ScheduleManager;
import org.astu.attendancetracker.core.application.schedule.impl.GroupBuilderImpl;
import org.astu.attendancetracker.core.domain.Group;
import org.astu.attendancetracker.core.domain.TeacherProfile;
import org.astu.attendancetracker.persistence.repositories.GroupRepository;
import org.astu.attendancetracker.persistence.repositories.ProfileRepository;
import org.astu.attendancetracker.presentation.services.ScheduleService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleManager scheduleManager;
    private final ProfileRepository profileRepository;
    private final GroupRepository groupRepository;

    public ScheduleServiceImpl(ScheduleManager scheduleFetcher, ProfileRepository profileRepository, GroupRepository groupRepository) {
        this.scheduleManager = scheduleFetcher;
        this.profileRepository = profileRepository;
        this.groupRepository = groupRepository;
    }

    public CompletableFuture<ApiTableGroupSchedule> getApiTableGroupSchedule(String groupName) {
        return scheduleManager.getGroupSchedule(groupName);
    }

    // Возвращает номер текущей недели
    @Cacheable(value = "schedule", key = "'current-week-number'")
    public CompletableFuture<Integer> getCurrentWeekNumber() {
        return scheduleManager.getCurrentWeekNumber();
    }

    @Transactional
    public void uploadGroupScheduleData(ApiTableGroupSchedule apiTableGroupSchedule, int currentWeekNumber) {
        // Преподаватели, которые преподают у группы groupName
        Set<TeacherProfile> teacherProfilesInSchedule = getTeacherProfilesFromDatabaseOnGroupSchedule(apiTableGroupSchedule);

        Group group = groupBuilder()
                .setGroupSchedule(apiTableGroupSchedule)
                .setGroup()
                .setDisciplines(5)
                .setTeachersForDisciplines(teacherProfilesInSchedule)
                .setLessons(currentWeekNumber)
                .build();

        groupRepository.save(group);
    }

    public GroupBuilder groupBuilder() {
        return new GroupBuilderImpl();
    }

    // Возвращает преподавателей из БД на основе имен в расписании (ApiTableGroupSchedule)
    private Set<TeacherProfile> getTeacherProfilesFromDatabaseOnGroupSchedule(ApiTableGroupSchedule schedule) {
        Set<String> teacherNames = schedule.getTeacherNames();
        Set<TeacherProfile> teachersFromDb = profileRepository.findAllTeacherProfilesByNameIn(teacherNames);
        // Если рамзеры не равны (т.е. какие-то преподаватели в apiTableGroupSchedule отсутствуют в БД)
        if (teachersFromDb.size() != teacherNames.size())
            throw new RuntimeException("Преподаватели у группы " + schedule.name() + " отсутствуют в БД. Необходимо обновить базу данных преподавателей");
        return teachersFromDb;
    }
}
