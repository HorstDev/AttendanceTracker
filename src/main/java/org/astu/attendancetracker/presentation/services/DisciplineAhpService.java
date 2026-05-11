package org.astu.attendancetracker.presentation.services;

import org.astu.attendancetracker.presentation.dto.DisciplineAhpDtos;

import java.util.List;
import java.util.UUID;

public interface DisciplineAhpService {

    List<DisciplineAhpDtos.TaughtDisciplineDto> listTaughtDisciplines(UUID currentUserId);

    DisciplineAhpDtos.DisciplineAhpStateDto getAhpState(UUID disciplineId, UUID currentUserId);

    DisciplineAhpDtos.DisciplineAhpStateDto submitJudgment(UUID disciplineId, UUID currentUserId, DisciplineAhpDtos.PairwiseRatiosRequest request);

    void deleteMyJudgment(UUID disciplineId, UUID currentUserId);
}
