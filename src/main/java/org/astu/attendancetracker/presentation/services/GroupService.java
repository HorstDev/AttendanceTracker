package org.astu.attendancetracker.presentation.services;

import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableGroupSchedule;
import org.astu.attendancetracker.core.application.schedule.GroupBuilder;
import org.astu.attendancetracker.core.domain.Group;
import org.astu.attendancetracker.presentation.viewModels.CompetencyWeightMatrixViews;
import org.astu.attendancetracker.presentation.viewModels.GroupDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface GroupService {
    // Группы, в которых преподаватель ведёт дисциплины (текущий семестр по расписанию в БД).
    List<Group> getSupervisedGroupsForTeacher(UUID userId);

    List<Group> getAllGroups();
    Group saveGroup(String groupName);
    Group findGroupById(UUID groupId);
    CompletableFuture<ApiTableGroupSchedule> getApiTableGroupSchedule(String groupName);

    ApiTableGroupSchedule getApiTableGroupScheduleFromClasspathJson();
    CompletableFuture<Integer> getCurrentWeekNumber();

    int getCurrentWeekNumberFromClasspathJson();
    void uploadSemesterForGroup(Group group, ApiTableGroupSchedule apiTableGroupSchedule,
                                 int currentWeekNumber, int currentSemester);
    GroupBuilder groupBuilder();
    void uploadCurriculumForGroup(UUID groupId, MultipartFile curriculumFile);
    CompletableFuture<HashSet<String>> getAllGroupsForTeacher(String teacherName);
    List<GroupDto> findGroupsByPartOfName(String partOfName);

    CompetencyWeightMatrixViews.MatrixDto getCompetencyWeightMatrix(UUID groupId);

    void saveCompetencyWeightMatrix(UUID groupId, List<CompetencyWeightMatrixViews.LinkWeightUpdate> updates);
}
