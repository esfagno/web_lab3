package ru.example.lab3.validation;

import ru.example.lab3.util.Constants;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InputValidator {
    private final double x;
    private final double y;
    private final double r;

    public void validate() {
        validateR();
        validateX();
        validateY();
    }

    private void validateR() {
        if (r != Math.floor(r) || r < Constants.R_MIN || r > Constants.R_MAX) {
            throw ValidationException.forR();
        }
    }

    private void validateX() {
        for (double validX : Constants.VALID_X_VALUES) {
            if (Math.abs(x - validX) < Constants.VALIDATION_EPSILON) return;
        }
        throw ValidationException.forX();
    }

    private void validateY() {
        if (y <= Constants.Y_MIN || y >= Constants.Y_MAX) {
            throw ValidationException.forY();
        }
    }
}