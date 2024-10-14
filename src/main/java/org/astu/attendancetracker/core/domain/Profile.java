package org.astu.attendancetracker.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Table(name = "profiles")
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING)
public abstract class Profile {
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
