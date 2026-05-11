package org.astu.attendancetracker.presentation.dto.report;

import java.util.List;
import java.util.UUID;

/**
 * JSON для Angular: отчёты по группе и студенту.
 */
public final class ReportDtos {

    private ReportDtos() {
    }

    public record SubjectInformationReportDto(
            String subjectName,
            double score,
            int notVisitedLessonCount,
            int startedLessonCount
    ) {
    }

    public record StudentReportDto(
            String studentName,
            List<SubjectInformationReportDto> subjectsInformationReport
    ) {
    }

    public record GroupReportDto(
            List<String> allSubjects,
            List<StudentReportDto> studentReports
    ) {
    }

    public record LabWorkStatusDto(
            UUID id,
            int number,
            double score,
            boolean isDone
    ) {
    }

    public record StudentSubjectReportDto(
            String subjectName,
            double score,
            int notVisitedLessonCount,
            int startedLessonCount,
            List<LabWorkStatusDto> labWorkUserStatuses,
            int labWorkNumberShouldDone,
            boolean labWorksShouldExist
    ) {
    }
}
