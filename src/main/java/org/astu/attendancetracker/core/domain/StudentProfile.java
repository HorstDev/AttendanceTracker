package org.astu.attendancetracker.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfile extends Profile {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "group_id")
    private Group group;

    @JsonIgnore
    @OneToMany(mappedBy = "studentProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LessonOutcome> lessonOutcomes = new ArrayList<>();

    public StudentProfile(Group group, String name) {
        super(name);
        this.group = group;
    }

    public Role getRole() {
        return Role.STUDENT;
    }
}
