package ru.example.lab3.beans;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Named("startBean")
@RequestScoped
public class StartBean {
    public static final String MOSCOW_ZONE_ID = "Europe/Moscow";

    public String getCurrentDateTime() {
        return ZonedDateTime.now(ZoneId.of(MOSCOW_ZONE_ID))
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }
}