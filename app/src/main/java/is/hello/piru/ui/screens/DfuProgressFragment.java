package is.hello.piru.ui.screens;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import javax.inject.Inject;

import is.hello.piru.R;
import is.hello.piru.bluetooth.PillDfuPresenter;
import is.hello.piru.ui.adapters.LogAdapter;
import is.hello.piru.ui.dialogs.ErrorDialogFragment;
import is.hello.piru.ui.screens.base.BaseFragment;

public class DfuProgressFragment extends BaseFragment {
    @Inject PillDfuPresenter presenter;

    private TextView statusText;
    private ProgressBar progressBar;
    private Button controlButton;
    private RecyclerView logRecycler;
    private LogAdapter adapter;

    //region Lifecycle

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dfu_progress, container, false);

        this.statusText = (TextView) view.findViewById(R.id.fragment_dfu_progress_status);
        this.progressBar = (ProgressBar) view.findViewById(R.id.fragment_dfu_progress_bar);
        this.controlButton = (Button) view.findViewById(R.id.fragment_dfu_progress_control);
        controlButton.setOnClickListener(this::start);

        this.logRecycler = (RecyclerView) view.findViewById(R.id.fragment_dfu_progress_log);
        logRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        logRecycler.setHasFixedSize(true);

        this.adapter = new LogAdapter(getActivity());
        logRecycler.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        subscribe(presenter.dfuProgress(), this::bindProgress, this::presentError);
        subscribe(presenter.dfuLog(), this::appendToLog);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        this.statusText = null;
        this.progressBar = null;
        this.logRecycler = null;
        this.logRecycler = null;
    }

    //endregion


    //region Bindings

    public void bindProgress(@NonNull PillDfuPresenter.Progress progress) {
        statusText.setText(progress.getStatus());
        progressBar.setMax(progress.totalParts);
        progressBar.setProgress(progress.currentPart);
    }

    public void presentError(Throwable e) {
        statusText.setText(R.string.title_error);
        progressBar.setProgress(0);

        controlButton.setEnabled(true);
        controlButton.setText(R.string.action_start);
        controlButton.setOnClickListener(this::start);

        ErrorDialogFragment dialogFragment = new ErrorDialogFragment.Builder()
                .withError(e)
                .build();
        dialogFragment.show(getFragmentManager());

        appendToLog(Pair.create(Log.ERROR, e.getMessage()));
    }

    public void appendToLog(@NonNull Pair<Integer, String> entry) {
        adapter.add(entry);
    }

    //endregion


    //region Actions

    public void start(@NonNull View sender) {
        if (presenter.isPillInDfuMode()) {
            startDfuService();
        } else {
            statusText.setText(R.string.dfu_status_enabling_dfu_mode);
            controlButton.setEnabled(false);

            subscribe(presenter.enterDfuMode(),
                    ignored -> startDfuService(),
                    this::presentError);
        }
    }

    private void startDfuService() {
        controlButton.setText(R.string.action_abort);
        controlButton.setOnClickListener(this::abort);
        controlButton.setEnabled(true);

        subscribe(presenter.startDfuService(), ignored -> {}, this::presentError);
    }

    public void abort(@NonNull View sender) {
        presenter.abort();
    }

    //endregion
}
