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
import java.util.ArrayList;

@Named
@ViewScoped
public class EmissionBean implements Serializable {

    @Inject
    private EmissionDAO dao;

    private String filterCountry;
    private String filterCode;
    private String filterYear;
    private String filterCo2;
    private List<String> selectedContinents = new ArrayList<>();

    private List<CountryEmission> allEmissions;
    private List<CountryEmission> filteredEmissions;

    private int currentPage = 0;
    private final int pageSize = 20;

    @PostConstruct
    public void init() {
        allEmissions = dao.findAll();
        filter();
    }

    public void filter() {
        List<CountryEmission> base = allEmissions;

        // Filter nach Kontinenten (wenn mindestens einer ausgewählt ist)
        if (!selectedContinents.isEmpty()) {
            base = base.stream()
                    .filter(e -> selectedContinents.contains(e.getContinent()))
                    .collect(Collectors.toList());
        }

        // Weitere Filter (Land, Code, Jahr, CO2)
        if (filterCountry != null && !filterCountry.isEmpty()) {
            base = base.stream()
                    .filter(e -> e.getCountry().toLowerCase().contains(filterCountry.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (filterCode != null && !filterCode.isEmpty()) {
            base = base.stream()
                    .filter(e -> e.getCountryCode().toLowerCase().contains(filterCode.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (filterYear != null && !filterYear.isEmpty()) {
            try {
                int year = Integer.parseInt(filterYear);
                base = base.stream()
                        .filter(e -> e.getYear() == year)
                        .collect(Collectors.toList());
            } catch (NumberFormatException ignored) {
            }
        }

        if (filterCo2 != null && !filterCo2.isEmpty()) {
            try {
                double value = Double.parseDouble(filterCo2.replace(",", "."));
                base = base.stream()
                        .filter(e -> Math.abs(e.getCo2Emissions() - value) < 0.001)
                        .collect(Collectors.toList());
            } catch (NumberFormatException ignored) {
            }
        }

        filteredEmissions = base;
        currentPage = 0;
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
        int total = (filteredEmissions != null) ? filteredEmissions.size() : allEmissions.size();
        return (int) Math.ceil((double) total / pageSize);
    }

    public int getTotalEntries() {
        return (filteredEmissions != null) ? filteredEmissions.size() : allEmissions.size();
    }

    public int getCurrentPageDisplay() {
        return currentPage + 1;
    }

    // Getter und Setter
    public List<String> getSelectedContinents() {
        return selectedContinents;
    }

    public void setSelectedContinents(List<String> selectedContinents) {
        this.selectedContinents = selectedContinents;
        filter(); // bei Änderung neu filtern
    }

    public String getFilterCountry() {
        return filterCountry;
    }

    public void setFilterCountry(String filterCountry) {
        this.filterCountry = filterCountry;
    }

    public String getFilterCode() {
        return filterCode;
    }

    public void setFilterCode(String filterCode) {
        this.filterCode = filterCode;
    }

    public String getFilterYear() {
        return filterYear;
    }

    public void setFilterYear(String filterYear) {
        this.filterYear = filterYear;
    }

    public String getFilterCo2() {
        return filterCo2;
    }

    public void setFilterCo2(String filterCo2) {
        this.filterCo2 = filterCo2;
    }

    public List<CountryEmission> getFilteredEmissions() {
        return getPageData();
    }

    public void toggleContinent(String continent) {
        if (selectedContinents.contains(continent)) {
            selectedContinents.remove(continent);
        } else {
            selectedContinents.add(continent);
        }
        filter();
    }

    public void clearContinents() {
        selectedContinents.clear();
        filter();
    }

    public boolean isFilterActive() {
        return (selectedContinents != null && !selectedContinents.isEmpty())
                || (filterCountry != null && !filterCountry.isEmpty())
                || (filterCode != null && !filterCode.isEmpty())
                || (filterYear != null && !filterYear.isEmpty())
                || (filterCo2 != null && !filterCo2.isEmpty());
    }

    public boolean isDataAvailable() {
        return isFilterActive() && !getPageData().isEmpty();
    }

}
