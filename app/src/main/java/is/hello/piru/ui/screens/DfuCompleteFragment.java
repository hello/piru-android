package is.hello.piru.ui.screens;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import javax.inject.Inject;

import is.hello.piru.R;
import is.hello.piru.bluetooth.PillDfuPresenter;
import is.hello.piru.ui.navigation.Navigation;
import is.hello.piru.ui.screens.base.BaseFragment;

public class DfuCompleteFragment extends BaseFragment {
    @Inject PillDfuPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dfu_complete, container, false);

        Button flashAnotherButton = (Button) view.findViewById(R.id.fragment_dfu_complete_flash_another);
        flashAnotherButton.setOnClickListener(this::flashAnother);

        Button chooseNewImageButton = (Button) view.findViewById(R.id.fragment_dfu_complete_choose_new_image);
        chooseNewImageButton.setOnClickListener(this::chooseNewImage);

        Button exitButton = (Button) view.findViewById(R.id.fragment_dfu_complete_exit);
        exitButton.setOnClickListener(this::exit);

        return view;
    }

    @Override
    public CharSequence getNavigationTitle(@NonNull Context context) {
        return context.getString(R.string.title_dfu_complete);
    }

    public void flashAnother(@NonNull View sender) {
        presenter.reset(false);
        getNavigation().pushFragment(new SelectPillFragment(), Navigation.FLAG_MAKE_HISTORY_ROOT);
    }

    public void chooseNewImage(@NonNull View sender) {
        presenter.reset(true);
        getNavigation().pushFragment(new SelectFirmwareFragment(), Navigation.FLAG_MAKE_HISTORY_ROOT);
    }

    public void exit(@NonNull View sender) {
        presenter.reset(true);
        getActivity().finish();
    }
}
