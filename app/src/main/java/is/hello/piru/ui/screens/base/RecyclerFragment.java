package is.hello.piru.ui.screens.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import is.hello.piru.R;

public abstract class RecyclerFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private boolean busy = false;

    protected SwipeRefreshLayout swipeRefreshLayout;
    protected RecyclerView recycler;
    protected ProgressBar busyIndicator;
    protected TextView empty;

    //region Lifecycle

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.busy = savedInstanceState.getBoolean("busy");
        }
    }

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler, container, false);

        this.swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_recycler_view_refresh);
        this.recycler = (RecyclerView) view.findViewById(R.id.fragment_recycler_view);
        this.busyIndicator = (ProgressBar) view.findViewById(R.id.fragment_recycler_busy);
        this.empty = (TextView) view.findViewById(R.id.fragment_recycler_empty);

        swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.primary_dark,
                R.color.secondary, R.color.secondary_dark);
        swipeRefreshLayout.setEnabled(!busy);
        swipeRefreshLayout.setOnRefreshListener(this);

        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler.setHasFixedSize(true);

        onConfigureRecycler(recycler);

        if (busy) {
            recycler.setVisibility(View.INVISIBLE);
            busyIndicator.setVisibility(View.VISIBLE);
        }

        return view;
    }

    protected abstract void onConfigureRecycler(@NonNull RecyclerView recyclerView);

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        this.recycler = null;
        this.busyIndicator = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("busy", busy);
    }

    //endregion


    public void setBusy(boolean busy) {
        if (busy != this.busy) {
            this.busy = busy;

            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.setEnabled(!busy);
            }

            if (recycler != null && busyIndicator != null) {
                if (busy) {
                    setEmpty(null);
                    recycler.setVisibility(View.INVISIBLE);
                    busyIndicator.setVisibility(View.VISIBLE);
                } else {
                    recycler.setVisibility(View.VISIBLE);
                    busyIndicator.setVisibility(View.GONE);
                }
            }

            invalidateTitles();
        }
    }

    public void setEmpty(@Nullable CharSequence message) {
        if (empty != null) {
            if (TextUtils.isEmpty(message)) {
                empty.setText(null);
                empty.setVisibility(View.VISIBLE);
            } else {
                empty.setText(message);
                empty.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onRefresh() {

    }
}
