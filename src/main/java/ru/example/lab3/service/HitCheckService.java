package ru.example.lab3.service;

import jakarta.enterprise.context.ApplicationScoped;
import ru.example.lab3.model.HitResult;


@ApplicationScoped
public class HitCheckService {


    public HitResult checkHit(double x, double y, double r) {

        boolean hit = isHit(x, y, r);
        return new HitResult(x, y, r, hit);
    }

    private boolean isHit(double x, double y, double r) {
        if (x <= 0 && y <= 0) {
            return x * x + y * y <= r * r;
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