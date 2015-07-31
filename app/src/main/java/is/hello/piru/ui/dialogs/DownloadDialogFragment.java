package is.hello.piru.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

import javax.inject.Inject;

import is.hello.buruberi.util.Rx;
import is.hello.piru.R;
import is.hello.piru.api.FirmwarePresenter;
import is.hello.piru.api.model.FirmwareVersion;
import rx.Observable;
import rx.Subscription;

import static is.hello.piru.PiruApplication.inject;

public class DownloadDialogFragment extends DialogFragment {
    public static final String TAG = DownloadDialogFragment.class.getSimpleName();

    private static final String ARG_FIRMWARE_VERSION = DownloadDialogFragment.class.getSimpleName() + ".ARG_FIRMWARE_VERSION";

    @Inject FirmwarePresenter firmwarePresenter;

    private FirmwareVersion firmwareVersion;
    private ProgressBar progressBar;
    private TextView status;

    private File destination;
    private @Nullable Subscription download;


    //region Lifecycle

    public static DownloadDialogFragment newInstance(@NonNull FirmwareVersion firmwareVersion) {
        DownloadDialogFragment fragment = new DownloadDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putParcelable(ARG_FIRMWARE_VERSION, firmwareVersion);
        fragment.setArguments(arguments);

        return fragment;
    }

    public DownloadDialogFragment() {
        inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.firmwareVersion = getArguments().getParcelable(ARG_FIRMWARE_VERSION);

        setCancelable(false);
        setRetainInstance(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.title_downloading);
        builder.setNegativeButton(android.R.string.cancel, (ignored, which) -> cancel());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams")
        View contentView = inflater.inflate(R.layout.fragment_dialog_download, null);
        this.progressBar = (ProgressBar) contentView.findViewById(R.id.fragment_dialog_download_progress);
        this.status = (TextView) contentView.findViewById(R.id.fragment_dialog_download_status);

        int outerInset = getResources().getDimensionPixelSize(R.dimen.gap_outer);
        builder.setView(contentView, outerInset, outerInset, outerInset, outerInset);

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();

        startIfNeeded();
    }

    //endregion


    //region Downloading

    private void cancel() {
        if (download != null && !download.isUnsubscribed()) {
            download.unsubscribe();
            this.download = null;
        }
    }

    private void startIfNeeded() {
        if (download == null) {
            this.destination = firmwarePresenter.createTemporaryFile(firmwareVersion);
            Observable<Integer> pendingDownload = firmwarePresenter.downloadFirmwareVersion(firmwareVersion, destination)
                    .lift(new Rx.OperatorConditionalBinding<>(this, f -> f.isAdded() && !f.getActivity().isFinishing()));
            this.download = pendingDownload.subscribe(this::bindProgress,
                                                      this::presentError,
                                                      this::bindCompletion);
        }
    }

    private void bindProgress(int progress) {
        if (progressBar != null) {
            progressBar.setProgress(progress);
            status.setText(progress + "%");
        }
    }

    private void presentError(Throwable e) {
        ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment.Builder()
                .withError(e)
                .build();
        errorDialogFragment.show(getFragmentManager());
        dismiss();
    }

    private void bindCompletion() {
        dismiss();

        Fragment target = getTargetFragment();
        if (target == null) {
            Log.w(getClass().getSimpleName(), "Target missing.");
            return;
        }

        int requestCode = getTargetRequestCode();
        if (download == null) {
            target.onActivityResult(requestCode, Activity.RESULT_CANCELED, null);
        } else {
            Intent data = new Intent();
            data.setData(Uri.fromFile(destination));
            target.onActivityResult(requestCode, Activity.RESULT_OK, data);
        }
    }

    //endregion
}
