package is.hello.piru.bluetooth;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import is.hello.buruberi.bluetooth.errors.GattException;
import is.hello.buruberi.bluetooth.stacks.BluetoothStack;
import is.hello.buruberi.bluetooth.stacks.GattPeripheral;
import is.hello.buruberi.bluetooth.stacks.OperationTimeout;
import is.hello.buruberi.bluetooth.stacks.GattService;
import is.hello.buruberi.bluetooth.stacks.util.AdvertisingData;
import is.hello.buruberi.bluetooth.stacks.util.Bytes;
import is.hello.buruberi.bluetooth.stacks.util.PeripheralCriteria;
import is.hello.piru.exception.PillNotFoundException;
import rx.Observable;

public final class PillPeripheral {
    //region Identifiers

    private static final byte[] NORMAL_ADVERTISEMENT_SERVICE_128_BIT = Bytes.fromString("23D1BCEA5F782315DEEF121210E10000");
    private static final byte[] DFU_ADVERTISEMENT_SERVICE_128_BIT = Bytes.fromString("23D1BCEA5F782315DEEF121230150000");

    private static final UUID SERVICE = UUID.fromString("0000e110-1212-efde-1523-785feabcd123");
    private static final UUID CHARACTERISTIC_COMMAND_UUID = UUID.fromString("0000DEED-0000-1000-8000-00805F9B34FB");

    private static final byte COMMAND_WIPE_FIRMWARE = 8;

    private static final int TIME_OUT_SECONDS  = 10;

    //endregion


    //region Fields

    private final GattPeripheral gattPeripheral;
    private final boolean inDfuMode;
    private GattService service;

    //endregion


    //region Creation

    public static Observable<List<PillPeripheral>> discover(@NonNull BluetoothStack bluetoothStack,
                                                            @NonNull PeripheralCriteria criteria) {
        criteria.setDuration(PeripheralCriteria.DEFAULT_DURATION_MS * 2);
        criteria.addPredicate(ad -> (ad.anyRecordMatches(AdvertisingData.TYPE_LIST_OF_128_BIT_SERVICE_CLASS_UUIDS,
                        b -> Arrays.equals(NORMAL_ADVERTISEMENT_SERVICE_128_BIT, b)) ||
                ad.anyRecordMatches(AdvertisingData.TYPE_INCOMPLETE_LIST_OF_128_BIT_SERVICE_CLASS_UUIDS,
                        b -> Arrays.equals(DFU_ADVERTISEMENT_SERVICE_128_BIT, b))));
        return bluetoothStack.discoverPeripherals(criteria)
                             .map(peripherals -> {
                                 List<PillPeripheral> pillPeripherals = new ArrayList<>();
                                 for (GattPeripheral peripheral : peripherals) {
                                     pillPeripherals.add(new PillPeripheral(peripheral));
                                 }
                                 return pillPeripherals;
                             });
    }

    PillPeripheral(@NonNull GattPeripheral gattPeripheral) {
        this.gattPeripheral = gattPeripheral;

        AdvertisingData advertisingData = gattPeripheral.getAdvertisingData();
        this.inDfuMode = advertisingData.anyRecordMatches(AdvertisingData.TYPE_INCOMPLETE_LIST_OF_128_BIT_SERVICE_CLASS_UUIDS,
                b -> Arrays.equals(DFU_ADVERTISEMENT_SERVICE_128_BIT, b));
    }

    //endregion


    //region Attributes

    public boolean isInDfuMode() {
        return inDfuMode;
    }

    public int getScanTimeRssi() {
        return gattPeripheral.getScanTimeRssi();
    }

    public String getAddress() {
        return gattPeripheral.getAddress();
    }

    public String getName() {
        return gattPeripheral.getName();
    }

    //endregion


    //region Timeouts

    @NonNull
    private OperationTimeout createOperationTimeout(@NonNull String name) {
        return gattPeripheral.createOperationTimeout(name, 30, TimeUnit.SECONDS);
    }

    //endregion


    //region Connecting

    @NonNull
    public Observable<PillPeripheral> connect() {
        Log.d(getClass().getSimpleName(), "connect()");

        if (inDfuMode) {
            return Observable.error(new IllegalStateException("Cannot connect to sleep pill in dfu mode."));
        }

        OperationTimeout operationTimeout = createOperationTimeout("Connect");
        return gattPeripheral.connect(GattPeripheral.CONNECT_FLAG_DEFAULTS,operationTimeout)
                         .flatMap(connectedPeripheral -> {
                             Log.d(getClass().getSimpleName(), "discoverService(" + SERVICE + ")");
                             return connectedPeripheral.discoverService(SERVICE, operationTimeout);
                         })
                         .map(service -> {
                             Log.d(getClass().getSimpleName(), "connected");
                             this.service = service;
                             return this;
                         });
    }

    @NonNull
    public Observable<PillPeripheral> disconnect() {
        return gattPeripheral.disconnect()
                         .map(ignored -> {
                             this.service = null;
                             return this;
                         });
    }

    @NonNull
    public Observable<PillPeripheral> createBond() {
        Log.d(getClass().getSimpleName(), "createBond()");

        return gattPeripheral.createBond().map(ignored -> {
            Log.d(getClass().getSimpleName(), "bond created");
            return this;
        });
    }

    public boolean isConnected() {
        return (gattPeripheral.getConnectionStatus() == GattPeripheral.STATUS_CONNECTED &&
                service != null);
    }

    //endregion


    //region Commands

    @NonNull
    private Observable<Void> writeCommand(@NonNull UUID identifier,
                                          @NonNull GattPeripheral.WriteType writeType,
                                          @NonNull byte[] payload) {
        Log.d(getClass().getSimpleName(), "writeCommand(" + identifier + ", " + writeType + ", " + Arrays.toString(payload) + ")");

        if (!isConnected()) {
            return Observable.error(new PillNotFoundException("writeCommand(...) requires a connection"));
        }
        return service.getCharacteristic(identifier).write(writeType, payload, gattPeripheral.createOperationTimeout("Animation",
                TIME_OUT_SECONDS,
                TimeUnit.SECONDS));
    }

    public Observable<PillPeripheral> wipeFirmware() {
        Log.d(getClass().getSimpleName(), "wipeFirmware()");

        byte[] payload = { COMMAND_WIPE_FIRMWARE };
        return writeCommand(CHARACTERISTIC_COMMAND_UUID, GattPeripheral.WriteType.NO_RESPONSE, payload)
                // There's a race condition inside of the BLE stack where disconnecting
                // immediately after calling writeCommand(...) will result in the command
                // not being written to the characteristic. 3 seconds seemed to be sufficient,
                // but quality of BLE varies a lot, so let's go with 5 to be safe.
                .delay(5, TimeUnit.SECONDS)
                .flatMap(ignored -> {
                    Log.d(getClass().getSimpleName(), "wipeFirmware command written");
                    return disconnect();
                });
    }

    //endregion
}
