package is.hello.piru.bluetooth;

import android.app.Activity;

import is.hello.piru.ui.DfuNotificationActivity;
import no.nordicsemi.android.dfu.DfuBaseService;

public class DfuService extends DfuBaseService {
    @Override
    protected Class<? extends Activity> getNotificationTarget() {
        return DfuNotificationActivity.class;
    }
}
