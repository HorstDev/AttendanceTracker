package org.astu.attendancetracker.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.astu.attendancetracker.presentation.dto.report.ReportDtos;
import org.astu.attendancetracker.presentation.services.AuthService;
import org.astu.attendancetracker.presentation.services.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Отчёты для Angular: посещаемость и сумма рейтинговых баллов по дисциплинам.
 */
@RestController
@RequestMapping("/api/v1/Report")
public class ReportController {

    private final ReportService reportService;
    private final AuthService authService;

    public ReportController(ReportService reportService, AuthService authService) {
        this.reportService = reportService;
        this.authService = authService;
    }

    @Operation(summary = "Личная успеваемость студента")
    @GetMapping("student-report")
    public List<ReportDtos.StudentSubjectReportDto> studentReport() {
        return reportService.getStudentSelfReport(authService.getCurrentUserId());
    }

    @Operation(summary = "Отчёт по группе (таблица студент × дисциплины)")
    @GetMapping("{groupId}")
    public ReportDtos.GroupReportDto groupReport(@PathVariable UUID groupId) {
        return reportService.getGroupReport(groupId, authService.getCurrentUserId());
    }
}
