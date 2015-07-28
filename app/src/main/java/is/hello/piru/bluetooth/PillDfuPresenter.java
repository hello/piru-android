package is.hello.piru.bluetooth;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Pair;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import is.hello.buruberi.bluetooth.errors.BluetoothGattError;
import is.hello.buruberi.bluetooth.errors.PeripheralNotFoundError;
import is.hello.buruberi.bluetooth.stacks.BluetoothStack;
import is.hello.buruberi.bluetooth.stacks.util.PeripheralCriteria;
import is.hello.buruberi.util.Rx;
import is.hello.piru.ui.util.FileUtils;
import is.hello.piru.ui.util.PresenterSubject;
import rx.Observable;

@Singleton public class PillDfuPresenter {
    private final Context context;
    private final BluetoothStack bluetoothStack;
    private PendingObservables<String> pending = new PendingObservables<>();

    public final PresenterSubject<List<PillPeripheral>> sleepPills = PresenterSubject.create();
    private boolean hasScanned = false;
    private boolean scanning = false;
    private @Nullable PillPeripheral targetPill;
    private @Nullable Uri firmwareImage;


    //region Lifecycle

    @Inject public PillDfuPresenter(@NonNull Context context,
                                    @NonNull BluetoothStack bluetoothStack) {
        this.context = context;
        this.bluetoothStack = bluetoothStack;
    }

    //endregion


    //region Scanning

    private Observable<List<PillPeripheral>> scanForPills() {
        return pending.bind("scanForPills", () -> PillPeripheral.discover(bluetoothStack, new PeripheralCriteria()));
    }

    public boolean hasScanned() {
        return hasScanned;
    }

    public boolean isScanning() {
        return scanning;
    }

    public void update() {
        Log.d(getClass().getSimpleName(), "update()");

        this.hasScanned = true;
        this.scanning = true;

        scanForPills().subscribe(pills -> {
            Log.d(getClass().getSimpleName(), "Found pills " + pills);
            this.scanning = false;
            sleepPills.onNext(pills);
        }, error -> {
            Log.e(getClass().getSimpleName(), "Could not scan for pills", error);
            this.scanning = false;
            sleepPills.onError(error);
        });
    }

    //endregion


    //region Pill Interactions

    public void reset(boolean clearFirmwareImage) {
        sleepPills.forget();

        this.hasScanned = false;
        this.scanning = false;
        this.targetPill = null;

        if (clearFirmwareImage) {
            this.firmwareImage = null;
        }
    }

    public void setFirmwareImage(@NonNull Uri firmwareImage) {
        this.firmwareImage = firmwareImage;
    }

    public void setTargetPill(@Nullable PillPeripheral targetPill) {
        this.targetPill = targetPill;
    }

    public boolean isPillInDfuMode() {
        return (targetPill != null && targetPill.isInDfuMode());
    }

    public Observable<PillPeripheral> enterDfuMode() {
        if (targetPill == null) {
            return Observable.error(new PeripheralNotFoundError());
        }

        Observable<PillPeripheral> connect = targetPill.connect()
                .flatMap(PillPeripheral::wipeFirmware)
                // Give the Sleep Pill time to restart into the bootloader
                // before we a) attempt to establish a bond with it;
                // and b) tell the dfu library to flash it. For sanity's sake.
                .delay(10, TimeUnit.SECONDS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // The DFU library does not appear to contain the work-around
            // for createBond() not working after connectGatt on Lollipop.
            return connect.flatMap(PillPeripheral::createBond);
        } else {
            return connect;
        }
    }

    public Observable<ComponentName> startDfuService() {
        return Observable.<ComponentName>create(subscriber -> {
            if (firmwareImage == null) {
                subscriber.onError(new FileNotFoundException());
                return;
            }

            if (targetPill == null) {
                subscriber.onError(new PeripheralNotFoundError());
                return;
            }

            Intent intent = new Intent(context, DfuService.class);

            intent.putExtra(DfuService.EXTRA_DEVICE_NAME, targetPill.getName());
            intent.putExtra(DfuService.EXTRA_DEVICE_ADDRESS, targetPill.getAddress());
            intent.putExtra(DfuService.EXTRA_FILE_TYPE, DfuService.TYPE_APPLICATION);
            intent.putExtra(DfuService.EXTRA_FILE_MIME_TYPE, DfuService.MIME_TYPE_OCTET_STREAM);
            intent.putExtra(DfuService.EXTRA_KEEP_BOND, false);

            String path = FileUtils.getPath(context, firmwareImage);
            if (path == null) {
                subscriber.onError(new FileNotFoundException());
                return;
            }
            intent.putExtra(DfuService.EXTRA_FILE_PATH, path);

            try {
                ComponentName componentName = context.startService(intent);
                subscriber.onNext(componentName);
                subscriber.onCompleted();
            } catch (SecurityException e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Rx.mainThreadScheduler());
    }

    public Observable<Integer> dfuProgress() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(DfuService.BROADCAST_ERROR);
        filter.addAction(DfuService.BROADCAST_PROGRESS);
        return Rx.fromLocalBroadcast(context, filter).flatMap(broadcast -> {
            if (DfuService.BROADCAST_ERROR.equals(broadcast.getAction())) {
                int errorCode = broadcast.getIntExtra(DfuService.EXTRA_DATA, BluetoothGattError.GATT_STACK_ERROR);
                return Observable.error(new DfuService.Error(errorCode));
            } else {
                int progress = broadcast.getIntExtra(DfuService.EXTRA_DATA, 0);
                return Observable.just(progress);
            }
        });
    }

    public Observable<Pair<Integer, String>> dfuLog() {
        IntentFilter filter = new IntentFilter(DfuService.BROADCAST_LOG);
        return Rx.fromLocalBroadcast(context, filter).map(broadcast -> {
            int logLevel = broadcast.getIntExtra(DfuService.EXTRA_LOG_LEVEL, Log.ERROR);
            String message = broadcast.getStringExtra(DfuService.EXTRA_LOG_MESSAGE);
            return Pair.create(logLevel, message);
        });
    }

    public void pause() {
        Intent command = new Intent(DfuService.BROADCAST_ACTION);
        command.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_PAUSE);
        LocalBroadcastManager.getInstance(context).sendBroadcast(command);
    }

    public void resume() {
        Intent command = new Intent(DfuService.BROADCAST_ACTION);
        command.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_RESUME);
        LocalBroadcastManager.getInstance(context).sendBroadcast(command);
    }

    public void abort() {
        Intent command = new Intent(DfuService.BROADCAST_ACTION);
        command.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_ABORT);
        LocalBroadcastManager.getInstance(context).sendBroadcast(command);
    }

    //endregion
}
