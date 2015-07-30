package is.hello.piru.api.services;

import android.support.annotation.NonNull;

import is.hello.piru.api.model.OAuthCredentials;
import is.hello.piru.api.model.OAuthSession;
import retrofit.http.Body;
import retrofit.http.POST;
import rx.Observable;

public interface CoreService {
    String ENDPOINT = "https://api.sense.is";

    @POST("/oauth2/token")
    Observable<OAuthSession> authorize(@NonNull @Body OAuthCredentials request);
}
