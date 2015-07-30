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
import is.hello.piru.api.services.SuripuApi;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

@Module(complete = false,
        injects = {SuripuApi.class})
public class ApiModule {
    @Singleton @Provides Gson provideGson() {
        return new GsonBuilder()
                .disableHtmlEscaping()
                .create();
    }

    @Singleton @Provides SessionStore provideSession(@NonNull Context context) {
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

    @Singleton @Provides AdminService provideAdminService(@NonNull RestAdapter.Builder builder) {
        return builder.setEndpoint(AdminService.ENDPOINT)
                      .build()
                      .create(AdminService.class);
    }

    @Singleton @Provides CoreService provideCoreService(@NonNull RestAdapter.Builder builder) {
        return builder.setEndpoint(CoreService.ENDPOINT)
                      .build()
                      .create(CoreService.class);
    }
}
