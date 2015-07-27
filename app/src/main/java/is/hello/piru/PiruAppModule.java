package is.hello.piru;

import android.content.Context;
import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;
import is.hello.piru.api.ApiModule;
import is.hello.piru.bluetooth.BluetoothModule;
import is.hello.piru.ui.screens.DfuProgressFragment;
import is.hello.piru.ui.screens.SelectPillFragment;
import is.hello.piru.ui.screens.SenseFragment;
import is.hello.piru.ui.screens.SessionFragment;

@Module(includes = {ApiModule.class, BluetoothModule.class},
        injects = {
                SessionFragment.class,
                SenseFragment.class,
                SelectPillFragment.class,
                DfuProgressFragment.class,
        })
public class PiruAppModule {
    private final Context applicationContext;

    public PiruAppModule(@NonNull Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Provides Context provideApplicationContext() {
        return applicationContext;
    }
}
