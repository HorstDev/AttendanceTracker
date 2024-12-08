package org.astu.attendancetracker.core.application.common.enums;

import lombok.Getter;

@Getter
public enum TypeOfGroupStudy {
    // Бакалавриат
    BACHELOR('Б'),
    // Специалитет
    SPECIALITY('С'),
    // Магистратура
    MASTER('М'),
    // Колледж
    COLLEGE('О');

    private final char typeOfGroupStudy;

    TypeOfGroupStudy(char typeOfGroupStudy) {
        this.typeOfGroupStudy = typeOfGroupStudy;
    }
}
