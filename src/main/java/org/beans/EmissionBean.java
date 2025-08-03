package org.beans;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.hero2zero.dao.EmissionDAO;
import org.hero2zero.entity.CountryEmission;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@Named
@ViewScoped
public class EmissionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EmissionDAO dao;

    private List<CountryEmission> allEmissionsApproved;

    private String vorauswahlHinweis = "";
    private boolean alleLaender = false;   // ← neu

    @PostConstruct
    public void init() {

        Locale loc = null;
        for (Iterator<Locale> it = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestLocales(); it.hasNext();) {
            Locale candidate = it.next();          // z B. de-DE, de
            if (!candidate.getCountry().isBlank()) {
                loc = candidate;                   // erstes mit Country nehmen
                break;
            }
        }
        if (loc == null) {                         // Fallback, falls nur „de“
            loc = new Locale("de", "DE");
        }

        String iso2 = loc.getCountry();                           // DE
        String iso3 = new Locale("", iso2).getISO3Country();      // DEU
        String name = loc.getDisplayCountry(Locale.ENGLISH);      // Germany

        if (!iso3.isBlank() && dao.existsByCountryCode(iso3)) {
            allEmissionsApproved = dao.findByCountryCode(iso3);
            vorauswahlHinweis = "Diese Seite hat anhand Ihres Browsers folgendes Land vorausgewählt: "
                    + iso3;
        } else if (dao.existsByCountry(name)) {
            allEmissionsApproved = dao.findByCountry(name);
            vorauswahlHinweis = "Diese Seite hat anhand Ihres Browsers folgendes Land vorausgewählt: "
                    + name;
            alleLaender = false;                           // Button sichtbar
        } else {
            allEmissionsApproved = dao.findAllApproved();
            vorauswahlHinweis = "Keine eindeutige Länderzuordnung erkannt – "
                    + "bitte nutzen Sie die Filterfelder.";
            alleLaender = true;                            // Button AUS-geblendet
        }
    }

    public List<CountryEmission> getAllEmissionsApproved() {
        return allEmissionsApproved;
    }

    public void ladeAlle() {
        allEmissionsApproved = dao.findAllApproved();
        vorauswahlHinweis = "";
        alleLaender = true;
    }

    public boolean isAlleLaender() {
        return alleLaender;
    }

    public String getVorauswahlHinweis() {
        return vorauswahlHinweis;
    }
}
