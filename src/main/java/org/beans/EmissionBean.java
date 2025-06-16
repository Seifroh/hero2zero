package org.beans;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import org.hero2zero.dao.EmissionDAO;
import org.hero2zero.entity.CountryEmission;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class EmissionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EmissionDAO dao;

    private List<CountryEmission> allEmissions;

    @PostConstruct
    public void init() {
        allEmissions = dao.findAll();
    }

    public List<CountryEmission> getAllEmissions() {
        return allEmissions;
    }
}
