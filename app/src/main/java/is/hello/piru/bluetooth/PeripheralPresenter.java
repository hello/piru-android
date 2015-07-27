package is.hello.piru.bluetooth;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import is.hello.buruberi.bluetooth.devices.HelloPeripheral.ConnectStatus;
import is.hello.buruberi.bluetooth.devices.SensePeripheral;
import is.hello.buruberi.bluetooth.errors.PeripheralNotFoundError;
import is.hello.buruberi.bluetooth.stacks.BluetoothStack;
import is.hello.buruberi.bluetooth.stacks.Peripheral;
import is.hello.buruberi.bluetooth.stacks.util.PeripheralCriteria;
import is.hello.piru.api.model.Device;
import rx.Observable;

@Singleton public class PeripheralPresenter {
    @Inject Context applicationContext;

    private final BluetoothStack bluetoothStack;
    private final PendingObservables<Token> pending = new PendingObservables<>();
    private boolean hasSense = false;
    private Observable<SensePeripheral> currentSense;

    @Inject public PeripheralPresenter(@NonNull BluetoothStack bluetoothStack) {
        this.bluetoothStack = bluetoothStack;
        this.currentSense = Observable.error(new PeripheralNotFoundError());
    }


    //region Sense Interactions

    private Observable<SensePeripheral> scanForSense(@NonNull Device device, boolean includeHighPowerPreScan) {
        Log.d(getClass().getSimpleName(), "scanForSense(" + device + ", " + includeHighPowerPreScan + ")");

        if (hasSense) {
            return currentSense;
        }

        return pending.bind(Token.SCAN_FOR_SENSE, () -> SensePeripheral.rediscover(bluetoothStack,
                device.getDeviceId(), includeHighPowerPreScan))
                      .doOnNext(sense -> {
                          Log.d(getClass().getSimpleName(), "Found " + sense);
                          this.currentSense = Observable.just(sense);
                          this.hasSense = true;
                      })
                      .doOnError(e -> {
                          Log.e(getClass().getSimpleName(), "Could not find Sense", e);
                          this.currentSense = Observable.error(e);
                          this.hasSense = false;
                      });
    }

    public Observable<ConnectStatus> connectToSense(@NonNull Device device, boolean includeHighPowerPreScan) {
        Log.d(getClass().getSimpleName(), "connectToSense(" + device + ", " + includeHighPowerPreScan + ")");

        return pending.bind(Token.CONNECT_TO_SENSE, () ->
                scanForSense(device, includeHighPowerPreScan).flatMap(SensePeripheral::connect));
    }

    public Observable<Void> beginPillDfu(@NonNull String pillId) {
        Log.d(getClass().getSimpleName(), "beginPillDfu(" + pillId + ")");

        return pending.bind(Token.BEGIN_PILL_DFU, () -> currentSense.flatMap(s -> s.beginPillDfu(pillId)));
    }

    //endregion


    //region Pill Interactions

    public Observable<List<Peripheral>> scanForPills(boolean includeHighPowerPreScan) {
        PeripheralCriteria criteria = new PeripheralCriteria();
        criteria.setWantsHighPowerPreScan(includeHighPowerPreScan);
        return bluetoothStack.discoverPeripherals(criteria);
    }

    public Intent createStartDfuIntent(@NonNull Peripheral peripheral) {
        Intent intent = new Intent(applicationContext, DfuService.class);
        intent.putExtra(DfuService.EXTRA_DEVICE_NAME, peripheral.getName());
        intent.putExtra(DfuService.EXTRA_DEVICE_ADDRESS, peripheral.getAddress());
        intent.putExtra(DfuService.EXTRA_KEEP_BOND, false);
        return intent;
    }

    //endregion


    private enum Token {
        SCAN_FOR_SENSE,
        CONNECT_TO_SENSE,
        BEGIN_PILL_DFU,
    }
}
