package org.astu.attendancetracker.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.astu.attendancetracker.core.application.common.enums.RoleName;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Table(name = "roles")
@Entity
@Getter
@Setter
public class Role {
    @Id
    @GeneratedValue
    private UUID id;
    private RoleName roleName;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<User> users = new ArrayList<>();
}
