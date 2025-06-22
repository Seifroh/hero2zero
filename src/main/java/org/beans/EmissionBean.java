package org.beans;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import org.hero2zero.dao.EmissionDAO;
import org.hero2zero.entity.CountryEmission;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

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

    public List<CountryEmission> getAllEmissionsApproved() {
        return dao.findAllApproved();
    }

    public boolean co2Filter(Object value, Object filter, Locale locale) {
        if (filter == null || filter.toString().isBlank()) {
            return true;
        }
        if (value == null) {
            return false;
        }
        try {
            double dbValue = ((Number) value).doubleValue();
            double filterValue = Double.parseDouble(filter.toString());
            return Math.abs(dbValue - filterValue) < 0.0001;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
