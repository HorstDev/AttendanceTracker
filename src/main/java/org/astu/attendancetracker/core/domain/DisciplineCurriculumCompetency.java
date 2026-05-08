package org.astu.attendancetracker.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "discipline_curriculum_competency",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"discipline_curriculum_id", "competency_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class DisciplineCurriculumCompetency {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "discipline_curriculum_id")
    private DisciplineCurriculum disciplineCurriculum;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competency_id")
    private Competency competency;

    /** Вклад дисциплины УП в компетенцию; по каждой компетенции сумма весов по связанным дисциплинам = 1 */
    @Column(nullable = false)
    private double weight;

    public DisciplineCurriculumCompetency(DisciplineCurriculum disciplineCurriculum, Competency competency, double weight) {
        this.disciplineCurriculum = disciplineCurriculum;
        this.competency = competency;
        this.weight = weight;
    }
}
