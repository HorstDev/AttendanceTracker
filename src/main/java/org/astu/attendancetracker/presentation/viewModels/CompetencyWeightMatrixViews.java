package org.astu.attendancetracker.presentation.viewModels;

import java.util.List;
import java.util.UUID;

/** DTO для страницы матрицы весов «дисциплина УП × компетенция». */
public final class CompetencyWeightMatrixViews {

    private CompetencyWeightMatrixViews() {}

    public record MatrixDto(List<ColumnDto> columns, List<RowDto> rows) {}

    public record ColumnDto(UUID competencyId, String abbreviation) {}

    public record CellDto(UUID linkId, boolean linked, double weight) {}

    public record RowDto(UUID disciplineCurriculumId, String disciplineName, List<CellDto> cells) {}

    public record LinkWeightUpdate(UUID linkId, double weight) {}
}
