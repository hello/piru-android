package is.hello.piru.ui.screens;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import javax.inject.Inject;

import is.hello.piru.R;
import is.hello.piru.bluetooth.DfuService;
import is.hello.piru.bluetooth.PillDfuPresenter;
import is.hello.piru.ui.adapters.LogAdapter;
import is.hello.piru.ui.dialogs.ErrorDialogFragment;
import is.hello.piru.ui.navigation.Navigation;
import is.hello.piru.ui.screens.base.BaseFragment;

public class DfuProgressFragment extends BaseFragment {
    @Inject PillDfuPresenter presenter;

    private TextView statusText;
    private ProgressBar progressBar;
    private ImageButton controlButton;
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
        this.controlButton = (ImageButton) view.findViewById(R.id.fragment_dfu_progress_control);
        controlButton.setOnClickListener(this::start);

        this.logRecycler = (RecyclerView) view.findViewById(R.id.fragment_dfu_progress_log);
        logRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        logRecycler.setHasFixedSize(true);
        logRecycler.setItemAnimator(null);

        this.adapter = new LogAdapter(getActivity());
        logRecycler.setAdapter(adapter);

        setButtonState(ButtonState.IDLE);

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

    public void bindProgress(int progress) {
        if (progress == DfuService.PROGRESS_COMPLETED) {
            onDfuCompleted();
        } else if (progress == DfuService.PROGRESS_ABORTED) {
            onDfuAborted();
        } else {
            statusText.setText(DfuService.getProgressString(progress));
            progressBar.setProgress(progress);
        }
    }

    public void presentError(Throwable e) {
        statusText.setText(R.string.title_error);
        progressBar.setProgress(0);

        setButtonState(ButtonState.IDLE);
        controlButton.setOnClickListener(this::start);

        ErrorDialogFragment dialogFragment = new ErrorDialogFragment.Builder()
                .withError(e)
                .build();
        dialogFragment.show(getFragmentManager());

        appendToLog(Pair.create(Log.ERROR, e.getMessage()));
    }

    public void appendToLog(@NonNull Pair<Integer, String> entry) {
        adapter.add(entry);
        logRecycler.post(() -> logRecycler.scrollToPosition(adapter.getItemCount() - 1));
    }

    //endregion


    //region Outcomes

    private void onDfuCompleted() {
        getNavigation().pushFragment(new DfuCompleteFragment(), Navigation.FLAG_MAKE_HISTORY_ROOT);
    }

    private void onDfuAborted() {
        statusText.setText(R.string.dfu_status_aborted);
        progressBar.setProgress(0);

        setButtonState(ButtonState.IDLE);
        controlButton.setOnClickListener(this::start);
    }

    //endregion


    //region Actions

    private void setButtonState(@NonNull ButtonState state) {
        controlButton.setContentDescription(getString(state.titleRes));

        Resources resources = getResources();
        Drawable rawDrawable = ResourcesCompat.getDrawable(resources, state.iconRes, null);
        Drawable drawable = DrawableCompat.wrap(rawDrawable).mutate();
        DrawableCompat.setTint(drawable, resources.getColor(R.color.secondary_normal));
        if (state.enabled) {
            rawDrawable.setAlpha(0xFF);
        } else {
            rawDrawable.setAlpha(0x77);
        }
        controlButton.setImageDrawable(rawDrawable);
        controlButton.setEnabled(state.enabled);
    }

    public void start(@NonNull View sender) {
        if (presenter.isPillInDfuMode()) {
            startDfuService();
        } else {
            statusText.setText(R.string.dfu_status_enabling_dfu_mode);
            setButtonState(ButtonState.STARTING_DFU);

            appendToLog(Pair.create(Log.INFO, "Entering DFU mode"));

            subscribe(presenter.enterDfuMode(),
                    ignored -> startDfuService(),
                    this::presentError);
        }
    }

    private void startDfuService() {
        appendToLog(Pair.create(Log.INFO, "Starting DFU service"));

        setButtonState(ButtonState.ABORT_DFU);
        controlButton.setOnClickListener(this::abort);

        subscribe(presenter.startDfuService(), ignored -> {}, this::presentError);
    }

    public void abort(@NonNull View sender) {
        presenter.abort();
    }

    //endregion


    enum ButtonState {
        IDLE(R.string.action_start, R.drawable.action_upload, true),
        STARTING_DFU(R.string.action_start, R.drawable.action_upload, false),
        ABORT_DFU(R.string.action_abort, R.drawable.action_stop, true);

        public final @StringRes int titleRes;
        public final @DrawableRes int iconRes;
        public final boolean enabled;

        ButtonState(@StringRes int titleRes,
                    @DrawableRes int iconRes,
                    boolean enabled) {
            this.titleRes = titleRes;
            this.iconRes = iconRes;
            this.enabled = enabled;
        }
    }
}
