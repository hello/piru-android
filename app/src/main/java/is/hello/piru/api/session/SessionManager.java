package is.hello.piru.api.session;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import retrofit.RequestInterceptor;

public class SessionManager implements RequestInterceptor {
    public static final String ACTION_SESSION_INVALIDATED = SessionManager.class.getName() + ".ACTION_SESSION_INVALIDATED";
    public static final String ACTION_LOGGED_OUT = SessionManager.class.getName() + ".ACTION_LOGGED_OUT";

    private static final String SHARED_PREFERENCES_NAME = "oauth_session";
    private static final String SESSION_KEY = "session";

    private final Context context;

    private final SharedPreferences preferences;
    private final Gson gson;

    public SessionManager(@NonNull Context context, @NonNull Gson gson) {
        this.context = context;

        this.preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
        this.gson = gson;

        BroadcastReceiver receiver = new BroadcastReceiver() {
            private long timeOfLastReception = 0;

            @Override
            public void onReceive(Context context, Intent intent) {
                if ((System.currentTimeMillis() - timeOfLastReception) > 1000) {
                    onSessionInvalidated();
                    this.timeOfLastReception = System.currentTimeMillis();
                }
            }
        };
        LocalBroadcastManager.getInstance(context)
                             .registerReceiver(receiver, new IntentFilter(ACTION_SESSION_INVALIDATED));
    }


    //region Accessors

    public final void setSession(@Nullable OAuthSession session) {
        SharedPreferences.Editor editor = preferences.edit();
        if (session != null) {
            try {
                String serializedValue = gson.toJson(session);
                editor.putString(SESSION_KEY, serializedValue);
            } catch (JsonSyntaxException e) {
                throw new RuntimeException(e);
            }
        } else {
            editor.remove(SESSION_KEY);
        }
        editor.apply();
    }

    public final @Nullable OAuthSession getSession() {
        if (preferences.contains(SESSION_KEY)) {
            String serializedValue = preferences.getString(SESSION_KEY, null);
            try {
                return gson.fromJson(serializedValue, OAuthSession.class);
            } catch (JsonSyntaxException e) {
                Log.e(getClass().getSimpleName(), "Could not deserialize persisted session", e);
            }
        }

        return null;
    }

    public boolean hasSession() {
        return preferences.contains(SESSION_KEY);
    }

    public @Nullable String getAccessToken() {
        OAuthSession session = getSession();
        if (session != null) {
            return session.getAccessToken();
        } else {
            return null;
        }
    }

    //endregion


    @Override
    public void intercept(RequestFacade request) {
        String accessToken = getAccessToken();
        if (accessToken != null) {
            request.addHeader("Authorization", "Bearer " + accessToken);
        }
    }


    protected void onSessionInvalidated() {
        Log.w(getClass().getSimpleName(), "Session invalidated, logging out.");
        logOut();
    }

    public void logOut() {
        setSession(null);
        LocalBroadcastManager.getInstance(context)
                             .sendBroadcast(new Intent(ACTION_LOGGED_OUT));
    }
}
