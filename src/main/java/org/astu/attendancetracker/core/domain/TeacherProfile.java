package org.astu.attendancetracker.core.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TeacherProfile extends Profile {
    private String apiTableId;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(                                                             // @JoinTable необходима для связей "много ко многим"
            name = "teacher_discipline",                                    // Имя таблицы для связи "много ко многим"
            joinColumns = { @JoinColumn(name = "teacher_id") },             // Поля, которые ссылаются на текущую таблицу (users)
            inverseJoinColumns = { @JoinColumn(name = "discipline_id") }    // Поля, ссылающиеся на сущность, находящуюся на другой стороне отношений
    )
    private List<Discipline> disciplines = new ArrayList<>();

    public TeacherProfile(String name, String apiTableId) {
        this.apiTableId = apiTableId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TeacherProfile other)) return false;
        return this.name.equals(other.name);
    }

    @Override
    public Role getRole() {
        return Role.TEACHER;
    }
}
