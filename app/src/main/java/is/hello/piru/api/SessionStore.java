package is.hello.piru.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import is.hello.piru.api.model.OAuthSession;

public class SessionStore {
    private static final String SUITE_NAME = "oauth_session";

    private final SharedPreferences store;

    public SessionStore(@NonNull Context context) {
        this.store = context.getSharedPreferences(SUITE_NAME, 0);
    }

    public void storeSession(@NonNull OAuthSession session) {
        store.edit()
             .putString("access_token", session.accessToken)
             .putString("account_id", session.accountId)
             .putString("refresh_token", session.refreshToken)
             .putString("token_type", session.tokenType)
             .putLong("expires_in", session.expiresIn)
             .apply();
    }

    public @Nullable String getAuthorizationHeader() {
        return store.getString("access_token", null);
    }
}
