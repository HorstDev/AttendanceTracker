package org.astu.attendancetracker.presentation.dto;

import java.util.UUID;

public final class DisciplineAhpDtos {

    private DisciplineAhpDtos() {
    }

    public record TaughtDisciplineDto(UUID id, String name, String groupName, int semester) {
    }

    public record PairwiseRatiosRequest(
            double attendanceVsRating,
            double attendanceVsLate,
            double ratingVsLate
    ) {
    }

    /** Сохранённая матрица текущего преподавателя (если есть). */
    public record SavedMatrixDto(
            double ratioAttendanceVsRating,
            double ratioAttendanceVsLate,
            double ratioRatingVsLate
    ) {
    }

    public record DisciplineAhpStateDto(
            UUID disciplineId,
            String disciplineName,
            String groupName,
            SavedMatrixDto savedMatrix
    ) {
    }
}
