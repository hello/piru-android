package is.hello.piru.api;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import is.hello.piru.api.services.AdminService;
import is.hello.piru.api.services.CoreService;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

@Module(complete = false,
        injects = {ApiPresenter.class})
public class ApiModule {
    @Singleton @Provides Gson provideGson() {
        return new GsonBuilder()
                .disableHtmlEscaping()
                .create();
    }

    @Singleton @Provides SuripuEndpoints provideSuripuEndpoints() {
        return new SuripuEndpoints();
    }

    @Singleton @Provides
    SessionStore provideSession(@NonNull Context context) {
        return new SessionStore(context);
    }

    @Provides RestAdapter.Builder provideConfiguredRestAdapterBuilder(@NonNull Gson gson,
                                                                      @NonNull SessionStore sessionStore) {
        return new RestAdapter.Builder()
                .setRequestInterceptor(request -> {
                    String authorizationHeader = sessionStore.getAuthorizationHeader();
                    if (authorizationHeader != null) {
                        request.addHeader("Authorization", authorizationHeader);
                    }
                })
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson));
    }

    @Singleton @Provides AdminService provideAdminService(@NonNull RestAdapter.Builder builder,
                                                          @NonNull SuripuEndpoints endpoints) {
        return builder.setEndpoint(endpoints.getAdminEndpoint())
                      .build()
                      .create(AdminService.class);
    }

    @Singleton @Provides CoreService provideCoreService(@NonNull RestAdapter.Builder builder,
                                                        @NonNull SuripuEndpoints endpoints) {
        return builder.setEndpoint(endpoints.getCoreEndpoint())
                      .build()
                      .create(CoreService.class);
    }
}
