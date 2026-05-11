package org.astu.attendancetracker.core.application.ahp;

/**
 * Веса из матрицы парных сравнений Саати (n = 3): нормировка по столбцам, усреднение по строкам.
 */
public final class SaatyAhpCalculator {

    private static final int N = 3;
    private static final double MIN_RATIO = 1.0 / 9.0;
    private static final double MAX_RATIO = 9.0;

    private SaatyAhpCalculator() {
    }

    public record Weights(double weightAttendance, double weightRating, double weightLateSubmission) {
    }

    public static Weights computeWeights(double attendanceVsRating, double attendanceVsLate, double ratingVsLate) {
        validateRatio(attendanceVsRating);
        validateRatio(attendanceVsLate);
        validateRatio(ratingVsLate);

        double[][] m = buildMatrix(attendanceVsRating, attendanceVsLate, ratingVsLate);

        double[] colSum = new double[N];
        for (int j = 0; j < N; j++) {
            for (int i = 0; i < N; i++) {
                colSum[j] += m[i][j];
            }
        }

        double[][] normalized = new double[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                normalized[i][j] = m[i][j] / colSum[j];
            }
        }

        double[] w = new double[N];
        for (int i = 0; i < N; i++) {
            double rowSum = 0;
            for (int j = 0; j < N; j++) {
                rowSum += normalized[i][j];
            }
            w[i] = rowSum / N;
        }

        return new Weights(w[0], w[1], w[2]);
    }

    private static void validateRatio(double value) {
        if (!(value > 0) || value < MIN_RATIO - 1e-12 || value > MAX_RATIO + 1e-12) {
            throw new IllegalArgumentException(
                    "Каждое отношение Саати должно быть в диапазоне [1/9, 9], получено: " + value
            );
        }
    }

    private static double[][] buildMatrix(double ag, double al, double gl) {
        double[][] m = new double[N][N];
        m[0][0] = 1;
        m[0][1] = ag;
        m[0][2] = al;
        m[1][0] = 1 / ag;
        m[1][1] = 1;
        m[1][2] = gl;
        m[2][0] = 1 / al;
        m[2][1] = 1 / gl;
        m[2][2] = 1;
        return m;
    }
}
