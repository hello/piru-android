package is.hello.piru.api.services;

import android.support.annotation.NonNull;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import is.hello.piru.api.model.FirmwareType;
import is.hello.piru.api.model.FirmwareVersion;
import is.hello.piru.api.model.OAuthCredentials;
import is.hello.piru.api.util.SessionStore;
import rx.Observable;

@Singleton public final class SuripuApi {
    @Inject CoreService coreService;
    @Inject AdminService adminService;
    @Inject
    SessionStore sessionStore;


    //region Sessions

    public boolean hasSession() {
        return sessionStore.hasSession();
    }

    public Observable<Void> authorize(@NonNull String username,
                                      @NonNull String password) {
        OAuthCredentials credentials = new OAuthCredentials(username, password);
        return coreService.authorize(credentials)
                          .map(session -> {
                              sessionStore.storeSession(session);
                              return null;
                          });
    }

    public void clearSession() {
        sessionStore.clearSession();
    }

    //endregion


    //region Firmware Versions

    public Observable<ArrayList<FirmwareVersion>> getStable(@NonNull FirmwareType type) {
        if (!hasSession()) {
            return Observable.error(new IllegalStateException("Cannot access firmware versions without session"));
        }

        return adminService.getStable(type);
    }

    public Observable<ArrayList<FirmwareVersion>> getUnstable(@NonNull FirmwareType type) {
        if (!hasSession()) {
            return Observable.error(new IllegalStateException("Cannot access firmware versions without session"));
        }

        return adminService.getUnstable(type);
    }

    //endregion
}
