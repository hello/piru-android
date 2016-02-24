package is.hello.piru.bluetooth;

import android.app.Activity;
import android.support.annotation.StringRes;

import is.hello.buruberi.bluetooth.errors.BuruberiException;
import is.hello.piru.R;
import is.hello.piru.ui.DfuNotificationActivity;
import no.nordicsemi.android.dfu.DfuBaseService;
import no.nordicsemi.android.error.GattError;

public class DfuService extends DfuBaseService {
    @Override
    protected Class<? extends Activity> getNotificationTarget() {
        return DfuNotificationActivity.class;
    }

    public static @StringRes int getProgressString(int progress) {
        switch (progress) {
            case DfuService.PROGRESS_CONNECTING:
                return R.string.dfu_status_connecting;
            case DfuService.PROGRESS_STARTING:
                return R.string.dfu_status_starting;
            case DfuService.PROGRESS_ENABLING_DFU_MODE:
                return R.string.dfu_status_enabling_dfu_mode;
            case DfuService.PROGRESS_VALIDATING:
                return R.string.dfu_status_validating;
            case DfuService.PROGRESS_DISCONNECTING:
                return R.string.dfu_status_disconnecting;
            case DfuService.PROGRESS_COMPLETED:
                return R.string.dfu_status_completed;
            case DfuService.PROGRESS_ABORTED:
                return R.string.dfu_status_aborted;
            default:
                if (progress >= 0) {
                    return R.string.dfu_status_uploading;
                } else {
                    return R.string.dfu_status_waiting;
                }
        }
    }

    public static class Error extends BuruberiException {
        public final int error;

        public Error(int error) {
            super(GattError.parse(error));
            this.error = error;
        }
    }
}
