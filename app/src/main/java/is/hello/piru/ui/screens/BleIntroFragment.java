package is.hello.piru.ui.screens;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import is.hello.piru.R;
import is.hello.piru.ui.screens.base.BaseFragment;

public class BleIntroFragment extends BaseFragment {
    @Override
    protected boolean wantsInjection() {
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro, container, false);

        Button next = (Button) view.findViewById(R.id.fragment_intro_next);
        next.setOnClickListener(ignored -> getNavigation().pushFragment(new SelectPillFragment()));

        return view;
    }
}
