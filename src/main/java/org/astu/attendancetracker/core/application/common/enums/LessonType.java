package org.astu.attendancetracker.core.application.common.enums;

import lombok.Getter;

// Типы занятий
@Getter
public enum LessonType {
    LECTURE("Лекция"),
    PRACTICE("Практика"),
    LABORATORY("Лабораторная");

    private final String type;

    LessonType(final String type) {
        this.type = type;
    }
}
