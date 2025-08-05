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

    /* ---------- DAO -------------------------------------------------- */
    @Inject
    private EmissionDAO dao;

    /* ---------- Felder für die UI ------------------------------------ */
    private String vorauswahlHinweis = "";
    private String selectedYear = "";
    private String selectedCountry;
    private String countryListText;

    private CountryEmission latestEmission;
    private boolean newerPending;
    private List<SelectItem> countryOptions;

    /* ---------- Fallback-Liste (DataTable nach „Alle Daten“) --------- */
    private List<CountryEmission> allEmissionsApproved;

    /* ================================================================= */
    @PostConstruct
    public void init() {

        /* 1 – Browser-Locale mit Country suchen ------------------------ */
        Locale loc = null;
        Iterator<Locale> locales = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestLocales();

        while (locales.hasNext()) {
            Locale cand = locales.next();          // de, de-DE, en-US …
            if (!cand.getCountry().isBlank()) {           // erstes mit Country
                loc = cand;
                break;
            }
        }
        if (loc == null) {                                // Fallback
            loc = new Locale("de", "DE");
        }

        /* 2 – ISO- und Name ableiten ---------------------------------- */
        String iso2 = loc.getCountry();                                // DE
        String iso3 = new Locale("", iso2).getISO3Country().toUpperCase(); // DEU
        String name = loc.getDisplayCountry(Locale.ENGLISH);           // Germany

        /* 3 – Vorauswahl + Hinweis ------------------------------------ */
        if (!iso3.isBlank() && dao.existsByCountryCode(iso3)) {
            selectedCountry = name;
            vorauswahlHinweis = "Diese Seite hat anhand Ihres Browsers folgendes Land vorausgewählt: "
                    + iso3 + ".";
        } else if (dao.existsByCountry(name)) {
            selectedCountry = name;
            vorauswahlHinweis = "Diese Seite hat anhand Ihres Browsers folgendes Land vorausgewählt: "
                    + name + ".";
        } else {
            selectedCountry = null;
            vorauswahlHinweis = "Keine eindeutige Länderzuordnung erkannt – bitte nutzen Sie die Filterfelder.";
        }


        /* 4 – Neuester freigegebener Datensatz laden ------------------ */
        CountryEmission ce = dao.findLatestApprovedByCountry(selectedCountry);
        if (ce != null) {
            selectedYear = String.valueOf(ce.getYear());
            latestEmission = ce;
        }
        
        /* 5 – Dropdown-Liste füllen ----------------------------------- */
        countryOptions = new ArrayList<>();
        for (Object[] row : dao.findCountriesWithCode()) {
            String land = (String) row[0];
            countryOptions.add(new SelectItem(land, land));
        }

        /* 6 – Startliste für DataTable -------------------------------- */
        allEmissionsApproved = dao.findAllApproved();

        /* 7 – Länderliste als Text für Anzeige bauen */
        StringBuilder sb = new StringBuilder();
        for (SelectItem si : countryOptions) {
            sb.append(si.getLabel()).append(", ");
        }
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2); // letztes Komma entfernen
        }
        countryListText = sb.toString();
    }

    /* ---------- Hilfsmethoden --------------------------------------- */
    private void loadLatest() {
        if (selectedCountry != null) {
            latestEmission = dao.findLatestApprovedByCountry(selectedCountry);
            if (latestEmission != null) {
                newerPending = dao.existsNewerPending(
                        selectedCountry, latestEmission.getYear());
            } else {
                newerPending = false;
            }
        } else {
            latestEmission = null;
            newerPending = false;
        }
    }

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

    /* ---------- Getter für XHTML ------------------------------------ */
    public String getVorauswahlHinweis() {
        return vorauswahlHinweis;
    }

    public String getSelectedCountry() {
        return selectedCountry;
    }

    public void setSelectedCountry(String c) {
        this.selectedCountry = c;
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

    public String getSelectedYear() {
        return selectedYear;
    }

    /* ---------- CO₂-Filter (für Admin-Seite) ------------------------ */
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
}
