package is.hello.piru.ui.screens;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import javax.inject.Inject;

import is.hello.commonsense.util.Errors;
import is.hello.commonsense.util.StringRef;
import is.hello.piru.R;
import is.hello.piru.bluetooth.PillDfuPresenter;
import is.hello.piru.bluetooth.PillPeripheral;
import is.hello.piru.ui.adapters.ArrayRecyclerAdapter;
import is.hello.piru.ui.adapters.HorizontalDividerDecoration;
import is.hello.piru.ui.adapters.PillsAdapter;
import is.hello.piru.ui.navigation.Navigation;
import is.hello.piru.ui.screens.base.RecyclerFragment;

public class SelectPillFragment extends RecyclerFragment implements ArrayRecyclerAdapter.OnItemClickedListener<PillPeripheral> {
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
        recyclerView.addItemDecoration(new HorizontalDividerDecoration(getResources()));

        this.adapter = new PillsAdapter(getActivity());
        adapter.setOnItemClickedListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setBusy(true);
        subscribe(presenter.sleepPills, this::bindPills, this::presentError);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        this.adapter = null;
    }

    @Override
    public CharSequence getNavigationTitle(@NonNull Context context) {
        return context.getString(R.string.title_select_sleep_pill);
    }

    @Override
    public CharSequence getNavigationSubtitle(@NonNull Context context) {
        if (presenter.isScanning()) {
            return context.getString(R.string.title_scanning);
        } else {
            return null;
        }
    }

    //endregion


    //region Bindings

    @Override
    public void onRefresh() {
        presenter.update();
        setBusy(true);
    }

    public void bindPills(@NonNull List<PillPeripheral>pills) {
        setBusy(false);

        adapter.clear();
        adapter.addAll(pills);

        if (pills.isEmpty()) {
            setEmpty(getString(R.string.message_no_pills));
        } else {
            setEmpty(null);
        }
    }

    public void presentError(Throwable error) {
        setBusy(false);

        adapter.clear();

        StringRef message = Errors.getDisplayMessage(error);
        if (message != null) {
            setEmpty(message.resolve(getActivity()));
        } else {
            setEmpty(error.getMessage());
        }
    }

    @Override
    public void onItemClicked(int position, PillPeripheral pill) {
        presenter.setTargetPill(pill);

        DfuProgressFragment fragment = new DfuProgressFragment();
        getNavigation().pushFragment(fragment, Navigation.FLAGS_DEFAULT);
    }

    //endregion
}
