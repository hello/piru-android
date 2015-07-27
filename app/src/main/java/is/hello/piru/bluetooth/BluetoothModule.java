package is.hello.piru.bluetooth;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import is.hello.buruberi.bluetooth.Buruberi;
import is.hello.buruberi.bluetooth.stacks.BluetoothStack;

@Module(complete = false,
        injects = {SensePresenter.class, PillDfuPresenter.class})
public class BluetoothModule {
    @Singleton @Provides BluetoothStack provideBluetoothStack(@NonNull Context context) {
        return new Buruberi()
                .setApplicationContext(context)
                .build();

    }
}
