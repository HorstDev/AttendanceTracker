package org.astu.attendancetracker.core.application;

import org.astu.attendancetracker.core.domain.Discipline;
import org.astu.attendancetracker.core.domain.DisciplineImportanceWeights;

import java.util.Optional;

public final class CompetencyMasteryCalculator {

    private static final double DEFAULT_W_ATTENDANCE = 0.35;
    private static final double DEFAULT_W_RATING = 0.35;
    private static final double DEFAULT_W_LATE = 0.30;
    private static final double LATE_PENALTY_FACTOR = 0.92;
    private static final double MAX_RATING = 100.0;

    private CompetencyMasteryCalculator() {
    }

    /**
     * @param groupConductedLessons число проведённых занятий по дисциплине в группе
     * @param studentStartedLessons число проведённых занятий, на которых есть запись у студента
     */
    public static double attendanceRatio(long groupConductedLessons, long studentStartedLessons, long studentMissedLessons) {
        if (groupConductedLessons <= 0) {
            return 0.0;
        }
        if (studentStartedLessons <= 0) {
            return 0.0;
        }
        long visited = studentStartedLessons - studentMissedLessons;
        return clamp01((double) visited / studentStartedLessons);
    }

    public static double ratingRatio(double totalRatingScore) {
        return clamp01(totalRatingScore / MAX_RATING);
    }

    public static double lateSubmissionFactor(boolean hasLateSubmission) {
        return hasLateSubmission ? LATE_PENALTY_FACTOR : 1.0;
    }

    public static double disciplineMasteryPercent(
            long groupConductedLessons,
            long studentStartedLessons,
            long studentMissedLessons,
            double totalRatingScore,
            boolean hasLateSubmission,
            Optional<DisciplineImportanceWeights> weights
    ) {
        double w1 = weights.map(DisciplineImportanceWeights::getWeightAttendance).orElse(DEFAULT_W_ATTENDANCE);
        double w2 = weights.map(DisciplineImportanceWeights::getWeightRating).orElse(DEFAULT_W_RATING);
        double w3 = weights.map(DisciplineImportanceWeights::getWeightLateSubmission).orElse(DEFAULT_W_LATE);

        double a = attendanceRatio(groupConductedLessons, studentStartedLessons, studentMissedLessons);
        double g = ratingRatio(totalRatingScore);
        double x = lateSubmissionFactor(hasLateSubmission);

        return 100.0 * (w1 * a + w2 * g + w3 * x);
    }

    public static double competencyMasteryPercent(double weightedDisciplineSum) {
        return weightedDisciplineSum * 100.0;
    }

    public static double weightedDisciplineContribution(double disciplineMasteryPercent, double curriculumWeight) {
        return curriculumWeight * (disciplineMasteryPercent / 100.0);
    }

    private static double clamp01(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }
}
