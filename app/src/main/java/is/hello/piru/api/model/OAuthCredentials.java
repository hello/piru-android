package is.hello.piru.api.model;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import retrofit.mime.TypedOutput;

public class OAuthCredentials implements TypedOutput {
    public static final String CLIENT_ID = "nonsensical-fantastic-android-pill-flasher";
    public static final String SHARED_SECRET = "13708312-3705-11e5-b47a-2cf0ee0ea64c";


    public final String username;
    public final String password;
    private final ByteArrayOutputStream outputStream;

    public OAuthCredentials(@NonNull String username,
                            @NonNull String password) {
        if (TextUtils.isEmpty(username)) {
            throw new IllegalArgumentException("username cannot be omitted");
        }

        if (TextUtils.isEmpty(password)) {
            throw new IllegalArgumentException("password cannot be omitted");
        }

        this.username = username;
        this.password = password;
        this.outputStream = new ByteArrayOutputStream();

        generate();
    }


    private void addField(@NonNull String name, @NonNull String value) {
        try {
            if (outputStream.size() > 0)
                outputStream.write('&');

            String pair = Uri.encode(name) + "=" + Uri.encode(value);
            outputStream.write(pair.getBytes("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void generate() {
        addField("grant_type", "password");
        addField("client_id", CLIENT_ID);
        addField("client_secret", SHARED_SECRET);
        addField("username", username);
        addField("password", password);
    }

    @Override
    public String fileName() {
        return null;
    }

    @Override
    public String mimeType() {
        return "application/x-www-form-urlencoded";
    }

    @Override
    public long length() {
        return outputStream.size();
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(outputStream.toByteArray());
    }
}
