package org.astu.attendancetracker.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.astu.attendancetracker.core.application.common.enums.TypeOfGroupStudy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Table(name = "groups", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Entity
@Getter
@Setter
public class Group {
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private LocalDateTime lastIncreaseCourse = LocalDateTime.of(1970, 1, 1, 0, 0, 0);

    @JsonIgnore
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Discipline> disciplines;

    @JsonIgnore
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<StudentProfile> students;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] curriculumFile;

    // Возвращает тип обучения группы
    public TypeOfGroupStudy typeOfGroupStudy() {
        int indexOfGroupType = name.indexOf('-') - 1;
        char groupStudyType = name.charAt(indexOfGroupType);
        for (TypeOfGroupStudy type : TypeOfGroupStudy.values())
            if (groupStudyType == type.getTypeOfGroupStudy())
                return type;

        throw new NoSuchElementException("У группы " + name + " неизвестный тип обучения группы: " + groupStudyType);
    }

    // Возвращает текущий курс группы
    public int course() {
        int indexOfCourse = name.indexOf('-') + 1;
        // ASCII ('1' - '0' в ASCII 49 - 48 = 1)
        return name.charAt(indexOfCourse) - '0';
    }

    // Увеличивает курс группы на 1
    public void increaseCourse() {
        int increasedCourse = course() + 1;
        int indexOfCourse = name.indexOf('-') + 1;
        char[] charArray = name.toCharArray();
        charArray[indexOfCourse] = Integer.toString(increasedCourse).charAt(0);
        name = new String(charArray);
        lastIncreaseCourse = LocalDateTime.now();
    }

    public boolean studyIsOver() {
        TypeOfGroupStudy type = typeOfGroupStudy();

        // У магистратуры 3-го курса не бывает. И так для всех остальных
        return switch(course()) {
            case 3 -> type == TypeOfGroupStudy.MASTER;
            case 5 -> type == TypeOfGroupStudy.BACHELOR || type == TypeOfGroupStudy.COLLEGE;
            case 6 -> type == TypeOfGroupStudy.SPECIALITY;
            default -> false;
        };
    }

    public int currentSemester(boolean isEvenSemester) {
        return isEvenSemester ? course() * 2 : course() * 2 - 1;
    }
}
