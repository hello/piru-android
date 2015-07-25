package is.hello.piru.api;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import is.hello.piru.api.model.SuripuEndpoint;
import is.hello.piru.api.session.SessionManager;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

@Module(complete = false,
        injects = {SessionPresenter.class})
public class ApiModule {
    @Singleton @Provides Gson provideGson() {
        return new GsonBuilder()
                .setDateFormat(SuripuApi.DATE_FORMAT)
                .disableHtmlEscaping()
                .create();
    }

    @Singleton @Provides SuripuEndpoint provideEndpoint() {
        return new SuripuEndpoint("8d3c1664-05ae-47e4-bcdb-477489590aa4",
                "4f771f6f-5c10-4104-bbc6-3333f5b11bf9", "https://api.hello.is");
    }

    @Singleton @Provides SessionManager provideSessionManager(@NonNull Context context,
                                                              @NonNull Gson gson) {
        return new SessionManager(context, gson);
    }

    @Singleton @Provides RestAdapter provideRestAdapter(@NonNull SuripuEndpoint endpoint,
                                                        @NonNull Gson gson,
                                                        @NonNull SessionManager sessionManager) {
        return new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setConverter(new GsonConverter(gson))
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setRequestInterceptor(sessionManager)
                .build();
    }

    @Singleton @Provides SuripuApi provideSuripuApi(@NonNull RestAdapter adapter) {
        return adapter.create(SuripuApi.class);
    }
}
