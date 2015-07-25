package is.hello.piru.api.model;

import android.support.annotation.NonNull;

import retrofit.Endpoint;

public class SuripuEndpoint implements Endpoint {
    private final String clientId;
    private final String clientSecret;
    private final String url;

    public SuripuEndpoint(@NonNull String clientId,
                          @NonNull String clientSecret,
                          @NonNull String url) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.url = url;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getName() {
        return url;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
