package ru.example.lab3.repository;

import jakarta.transaction.Transactional;
import ru.example.lab3.model.HitResult;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
@Transactional
public class HitResultRepository {

    @PersistenceContext
    private EntityManager em;

    private static final Logger logger = Logger.getLogger(HitResultRepository.class.getName());

    public void save(HitResult result) {
        logger.info(() -> "Saving to database: " + result);
        try {
            em.persist(result);
            logger.fine("Successfully persisted to database");
        } catch (Exception e) {
            logger.severe("Database save failed: " + e.getMessage());
            throw e;
        }
    }

    public List<HitResult> findAll() {
        logger.fine("Querying all results from database");
        try {
            List<HitResult> results = em.createQuery("SELECT h FROM HitResult h ORDER BY h.timestamp DESC", HitResult.class)
                    .getResultList();
            logger.info(() -> "Database query returned " + results.size() + " results");
            return results;
        } catch (Exception e) {
            logger.severe("Database query failed: " + e.getMessage());
            throw e;
        }
    }
}