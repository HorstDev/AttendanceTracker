package org.astu.attendancetracker.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "competencies")
@Getter
@Setter
@NoArgsConstructor
public class Competency {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true)
    private String abbreviation;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany(mappedBy = "competencies")
    private Set<Discipline> disciplines = new HashSet<>();

    public Competency(String abbreviation, String description) {
        this.abbreviation = abbreviation;
        this.description = description;
    }
}
