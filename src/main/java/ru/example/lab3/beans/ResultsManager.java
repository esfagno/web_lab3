package ru.example.lab3.beans;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ru.example.lab3.model.HitResult;
import ru.example.lab3.repository.HitResultRepository;

import java.util.List;

@Named("resultsManager")
@ApplicationScoped
public class ResultsManager {

    @Inject
    private HitResultRepository repository;

    public void addResult(HitResult result) {
        repository.save(result);
    }

    public List<HitResult> getAllResults() {
        List<HitResult> results = repository.findAll();
        return results;
    }
}