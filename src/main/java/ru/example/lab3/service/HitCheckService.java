package ru.example.lab3.service;

import jakarta.enterprise.context.ApplicationScoped;
import ru.example.lab3.model.HitResult;
import ru.example.lab3.validation.InputValidator;
import ru.example.lab3.validation.ValidationException;

import java.util.logging.Logger;

@ApplicationScoped
public class HitCheckService {

    private static final Logger logger = Logger.getLogger(HitCheckService.class.getName());

    public HitResult checkHit(double x, double y, double r) {
        logger.info(() -> String.format("Checking hit: x=%.2f, y=%.2f, r=%.2f", x, y, r));

        try {
            new InputValidator(x, y, r).validate();
            boolean hit = isHit(x, y, r);
            logger.info(() -> "Hit result: " + (hit ? "HIT" : "MISS"));
            return new HitResult(x, y, r, hit);
        } catch (ValidationException e) {
            logger.warning("Validation failed: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.severe("Unexpected error in checkHit: " + e.getMessage());
            throw e;
        }
    }

    private boolean isHit(double x, double y, double r) {
        if (x <= 0 && y <= 0) {
            boolean inCircle = x * x + y * y <= r * r;
            return inCircle;
        }
        if (x >= -r / 2 && x <= 0 && y >= 0 && y <= r) {
            return true;
        }
        if (x >= 0 && y >= 0) {
            return y <= -x / 2.0 + r / 2.0;
        }
        return false;
    }
}