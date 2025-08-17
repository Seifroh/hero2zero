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

    public List<CountryEmission> findAllPending() {
        return em.createQuery(
                "SELECT c FROM CountryEmission c WHERE c.approved = false",
                CountryEmission.class)
                .getResultList();
    }

    public void create(CountryEmission emission) {
        em.persist(emission);
    }

    public void update(CountryEmission emission) {
        em.merge(emission);
    }

    public boolean existsByCountry(String country) {
        Long cnt = em.createQuery(
                "SELECT COUNT(e) FROM CountryEmission e WHERE e.country = :c", Long.class)
                .setParameter("c", country)
                .getSingleResult();
        return cnt > 0;
    }

    public List<CountryEmission> findByCountry(String country) {
        return em.createQuery(
                "SELECT e FROM CountryEmission e WHERE e.country = :c", CountryEmission.class)
                .setParameter("c", country)
                .getResultList();
    }

    public boolean existsByCountryCode(String code) {
        Long count = em.createQuery(
                "SELECT COUNT(e) FROM CountryEmission e WHERE e.countryCode = :code",
                Long.class)
                .setParameter("code", code)
                .getSingleResult();
        return count > 0;
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

    public CountryEmission findLatestApprovedByCountry(String country) {
        List<CountryEmission> list = em.createQuery(
                "SELECT e FROM CountryEmission e "
                + "WHERE e.country = :c AND e.approved = true "
                + "ORDER BY e.year DESC", CountryEmission.class)
                .setParameter("c", country)
                .setMaxResults(1)
                .getResultList();

        return list.isEmpty() ? null : list.get(0);
    }

    public boolean existsNewerPending(String country, int year) {
        Long cnt = em.createQuery(
                "SELECT COUNT(e) FROM CountryEmission e "
                + "WHERE e.country = :c AND e.approved = false AND e.year > :y",
                Long.class)
                .setParameter("c", country)
                .setParameter("y", year)
                .getSingleResult();
        return cnt > 0;
    }

    public List<Object[]> findCountriesWithCode() {
        return em.createQuery(
                "SELECT DISTINCT e.country, e.countryCode "
                + "FROM CountryEmission e "
                + "WHERE e.country IS NOT NULL AND e.countryCode IS NOT NULL "
                + "ORDER BY e.country", Object[].class)
                .getResultList();
    }

    public List<Object[]> findAllCountries() {
        return em.createQuery(
                "SELECT DISTINCT e.country, e.countryCode "
                + "FROM CountryEmission e "
                + "WHERE e.country IS NOT NULL "
                + "ORDER BY e.country",
                Object[].class
        ).getResultList();
    }

    public void delete(CountryEmission emission) {
        CountryEmission toRemove = em.find(CountryEmission.class, emission.getId());
        if (toRemove != null) {
            em.remove(toRemove);
        }
    }
}
