package org.astu.attendancetracker.core.domain;

import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Setter
public class TeacherProfile extends Profile {
    private String apiTableId;
}
