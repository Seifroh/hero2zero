package org.beans;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.hero2zero.dao.EmissionDAO;
import org.hero2zero.entity.CountryEmission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@Named
@ViewScoped
public class EmissionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // DAO
    @Inject
    private EmissionDAO dao;

    // Felder für die UI
    private String vorauswahlHinweis = "";
    private String selectedYear = "";
    private String selectedCountry;
    private String countryListText;
    private boolean showCitizenshipDialog;

    private CountryEmission latestEmission;
    private boolean newerPending;
    private List<SelectItem> countryOptions;

    // Fallback-Liste (DataTable nach „Alle Daten“)
    private List<CountryEmission> allEmissionsApproved;

    // Lifecycle
    @PostConstruct
    public void init() {

        // 1 – Startliste für DataTable (alle freigegebenen)
        allEmissionsApproved = dao.findAllApproved();

        // 2 – Dropdown-Liste füllen (nur Namen)
        countryOptions = new ArrayList<>();
        for (Object[] row : dao.findCountriesWithCode()) {
            String land = (String) row[0];
            countryOptions.add(new SelectItem(land, land));
        }

        // 3 – Länderliste als Text für Anzeige bauen
        StringBuilder sb = new StringBuilder();
        for (SelectItem si : countryOptions) {
            sb.append(si.getLabel()).append(", ");
        }
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }
        countryListText = sb.toString();

        // 4 – Staatsbürgerschaft abfragen, keine Vorauswahl
        selectedCountry = "";
        selectedYear = "";
        vorauswahlHinweis = "";
        showCitizenshipDialog = true;
    }

// Getter für XHTML
    public boolean isShowCitizenshipDialog() {
        return showCitizenshipDialog;
    }

// Auswahl übernehmen
    public void applyCitizenship() {
        loadLatest();
        allEmissionsApproved = dao.findByCountry(selectedCountry);
        showCitizenshipDialog = false;
    }

    // Aktionen / Listener
    public void onCountryChange() {
        loadLatest();
        allEmissionsApproved = dao.findByCountry(selectedCountry);
    }

    public void ladeAlle() {
        allEmissionsApproved = dao.findAllApproved();  // komplette Liste
        selectedCountry = "";
        selectedYear = "";
        vorauswahlHinweis = "";
    }

    // Hilfsmethoden
    private void loadLatest() {
        if (selectedCountry != null && !selectedCountry.isBlank()) {
            latestEmission = dao.findLatestApprovedByCountry(selectedCountry);
            if (latestEmission != null) {
                selectedYear = String.valueOf(latestEmission.getYear());   // <– setzt Jahr
                newerPending = dao.existsNewerPending(selectedCountry, latestEmission.getYear());
            } else {
                selectedYear = "";                                         // <– leert Jahr
                newerPending = false;
            }
        } else {
            latestEmission = null;
            selectedYear = "";                                             // <– leert Jahr
            newerPending = false;
        }
    }

    // Utility (Filterfunktion)
    public boolean co2Filter(Object value, Object filter, Locale locale) {
        if (filter == null || filter.toString().isBlank()) {
            return true;
        }
        if (value == null) {
            return false;
        }
        try {
            double v = ((Number) value).doubleValue();
            double f = Double.parseDouble(filter.toString());
            return Math.abs(v - f) < 0.0001;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Getter / Setter
    public String getVorauswahlHinweis() {
        return vorauswahlHinweis;
    }

    public String getSelectedCountry() {
        return selectedCountry;
    }

    public void setSelectedCountry(String c) {
        this.selectedCountry = c;
    }

    public String getSelectedYear() {
        return selectedYear;
    }

    public void setSelectedYear(String y) {
        this.selectedYear = y;
    }

    public CountryEmission getLatestEmission() {
        return latestEmission;
    }

    public boolean isNewerPending() {
        return newerPending;
    }

    public List<SelectItem> getCountryOptions() {
        return countryOptions;
    }

    public List<CountryEmission> getAllEmissionsApproved() {
        return allEmissionsApproved;
    }

    public String getCountryListText() {
        return countryListText;
    }
}
