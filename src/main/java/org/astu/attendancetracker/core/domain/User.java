package org.astu.attendancetracker.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Table(name = "users")
@Entity
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue
    private UUID id;
    private String login;
    private String passwordHash;
    private String passwordSalt;
    private String refreshToken;
    private LocalDateTime tokenCreated;
    private LocalDateTime tokenExpires;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Profile profile;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(                                                     // @JoinTable необходима для связей "много ко многим"
            name = "user_roles",                                    // Имя таблицы для связи "много ко многим"
            joinColumns = { @JoinColumn(name = "user_id") },        // Поля, которые ссылаются на текущую таблицу (users)
            inverseJoinColumns = { @JoinColumn(name = "role_id") }  // Поля, ссылающиеся на сущность, находящуюся на другой стороне отношений
    )
    private List<Role> roles = new ArrayList<>();
}
