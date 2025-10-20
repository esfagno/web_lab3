package ru.example.lab3.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.example.lab3.util.Constants;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "HIT_RESULTS")
@Data
@NoArgsConstructor
public class HitResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double x;
    private double y;
    private double r;
    private boolean hit;
    private String timestamp;

    public HitResult(double x, double y, double r, boolean hit) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.hit = hit;
        this.timestamp = ZonedDateTime.now(ZoneId.of(Constants.MOSCOW_ZONE_ID))
                .format(DateTimeFormatter.ofPattern(Constants.TIMESTAMP_PATTERN));
    }
}