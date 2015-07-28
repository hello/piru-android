package is.hello.piru.ui.screens;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import javax.inject.Inject;

import is.hello.piru.R;
import is.hello.piru.bluetooth.PillDfuPresenter;
import is.hello.piru.ui.screens.base.BaseFragment;
import is.hello.piru.ui.util.FileUtils;

public class SelectImageFragment extends BaseFragment {
    private static final int REQUEST_CODE_IMAGE = 0x01;

    @Inject PillDfuPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_image, container, false);

        Button select = (Button) view.findViewById(R.id.fragment_select_image_button);
        select.setOnClickListener(this::select);

        return view;
    }

    @Override
    public CharSequence getNavigationSubtitle(@NonNull Context context) {
        return "Select Firmware Image";
    }

    public void select(@NonNull View ignored) {
        Intent intent = FileUtils.createGetContentIntent();
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        startActivityForResult(intent, REQUEST_CODE_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = data.getData();
            presenter.setImageUri(imageUri);

            Log.d(getClass().getSimpleName(), "Picked '" + imageUri + "'");

            getNavigation().pushFragment(new BleIntroFragment());
        }
    }
}
