package is.hello.piru.api;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import is.hello.piru.api.model.FirmwareType;
import is.hello.piru.api.model.FirmwareVersion;
import is.hello.piru.api.services.SuripuApi;
import is.hello.piru.ui.util.PresenterSubject;
import rx.Observable;
import rx.Subscription;

@Singleton public class FirmwarePresenter {
    private static final String SAVED_VERSIONS = FirmwarePresenter.class.getName() + ".SAVED_VERSIONS";
    private static final String SAVED_TYPE = FirmwarePresenter.class.getName() + ".SAVED_TYPE";

    @Inject SuripuApi api;

    public final PresenterSubject<ArrayList<FirmwareVersion>> versions = PresenterSubject.create();

    private @Nullable WeakReference<Subscription> currentUpdate;
    private Type type = Type.UNSTABLE;

    //region Lifecycle

    public void restoreInstanceState(@NonNull Bundle inState) {
        if (!versions.hasValue()) {
            this.type = Type.valueOf(inState.getString(SAVED_TYPE));

            @SuppressWarnings("unchecked")
            ArrayList<FirmwareVersion> restored = (ArrayList<FirmwareVersion>) inState.getSerializable(SAVED_VERSIONS);
            versions.onNext(restored);
        }
    }

    public void saveInstanceState(@NonNull Bundle outState) {
        outState.putString(SAVED_TYPE, type.toString());
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
        switch (type) {
            case STABLE:
                return api.getStable(FirmwareType.PILL);
            case UNSTABLE:
                return api.getUnstable(FirmwareType.PILL);
            default:
                throw new IllegalStateException("Unknown type " + type);
        }
    }

    public void setType(@NonNull Type type) {
        this.type = type;
        update();
    }

    //endregion


    public enum Type {
        STABLE,
        UNSTABLE,
    }
}
