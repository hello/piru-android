package is.hello.piru.exception;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import is.hello.buruberi.bluetooth.errors.BuruberiException;
import is.hello.common.util.Errors;
import is.hello.common.util.StringRef;
import is.hello.piru.R;

public class PillNotFoundException extends BuruberiException implements Errors.Reporting{
    public PillNotFoundException(){
        super("No Pills Found");
    }
    public PillNotFoundException(String detailMessage) {
        super(detailMessage);
    }

    public PillNotFoundException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    @Nullable
    @Override
    public String getContextInfo() {
        return null;
    }

    @NonNull
    @Override
    public StringRef getDisplayMessage() {
        return StringRef.from(R.string.message_no_pills);
    }
}
