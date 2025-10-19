package ru.example.lab3.beans;

import ru.example.lab3.model.HitResult;
import ru.example.lab3.repository.HitResultRepository;

import jakarta.inject.Named;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

@Named("resultsManager")
@ApplicationScoped
public class ResultsManager {

    @Inject
    private HitResultRepository repository;

    private static final Logger logger = Logger.getLogger(ResultsManager.class.getName());

    public void addResult(HitResult result) {
        logger.info(() -> "Adding result to repository: " + result);
        repository.save(result);
        logger.fine("Result saved successfully");
    }

    public List<HitResult> getAllResults() {
        logger.fine("Fetching all results from repository");
        List<HitResult> results = repository.findAll();
        logger.info(() -> "Retrieved " + results.size() + " results");
        return results;
    }
}