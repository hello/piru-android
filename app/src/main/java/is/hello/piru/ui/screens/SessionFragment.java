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
import android.widget.TextView;

import javax.inject.Inject;

import is.hello.piru.R;
import is.hello.piru.api.SessionPresenter;
import is.hello.piru.ui.dialogs.ErrorDialogFragment;
import is.hello.piru.ui.dialogs.LoadingDialogFragment;
import is.hello.piru.ui.screens.base.BaseFragment;
import is.hello.piru.ui.util.Input;

public class SessionFragment extends BaseFragment {
    @Inject SessionPresenter presenter;

    private TextView title;
    private EditText emailText;
    private EditText passwordText;
    private Button actionButton;


    //region Lifecycle

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_session, container, false);

        this.title = (TextView) view.findViewById(R.id.fragment_session_title);
        this.emailText = (EditText) view.findViewById(R.id.fragment_session_email);
        this.passwordText = (EditText) view.findViewById(R.id.fragment_session_password);
        this.actionButton = (Button) view.findViewById(R.id.fragment_session_action);

        Input.setOnSubmitListener(passwordText, this::submitCredentials);

        TextView info = (TextView) view.findViewById(R.id.fragment_session_info);
        info.setText(presenter.getEndpoint().getUrl());

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        subscribe(presenter.hasSession, this::bindHasSession);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        this.title = null;
        this.emailText = null;
        this.passwordText = null;
        this.actionButton = null;
    }

    //endregion


    //region Bindings

    public void bindHasSession(boolean hasSession) {
        emailText.setEnabled(!hasSession);
        passwordText.setEnabled(!hasSession);

        if (hasSession) {
            title.setText(R.string.signed_in);
            actionButton.setText(R.string.sign_out);
            actionButton.setOnClickListener(this::clearSession);
        } else {
            title.setText(R.string.sign_in);
            actionButton.setText(R.string.sign_in);
            actionButton.setOnClickListener(this::submitCredentials);

            emailText.requestFocus();
        }
    }

    //endregion


    //region Actions

    public void submitCredentials(@NonNull View sender) {
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment.Builder()
                    .withMessage(R.string.error_credentials_missing)
                    .build();
            errorDialogFragment.show(getFragmentManager());
            return;
        }

        Input.closeSoftKeyboard(passwordText);

        LoadingDialogFragment.show(getFragmentManager());
        subscribe(presenter.authorize(email, password), session -> {
            LoadingDialogFragment.close(getFragmentManager());

            emailText.setText(null);
            passwordText.setText(null);

            getNavigation().pushFragment(new SenseFragment());
        }, error -> {
            LoadingDialogFragment.close(getFragmentManager());

            ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment.Builder()
                    .withError(error)
                    .build();
            errorDialogFragment.show(getFragmentManager());
        });
    }

    public void clearSession(@NonNull View sender) {
        presenter.clearSession();
    }

    //endregion
}
