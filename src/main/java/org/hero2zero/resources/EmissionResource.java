package org.hero2zero.resources;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.hero2zero.dao.EmissionDAO;
import org.hero2zero.entity.CountryEmission;
import java.util.List;

@Path("emissions")
public class EmissionResource {

    @Inject
    private EmissionDAO dao;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<CountryEmission> getAll() {
        return dao.listAll();
    }
}
