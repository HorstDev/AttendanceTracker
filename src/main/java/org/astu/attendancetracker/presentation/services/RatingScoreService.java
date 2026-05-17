package org.astu.attendancetracker.presentation.services;

public interface RatingScoreService {

    /** Проставляет 100 баллов каждому студенту по каждой дисциплине его группы (без просрочки). */
    int awardMaxScoreWithoutDelayToAllStudents();

    /** Удаляет все записи из rating_scores. */
    long deleteAllScores();
}
