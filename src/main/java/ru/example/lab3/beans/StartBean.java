package ru.example.lab3.beans;

import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Named("startBean")
@RequestScoped
public class StartBean {
    public String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }
}