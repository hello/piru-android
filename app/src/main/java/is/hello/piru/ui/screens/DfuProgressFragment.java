package is.hello.piru.ui.screens;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import is.hello.piru.R;
import is.hello.piru.bluetooth.PillDfuPresenter;
import is.hello.piru.ui.screens.base.BaseFragment;

public class DfuProgressFragment extends BaseFragment {
    @Inject PillDfuPresenter presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dfu_progress, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        subscribe(presenter.dfuProgress(), progress -> {
            Log.d(getClass().getSimpleName(), "progress: " + progress);
        }, e -> {
            Log.e(getClass().getSimpleName(), "Failed to flash image", e);
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        subscribe(presenter.startDfuService(), ignored -> Log.d(getClass().getSimpleName(), "started"));
    }
}
