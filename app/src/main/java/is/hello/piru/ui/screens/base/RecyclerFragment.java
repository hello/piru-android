package is.hello.piru.ui.screens.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import is.hello.piru.R;

public abstract class RecyclerFragment extends BaseFragment {
    private boolean busy = false;

    protected RecyclerView recycler;
    protected ProgressBar busyIndicator;

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

        this.recycler = (RecyclerView) view.findViewById(R.id.fragment_recycler_view);
        this.busyIndicator = (ProgressBar) view.findViewById(R.id.fragment_recycler_busy);

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

            if (recycler != null && busyIndicator != null) {
                if (busy) {
                    recycler.setVisibility(View.INVISIBLE);
                    busyIndicator.setVisibility(View.VISIBLE);
                } else {
                    recycler.setVisibility(View.VISIBLE);
                    busyIndicator.setVisibility(View.GONE);
                }
            }
        }
    }
}
