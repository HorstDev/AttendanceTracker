package org.astu.attendancetracker.core.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table
@Data
public class TeacherProfile {
    @Id
    @GeneratedValue
    private long id;
    private String apiTableId;
}
