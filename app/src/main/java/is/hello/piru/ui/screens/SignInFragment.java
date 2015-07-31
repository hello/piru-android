package is.hello.piru.ui.screens;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import javax.inject.Inject;

import is.hello.piru.R;
import is.hello.piru.api.services.SuripuApi;
import is.hello.piru.ui.dialogs.ErrorDialogFragment;
import is.hello.piru.ui.dialogs.LoadingDialogFragment;
import is.hello.piru.ui.navigation.Navigation;
import is.hello.piru.ui.screens.base.BaseFragment;
import is.hello.piru.ui.util.Input;

public class SignInFragment extends BaseFragment {
    @Inject SuripuApi api;

    private EditText emailText;
    private EditText passwordText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        this.emailText = (EditText) view.findViewById(R.id.fragment_sign_in_email);
        this.passwordText = (EditText) view.findViewById(R.id.fragment_sign_in_password);
        Input.setOnSubmitListener(passwordText, this::submit);

        Button submitButton = (Button) view.findViewById(R.id.fragment_sign_in_action);
        submitButton.setOnClickListener(this::submit);

        return view;
    }

    public void submit(@NonNull View sender) {
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment.Builder()
                    .withMessage(R.string.error_missing_email_or_password)
                    .build();
            errorDialogFragment.show(getFragmentManager());

            return;
        }

        Input.closeSoftKeyboard(passwordText);

        LoadingDialogFragment.show(getFragmentManager());
        subscribe(api.authorize(email, password),
                ignored -> {
                    LoadingDialogFragment.close(getFragmentManager());
                    getNavigation().pushFragment(new SelectFirmwareFragment(), Navigation.FLAG_MAKE_HISTORY_ROOT);
                },
                this::presentError);
    }

    public void presentError(Throwable e) {
        LoadingDialogFragment.close(getFragmentManager());

        ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment.Builder()
                .withError(e)
                .build();
        errorDialogFragment.show(getFragmentManager());

        passwordText.requestFocus();
    }
}
