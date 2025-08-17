package org.beans;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.hero2zero.dao.EmissionDAO;
import org.hero2zero.entity.CountryEmission;
import org.primefaces.event.CellEditEvent;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Named
@ViewScoped
public class EmissionsAdminBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // Konstanten
    private static final String ROLE_ADMIN = "admin";
    private static final String NC_PREFIX = "NC:";
    private static final int MIN_YEAR = 1750;
    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");

    // Injections
    @Inject
    private EmissionDAO dao;

    // Zustand
    private List<CountryEmission> allEmissions;
    private CountryEmission newEmission;
    private List<CountryEmission> pendingEmissions;
    private String selectedCountryCode;
    private List<SelectItem> availableCountries;
    private Map<String, String> countryByCode;

    // Lifecycle
    @PostConstruct
    public void init() {
        refresh();
        newEmission = new CountryEmission();

        availableCountries = new ArrayList<>();
        countryByCode = new LinkedHashMap<>();
        Set<String> seen = new LinkedHashSet<>();

        for (Object[] row : dao.findAllCountries()) {
            String land = row[0] != null ? row[0].toString().trim() : "";
            String code = row[1] != null ? row[1].toString().trim() : "";
            if (land.isEmpty()) {
                continue;
            }

            String nameKey = land.toUpperCase();
            if (!seen.add(nameKey)) {
                continue; // jedes Land nur einmal
            }
            String valueKey;
            String label;
            if (!code.isEmpty()) {
                valueKey = code;                 // Auswahlwert = Code
                label = land + " (" + code + ")";
            } else {
                valueKey = NC_PREFIX + land;     // NC = no code
                label = land;
            }

            countryByCode.put(valueKey, land);   // Schlüssel -> Land
            availableCountries.add(new SelectItem(valueKey, label));
        }
    }

    // Aktionen
    public void saveNew() {
        String err = validateYearRule(newEmission.getYear());
        if (err != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Jahr muss abgeschlossen sein", err));
            return;
        }

        onCountryCodeChange();

        if (dao.existsByCountryCodeAndYear(newEmission.getCountryCode(), newEmission.getYear())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Eintrag existiert bereits!",
                            "Für dieses Land und Jahr gibt es schon einen Datensatz."));
            return;
        }

        try {
            dao.create(newEmission);

            String user = FacesContext.getCurrentInstance()
                    .getExternalContext().getUserPrincipal().getName();
            String entry = LocalDate.now() + " durch " + user + " neu angelegt.";
            newEmission.setChangeLog(entry);

            dao.update(newEmission);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Neuer Datensatz angelegt"));
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Anlage fehlgeschlagen", ex.getMessage()));
            return;
        }

        refresh();
        newEmission = new CountryEmission();
    }

    public void approve(CountryEmission e) {
        e.setApproved(true);
        dao.update(e);
        refresh();
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage("Datensatz freigegeben", "ID: " + e.getId()));
    }

    public void delete(CountryEmission e) {
        dao.delete(e);
        refresh();
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage("Datensatz gelöscht, ID: " + e.getId()));
    }

    // Listener
    public void onCellEdit(CellEditEvent event) {
        int row = event.getRowIndex();
        CountryEmission edited = allEmissions.get(row);

        Object newValue = event.getNewValue();
        if (newValue != null && !newValue.equals(event.getOldValue())) {
            edited.setCo2Emissions((Double) newValue);
            edited.setApproved(false);
            dao.update(edited);
            refresh();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Geändert und Liste neu geladen"));
        }
    }

    public void onCountryCodeChange() {
        if (selectedCountryCode == null || selectedCountryCode.isBlank()) {
            return;
        }

        String key = selectedCountryCode.trim();
        String land = countryByCode.get(key);

        if (key.startsWith(NC_PREFIX)) {
            String countryOnly = key.substring(NC_PREFIX.length()).trim();
            newEmission.setCountry(land != null ? land : countryOnly);
            newEmission.setCountryCode(null);
        } else {
            newEmission.setCountry(land);
            newEmission.setCountryCode(key);
        }
    }

    // Interne Helfer
    private void refresh() {
        reloadList();
        pendingEmissions = dao.findAllPending();
    }

    private void reloadList() {
        boolean isAdmin = FacesContext.getCurrentInstance()
                .getExternalContext()
                .isUserInRole(ROLE_ADMIN);
        allEmissions = isAdmin ? dao.listAll() : dao.findAllApproved();
    }

    private int closedYearMax() {
        return LocalDate.now(ZONE).minusYears(1).getYear();
    }

    private String validateYearRule(int y) {
        if (y < MIN_YEAR) {
            return "Minimal " + MIN_YEAR + ".";
        }
        int max = closedYearMax();
        if (y > max) {
            return "Maximal " + max + ".";
        }
        return null;
    }

    // Validator
    public void validateClosedYear(FacesContext ctx, UIComponent c, Object value) {
        if (value == null) {
            return;
        }
        int y = Integer.parseInt(value.toString());
        String err = validateYearRule(y);
        if (err != null) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Jahr muss abgeschlossen sein", err));
        }
    }

    // Getter / Setter
    public List<CountryEmission> getAllEmissions() {
        return allEmissions;
    }

    public CountryEmission getNewEmission() {
        return newEmission;
    }

    public List<CountryEmission> getPendingEmissions() {
        return pendingEmissions;
    }

    public int getPendingCount() {
        return pendingEmissions == null ? 0 : pendingEmissions.size();
    }

    public List<SelectItem> getAvailableCountries() {
        return availableCountries;
    }

    public String getSelectedCountryCode() {
        return selectedCountryCode;
    }

    public void setSelectedCountryCode(String code) {
        this.selectedCountryCode = code;
    }

    public int getYearMax() {
        return closedYearMax();
    }
}
