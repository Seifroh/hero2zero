package org.hero2zero.minimal;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named("hello")
@RequestScoped
public class HelloBean {
    public String getMessage() {
        return "JSF läuft auf Payara 6!";
    }
}
