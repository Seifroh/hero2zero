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
import java.util.Arrays;

@Named
@ViewScoped
public class EmissionsAdminBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EmissionDAO dao;

    private List<CountryEmission> allEmissions;
    private CountryEmission newEmission;
    private List<CountryEmission> availableCountries;

    @PostConstruct
    public void init() {
        // alle Datensätze, auch unfreigegebene
        allEmissions = dao.listAll();
        newEmission = new CountryEmission();
        availableCountries = dao.findDistinctCountries();
    }

    public List<CountryEmission> getAllEmissions() {
        return allEmissions;
    }

    public CountryEmission getNewEmission() {
        return newEmission;
    }

    public void onCellEdit(CellEditEvent event) {
        int row = event.getRowIndex();
        CountryEmission edited = allEmissions.get(row);

        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();

        // Falls Wert wirklich geändert wurde
        if (newValue != null && !newValue.equals(oldValue)) {
            String username = FacesContext.getCurrentInstance()
                    .getExternalContext().getUserPrincipal().getName();

            String logEntry = String.format(
                    "%s %s: %s → %s; ",
                    java.time.LocalDate.now(),
                    username,
                    oldValue,
                    newValue
            );

            String prevLog = edited.getChangeLog();
            if (prevLog != null) {
                // z.B. nur die letzten 2 Einträge behalten
                String[] parts = prevLog.split(";");
                prevLog = (parts.length > 2)
                        ? String.join(";", Arrays.copyOfRange(parts, parts.length - 2, parts.length)) + ";"
                        : prevLog;
            } else {
                prevLog = "";
            }

            edited.setChangeLog(prevLog + logEntry);

            edited.setApproved(false);
            dao.update(edited);
            allEmissions = dao.listAll();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("CO₂-Wert geändert, Änderung protokolliert, Freigabe zurückgesetzt."));
        }
    }

    public void approve(CountryEmission e) {
        e.setApproved(true);
        dao.update(e);
        allEmissions = dao.listAll();

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage("Datensatz freigegeben", "ID: " + e.getId()));
    }

    public void saveNew() {
        // Dublettenprüfung
        if (dao.existsByCountryCodeAndYear(newEmission.getCountryCode(), newEmission.getYear())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Eintrag existiert bereits!",
                            "Für dieses Land und Jahr gibt es schon einen Datensatz."));
            return; // Speichern abbrechen
        }

        // neu angelegte Einträge sind standardmäßig approved=false
        dao.create(newEmission);
        allEmissions = dao.listAll();
        newEmission = new CountryEmission();
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage("Neuer Datensatz angelegt"));
    }

    public List<CountryEmission> getAvailableCountries() {
        return availableCountries;
    }

}
