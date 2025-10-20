package ru.example.lab3.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import ru.example.lab3.model.HitResult;

import java.util.List;

@ApplicationScoped
@Transactional
public class HitResultRepository {

    @PersistenceContext
    private EntityManager em;

    public void save(HitResult result) {
        try {
            em.persist(result);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<HitResult> findAll() {
        try {
            List<HitResult> results = em.createQuery("SELECT h FROM HitResult h ORDER BY h.timestamp DESC", HitResult.class)
                    .getResultList();
            return results;
        } catch (Exception e) {
            throw e;
        }
    }
}