package org.hero2zero.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hero2zero.entity.CountryEmission;
import java.util.List;

@ApplicationScoped
public class EmissionDAO {
    @PersistenceContext(unitName = "Hero2ZeroPU")
    private EntityManager em;

    public List<CountryEmission> listAll() {
        return em.createQuery("SELECT c FROM CountryEmission c", CountryEmission.class)
                 .getResultList();
    }
}
