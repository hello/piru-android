package is.hello.piru.api;

import android.support.annotation.NonNull;

import java.util.ArrayList;

import is.hello.piru.api.model.Device;
import is.hello.piru.api.session.OAuthCredentials;
import is.hello.piru.api.session.OAuthSession;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import rx.Observable;

public interface SuripuApi {
    String DATE_FORMAT = "yyyy-MM-dd";
    String TIME_FORMAT = "HH:mm";


    //region OAuth

    @POST("/v1/oauth2/token")
    Observable<OAuthSession> authorize(@NonNull @Body OAuthCredentials request);

    //endregion


    //region Devices

    @GET("/v1/devices")
    Observable<ArrayList<Device>> registeredDevices();

    @DELETE("/v1/devices/pill/{id}")
    Observable<Void> unregisterPill(@Path("id") @NonNull String pillId);

    @DELETE("/v1/devices/sense/{id}")
    Observable<Void> unregisterSense(@Path("id") @NonNull String senseId);

    @DELETE("/v1/devices/sense/{id}/all")
    Observable<Void> removeSenseAssociations(@Path("id") @NonNull String senseId);

    //endregion
}
