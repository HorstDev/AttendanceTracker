package org.astu.attendancetracker.presentation.services;

import org.astu.attendancetracker.presentation.dto.report.ReportDtos;

import java.util.List;
import java.util.UUID;

public interface ReportService {

    ReportDtos.GroupReportDto getGroupReport(UUID groupId, UUID currentUserId);

    List<ReportDtos.StudentSubjectReportDto> getStudentSelfReport(UUID currentUserId);
}
