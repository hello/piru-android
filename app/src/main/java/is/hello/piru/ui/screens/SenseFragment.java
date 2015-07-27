package is.hello.piru.ui.screens;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import javax.inject.Inject;

import is.hello.piru.bluetooth.SensePresenter;
import is.hello.piru.ui.screens.base.RecyclerFragment;

public class SenseFragment extends RecyclerFragment {
    @Inject SensePresenter presenter;

    @Override
    protected void onConfigureRecycler(@NonNull RecyclerView recyclerView) {

    }
}
