package org.astu.attendancetracker.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.astu.attendancetracker.presentation.dto.DisciplineAhpDtos;
import org.astu.attendancetracker.presentation.services.AuthService;
import org.astu.attendancetracker.presentation.services.DisciplineAhpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/discipline")
public class DisciplineController {

    private final DisciplineAhpService disciplineAhpService;
    private final AuthService authService;

    public DisciplineController(DisciplineAhpService disciplineAhpService, AuthService authService) {
        this.disciplineAhpService = disciplineAhpService;
        this.authService = authService;
    }

    @Operation(summary = "Дисциплины, которые ведёт текущий преподаватель")
    @GetMapping("taught-disciplines")
    public List<DisciplineAhpDtos.TaughtDisciplineDto> listTaughtDisciplines() {
        UUID userId = authService.getCurrentUserId();
        return disciplineAhpService.listTaughtDisciplines(userId);
    }

    @Operation(summary = "Состояние AHP по дисциплине: агрегированные веса и суждения экспертов")
    @GetMapping("{disciplineId}/ahp")
    public DisciplineAhpDtos.DisciplineAhpStateDto getAhpState(@PathVariable UUID disciplineId) {
        UUID userId = authService.getCurrentUserId();
        return disciplineAhpService.getAhpState(disciplineId, userId);
    }

    @Operation(summary = "Сохранить или обновить матрицу парных сравнений Саати (эксперт = текущий преподаватель)")
    @PostMapping("{disciplineId}/ahp/judgment")
    public DisciplineAhpDtos.DisciplineAhpStateDto submitJudgment(
            @PathVariable UUID disciplineId,
            @RequestBody DisciplineAhpDtos.PairwiseRatiosRequest request
    ) {
        UUID userId = authService.getCurrentUserId();
        return disciplineAhpService.submitJudgment(disciplineId, userId, request);
    }

    @Operation(summary = "Удалить своё суждение по дисциплине")
    @DeleteMapping("{disciplineId}/ahp/judgment")
    public ResponseEntity<Void> deleteMyJudgment(@PathVariable UUID disciplineId) {
        UUID userId = authService.getCurrentUserId();
        disciplineAhpService.deleteMyJudgment(disciplineId, userId);
        return ResponseEntity.noContent().build();
    }
}
