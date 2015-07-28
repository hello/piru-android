package is.hello.piru.ui.navigation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import is.hello.piru.R;

public interface Navigation {
    int FLAGS_DEFAULT = 0;
    int FLAG_MAKE_HISTORY_ROOT = (1 << 1);

    @IntDef(flag = true, value = {
            FLAGS_DEFAULT,
            FLAG_MAKE_HISTORY_ROOT,
    })
    @Retention(RetentionPolicy.SOURCE)
    @Documented
    @Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
    @interface PushOptions {}

    @Nullable Navigation.Fragment getTopFragment();
    void pushFragment(@NonNull Fragment fragment, @PushOptions int options);
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
