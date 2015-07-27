package is.hello.piru;

import android.app.Application;

import dagger.ObjectGraph;
import is.hello.piru.bluetooth.BluetoothModule;

public class PiruApplication extends Application {
    private static PiruApplication instance;

    private ObjectGraph graph;

    public static PiruApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        PiruApplication.instance = this;

        this.graph = ObjectGraph.create(
                new BluetoothModule(),
                new PiruAppModule(this)
        );
    }

    public static <T> T inject(T instance) {
        return getInstance().graph.inject(instance);
    }
}
