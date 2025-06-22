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

    @PostConstruct
    public void init() {
        // alle Datensätze, auch unfreigegebene
        allEmissions = dao.listAll();
        newEmission = new CountryEmission();
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
        dao.update(edited);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage("CO₂-Wert aktualisiert",
                        "Neuer Wert: " + edited.getCo2Emissions()));
    }

    public void approve(CountryEmission e) {
        e.setApproved(true);
        dao.update(e);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage("Datensatz freigegeben", "ID: " + e.getId()));
    }

    public void saveNew() {
        // neu angelegte Einträge sind standardmäßig approved=false
        dao.create(newEmission);
        allEmissions = dao.listAll();
        newEmission = new CountryEmission();
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage("Neuer Datensatz angelegt"));
    }
}
