package is.hello.piru.ui.screens;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import javax.inject.Inject;

import is.hello.piru.R;
import is.hello.piru.api.FirmwarePresenter;
import is.hello.piru.api.model.FirmwareVersion;
import is.hello.piru.bluetooth.PillDfuPresenter;
import is.hello.piru.ui.adapters.ArrayRecyclerAdapter;
import is.hello.piru.ui.adapters.FirmwareVersionAdapter;
import is.hello.piru.ui.adapters.HorizontalDividerDecoration;
import is.hello.piru.ui.dialogs.DownloadDialogFragment;
import is.hello.piru.ui.dialogs.ErrorDialogFragment;
import is.hello.piru.ui.navigation.Navigation;
import is.hello.piru.ui.screens.base.RecyclerFragment;
import is.hello.piru.ui.util.FileUtils;

public class SelectFirmwareFragment extends RecyclerFragment implements ArrayRecyclerAdapter.OnItemClickedListener<FirmwareVersion>,ArrayRecyclerAdapter.OnItemLongClickedListener<FirmwareVersion> {
    private static final int REQUEST_CODE_IMAGE = 0x01;
    private static final int REQUEST_CODE_DOWNLOAD = 0x02;

    @Inject FirmwarePresenter firmwarePresenter;
    @Inject PillDfuPresenter pillDfuPresenter;

    private FirmwareVersionAdapter adapter;

    //region Lifecycle

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            firmwarePresenter.restoreInstanceState(savedInstanceState);
            invalidateTitles();
        } else {
            firmwarePresenter.setFilterType(FirmwarePresenter.FilterType.UNSTABLE);
        }

        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    protected void onConfigureRecycler(@NonNull RecyclerView recyclerView) {
        recyclerView.addItemDecoration(new HorizontalDividerDecoration(getResources()));

        this.adapter = new FirmwareVersionAdapter(getActivity());
        adapter.setOnItemClickedListener(this);
        adapter.setOnItemLongClickedListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setBusy(true);
        subscribe(firmwarePresenter.versions, this::bindVersions, this::presentError);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        firmwarePresenter.saveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        this.adapter = null;
    }

    //endregion


    //region Menu

    @Override
    public CharSequence getNavigationTitle(@NonNull Context context) {
        return context.getString(R.string.title_select_firmware);
    }

    @Override
    public CharSequence getNavigationSubtitle(@NonNull Context context) {
        return context.getString(firmwarePresenter.getFilterType().titleRes);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_select_image, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_stable:
                firmwarePresenter.setFilterType(FirmwarePresenter.FilterType.STABLE);
                invalidateTitles();
                return true;

            case R.id.action_show_unstable:
                firmwarePresenter.setFilterType(FirmwarePresenter.FilterType.UNSTABLE);
                invalidateTitles();
                return true;

            case R.id.action_choose_file:
                chooseLocalFile();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefresh() {
        firmwarePresenter.update();
        setBusy(true);
    }

    //endregion


    //region Bindings

    public void bindVersions(@NonNull ArrayList<FirmwareVersion> versions) {
        setBusy(false);

        adapter.clear();
        adapter.addAll(versions);
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
        DownloadDialogFragment downloadDialogFragment = DownloadDialogFragment.newInstance(item);
        downloadDialogFragment.setTargetFragment(this, REQUEST_CODE_DOWNLOAD);
        downloadDialogFragment.show(getFragmentManager(), DownloadDialogFragment.TAG);
    }

    @Override
    public boolean onItemLongClicked(int position, FirmwareVersion item) {
        Toast.makeText(getActivity().getApplicationContext(),
                item.getName(), Toast.LENGTH_SHORT).show();
        return true;
    }

    //endregion


    //region Local Images

    private void chooseLocalFile() {
        Intent intent = FileUtils.createGetContentIntent();
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        startActivityForResult(intent, REQUEST_CODE_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == REQUEST_CODE_IMAGE || requestCode == REQUEST_CODE_DOWNLOAD)
                && resultCode == Activity.RESULT_OK) {
            Uri imageUri = data.getData();
            pillDfuPresenter.setFirmwareImage(imageUri);
            getNavigation().pushFragment(new BleIntroFragment(), Navigation.FLAGS_DEFAULT);
        }
    }

    //endregion
}
