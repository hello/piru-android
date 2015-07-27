package is.hello.piru.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import is.hello.piru.R;

public class LoadingDialogFragment extends DialogFragment {
    public static final String TAG = LoadingDialogFragment.class.getSimpleName();

    //region Lifecycle

    public static LoadingDialogFragment show(@NonNull FragmentManager fm) {
        LoadingDialogFragment dialogFragment = (LoadingDialogFragment) fm.findFragmentByTag(TAG);
        if (dialogFragment == null) {
            dialogFragment = new LoadingDialogFragment();
            dialogFragment.show(fm, TAG);
        }
        return dialogFragment;
    }

    public static void close(@NonNull FragmentManager fm) {
        LoadingDialogFragment dialogFragment = (LoadingDialogFragment) fm.findFragmentByTag(TAG);
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setMessage(R.string.title_loading);
        dialog.setView(R.layout.dialog_loading);
        return dialog.create();
    }

    //endregion


    public void setText(@StringRes int stringRes) {
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            dialog.setMessage(getString(stringRes));
        }
    }

    public void setText(CharSequence string) {
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            dialog.setMessage(string);
        }
    }
}
