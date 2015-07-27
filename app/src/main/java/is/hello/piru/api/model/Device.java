package is.hello.piru.api.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class Device {
    @SerializedName("type")
    private Type type;

    @SerializedName("device_id")
    private String deviceId;

    @SerializedName("state")
    private State state;

    @SerializedName("firmware_version")
    private String firmwareVersion;

    @SerializedName("last_updated")
    private long lastUpdated;

    @SerializedName("color")
    private Color color;


    //region Util

    public static Map<Type, Device> getDevicesMap(@NonNull Iterable<Device> devices) {
        Map<Type, Device> map = new HashMap<>();
        for (Device device : devices) {
            map.put(device.getType(), device);
        }
        return map;
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

    public long getLastUpdated() {
        return lastUpdated;
    }

    public Color getColor() {
        return color;
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
