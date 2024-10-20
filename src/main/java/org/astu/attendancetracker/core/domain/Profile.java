package org.astu.attendancetracker.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Table(name = "profiles")
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
public abstract class Profile {
    @Id
    @GeneratedValue
    private UUID id;
    protected String name;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Profile(String name) {
        this.name = name;
    }
}
