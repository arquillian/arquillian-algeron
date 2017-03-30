package org.arquillian.algeron.pact.provider.skip;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Lock(LockType.READ)
@Singleton
@Path("/")
public class MyService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getMessage() {
        return "{\"responsetest\": true, \"nom\": \"harry\"}";
    }
}
