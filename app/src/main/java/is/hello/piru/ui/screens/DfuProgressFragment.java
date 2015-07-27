package is.hello.piru.ui.screens;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import is.hello.buruberi.bluetooth.stacks.Peripheral;
import is.hello.piru.R;
import is.hello.piru.bluetooth.PillDfuPresenter;
import is.hello.piru.ui.screens.base.BaseFragment;

public class DfuProgressFragment extends BaseFragment {
    private static final String ARG_NAME = DfuProgressFragment.class.getName() + ".ARG_NAME";
    private static final String ARG_ADDRESS = DfuProgressFragment.class.getName() + ".ARG_ADDRESS";

    @Inject PillDfuPresenter presenter;

    public static DfuProgressFragment newInstance(@NonNull Peripheral pill) {
        DfuProgressFragment fragment = new DfuProgressFragment();

        Bundle arguments = new Bundle();
        arguments.putString(ARG_NAME, pill.getName());
        arguments.putString(ARG_ADDRESS, pill.getAddress());
        fragment.setArguments(arguments);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dfu_progress, container, false);

        return view;
    }
}
