package is.hello.piru.api.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.squareup.okhttp.HttpUrl;

import java.util.Date;

public final class FirmwareVersion implements Parcelable {
    @SerializedName("name")
    private String name;

    @SerializedName("url")
    private HttpUrl url;

    @SerializedName("created")
    private long createdAt;


    //region Serialization

    public FirmwareVersion(@NonNull Parcel in) {
        this.name = in.readString();
        this.url = HttpUrl.parse(in.readString());
        this.createdAt = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(url.toString());
        out.writeLong(createdAt);
    }

    public static final Creator<FirmwareVersion> CREATOR = new Creator<FirmwareVersion>() {
        @Override
        public FirmwareVersion createFromParcel(@NonNull Parcel in) {
            return new FirmwareVersion(in);
        }

        @Override
        public FirmwareVersion[] newArray(int size) {
            return new FirmwareVersion[size];
        }
    };

    //endregion


    //region Attributes

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        int lastSlash = name.lastIndexOf("/");
        if (lastSlash == -1) {
            return name;
        } else {
            return name.substring(lastSlash + 1);
        }
    }

    public HttpUrl getUrl() {
        return url;
    }

    public Date getCreatedAt() {
        return new Date(createdAt);
    }

    public String createFilename() {
        int lastNameDot = name.lastIndexOf(".");
        String extension = lastNameDot != -1 ? name.substring(lastNameDot) : "";
        return "Piru-" + createdAt + extension;
    }


    @Override
    public String toString() {
        return "FirmwareVersion{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }

    //endregion
}
