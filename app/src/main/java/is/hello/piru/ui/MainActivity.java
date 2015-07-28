package is.hello.piru.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import is.hello.piru.R;
import is.hello.piru.ui.navigation.Navigation;
import is.hello.piru.ui.screens.SelectImageFragment;

public class MainActivity extends AppCompatActivity implements Navigation, FragmentManager.OnBackStackChangedListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if (savedInstanceState == null) {
            pushFragment(new SelectImageFragment());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getSupportFragmentManager().removeOnBackStackChangedListener(this);
    }

    @Nullable
    @Override
    public Navigation.Fragment getTopFragment() {
        return (Navigation.Fragment) getSupportFragmentManager().findFragmentById(R.id.activity_main_container);
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

        showFragmentTitles(fragment);
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

    @Override
    public void invalidateFragmentTitles(@NonNull Navigation.Fragment fragment) {
        Navigation.Fragment topFragment = getTopFragment();
        if (topFragment == fragment) {
            showFragmentTitles(topFragment);
        }
    }

    @Override
    public void onBackStackChanged() {
        showFragmentTitles(getTopFragment());
    }

    private void showFragmentTitles(@Nullable Navigation.Fragment fragment) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (fragment != null) {
                actionBar.setTitle(fragment.getNavigationTitle(this));
                actionBar.setSubtitle(fragment.getNavigationSubtitle(this));
            } else {
                actionBar.setTitle(R.string.app_name);
                actionBar.setSubtitle(null);
            }
        }
    }
}
