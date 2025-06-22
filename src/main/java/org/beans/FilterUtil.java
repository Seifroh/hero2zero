package org.beans;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.util.Locale;

/**
 * Utility-Bean für gemeinsame Filter-Funktionen.
 */
@Named("filterUtil")
@ApplicationScoped
public class FilterUtil {

    /**
     * Custom-Filter für CO₂-Werte, vergleicht numerisch (z. B. 0 == 0.0).
     *
     * @param value Datenbankwert (Number oder null)
     * @param filter Eingabewert (String)
     * @param locale Lokal wird hier nicht genutzt
     * @return true, wenn kein Filter oder numerisch gleich innerhalb Toleranz
     */
    public boolean co2Filter(Object value, Object filter, Locale locale) {
        if (filter == null || filter.toString().isBlank()) {
            return true;
        }
        if (value == null) {
            return false;
        }
        try {
            double dbValue = ((Number) value).doubleValue();
            double filterValue = Double.parseDouble(filter.toString());
            return Math.abs(dbValue - filterValue) < 0.0001;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
