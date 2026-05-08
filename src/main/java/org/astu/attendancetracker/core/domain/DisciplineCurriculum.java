package org.astu.attendancetracker.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @OneToMany(mappedBy = "disciplineCurriculum", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DisciplineCurriculumCompetency> competencyLinks = new ArrayList<>();

    public DisciplineCurriculum(String name, Group group) {
        this.name = name;
        this.group = group;
    }

    public void addCompetencyLink(Competency competency, double weight) {
        DisciplineCurriculumCompetency link = new DisciplineCurriculumCompetency(this, competency, weight);
        competencyLinks.add(link);
        competency.getCurriculumCompetencyLinks().add(link);
    }

    public Set<Competency> getLinkedCompetencies() {
        return competencyLinks.stream().map(DisciplineCurriculumCompetency::getCompetency).collect(Collectors.toSet());
    }
}
