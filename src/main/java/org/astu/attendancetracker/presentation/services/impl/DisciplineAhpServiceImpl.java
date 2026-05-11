package org.astu.attendancetracker.presentation.services.impl;

import lombok.RequiredArgsConstructor;
import org.astu.attendancetracker.core.application.ahp.SaatyAhpCalculator;
import org.astu.attendancetracker.core.domain.Discipline;
import org.astu.attendancetracker.core.domain.DisciplineAhpExpertJudgment;
import org.astu.attendancetracker.core.domain.DisciplineImportanceWeights;
import org.astu.attendancetracker.core.domain.TeacherProfile;
import org.astu.attendancetracker.persistence.repositories.DisciplineAhpExpertJudgmentRepository;
import org.astu.attendancetracker.persistence.repositories.DisciplineImportanceWeightsRepository;
import org.astu.attendancetracker.persistence.repositories.DisciplineRepository;
import org.astu.attendancetracker.persistence.repositories.ProfileRepository;
import org.astu.attendancetracker.presentation.dto.DisciplineAhpDtos;
import org.astu.attendancetracker.presentation.services.DisciplineAhpService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DisciplineAhpServiceImpl implements DisciplineAhpService {

    private final DisciplineRepository disciplineRepository;
    private final DisciplineAhpExpertJudgmentRepository judgmentRepository;
    private final DisciplineImportanceWeightsRepository importanceWeightsRepository;
    private final ProfileRepository profileRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DisciplineAhpDtos.TaughtDisciplineDto> listTaughtDisciplines(UUID currentUserId) {
        TeacherProfile teacher = requireTeacher(currentUserId);
        return disciplineRepository.findDistinctByTeacherProfileId(teacher.getId()).stream()
                .sorted(Comparator.comparing(Discipline::getName, String.CASE_INSENSITIVE_ORDER))
                .map(d -> new DisciplineAhpDtos.TaughtDisciplineDto(
                        d.getId(),
                        d.getName(),
                        d.getGroup().getName(),
                        d.getSemester()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DisciplineAhpDtos.DisciplineAhpStateDto getAhpState(UUID disciplineId, UUID currentUserId) {
        TeacherProfile teacher = requireTeacher(currentUserId);
        Discipline discipline = loadDisciplineForTeacher(disciplineId, teacher);
        return buildState(discipline, teacher);
    }

    @Override
    @Transactional
    public DisciplineAhpDtos.DisciplineAhpStateDto submitJudgment(
            UUID disciplineId,
            UUID currentUserId,
            DisciplineAhpDtos.PairwiseRatiosRequest request
    ) {
        validatePairwiseRequest(request);
        TeacherProfile teacher = requireTeacher(currentUserId);
        Discipline discipline = loadDisciplineForTeacher(disciplineId, teacher);

        SaatyAhpCalculator.Weights weights;
        try {
            weights = SaatyAhpCalculator.computeWeights(
                    request.attendanceVsRating(),
                    request.attendanceVsLate(),
                    request.ratingVsLate()
            );
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        DisciplineAhpExpertJudgment judgment = judgmentRepository
                .findByDiscipline_IdAndTeacher_Id(discipline.getId(), teacher.getId())
                .orElseGet(DisciplineAhpExpertJudgment::new);

        if (judgment.getId() == null) {
            judgment.setDiscipline(discipline);
            judgment.setTeacher(teacher);
        }

        judgment.applyComputed(
                request.attendanceVsRating(),
                request.attendanceVsLate(),
                request.ratingVsLate(),
                weights.weightAttendance(),
                weights.weightRating(),
                weights.weightLateSubmission()
        );

        judgmentRepository.save(judgment);
        syncAggregatedWeights(discipline);

        return buildState(discipline, teacher);
    }

    @Override
    @Transactional
    public void deleteMyJudgment(UUID disciplineId, UUID currentUserId) {
        TeacherProfile teacher = requireTeacher(currentUserId);
        Discipline discipline = loadDisciplineForTeacher(disciplineId, teacher);
        judgmentRepository.deleteByDiscipline_IdAndTeacher_Id(discipline.getId(), teacher.getId());
        syncAggregatedWeights(discipline);
    }

    private void validatePairwiseRequest(DisciplineAhpDtos.PairwiseRatiosRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Тело запроса обязательно");
        }
        checkRatio("attendanceVsRating", request.attendanceVsRating());
        checkRatio("attendanceVsLate", request.attendanceVsLate());
        checkRatio("ratingVsLate", request.ratingVsLate());
    }

    private void checkRatio(String name, double value) {
        if (!(value > 0) || value < (1.0 / 9.0) - 1e-12 || value > 9.0 + 1e-12) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Поле " + name + " должно быть в диапазоне [1/9, 9], получено: " + value
            );
        }
    }

    private TeacherProfile requireTeacher(UUID currentUserId) {
        return profileRepository.findTeacherProfileByUserId(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступно только преподавателям"));
    }

    private Discipline loadDisciplineForTeacher(UUID disciplineId, TeacherProfile teacher) {
        Discipline discipline = disciplineRepository.findByIdFetchedTeachers(disciplineId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Дисциплина не найдена"));
        boolean teaches = discipline.getTeacherProfiles().stream()
                .anyMatch(t -> t.getId().equals(teacher.getId()));
        if (!teaches) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Вы не ведёте эту дисциплину");
        }
        return discipline;
    }

    private void syncAggregatedWeights(Discipline discipline) {
        List<DisciplineAhpExpertJudgment> judgments = judgmentRepository.findAllFetchedByDisciplineId(discipline.getId());
        if (judgments.isEmpty()) {
            importanceWeightsRepository.findByDiscipline_Id(discipline.getId()).ifPresent(w -> {
                discipline.setImportanceWeights(null);
                importanceWeightsRepository.delete(w);
            });
            disciplineRepository.save(discipline);
            return;
        }

        double prodA = 1;
        double prodG = 1;
        double prodL = 1;
        int k = judgments.size();
        for (DisciplineAhpExpertJudgment j : judgments) {
            prodA *= j.getWeightAttendance();
            prodG *= j.getWeightRating();
            prodL *= j.getWeightLateSubmission();
        }
        double gA = Math.pow(prodA, 1.0 / k);
        double gG = Math.pow(prodG, 1.0 / k);
        double gL = Math.pow(prodL, 1.0 / k);
        double sum = gA + gG + gL;

        DisciplineImportanceWeights weights = importanceWeightsRepository.findByDiscipline_Id(discipline.getId())
                .orElseGet(() -> {
                    DisciplineImportanceWeights created = new DisciplineImportanceWeights();
                    created.setDiscipline(discipline);
                    discipline.setImportanceWeights(created);
                    return created;
                });
        weights.setWeightAttendance(gA / sum);
        weights.setWeightRating(gG / sum);
        weights.setWeightLateSubmission(gL / sum);
        importanceWeightsRepository.save(weights);
    }

    private DisciplineAhpDtos.DisciplineAhpStateDto buildState(Discipline discipline, TeacherProfile currentTeacher) {
        DisciplineAhpDtos.SavedMatrixDto saved = judgmentRepository
                .findByDiscipline_IdAndTeacher_Id(discipline.getId(), currentTeacher.getId())
                .map(j -> new DisciplineAhpDtos.SavedMatrixDto(
                        j.getRatioAttendanceVsRating(),
                        j.getRatioAttendanceVsLate(),
                        j.getRatioRatingVsLate()
                ))
                .orElse(null);

        return new DisciplineAhpDtos.DisciplineAhpStateDto(
                discipline.getId(),
                discipline.getName(),
                discipline.getGroup().getName(),
                saved
        );
    }
}
