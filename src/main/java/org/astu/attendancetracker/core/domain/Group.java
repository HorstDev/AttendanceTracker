package org.astu.attendancetracker.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Table(name = "groups")
@Entity
@Getter
@Setter
public class Group {
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private LocalDateTime lastIncreaseCourse = LocalDateTime.of(1970, 1, 1, 0, 0, 0);

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Discipline> disciplines;
}
