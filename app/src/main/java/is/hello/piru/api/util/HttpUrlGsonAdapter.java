package is.hello.piru.api.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.squareup.okhttp.HttpUrl;

import java.lang.reflect.Type;

public class HttpUrlGsonAdapter implements JsonSerializer<HttpUrl>, JsonDeserializer<HttpUrl> {
    @Override
    public HttpUrl deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String url = json.getAsString();
        return HttpUrl.parse(url);
    }

    @Override
    public JsonElement serialize(HttpUrl src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}
