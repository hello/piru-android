package is.hello.piru.api.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public final class FirmwareVersion implements Serializable {
    @SerializedName("name")
    private String name;

    @SerializedName("url")
    private String url;

    @SerializedName("created")
    private long createdAt;


    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public long getCreatedAt() {
        return createdAt;
    }


    @Override
    public String toString() {
        return "FirmwareVersion{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
