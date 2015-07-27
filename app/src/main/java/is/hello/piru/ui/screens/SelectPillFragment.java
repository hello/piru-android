package is.hello.piru.ui.screens;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import is.hello.piru.R;
import is.hello.piru.bluetooth.PillDfuPresenter;

public class SelectPillFragment extends BaseFragment {
    @Inject PillDfuPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_pill, container, false);

        return view;
    }
}
