package is.hello.piru.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import is.hello.piru.R;
import is.hello.piru.ui.navigation.Navigation;
import is.hello.piru.ui.screens.SessionFragment;

public class MainActivity extends AppCompatActivity implements Navigation {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            pushFragment(new SessionFragment());
        }
    }

    @Override
    public void pushFragment(@NonNull Navigation.Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        String tag = fragment.getNavigationTag();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (fragmentManager.findFragmentById(R.id.activity_main_container) != null) {
            transaction.replace(R.id.activity_main_container, fragment, tag);
            transaction.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            transaction.addToBackStack(tag);
        } else {
            transaction.add(R.id.activity_main_container, fragment, tag);
        }

        transaction.commit();
    }

    @Override
    public void popFragment(@NonNull Navigation.Fragment fragment) {
        getSupportFragmentManager().popBackStack(fragment.getNavigationTag(),
                FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void popFragment() {
        getSupportFragmentManager().popBackStack();
    }
}
