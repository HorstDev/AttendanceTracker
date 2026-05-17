package org.astu.attendancetracker.presentation.services.impl;

import lombok.RequiredArgsConstructor;
import org.astu.attendancetracker.core.application.CompetencyMasteryCalculator;
import org.astu.attendancetracker.core.application.CurriculumAnalyzer;
import org.astu.attendancetracker.core.domain.Competency;
import org.astu.attendancetracker.core.domain.Discipline;
import org.astu.attendancetracker.core.domain.DisciplineCurriculum;
import org.astu.attendancetracker.core.domain.DisciplineCurriculumCompetency;
import org.astu.attendancetracker.core.domain.DisciplineImportanceWeights;
import org.astu.attendancetracker.core.domain.Group;
import org.astu.attendancetracker.core.domain.Role;
import org.astu.attendancetracker.core.domain.StudentProfile;
import org.astu.attendancetracker.core.domain.User;
import org.astu.attendancetracker.persistence.repositories.CompetencyRepository;
import org.astu.attendancetracker.persistence.repositories.DisciplineCurriculumCompetencyRepository;
import org.astu.attendancetracker.persistence.repositories.DisciplineCurriculumRepository;
import org.astu.attendancetracker.persistence.repositories.DisciplineImportanceWeightsRepository;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.Optional;
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
    private final CompetencyRepository competencyRepository;
    private final DisciplineCurriculumRepository disciplineCurriculumRepository;
    private final DisciplineCurriculumCompetencyRepository disciplineCurriculumCompetencyRepository;
    private final DisciplineImportanceWeightsRepository disciplineImportanceWeightsRepository;

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

    @Override
    @Transactional(readOnly = true)
    public ReportDtos.GroupCompetencyReportDto getGroupCompetencyMasteryReport(UUID groupId, UUID currentUserId) {
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

        List<Competency> competencies = competencyRepository.findByGroup_IdOrderByAbbreviationAsc(groupId);
        List<String> allCompetencies = competencies.stream().map(Competency::getAbbreviation).toList();

        List<Discipline> currentSemesterDisciplines =
                disciplineRepository.findByGroupInCurrentSemesterWithCompetencies(groupId);
        Map<UUID, List<Discipline>> operationalDisciplinesByCompetencyId =
                mapOperationalDisciplinesByCompetency(currentSemesterDisciplines);
        Set<CompetencyDisciplineKey> operationalCompetencyLinks =
                buildOperationalCompetencyLinks(currentSemesterDisciplines);
        Map<CompetencyDisciplineKey, Double> curriculumWeights =
                buildCurriculumWeightsForOperationalLinks(group, operationalCompetencyLinks);

        List<StudentProfile> students = profileRepository.findStudentProfilesByGroupId(groupId).stream()
                .sorted(Comparator.comparing(StudentProfile::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<ReportDtos.StudentCompetencyReportDto> studentReports = new ArrayList<>();
        for (StudentProfile student : students) {
            List<ReportDtos.CompetencyInformationReportDto> row = new ArrayList<>();
            for (Competency competency : competencies) {
                double mastery = computeCompetencyMasteryPercent(
                        student,
                        competency,
                        operationalDisciplinesByCompetencyId.getOrDefault(competency.getId(), List.of()),
                        curriculumWeights
                );
                row.add(new ReportDtos.CompetencyInformationReportDto(competency.getAbbreviation(), mastery));
            }
            studentReports.add(new ReportDtos.StudentCompetencyReportDto(student.getName(), row));
        }

        return new ReportDtos.GroupCompetencyReportDto(allCompetencies, studentReports);
    }

    /** Дисциплины текущего семестра из {@code disciplines}, связанные через {@code discipline_competency}. */
    private Map<UUID, List<Discipline>> mapOperationalDisciplinesByCompetency(List<Discipline> currentSemesterDisciplines) {
        Map<UUID, List<Discipline>> result = new HashMap<>();
        for (Discipline discipline : currentSemesterDisciplines) {
            for (Competency competency : discipline.getCompetencies()) {
                result.computeIfAbsent(competency.getId(), ignored -> new ArrayList<>()).add(discipline);
            }
        }
        return result;
    }

    private Set<CompetencyDisciplineKey> buildOperationalCompetencyLinks(List<Discipline> currentSemesterDisciplines) {
        Set<CompetencyDisciplineKey> links = new HashSet<>();
        for (Discipline discipline : currentSemesterDisciplines) {
            for (Competency competency : discipline.getCompetencies()) {
                links.add(new CompetencyDisciplineKey(competency.getId(), discipline.getId()));
            }
        }
        return links;
    }

    /**
     * Веса из {@code discipline_curriculum_competency} только для пар (компетенция, дисциплина расписания),
     * подтверждённых связью {@code discipline_competency}.
     */
    private Map<CompetencyDisciplineKey, Double> buildCurriculumWeightsForOperationalLinks(
            Group group,
            Set<CompetencyDisciplineKey> operationalCompetencyLinks
    ) {
        List<Discipline> allGroupDisciplines = disciplineRepository.findByGroup(group);
        List<DisciplineCurriculum> curriculumDisciplines =
                disciplineCurriculumRepository.findByGroup_IdOrderByNameAsc(group.getId());
        Map<UUID, Discipline> scheduleByCurriculumId =
                mapScheduleDisciplinesToCurriculum(allGroupDisciplines, curriculumDisciplines);

        Map<CompetencyDisciplineKey, Double> weights = new HashMap<>();
        for (DisciplineCurriculumCompetency link : disciplineCurriculumCompetencyRepository.findAllFetchedByGroupId(group.getId())) {
            Discipline operational = scheduleByCurriculumId.get(link.getDisciplineCurriculum().getId());
            if (operational == null) {
                continue;
            }
            CompetencyDisciplineKey key = new CompetencyDisciplineKey(link.getCompetency().getId(), operational.getId());
            if (!operationalCompetencyLinks.contains(key)) {
                continue;
            }
            weights.put(key, link.getWeight());
        }
        return weights;
    }

    private Map<UUID, Discipline> mapScheduleDisciplinesToCurriculum(
            List<Discipline> scheduleDisciplines,
            List<DisciplineCurriculum> curriculumDisciplines
    ) {
        Map<UUID, Discipline> result = new HashMap<>();
        for (DisciplineCurriculum curriculum : curriculumDisciplines) {
            Discipline bestMatch = null;
            double maxSimilarity = 0;
            for (Discipline schedule : scheduleDisciplines) {
                if (CurriculumAnalyzer.matchScheduleDisciplineToCurriculum(schedule, List.of(curriculum)).isEmpty()) {
                    continue;
                }
                double similarity = CurriculumAnalyzer.jaccardNameSimilarity(schedule.getName(), curriculum.getName());
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    bestMatch = schedule;
                }
            }
            if (bestMatch != null) {
                result.put(curriculum.getId(), bestMatch);
            }
        }
        return result;
    }

    private double computeCompetencyMasteryPercent(
            StudentProfile student,
            Competency competency,
            List<Discipline> operationalDisciplines,
            Map<CompetencyDisciplineKey, Double> curriculumWeights
    ) {
        if (operationalDisciplines.isEmpty()) {
            return 0.0;
        }

        List<Discipline> taughtDisciplines = new ArrayList<>();
        for (Discipline discipline : operationalDisciplines) {
            if (lessonOutcomeRepository.countConductedLessonsForDiscipline(discipline.getId()) > 0) {
                taughtDisciplines.add(discipline);
            }
        }
        if (taughtDisciplines.isEmpty()) {
            return 0.0;
        }

        double weightedSum = 0.0;
        double assignedWeightSum = 0.0;
        List<Discipline> disciplinesWithoutWeight = new ArrayList<>();

        for (Discipline discipline : taughtDisciplines) {
            CompetencyDisciplineKey key = new CompetencyDisciplineKey(competency.getId(), discipline.getId());
            Double weight = curriculumWeights.get(key);
            if (weight != null) {
                double disciplinePercent = computeDisciplineMasteryPercent(
                        student,
                        discipline,
                        lessonOutcomeRepository.countConductedLessonsForDiscipline(discipline.getId())
                );
                weightedSum += CompetencyMasteryCalculator.weightedDisciplineContribution(disciplinePercent, weight);
                assignedWeightSum += weight;
            } else {
                disciplinesWithoutWeight.add(discipline);
            }
        }

        if (!disciplinesWithoutWeight.isEmpty()) {
            double remainingWeight = Math.max(0.0, 1.0 - assignedWeightSum);
            double fallbackWeight = remainingWeight / disciplinesWithoutWeight.size();
            for (Discipline discipline : disciplinesWithoutWeight) {
                long conductedLessons = lessonOutcomeRepository.countConductedLessonsForDiscipline(discipline.getId());
                double disciplinePercent = computeDisciplineMasteryPercent(student, discipline, conductedLessons);
                weightedSum += CompetencyMasteryCalculator.weightedDisciplineContribution(disciplinePercent, fallbackWeight);
            }
        }

        return CompetencyMasteryCalculator.competencyMasteryPercent(weightedSum);
    }

    private record CompetencyDisciplineKey(UUID competencyId, UUID disciplineId) {
    }

    private double computeDisciplineMasteryPercent(
            StudentProfile student,
            Discipline discipline,
            long conductedLessons
    ) {
        long studentStarted = lessonOutcomeRepository.countStartedForStudentAndDiscipline(student.getId(), discipline.getId());
        long studentMissed = lessonOutcomeRepository.countNotVisitedForStudentAndDiscipline(student.getId(), discipline.getId());
        double ratingSum = ratingScoreRepository.sumScoreForStudentAndDiscipline(student.getId(), discipline.getId());
        boolean hasLate = ratingScoreRepository.hasLateSubmissionForStudentAndDiscipline(student.getId(), discipline.getId());
        Optional<DisciplineImportanceWeights> weights =
                disciplineImportanceWeightsRepository.findByDiscipline_Id(discipline.getId());
        return CompetencyMasteryCalculator.disciplineMasteryPercent(
                conductedLessons,
                studentStarted,
                studentMissed,
                ratingSum,
                hasLate,
                weights
        );
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
