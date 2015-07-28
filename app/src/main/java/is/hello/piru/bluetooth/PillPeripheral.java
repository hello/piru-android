package is.hello.piru.bluetooth;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import is.hello.buruberi.bluetooth.errors.PeripheralConnectionError;
import is.hello.buruberi.bluetooth.stacks.BluetoothStack;
import is.hello.buruberi.bluetooth.stacks.OperationTimeout;
import is.hello.buruberi.bluetooth.stacks.Peripheral;
import is.hello.buruberi.bluetooth.stacks.PeripheralService;
import is.hello.buruberi.bluetooth.stacks.util.AdvertisingData;
import is.hello.buruberi.bluetooth.stacks.util.Bytes;
import is.hello.buruberi.bluetooth.stacks.util.PeripheralCriteria;
import rx.Observable;

public final class PillPeripheral {
    //region Identifiers

    private static final byte[] NORMAL_ADVERTISEMENT_SERVICE_128_BIT = Bytes.fromString("23D1BCEA5F782315DEEF121210E10000");
    private static final byte[] DFU_ADVERTISEMENT_SERVICE_128_BIT = Bytes.fromString("23D1BCEA5F782315DEEF121230150000");

    private static final UUID SERVICE = UUID.fromString("0000e110-1212-efde-1523-785feabcd123");
    private static final UUID CHARACTERISTIC_COMMAND_UUID = UUID.fromString("0000DEED-0000-1000-8000-00805F9B34FB");

    private static final byte COMMAND_WIPE_FIRMWARE = 8;

    //endregion


    //region Fields

    private final Peripheral peripheral;
    private final boolean inDfuMode;
    private PeripheralService service;

    //endregion


    //region Creation

    public static Observable<List<PillPeripheral>> discover(@NonNull BluetoothStack bluetoothStack,
                                                            @NonNull PeripheralCriteria criteria) {
        criteria.addPredicate(ad -> {
            return (ad.anyRecordMatches(AdvertisingData.TYPE_LIST_OF_128_BIT_SERVICE_CLASS_UUIDS,
                            b -> Arrays.equals(NORMAL_ADVERTISEMENT_SERVICE_128_BIT, b)) ||
                    ad.anyRecordMatches(AdvertisingData.TYPE_INCOMPLETE_LIST_OF_128_BIT_SERVICE_CLASS_UUIDS,
                            b -> Arrays.equals(DFU_ADVERTISEMENT_SERVICE_128_BIT, b)));
        });
        return bluetoothStack.discoverPeripherals(criteria)
                             .map(peripherals -> {
                                 List<PillPeripheral> pillPeripherals = new ArrayList<>();
                                 for (Peripheral peripheral : peripherals) {
                                     pillPeripherals.add(new PillPeripheral(peripheral));
                                 }
                                 return pillPeripherals;
                             });
    }

    PillPeripheral(@NonNull Peripheral peripheral) {
        this.peripheral = peripheral;

        AdvertisingData advertisingData = peripheral.getAdvertisingData();
        this.inDfuMode = advertisingData.anyRecordMatches(AdvertisingData.TYPE_INCOMPLETE_LIST_OF_128_BIT_SERVICE_CLASS_UUIDS,
                b -> Arrays.equals(DFU_ADVERTISEMENT_SERVICE_128_BIT, b));
    }

    //endregion


    //region Attributes

    public boolean isInDfuMode() {
        return inDfuMode;
    }

    public int getScanTimeRssi() {
        return peripheral.getScanTimeRssi();
    }

    public String getAddress() {
        return peripheral.getAddress();
    }

    public String getName() {
        return peripheral.getName();
    }

    //endregion


    //region Timeouts

    @NonNull
    private OperationTimeout createOperationTimeout(@NonNull String name) {
        return peripheral.createOperationTimeout(name, 30, TimeUnit.SECONDS);
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
        return peripheral.connect(operationTimeout)
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
        return peripheral.disconnect()
                         .map(ignored -> {
                             this.service = null;
                             return this;
                         });
    }

    public boolean isConnected() {
        return (peripheral.getConnectionStatus() == Peripheral.STATUS_CONNECTED &&
                service != null);
    }

    //endregion


    //region Commands

    @NonNull
    private Observable<Void> writeCommand(@NonNull UUID identifier,
                                          @NonNull Peripheral.WriteType writeType,
                                          @NonNull byte[] payload) {
        Log.d(getClass().getSimpleName(), "writeCommand(" + identifier + ", " + writeType + ", " + Arrays.toString(payload) + ")");

        if (!isConnected()) {
            return Observable.error(new PeripheralConnectionError("writeCommand(...) requires a connection"));
        }

        return peripheral.writeCommand(service, identifier, writeType, payload, createOperationTimeout("Write Command"));
    }

    public Observable<PillPeripheral> wipeFirmware() {
        Log.d(getClass().getSimpleName(), "wipeFirmware()");

        byte[] payload = { COMMAND_WIPE_FIRMWARE };
        return writeCommand(CHARACTERISTIC_COMMAND_UUID, Peripheral.WriteType.NO_RESPONSE, payload)
                .flatMap(ignored -> {
                    Log.d(getClass().getSimpleName(), "wipeFirmware command written");
                    return disconnect();
                });
    }

    //endregion
}
