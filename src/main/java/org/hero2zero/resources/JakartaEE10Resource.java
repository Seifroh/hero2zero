package org.hero2zero.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("hello")
public class JakartaEE10Resource {

    @GET
    @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
    public String hello() {
        return "REST läuft auf Payara 6!";
    }
}
