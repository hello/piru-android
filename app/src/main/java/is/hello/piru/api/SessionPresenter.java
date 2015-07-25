package is.hello.piru.api;

import android.support.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import is.hello.piru.api.model.SuripuEndpoint;
import is.hello.piru.api.session.OAuthCredentials;
import is.hello.piru.api.session.OAuthSession;
import is.hello.piru.api.session.SessionManager;
import rx.Observable;
import rx.subjects.ReplaySubject;

@Singleton public class SessionPresenter {
    private final SuripuEndpoint endpoint;
    private final SessionManager sessionManager;
    private final SuripuApi suripuApi;

    public final ReplaySubject<Boolean> hasSession = ReplaySubject.createWithSize(1);

    @Inject public SessionPresenter(@NonNull SuripuEndpoint endpoint,
                                    @NonNull SessionManager sessionManager,
                                    @NonNull SuripuApi suripuApi) {
        this.endpoint = endpoint;
        this.sessionManager = sessionManager;
        this.suripuApi = suripuApi;

        hasSession.onNext(sessionManager.hasSession());
    }

    public SuripuEndpoint getEndpoint() {
        return endpoint;
    }

    public Observable<OAuthSession> authorize(@NonNull String username, @NonNull String password) {
        OAuthCredentials credentials = new OAuthCredentials(endpoint, username, password);
        return suripuApi.authorize(credentials)
                        .doOnNext(sessionManager::setSession)
                        .doOnCompleted(() -> hasSession.onNext(true));
    }

    public void clearSession() {
        sessionManager.logOut();
        hasSession.onNext(false);
    }
}
