package org.astu.attendancetracker.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Агрегированные веса важности показателей A (посещаемость), G (рейтинг), L (задержка сдачи) для дисциплины.
 * Сумма весов = 1. Формируются как нормированное среднее геометрическое по весам экспертов (метод Саати).
 */
@Entity
@Table(name = "discipline_importance_weights")
@Getter
@Setter
@NoArgsConstructor
public class DisciplineImportanceWeights {

    @Id
    @GeneratedValue
    private UUID id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "discipline_id", nullable = false, unique = true)
    private Discipline discipline;

    @Column(nullable = false)
    private double weightAttendance;

    @Column(nullable = false)
    private double weightRating;

    @Column(nullable = false)
    private double weightLateSubmission;

    public DisciplineImportanceWeights(Discipline discipline, double weightAttendance, double weightRating, double weightLateSubmission) {
        this.discipline = discipline;
        this.weightAttendance = weightAttendance;
        this.weightRating = weightRating;
        this.weightLateSubmission = weightLateSubmission;
    }
}
