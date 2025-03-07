package org.astu.attendancetracker.presentation.services;

import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableGroupSchedule;
import org.astu.attendancetracker.core.application.schedule.GroupBuilder;
import org.astu.attendancetracker.core.domain.Group;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface GroupService {
    List<Group> getAllGroups();
    Group saveGroup(String groupName);
    Group findGroupById(UUID groupId);
    CompletableFuture<ApiTableGroupSchedule> getApiTableGroupSchedule(String groupName);
    CompletableFuture<Integer> getCurrentWeekNumber();
    void uploadSemesterForGroup(Group group, ApiTableGroupSchedule apiTableGroupSchedule,
                                 int currentWeekNumber, int currentSemester);
    GroupBuilder groupBuilder();
    void uploadCurriculumForGroup(UUID groupId, MultipartFile curriculumFile);
    CompletableFuture<HashSet<String>> getAllGroupsForTeacher(String teacherName);
}
