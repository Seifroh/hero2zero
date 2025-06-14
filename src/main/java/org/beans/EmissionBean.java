package org.beans;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import org.hero2zero.dao.EmissionDAO;
import org.hero2zero.entity.CountryEmission;
import java.io.Serializable;

import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class EmissionBean implements Serializable {

    @Inject
    private EmissionDAO dao;

    private String selectedContinent;
    private List<CountryEmission> allEmissions;
    private List<CountryEmission> filteredEmissions;

    private int currentPage = 0;
    private final int pageSize = 20;

    @PostConstruct
    public void init() {
        allEmissions = dao.findAll();
        filter(); // Startfilterung (leer)
    }

    public void filter() {
        currentPage = 0; // Zurücksetzen bei Filterwechsel
        if (selectedContinent == null || selectedContinent.isEmpty()) {
            filteredEmissions = allEmissions;
        } else {
            filteredEmissions = allEmissions.stream()
                    .filter(e -> selectedContinent.equals(e.getContinent()))
                    .collect(Collectors.toList());
        }
    }

    public List<CountryEmission> getPageData() {
        List<CountryEmission> list = (filteredEmissions != null) ? filteredEmissions : allEmissions;
        int from = currentPage * pageSize;
        int to = Math.min(from + pageSize, list.size());
        return list.subList(from, to);
    }

    public void nextPage() {
        if (isHasNext()) {
            currentPage++;
        }
    }

    public void prevPage() {
        if (isHasPrev()) {
            currentPage--;
        }
    }

    public boolean isHasNext() {
        int total = (filteredEmissions != null ? filteredEmissions.size() : allEmissions.size());
        return (currentPage + 1) * pageSize < total;
    }

    public boolean isHasPrev() {
        return currentPage > 0;
    }

    public int getTotalPages() {
        int total = (filteredEmissions != null) ? filteredEmissions.size() : dao.listAll().size();
        return (int) Math.ceil((double) total / pageSize);
    }

    public int getTotalEntries() {
        return (filteredEmissions != null) ? filteredEmissions.size() : dao.listAll().size();
    }

    public int getCurrentPageDisplay() {
        return currentPage + 1;
    }

    // Getter und Setter
    public List<CountryEmission> getFilteredEmissions() {
        return getPageData(); // Gibt nur den aktuellen Ausschnitt zurück
    }

    public String getSelectedContinent() {
        return selectedContinent;
    }

    public void setSelectedContinent(String selectedContinent) {
        this.selectedContinent = selectedContinent;
        filter();
    }
}
