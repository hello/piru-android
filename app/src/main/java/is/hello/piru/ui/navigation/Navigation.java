package is.hello.piru.ui.navigation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import is.hello.piru.R;

public interface Navigation {
    @Nullable Navigation.Fragment getTopFragment();
    void pushFragment(@NonNull Navigation.Fragment fragment);
    void popFragment(@NonNull Navigation.Fragment fragment);
    void popFragment();
    void invalidateFragmentTitles(@NonNull Navigation.Fragment fragment);

    @SuppressLint("ValidFragment")
    class Fragment extends android.support.v4.app.Fragment {
        public CharSequence getNavigationTitle(@NonNull Context context) {
            return context.getString(R.string.app_name);
        }

        public CharSequence getNavigationSubtitle(@NonNull Context context) {
            return null;
        }

        public void invalidateTitles() {
            Navigation navigation = getNavigation();
            if (navigation != null) {
                navigation.invalidateFragmentTitles(this);
            }
        }

        public String getNavigationTag() {
            return getClass().getSimpleName();
        }

        public Navigation getNavigation() {
            return (Navigation) getActivity();
        }
    }
}
