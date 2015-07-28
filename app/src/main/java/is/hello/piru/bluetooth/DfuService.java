package is.hello.piru.bluetooth;

import android.app.Activity;

import is.hello.buruberi.bluetooth.errors.BluetoothError;
import is.hello.piru.ui.DfuNotificationActivity;
import no.nordicsemi.android.dfu.DfuBaseService;
import no.nordicsemi.android.error.GattError;

public class DfuService extends DfuBaseService {
    @Override
    protected Class<? extends Activity> getNotificationTarget() {
        return DfuNotificationActivity.class;
    }

    public static class Error extends BluetoothError {
        public final int error;

        public Error(int error) {
            super(GattError.parse(error));
            this.error = error;
        }
    }
}
