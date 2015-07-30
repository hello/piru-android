package is.hello.piru.ui.screens;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import javax.inject.Inject;

import is.hello.piru.api.FirmwarePresenter;
import is.hello.piru.api.model.FirmwareVersion;
import is.hello.piru.bluetooth.PillDfuPresenter;
import is.hello.piru.ui.adapters.ArrayRecyclerAdapter;
import is.hello.piru.ui.adapters.FirmwareAdapter;
import is.hello.piru.ui.adapters.HorizontalDividerDecoration;
import is.hello.piru.ui.dialogs.ErrorDialogFragment;
import is.hello.piru.ui.navigation.Navigation;
import is.hello.piru.ui.screens.base.RecyclerFragment;
import is.hello.piru.ui.util.FileUtils;

public class SelectImageFragment extends RecyclerFragment implements ArrayRecyclerAdapter.OnItemClickedListener<FirmwareVersion> {
    private static final int REQUEST_CODE_IMAGE = 0x01;

    @Inject FirmwarePresenter firmwarePresenter;
    @Inject PillDfuPresenter pillDfuPresenter;

    private FirmwareAdapter adapter;

    //region Lifecycle

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            firmwarePresenter.restoreInstanceState(savedInstanceState);
        } else {
            firmwarePresenter.setType(FirmwarePresenter.Type.UNSTABLE);
        }

        setRetainInstance(true);
    }

    @Override
    protected void onConfigureRecycler(@NonNull RecyclerView recyclerView) {
        recyclerView.addItemDecoration(new HorizontalDividerDecoration(getResources()));

        this.adapter = new FirmwareAdapter(getActivity());
        adapter.setOnItemClickedListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        subscribe(firmwarePresenter.versions, this::bindVersions, this::presentError);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        firmwarePresenter.saveInstanceState(outState);
    }

    //endregion


    //region Menu

    @Override
    public CharSequence getNavigationSubtitle(@NonNull Context context) {
        return "Select Firmware Image";
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //endregion


    //region Bindings

    public void bindVersions(@NonNull ArrayList<FirmwareVersion> versions) {
        setBusy(false);
    }

    public void presentError(Throwable e) {
        setBusy(false);

        ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment.Builder()
                .withError(e)
                .build();
        errorDialogFragment.show(getFragmentManager());
    }

    @Override
    public void onItemClicked(int position, FirmwareVersion item) {

    }

    //endregion


    //region Local Images

    private void selectLocal() {
        Intent intent = FileUtils.createGetContentIntent();
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        startActivityForResult(intent, REQUEST_CODE_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = data.getData();
            pillDfuPresenter.setFirmwareImage(imageUri);

            Log.d(getClass().getSimpleName(), "Picked '" + imageUri + "'");

            getNavigation().pushFragment(new BleIntroFragment(), Navigation.FLAGS_DEFAULT);
        }
    }

    //endregion
}
