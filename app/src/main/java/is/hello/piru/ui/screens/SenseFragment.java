package is.hello.piru.ui.screens;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import is.hello.buruberi.bluetooth.devices.HelloPeripheral;
import is.hello.piru.R;
import is.hello.piru.api.SuripuApi;
import is.hello.piru.api.model.Device;
import is.hello.piru.bluetooth.SensePresenter;
import is.hello.piru.ui.adapters.ArrayRecyclerAdapter;
import is.hello.piru.ui.adapters.CardDecoration;
import is.hello.piru.ui.dialogs.ErrorDialogFragment;
import is.hello.piru.ui.dialogs.LoadingDialogFragment;
import is.hello.piru.ui.screens.base.RecyclerFragment;

public class SenseFragment extends RecyclerFragment {
    @Inject SuripuApi api;
    @Inject SensePresenter presenter;

    private DevicesAdapter adapter;
    private MenuItem nextItem;

    //region Lifecycle


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    protected void onConfigureRecycler(@NonNull RecyclerView recyclerView) {
        recyclerView.addItemDecoration(new CardDecoration(getResources()));

        this.adapter = new DevicesAdapter(getActivity());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setBusy(true);
        subscribe(api.registeredDevices(), this::bindDevices, this::presentError);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        this.nextItem = null;
        this.adapter = null;
    }

    //endregion

    //region Menu

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.simple_next, menu);

        this.nextItem = menu.findItem(R.id.action_next);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_next: {
                next();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        nextItem.setEnabled(adapter.getSelectedDeviceCount() == 2);
    }

    public void invalidateOptionsMenu() {
        getActivity().invalidateOptionsMenu();
    }

    //endregion


    //region Bindings

    public void bindDevices(@NonNull List<Device> devices) {
        setBusy(false);

        adapter.clear();
        adapter.addAll(devices);
    }

    public void presentError(@NonNull Throwable e) {
        LoadingDialogFragment.close(getFragmentManager());
        setBusy(false);

        ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment.Builder().withError(e).build();
        errorDialogFragment.show(getFragmentManager());
    }

    //endregion


    //region Starting dfu

    private void next() {
        List<Device> devices = adapter.getSelectedDevices();
        Map<Device.Type, Device> devicesMap = Device.getDevicesMap(devices);
        if (devicesMap.size() != 2) {
            ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment.Builder()
                    .withMessage(R.string.error_wrong_device_selection)
                    .build();
            errorDialogFragment.show(getFragmentManager());

            return;
        }

        connect(devicesMap.get(Device.Type.SENSE), devicesMap.get(Device.Type.PILL));
    }

    private void connect(@NonNull Device sense, @NonNull Device pill) {
        LoadingDialogFragment loadingDialogFragment = LoadingDialogFragment.show(getFragmentManager());
        subscribe(presenter.connectToSense(sense, false),
                status -> {
                    if (status == HelloPeripheral.ConnectStatus.CONNECTED) {
                        loadingDialogFragment.setText("Beginning Dfu");
                        beginDfu(pill);
                    } else {
                        loadingDialogFragment.setText(status.toString());
                    }
                },
                this::presentError);
    }

    private void beginDfu(@NonNull Device pill) {
        subscribe(presenter.beginPillDfu(pill.getDeviceId()),
                ignored -> {
                    LoadingDialogFragment.close(getFragmentManager());
                    getNavigation().pushFragment(new SelectPillFragment());
                },
                this::presentError);
    }

    //endregion


    class DevicesAdapter extends ArrayRecyclerAdapter<Device, DevicesAdapter.ViewHolder> {
        private final LayoutInflater inflater;
        private final SparseBooleanArray selectedPositions = new SparseBooleanArray();

        DevicesAdapter(@NonNull Context context) {
            this.inflater = LayoutInflater.from(context);
        }


        int getSelectedDeviceCount() {
            return selectedPositions.size();
        }

        List<Device> getSelectedDevices() {
            List<Device> devices = new ArrayList<>();
            for (int i = 0, size = selectedPositions.size(); i < size; i++) {
                int key = selectedPositions.keyAt(i);
                if (selectedPositions.get(key)) {
                    devices.add(getItem(key));
                }
            }
            return devices;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.item_device, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Device device = getItem(position);

            holder.selected.setChecked(selectedPositions.get(position));
            holder.nameText.setText(device.getType().toString());
            holder.idText.setText(device.getDeviceId());
            holder.versionText.setText(device.getFirmwareVersion());
            holder.colorText.setText(device.getColor().toString());
        }


        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final CheckBox selected;
            final TextView nameText;
            final TextView idText;
            final TextView versionText;
            final TextView colorText;

            ViewHolder(@NonNull View itemView) {
                super(itemView);

                this.selected = (CheckBox) itemView.findViewById(R.id.item_device_selected);
                this.nameText = (TextView) itemView.findViewById(R.id.item_device_name);
                this.idText = (TextView) itemView.findViewById(R.id.item_device_id);
                this.versionText = (TextView) itemView.findViewById(R.id.item_device_version);
                this.colorText = (TextView) itemView.findViewById(R.id.item_device_color);

                itemView.setOnClickListener(this);
                selected.setOnClickListener(this);
            }

            @Override
            public void onClick(View ignored) {
                int position = getAdapterPosition();
                if (selectedPositions.get(position)) {
                    selectedPositions.delete(position);
                    selected.setChecked(false);
                } else {
                    selectedPositions.put(position, true);
                    selected.setChecked(true);
                }

                invalidateOptionsMenu();
            }
        }
    }
}
