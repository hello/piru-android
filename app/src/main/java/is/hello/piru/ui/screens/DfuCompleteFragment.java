package is.hello.piru.ui.screens;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import is.hello.piru.R;

public class DfuCompleteFragment extends BaseFragment {
    @Override
    protected boolean wantsInjection() {
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dfu_complete, container, false);

        return view;
    }
}
