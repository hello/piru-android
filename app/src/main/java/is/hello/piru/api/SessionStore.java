package is.hello.piru.api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import is.hello.piru.api.model.OAuthSession;

public class SessionStore {
    public static final String ACTION_SESSION_CHANGED = SessionStore.class.getName() + ".ACTION_SESSION_CHANGED";

    private static final String SUITE_NAME = "oauth_session";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_ACCOUNT_ID = "account_id";

    private final LocalBroadcastManager broadcastManager;
    private final SharedPreferences store;

    public SessionStore(@NonNull Context context) {
        this.broadcastManager = LocalBroadcastManager.getInstance(context);
        this.store = context.getSharedPreferences(SUITE_NAME, 0);
    }

    public void storeSession(@NonNull OAuthSession session) {
        store.edit()
             .putString(KEY_ACCESS_TOKEN, session.accessToken)
             .putString(KEY_ACCOUNT_ID, session.accountId)
             .apply();

        broadcastManager.sendBroadcast(new Intent(ACTION_SESSION_CHANGED));
    }

    public boolean hasSession() {
        return store.contains(KEY_ACCESS_TOKEN);
    }

    public @Nullable String getAuthorizationHeader() {
        return store.getString(KEY_ACCESS_TOKEN, null);
    }

    public @Nullable String getAccountId() {
        return store.getString(KEY_ACCOUNT_ID, null);
    }

    public void clearSession() {
        store.edit()
             .clear()
             .apply();

        broadcastManager.sendBroadcast(new Intent(ACTION_SESSION_CHANGED));
    }
}
