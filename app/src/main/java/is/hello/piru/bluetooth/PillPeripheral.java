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
import is.hello.buruberi.bluetooth.stacks.util.PeripheralCriteria;
import rx.Observable;

public final class PillPeripheral {
    //region Identifiers

    private static final String ADVERTISEMENT_SERVICE_128_BIT = "23D1BCEA5F782315DEEF121210E10000";

    private static final UUID SERVICE = UUID.fromString("23D1BCEA-5F78-2315-DEEF-121210E10000");
    private static final UUID CHARACTERISTIC_COMMAND_UUID = UUID.fromString("0000DEED-0000-1000-8000-00805F9B34FB");

    private static final byte COMMAND_WIPE_FIRMWARE = 8;

    //endregion


    //region Fields

    private final Peripheral peripheral;
    private PeripheralService service;

    //endregion


    //region Creation

    public static Observable<List<PillPeripheral>> discover(@NonNull BluetoothStack bluetoothStack,
                                                            @NonNull PeripheralCriteria criteria) {
        criteria.setDuration(PeripheralCriteria.DEFAULT_DURATION_MS * 2);
        criteria.addExactMatchPredicate(AdvertisingData.TYPE_LIST_OF_128_BIT_SERVICE_CLASS_UUIDS, ADVERTISEMENT_SERVICE_128_BIT);
        return bluetoothStack.discoverPeripherals(criteria).map(peripherals -> {
            List<PillPeripheral> pillPeripherals = new ArrayList<>();
            for (Peripheral peripheral : peripherals) {
                pillPeripherals.add(new PillPeripheral(peripheral));
            }
            return pillPeripherals;
        });
    }

    PillPeripheral(@NonNull Peripheral peripheral) {
        this.peripheral = peripheral;
    }

    //endregion


    //region Attributes

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

    public Observable<Void> wipeFirmware() {
        Log.d(getClass().getSimpleName(), "wipeFirmware()");

        byte[] payload = { COMMAND_WIPE_FIRMWARE };
        return writeCommand(CHARACTERISTIC_COMMAND_UUID, Peripheral.WriteType.NO_RESPONSE, payload);
    }

    //endregion
}
