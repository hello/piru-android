package is.hello.piru.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import is.hello.commonsense.util.Errors;
import is.hello.commonsense.util.StringRef;
import is.hello.piru.R;

public class ErrorDialogFragment extends DialogFragment {
    private static final String ARG_MESSAGE = ErrorDialogFragment.class.getName() + ".ARG_MESSAGE";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_error);

        StringRef message = getArguments().getParcelable(ARG_MESSAGE);
        if (message != null) {
            builder.setMessage(message.resolve(getActivity()));
        } else {
            builder.setMessage(R.string.error_message_generic);
        }

        builder.setPositiveButton(android.R.string.ok, null);

        return builder.create();
    }

    public void show(@NonNull FragmentManager fm) {
        show(fm, getClass().getSimpleName());
    }

    public static class Builder {
        private final Bundle arguments = new Bundle();

        public Builder withError(@Nullable Throwable e) {
            if (e != null) {
                return withMessage(Errors.getDisplayMessage(e));
            } else {
                return withMessage((StringRef) null);
            }
        }

        public Builder withMessage(@Nullable StringRef messageRef) {
            arguments.putParcelable(ARG_MESSAGE, messageRef);
            return this;
        }

        public Builder withMessage(@Nullable String message) {
            if (message == null) {
                arguments.remove(ARG_MESSAGE);
                return this;
            } else {
                return withMessage(StringRef.from(message));
            }
        }

        public Builder withMessage(@StringRes int messageRes) {
            return withMessage(StringRef.from(messageRes));
        }

        public ErrorDialogFragment build() {
            ErrorDialogFragment fragment = new ErrorDialogFragment();
            fragment.setArguments(arguments);
            return fragment;
        }
    }
}
