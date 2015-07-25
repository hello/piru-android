package is.hello.piru.api.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;
import org.joda.time.Hours;

import java.util.HashMap;
import java.util.Map;

public class Device {
    public static final int MISSING_THRESHOLD_HRS = 24;

    @SerializedName("type")
    private Type type;

    @SerializedName("device_id")
    private String deviceId;

    @SerializedName("state")
    private State state;

    @SerializedName("firmware_version")
    private String firmwareVersion;

    @SerializedName("last_updated")
    private DateTime lastUpdated;

    @SerializedName("color")
    private Color color;

    @Expose(deserialize = false, serialize = false)
    private boolean exists = true;


    //region Util

    public static Map<Type, Device> getDevicesMap(@NonNull Iterable<Device> devices) {
        Map<Type, Device> map = new HashMap<>();
        for (Device device : devices) {
            map.put(device.getType(), device);
        }
        return map;
    }

    //endregion


    //region Creation

    public static Device createPlaceholder(@NonNull Type type) {
        Device device = new Device();
        device.type = type;
        device.exists = false;
        device.state = State.UNKNOWN;
        return device;
    }

    //endregion


    public Type getType() {
        return type;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public State getState() {
        return state;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public DateTime getLastUpdated() {
        return lastUpdated;
    }

    public Color getColor() {
        return color;
    }

    public boolean exists() {
        return exists;
    }

    /**
     * Returns the number of hours since the device was last updated.
     * <p/>
     * Returns 0 if the device has not reported being update yet. This state
     * happens immediately after a device has been paired to an account.
     */
    public int getHoursSinceLastUpdated() {
        if (lastUpdated != null) {
            return Hours.hoursBetween(lastUpdated, DateTime.now()).getHours();
        } else {
            return 0;
        }
    }

    /**
     * Returns whether or not the device is considered to be missing.
     * <p/>
     * Differs from {@link #getHoursSinceLastUpdated()} by considering
     * a missing last updated value to indicate a device is missing.
     */
    public boolean isMissing() {
        return (!exists || (getLastUpdated() == null) ||
                (getHoursSinceLastUpdated() >= MISSING_THRESHOLD_HRS));
    }

    @Override
    public String toString() {
        return "Device{" +
                "type=" + type +
                ", deviceId='" + deviceId + '\'' +
                ", state=" + state +
                ", firmwareVersion='" + firmwareVersion + '\'' +
                ", lastUpdated=" + lastUpdated +
                ", color=" + color +
                ", exists=" + exists +
                '}';
    }


    public enum Type {
        PILL,
        SENSE,
        OTHER,
    }

    public enum State {
        NORMAL,
        LOW_BATTERY,
        UNKNOWN,
    }

    public enum Color {
        BLACK,
        WHITE,
        BLUE,
        RED,
        UNKNOWN
    }
}
