package org.beans;

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

    private String filterCode;
    private List<CountryEmission> filteredEmissions;

    public void loadByCode() {
        this.filteredEmissions = dao.findByCountryCode(filterCode);
    }

    public List<CountryEmission> getFilteredEmissions() {
        return filteredEmissions;
    }

    public String getFilterCode() {
        return filterCode;
    }

    public void setFilterCode(String filterCode) {
        this.filterCode = filterCode;
    }
}
