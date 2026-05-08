package org.astu.attendancetracker.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Table(
        name = "disciplines_curriculum",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "name"})
)
@Entity
@Getter
@Setter
@NoArgsConstructor
public class DisciplineCurriculum {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "discipline_curriculum_competency",
            joinColumns = @JoinColumn(name = "discipline_curriculum_id"),
            inverseJoinColumns = @JoinColumn(name = "competency_id")
    )
    private Set<Competency> curriculumCompetencies = new HashSet<>();

    public DisciplineCurriculum(String name, Group group) {
        this.name = name;
        this.group = group;
    }
}
