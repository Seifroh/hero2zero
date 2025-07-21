package org.hero2zero.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hero2zero.entity.CountryEmission;
import java.util.List;

@Stateless
public class EmissionDAO {

    @PersistenceContext(unitName = "Hero2ZeroPU")
    private EntityManager em;

    public List<CountryEmission> listAll() {
        return em.createQuery("SELECT c FROM CountryEmission c", CountryEmission.class)
                .getResultList();
    }

    public List<CountryEmission> findByCountryCode(String code) {
        return em.createQuery(
                "SELECT c FROM CountryEmission c WHERE c.countryCode = :code", CountryEmission.class)
                .setParameter("code", code)
                .getResultList();
    }

    public List<CountryEmission> findByContinent(String continent) {
        return em.createQuery(
                "SELECT c FROM CountryEmission c WHERE c.continent = :continent", CountryEmission.class)
                .setParameter("continent", continent)
                .getResultList();
    }

    public List<CountryEmission> findAll() {
        return em.createQuery("SELECT c FROM CountryEmission c", CountryEmission.class)
                .getResultList();
    }

    public List<CountryEmission> findAllApproved() {
        return em.createQuery(
                "SELECT c FROM CountryEmission c WHERE c.approved = true",
                CountryEmission.class)
                .getResultList();
    }

    public void create(CountryEmission emission) {
        em.persist(emission);
    }

    public void update(CountryEmission emission) {
        em.merge(emission);
    }

    public boolean existsByCountryCodeAndYear(String countryCode, int year) {
        Long count = (Long) em.createQuery(
                "SELECT COUNT(e) FROM CountryEmission e WHERE e.countryCode = :code AND e.year = :year")
                .setParameter("code", countryCode)
                .setParameter("year", year)
                .getSingleResult();
        return count > 0;
    }

    public List<CountryEmission> findDistinctCountries() {
        return em.createQuery(
                "SELECT DISTINCT new org.hero2zero.entity.CountryEmission(e.country, e.countryCode) "
                + "FROM CountryEmission e ORDER BY e.country", CountryEmission.class)
                .getResultList();
    }

}
