package is.hello.piru.bluetooth;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Pair;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import is.hello.buruberi.bluetooth.errors.BluetoothGattError;
import is.hello.buruberi.bluetooth.stacks.BluetoothStack;
import is.hello.buruberi.bluetooth.stacks.Peripheral;
import is.hello.buruberi.bluetooth.stacks.util.AdvertisingData;
import is.hello.buruberi.bluetooth.stacks.util.PeripheralCriteria;
import is.hello.buruberi.util.Rx;
import rx.Observable;
import rx.subjects.ReplaySubject;

@Singleton public class PillDfuPresenter {
    public static final String SERVICE_ID_128_BIT = "23D1BCEA5F782315DEEF121210E10000";

    private final Context context;
    private final BluetoothStack bluetoothStack;
    private PendingObservables<String> pending = new PendingObservables<>();

    public final ReplaySubject<List<Peripheral>> sleepPills = ReplaySubject.createWithSize(1);
    private boolean hasScanned = false;


    //region Lifecycle

    @Inject public PillDfuPresenter(@NonNull Context context,
                                    @NonNull BluetoothStack bluetoothStack) {
        this.context = context;
        this.bluetoothStack = bluetoothStack;
    }

    //endregion


    //region Scanning

    private Observable<List<Peripheral>> scanForPills() {
        return pending.bind("scanForPills", () -> {
            PeripheralCriteria criteria = new PeripheralCriteria();
            criteria.setDuration(PeripheralCriteria.DEFAULT_DURATION_MS * 2);
            criteria.addExactMatchPredicate(AdvertisingData.TYPE_LIST_OF_128_BIT_SERVICE_CLASS_UUIDS, SERVICE_ID_128_BIT);
            return bluetoothStack.discoverPeripherals(criteria);
        });
    }

    public boolean hasScanned() {
        return hasScanned;
    }

    public void update() {
        Log.d(getClass().getSimpleName(), "update()");

        this.hasScanned = true;

        scanForPills().subscribe(pills -> {
            Log.d(getClass().getSimpleName(), "Found pills " + pills);
            sleepPills.onNext(pills);
        }, error -> {
            Log.e(getClass().getSimpleName(), "Could not scan for pills", error);
        });
    }

    //endregion


    //region Pill Interactions

    public Observable<ComponentName> startDfuService(@NonNull String name, @NonNull String address, @NonNull Uri imageUri) {
        return Observable.<ComponentName>create(subscriber -> {
            Intent intent = new Intent(context, DfuService.class);

            intent.putExtra(DfuService.EXTRA_DEVICE_NAME, name);
            intent.putExtra(DfuService.EXTRA_DEVICE_ADDRESS, address);
            intent.putExtra(DfuService.EXTRA_FILE_TYPE, DfuService.TYPE_APPLICATION);
            intent.putExtra(DfuService.EXTRA_FILE_MIME_TYPE, DfuService.MIME_TYPE_OCTET_STREAM);
            intent.putExtra(DfuService.EXTRA_FILE_URI, imageUri);
            intent.putExtra(DfuService.EXTRA_KEEP_BOND, false);

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
                return Observable.error(new BluetoothGattError(errorCode, null));
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
