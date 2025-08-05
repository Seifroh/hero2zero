package org.beans;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.hero2zero.dao.EmissionDAO;
import org.hero2zero.entity.CountryEmission;
import org.primefaces.event.CellEditEvent;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class EmissionsAdminBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EmissionDAO dao;

    private List<CountryEmission> allEmissions;
    private CountryEmission newEmission;
    private List<CountryEmission> availableCountries;
    private String selectedCountryCode;

    @PostConstruct
    public void init() {
        reloadList();
        newEmission = new CountryEmission();
        availableCountries = dao.findDistinctCountries();
    }

    private void reloadList() {
        boolean isAdmin = FacesContext.getCurrentInstance()
                .getExternalContext()
                .isUserInRole("admin");
        if (isAdmin) {
            allEmissions = dao.listAll();
        } else {
            allEmissions = dao.findAllApproved();
        }
    }

    public void onCellEdit(CellEditEvent event) {
        int row = event.getRowIndex();
        CountryEmission edited = allEmissions.get(row);

        Object newValue = event.getNewValue();
        if (newValue != null && !newValue.equals(event.getOldValue())) {
            edited.setCo2Emissions((Double) newValue);
            edited.setApproved(false);
            dao.update(edited);
            reloadList();
            FacesContext.getCurrentInstance()
                    .addMessage(null,
                            new FacesMessage("Geändert und Liste neu geladen"));
        }
    }

    public void approve(CountryEmission e) {
        e.setApproved(true);
        dao.update(e);
        reloadList();
        FacesContext.getCurrentInstance()
                .addMessage(null,
                        new FacesMessage("Datensatz freigegeben", "ID: " + e.getId()));
    }

    public void saveNew() {
        onCountryCodeChange();

        if (dao.existsByCountryCodeAndYear(
                newEmission.getCountryCode(), newEmission.getYear())) {
            FacesContext.getCurrentInstance()
                    .addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Eintrag existiert bereits!",
                                    "Für dieses Land und Jahr gibt es schon einen Datensatz."));
            return;
        }
        System.out.println("DEBUG newEmission.country    = " + newEmission.getCountry());
        System.out.println("DEBUG newEmission.countryCode= " + newEmission.getCountryCode());

        dao.create(newEmission);
        reloadList();
        newEmission = new CountryEmission();
        FacesContext.getCurrentInstance()
                .addMessage(null,
                        new FacesMessage("Neuer Datensatz angelegt"));
    }

    public void onCountryCodeChange() {
        // Wenn nichts ausgewählt ist, nichts tun
        if (selectedCountryCode == null || selectedCountryCode.isBlank()) {
            return;
        }
        for (CountryEmission c : availableCountries) {
            String code = c.getCountryCode();
            // Nur vergleichen, wenn code nicht null
            if (code != null && code.equals(selectedCountryCode)) {
                // CountryCode setzen
                newEmission.setCountryCode(code);
                // immer das Land setzen
                newEmission.setCountry(c.getCountry());
                break;
            }
        }
    }

    public List<CountryEmission> getAllEmissions() {
        return allEmissions;
    }

    public CountryEmission getNewEmission() {
        return newEmission;
    }

    public List<CountryEmission> getAvailableCountries() {
        return availableCountries;
    }

    public String getSelectedCountryCode() {
        return selectedCountryCode;
    }

    public void setSelectedCountryCode(String code) {
        this.selectedCountryCode = code;
    }

}
