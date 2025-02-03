package org.astu.attendancetracker.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Table(name = "disciplines")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Discipline {
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private int semester;
    // Экзамен
    private boolean exam;
    // Зачет
    private boolean credit;
    // Зачет с оценкой
    private boolean creditWithAssessment;
    // Курсовой проект
    private boolean courseProject;

    @OneToMany(mappedBy = "discipline", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Lesson> lessons = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToMany(mappedBy = "disciplines", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<TeacherProfile> teacherProfiles = new HashSet<>();

    public Discipline(String name, int semester) {
        this.name = name;
        this.semester = semester;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) return false;
        Discipline other = (Discipline) obj;
        return this.name.equals(other.name);
    }
}
