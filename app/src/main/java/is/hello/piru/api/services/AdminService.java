package is.hello.piru.api.services;

import android.support.annotation.NonNull;

import java.util.List;

import is.hello.piru.api.model.FirmwareType;
import is.hello.piru.api.model.FirmwareVersion;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

public interface AdminService {
    String ENDPOINT = "https://admin-api.sense.is";

    @GET("/v1/download/{type}/firmware/stable")
    Observable<List<FirmwareVersion>> getStable(@NonNull @Path("type") FirmwareType type);

    @GET("/v1/download/{type}/firmware")
    Observable<List<FirmwareVersion>> getUnstable(@NonNull @Path("type") FirmwareType type);

}
