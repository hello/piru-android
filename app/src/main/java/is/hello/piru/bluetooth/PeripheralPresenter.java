package is.hello.piru.bluetooth;

import android.support.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import is.hello.buruberi.bluetooth.stacks.BluetoothStack;

@Singleton public class PeripheralPresenter {
    private final BluetoothStack bluetoothStack;

    @Inject public PeripheralPresenter(@NonNull BluetoothStack bluetoothStack) {
        this.bluetoothStack = bluetoothStack;
    }
}
