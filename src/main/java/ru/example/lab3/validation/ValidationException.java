package ru.example.lab3.validation;

import lombok.experimental.StandardException;

@StandardException
public class ValidationException extends RuntimeException {
    public static ValidationException forR() {
        return new ValidationException("R must be an integer between 1 and 4.");
    }
    public static ValidationException forX() {
        return new ValidationException("X must be one of: -3, -2, -1, 0, 1, 2, 3, 4, 5");
    }
    public static ValidationException forY() {
        return new ValidationException("Y must be in range (-5, 3).");
    }
}