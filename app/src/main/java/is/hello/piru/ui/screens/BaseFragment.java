package is.hello.piru.ui.screens;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import is.hello.buruberi.util.Rx;
import is.hello.piru.ui.navigation.Navigation;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

import static is.hello.piru.PiruApplication.inject;

public abstract class BaseFragment extends Navigation.Fragment {
    private final List<Subscription> subscriptions = new ArrayList<>();

    //region Lifecycle

    public BaseFragment() {
        if (wantsInjection()) {
            inject(this);
        }
    }

    protected boolean wantsInjection() {
        return true;
    }

    @Override
    public abstract @Nullable View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        for (Subscription subscription : subscriptions) {
            subscription.unsubscribe();
        }
    }

    //endregion


    //region Scoped Subscriptions

    public <T> Observable<T> bind(@NonNull Observable<T> observable) {
        return observable.observeOn(Rx.mainThreadScheduler())
                         .lift(new Rx.OperatorConditionalBinding<>(this, f -> f.isAdded() && !f.getActivity().isFinishing()));
    }

    public <T> void subscribe(@NonNull Observable<T> observable,
                              @NonNull Action1<T> onNext,
                              @NonNull Action1<Throwable> onError) {
        Subscription subscription = bind(observable).unsafeSubscribe(new Subscriber<T>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                if (isUnsubscribed()) {
                    return;
                }

                onError.call(e);
            }

            @Override
            public void onNext(T value) {
                if (isUnsubscribed()) {
                    return;
                }

                onNext.call(value);
            }
        });
        subscriptions.add(subscription);
    }

    public <T> void subscribe(@NonNull Observable<T> observable,
                              @NonNull Action1<T> onNext) {
        subscribe(observable, onNext, e -> Log.e(getClass().getSimpleName(), "Ignored error", e));
    }

    //endregion
}
