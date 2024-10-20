package org.astu.attendancetracker.core.application.common.exceptions;

// Выбрасывается при некорректном порядке вызовов методов
public class InvalidMethodOrderException extends RuntimeException {

    public InvalidMethodOrderException(String message) {
        super(message);
    }
}
