package is.hello.piru.bluetooth;

import android.support.annotation.NonNull;
import android.util.Log;

import javax.inject.Inject;
import javax.inject.Singleton;

import is.hello.buruberi.bluetooth.devices.HelloPeripheral.ConnectStatus;
import is.hello.buruberi.bluetooth.devices.SensePeripheral;
import is.hello.buruberi.bluetooth.stacks.BluetoothStack;
import is.hello.piru.api.model.Device;
import rx.Observable;
import rx.subjects.ReplaySubject;

@Singleton public class SensePresenter {
    private final BluetoothStack bluetoothStack;
    private final PendingObservables<Task> pending = new PendingObservables<>();
    private final ReplaySubject<SensePeripheral> peripheral = ReplaySubject.createWithSize(1);

    @Inject public SensePresenter(@NonNull BluetoothStack bluetoothStack) {
        this.bluetoothStack = bluetoothStack;
        peripheral.onNext(null);
    }


    //region Sense Interactions

    private Observable<SensePeripheral> current() {
        return peripheral.take(1);
    }

    private Observable<SensePeripheral> scanForSense(@NonNull Device device, boolean includeHighPowerPreScan) {
        Log.d(getClass().getSimpleName(), "scanForSense(" + device + ", " + includeHighPowerPreScan + ")");

        return pending.bind(Task.SCAN, () -> {
            return current().flatMap(existing -> {
                if (existing != null) {
                    return Observable.just(existing);
                } else {
                    return SensePeripheral.rediscover(bluetoothStack, device.getDeviceId(), includeHighPowerPreScan).doOnNext(sense -> {
                        Log.d(getClass().getSimpleName(), "Found " + sense);
                        peripheral.onNext(sense);
                    }).doOnError(e -> {
                        Log.e(getClass().getSimpleName(), "Could not find Sense", e);
                        peripheral.onNext(null);
                    });
                }
            });
        });
    }

    public Observable<ConnectStatus> connectToSense(@NonNull Device device, boolean includeHighPowerPreScan) {
        Log.d(getClass().getSimpleName(), "connectToSense(" + device + ", " + includeHighPowerPreScan + ")");

        return pending.bind(Task.CONNECT_STATE_CHANGE, () ->
                scanForSense(device, includeHighPowerPreScan).flatMap(SensePeripheral::connect));
    }

    public Observable<Void> beginPillDfu(@NonNull String pillId) {
        Log.d(getClass().getSimpleName(), "beginPillDfu(" + pillId + ")");

        return pending.bind(Task.COMMAND, () -> peripheral.flatMap(s -> s.beginPillDfu(pillId)));
    }

    public Observable<Void> disconnect() {
        return pending.bind(Task.CONNECT_STATE_CHANGE, () -> peripheral.flatMap(sense -> {
            if (sense != null) {
                return sense.disconnect().map(ignored -> null);
            } else {
                return Observable.just(null);
            }
        }));
    }

    //endregion


    private enum Task {
        SCAN,
        CONNECT_STATE_CHANGE,
        COMMAND,
    }
}
