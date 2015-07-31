package is.hello.piru.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import is.hello.piru.R;
import is.hello.piru.api.SessionStore;
import is.hello.piru.ui.navigation.Navigation;
import is.hello.piru.ui.screens.SelectImageFragment;
import is.hello.piru.ui.screens.SignInFragment;

import static is.hello.piru.PiruApplication.inject;

public class MainActivity extends AppCompatActivity implements Navigation, FragmentManager.OnBackStackChangedListener {
    @Inject SessionStore sessionStore;

    public MainActivity() {
        inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().addOnBackStackChangedListener(this);
        if (sessionStore.hasSession()) {
            pushFragment(new SelectImageFragment(), Navigation.FLAG_MAKE_HISTORY_ROOT);
        } else {
            pushFragment(new SignInFragment(), Navigation.FLAG_MAKE_HISTORY_ROOT);
        }

        updateUpButton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getSupportFragmentManager().removeOnBackStackChangedListener(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        popFragment();
        return true;
    }

    @Nullable
    @Override
    public Navigation.Fragment getTopFragment() {
        return (Navigation.Fragment) getSupportFragmentManager().findFragmentById(R.id.activity_main_container);
    }

    @Override
    public void pushFragment(@NonNull Fragment fragment, @PushOptions int options) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        if ((options & FLAG_MAKE_HISTORY_ROOT) == FLAG_MAKE_HISTORY_ROOT) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        String tag = fragment.getNavigationTag();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (fragmentManager.findFragmentById(R.id.activity_main_container) != null) {
            transaction.setCustomAnimations(R.anim.fade_slide_up, R.anim.fade_slide_down,
                    0, android.R.anim.fade_out);
            transaction.replace(R.id.activity_main_container, fragment, tag);
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
        updateUpButton();
    }

    @SuppressWarnings("ConstantConditions")
    private void updateUpButton() {
        boolean canGoBack = getSupportFragmentManager().getBackStackEntryCount() > 0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(canGoBack);
    }

    @SuppressWarnings("ConstantConditions")
    private void showFragmentTitles(@Nullable Navigation.Fragment fragment) {
        ActionBar actionBar = getSupportActionBar();
        if (fragment != null) {
            actionBar.setTitle(fragment.getNavigationTitle(this));
            actionBar.setSubtitle(fragment.getNavigationSubtitle(this));
        } else {
            actionBar.setTitle(R.string.app_name);
            actionBar.setSubtitle(null);
        }
    }
}
