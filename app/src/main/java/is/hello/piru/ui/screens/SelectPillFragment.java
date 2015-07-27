package is.hello.piru.ui.screens;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import javax.inject.Inject;

import is.hello.piru.bluetooth.PillDfuPresenter;
import is.hello.piru.ui.screens.base.RecyclerFragment;

public class SelectPillFragment extends RecyclerFragment {
    @Inject PillDfuPresenter presenter;

    @Override
    protected void onConfigureRecycler(@NonNull RecyclerView recyclerView) {

    }
}
