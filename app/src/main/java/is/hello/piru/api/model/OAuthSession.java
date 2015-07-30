package is.hello.piru.api.model;

import com.google.gson.annotations.SerializedName;

public class OAuthSession {
    @SerializedName("access_token")
    public String accessToken;

    @SerializedName("expires_in")
    public long expiresIn;

    @SerializedName("refresh_token")
    public String refreshToken;

    @SerializedName("token_type")
    public String tokenType;

    @SerializedName("account_id")
    public String accountId;

    @Override
    public String toString() {
        return "OAuthSession{" +
                "accessToken='" + accessToken + '\'' +
                ", expiresIn=" + expiresIn +
                ", refreshToken='" + refreshToken + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", accountId='" + accountId + '\'' +
                '}';
    }
}
