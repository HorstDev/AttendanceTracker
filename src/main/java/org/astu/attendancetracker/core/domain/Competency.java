package org.astu.attendancetracker.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "competencies",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_competency_abbreviation_group",
                columnNames = {"abbreviation", "group_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Competency {
    @Id
    @GeneratedValue
    private UUID id;

    private String abbreviation;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToMany(mappedBy = "competencies")
    private Set<Discipline> disciplines = new HashSet<>();

    @OneToMany(mappedBy = "competency", fetch = FetchType.LAZY)
    private List<DisciplineCurriculumCompetency> curriculumCompetencyLinks = new ArrayList<>();

    public Competency(String abbreviation, String description) {
        this.abbreviation = abbreviation;
        this.description = description;
    }

    public Competency(String abbreviation, String description, Group group) {
        this.abbreviation = abbreviation;
        this.description = description;
        this.group = group;
    }
}
