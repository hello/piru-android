package is.hello.piru.ui.screens;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

public class SessionFragment extends BaseFragment {
    @Inject SessionPresenter presenter;

    private TextView title;
    private EditText email;
    private EditText password;
    private Button action;


    //region Lifecycle

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_session, container, false);

        this.title = (TextView) view.findViewById(R.id.fragment_session_title);
        this.email = (EditText) view.findViewById(R.id.fragment_session_email);
        this.password = (EditText) view.findViewById(R.id.fragment_session_password);
        this.action = (Button) view.findViewById(R.id.fragment_session_action);

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
        this.email = null;
        this.password = null;
        this.action = null;
    }

    //endregion


    //region Bindings

    public void bindHasSession(boolean hasSession) {
        email.setEnabled(!hasSession);
        password.setEnabled(!hasSession);

        if (hasSession) {
            title.setText(R.string.signed_in);
            action.setText(R.string.sign_out);
            action.setOnClickListener(this::clearSession);
        } else {
            title.setText(R.string.sign_in);
            action.setText(R.string.sign_in);
            action.setOnClickListener(this::submitCredentials);

            email.requestFocus();
        }
    }

    //endregion


    //region Actions

    public void submitCredentials(@NonNull View sender) {
        subscribe(presenter.authorize(email.getText().toString(), password.getText().toString()), session -> {
            email.setText(null);
            password.setText(null);
        }, error -> {
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
