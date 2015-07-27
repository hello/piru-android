package is.hello.piru.ui.screens;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import javax.inject.Inject;

import is.hello.buruberi.bluetooth.stacks.Peripheral;
import is.hello.piru.R;
import is.hello.piru.bluetooth.PillDfuPresenter;
import is.hello.piru.ui.adapters.ArrayRecyclerAdapter;
import is.hello.piru.ui.adapters.PillsAdapter;
import is.hello.piru.ui.screens.base.RecyclerFragment;

public class SelectPillFragment extends RecyclerFragment implements ArrayRecyclerAdapter.OnItemClickedListener<Peripheral> {
    @Inject PillDfuPresenter presenter;

    private PillsAdapter adapter;

    //region Lifecycle

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!presenter.hasScanned()) {
            presenter.update();
        }

        setRetainInstance(true);
    }

    @Override
    protected void onConfigureRecycler(@NonNull RecyclerView recyclerView) {
        this.adapter = new PillsAdapter(getActivity());
        adapter.setOnItemClickedListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setBusy(true);
        subscribe(presenter.sleepPills, this::bindPills);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        this.adapter = null;
    }

    //endregion


    //region Bindings

    @Override
    public void onRefresh() {
        setBusy(true);
        presenter.update();
    }

    public void bindPills(@NonNull List<Peripheral> pills) {
        setBusy(false);

        adapter.clear();
        adapter.addAll(pills);

        if (pills.isEmpty()) {
            setEmpty(getString(R.string.message_no_pills));
        } else {
            setEmpty(null);
        }
    }

    @Override
    public void onItemClicked(int position, Peripheral pill) {
        DfuProgressFragment fragment = DfuProgressFragment.newInstance(pill);
        getNavigation().pushFragment(fragment);
    }

    //endregion
}
