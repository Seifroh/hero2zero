package org.hero2zero.beans;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import org.hero2zero.dao.EmissionDAO;
import org.hero2zero.entity.CountryEmission;
import java.util.List;

@Named
@RequestScoped
public class EmissionBean {
    @Inject
    private EmissionDAO dao;

    public List<CountryEmission> getAllEmissions() {
        return dao.listAll();
    }
}
