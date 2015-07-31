package is.hello.piru.api.util;

import android.support.annotation.NonNull;
import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import is.hello.buruberi.util.Rx;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

/**
 * Downloads the resource at a given URL to an output stream, typically a file.
 */
public final class Download implements Observable.OnSubscribe<Integer> {
    private static final String LOG_TAG = Download.class.getSimpleName();
    private static final int CHUNK_SIZE = 4096;

    private final OkHttpClient client;
    private final HttpUrl url;
    private final StreamProvider provider;

    //region Creation

    public static Observable<Integer> create(@NonNull OkHttpClient client,
                                             @NonNull HttpUrl url,
                                             @NonNull StreamProvider provider) {
        return Observable.create(new Download(client, url, provider))
                         .onBackpressureDrop()
                         .subscribeOn(Schedulers.io())
                         .observeOn(Rx.mainThreadScheduler());
    }

    public static Observable<Integer> toFile(@NonNull OkHttpClient client,
                                             @NonNull HttpUrl url,
                                             @NonNull File outputFile) {
        return create(client, url, () -> new FileOutputStream(outputFile));
    }

    private Download(@NonNull OkHttpClient client,
                     @NonNull HttpUrl url,
                     @NonNull StreamProvider provider) {
        this.client = client;
        this.url = url;
        this.provider = provider;
    }

    //endregion


    //region Processing

    private static int calculateProgress(long length, long loaded) {
        int progress = (int) Math.round((loaded / (double) length) * 100.0);
        Log.d(LOG_TAG, "Download " + progress + "% complete");
        return progress;
    }

    @Override
    public void call(Subscriber<? super Integer> subscriber) {
        try {
            Log.d(LOG_TAG, "Starting download for '" + url + "'");

            Call call = client.newCall(new Request.Builder().url(url).build());
            Subscription child = Subscriptions.create(call::cancel);
            subscriber.add(child);

            Response response = call.execute();
            ResponseBody body = response.body();

            long length = body.contentLength(),
                 loaded = 0;
            byte[] buffer = new byte[CHUNK_SIZE];
            try (InputStream in = body.byteStream();
                 OutputStream out = provider.openStream()) {
                int chunkAmount;
                while ((chunkAmount = in.read(buffer)) > 0) {
                    if (child.isUnsubscribed()) {
                        Log.d(LOG_TAG, "Request canceled, terminating early");
                        return;
                    }

                    out.write(buffer, 0, chunkAmount);

                    loaded += chunkAmount;
                    subscriber.onNext(calculateProgress(length, loaded));
                }
                out.flush();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Download for '" + url + "' failed", e);
            subscriber.onError(e);
        }

        Log.d(LOG_TAG, "Finished download for '" + url + "'");
        subscriber.onCompleted();
    }

    //endregion


    public interface StreamProvider {
        @NonNull OutputStream openStream() throws IOException;
    }
}
