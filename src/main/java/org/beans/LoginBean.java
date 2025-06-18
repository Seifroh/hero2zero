package org.beans;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ExternalContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@Named
@RequestScoped
public class LoginBean {

    public void logout() {
        FacesContext faces = FacesContext.getCurrentInstance();
        ExternalContext ec = faces.getExternalContext();
        HttpServletRequest req = (HttpServletRequest) ec.getRequest();

        try {
            req.logout();
        } catch (ServletException ignored) {
        }

        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        try {
            ec.redirect(ec.getRequestContextPath() + "/emissions.xhtml");
        } catch (IOException ignored) {
        }

        faces.responseComplete();
    }

    public boolean isLoggedIn() {
        return FacesContext.getCurrentInstance()
                .getExternalContext()
                .getUserPrincipal() != null;
    }
}
