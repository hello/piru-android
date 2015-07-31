package is.hello.piru.api;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import is.hello.piru.R;
import is.hello.piru.api.model.FirmwareType;
import is.hello.piru.api.model.FirmwareVersion;
import is.hello.piru.api.services.SuripuApi;
import is.hello.piru.api.util.Download;
import is.hello.piru.ui.util.PresenterSubject;
import rx.Observable;
import rx.Subscription;

@Singleton public class FirmwarePresenter {
    private static final String SAVED_VERSIONS = FirmwarePresenter.class.getName() + ".SAVED_VERSIONS";
    private static final String SAVED_TYPE = FirmwarePresenter.class.getName() + ".SAVED_TYPE";

    @Inject OkHttpClient httpClient;
    @Inject SuripuApi api;

    public final PresenterSubject<ArrayList<FirmwareVersion>> versions = PresenterSubject.create();

    private @Nullable WeakReference<Subscription> currentUpdate;
    private FilterType filterType = FilterType.UNSTABLE;

    //region Lifecycle

    public void restoreInstanceState(@NonNull Bundle inState) {
        if (!versions.hasValue()) {
            this.filterType = FilterType.valueOf(inState.getString(SAVED_TYPE));

            @SuppressWarnings("unchecked")
            ArrayList<FirmwareVersion> restored = (ArrayList<FirmwareVersion>) inState.getSerializable(SAVED_VERSIONS);
            versions.onNext(restored);
        }
    }

    public void saveInstanceState(@NonNull Bundle outState) {
        outState.putString(SAVED_TYPE, filterType.toString());
        versions.saveState(SAVED_VERSIONS, outState);
    }

    public void update() {
        Subscription currentUpdate = this.currentUpdate != null ? this.currentUpdate.get() : null;
        if (currentUpdate != null) {
            currentUpdate.unsubscribe();
            this.currentUpdate.clear();
            this.currentUpdate = null;
        }

        this.currentUpdate = new WeakReference<>(createUpdate().subscribe(versions));
    }

    //endregion


    //region Attributes

    private Observable<ArrayList<FirmwareVersion>> createUpdate() {
        switch (filterType) {
            case STABLE:
                return api.getStable(FirmwareType.PILL);
            case UNSTABLE:
                return api.getUnstable(FirmwareType.PILL);
            default:
                throw new IllegalStateException("Unknown type " + filterType);
        }
    }

    public void setFilterType(@NonNull FilterType filterType) {
        if (this.filterType != filterType || !versions.hasValue()) {
            Log.d(getClass().getSimpleName(), "setFilterType(" + filterType + ")");

            this.filterType = filterType;
            update();
        }
    }

    public FilterType getFilterType() {
        return filterType;
    }

    //endregion


    //region Downloads

    public File createTemporaryFile(@NonNull FirmwareVersion version) {
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return new File(directory, filterType + "-" + version.createFilename());
    }

    public Observable<Integer> downloadFirmwareVersion(@NonNull FirmwareVersion version,
                                                       @NonNull File destination) {
        return Download.toFile(httpClient, version.getUrl(), destination);
    }

    //endregion


    public enum FilterType {
        STABLE(R.string.title_filter_type_stable),
        UNSTABLE(R.string.title_filter_type_unstable);

        public final @StringRes int titleRes;

        FilterType(@StringRes int titleRes) {
            this.titleRes = titleRes;
        }
    }
}
