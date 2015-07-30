package is.hello.piru.api;

import retrofit.Endpoint;
import retrofit.Endpoints;

public class SuripuEndpoints {
    public String getClientId() {
        return null;
    }

    public String getClientSecret() {
        return null;
    }

    public Endpoint getAdminEndpoint() {
        return Endpoints.newFixedEndpoint("https://admin.sense.is", "Admin");
    }

    public Endpoint getCoreEndpoint() {
        return Endpoints.newFixedEndpoint("https://api.sense.is", "Core");
    }
}
