package org.astu.attendancetracker.presentation.services.impl;

import lombok.RequiredArgsConstructor;
import org.astu.attendancetracker.core.domain.Discipline;
import org.astu.attendancetracker.core.domain.Group;
import org.astu.attendancetracker.core.domain.Role;
import org.astu.attendancetracker.core.domain.StudentProfile;
import org.astu.attendancetracker.core.domain.User;
import org.astu.attendancetracker.persistence.repositories.DisciplineRepository;
import org.astu.attendancetracker.persistence.repositories.GroupRepository;
import org.astu.attendancetracker.persistence.repositories.LessonOutcomeRepository;
import org.astu.attendancetracker.persistence.repositories.ProfileRepository;
import org.astu.attendancetracker.persistence.repositories.RatingScoreRepository;
import org.astu.attendancetracker.persistence.repositories.UserRepository;
import org.astu.attendancetracker.presentation.dto.report.ReportDtos;
import org.astu.attendancetracker.presentation.services.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final DisciplineRepository disciplineRepository;
    private final ProfileRepository profileRepository;
    private final LessonOutcomeRepository lessonOutcomeRepository;
    private final RatingScoreRepository ratingScoreRepository;

    @Override
    @Transactional(readOnly = true)
    public ReportDtos.GroupReportDto getGroupReport(UUID groupId, UUID currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (user.getRole() != Role.ADMIN && user.getRole() != Role.TEACHER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Группа не найдена"));

        if (user.getRole() == Role.TEACHER && !teacherHasGroup(currentUserId, groupId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа к отчёту этой группы");
        }

        List<Discipline> disciplines = disciplineRepository.findByGroupInCurrentSemester(groupId).stream()
                .sorted(Comparator.comparing(Discipline::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<String> allSubjects = disciplines.stream().map(Discipline::getName).toList();

        List<StudentProfile> students = profileRepository.findStudentProfilesByGroupId(groupId).stream()
                .sorted(Comparator.comparing(StudentProfile::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<ReportDtos.StudentReportDto> studentReports = new ArrayList<>();
        for (StudentProfile student : students) {
            List<ReportDtos.SubjectInformationReportDto> row = new ArrayList<>();
            for (Discipline d : disciplines) {
                long started = lessonOutcomeRepository.countStartedForStudentAndDiscipline(student.getId(), d.getId());
                long missed = lessonOutcomeRepository.countNotVisitedForStudentAndDiscipline(student.getId(), d.getId());
                double score = ratingScoreRepository.sumScoreForStudentAndDiscipline(student.getId(), d.getId());
                row.add(new ReportDtos.SubjectInformationReportDto(
                        d.getName(),
                        score,
                        (int) missed,
                        (int) started
                ));
            }
            studentReports.add(new ReportDtos.StudentReportDto(student.getName(), row));
        }

        return new ReportDtos.GroupReportDto(allSubjects, studentReports);
    }

    private boolean teacherHasGroup(UUID userId, UUID groupId) {
        return profileRepository.findTeacherProfileByUserIdWithDisciplinesAndGroups(userId)
                .map(t -> t.getDisciplines().stream()
                        .map(Discipline::getGroup)
                        .filter(Objects::nonNull)
                        .anyMatch(g -> g.getId().equals(groupId)))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportDtos.StudentSubjectReportDto> getStudentSelfReport(UUID currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (user.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        StudentProfile student = profileRepository.findStudentProfileByUserId(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Профиль студента не найден"));

        Group group = student.getGroup();
        if (group == null) {
            return List.of();
        }

        List<Discipline> disciplines = disciplineRepository.findByGroupInCurrentSemester(group.getId()).stream()
                .sorted(Comparator.comparing(Discipline::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<ReportDtos.StudentSubjectReportDto> out = new ArrayList<>();
        for (Discipline d : disciplines) {
            long started = lessonOutcomeRepository.countStartedForStudentAndDiscipline(student.getId(), d.getId());
            long missed = lessonOutcomeRepository.countNotVisitedForStudentAndDiscipline(student.getId(), d.getId());
            double score = ratingScoreRepository.sumScoreForStudentAndDiscipline(student.getId(), d.getId());
            out.add(new ReportDtos.StudentSubjectReportDto(
                    d.getName(),
                    score,
                    (int) missed,
                    (int) started,
                    List.of(),
                    0,
                    false
            ));
        }
        return out;
    }
}
