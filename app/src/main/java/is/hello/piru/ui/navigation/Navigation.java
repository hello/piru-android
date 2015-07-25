package is.hello.piru.ui.navigation;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

public interface Navigation {
    void pushFragment(@NonNull Navigation.Fragment fragment);
    void popFragment(@NonNull Navigation.Fragment fragment);
    void popFragment();

    @SuppressLint("ValidFragment")
    class Fragment extends android.support.v4.app.Fragment {
        public String getNavigationTag() {
            return getClass().getSimpleName();
        }

        public Navigation getNavigation() {
            return (Navigation) getActivity();
        }
    }
}
