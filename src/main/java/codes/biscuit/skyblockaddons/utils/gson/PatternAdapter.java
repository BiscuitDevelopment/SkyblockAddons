package codes.biscuit.skyblockaddons.utils.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.regex.Pattern;

public class PatternAdapter extends TypeAdapter<Pattern> {

    @Override
    public void write(JsonWriter out, Pattern value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.pattern());
    }

    @Override
    public Pattern read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String patternString = in.nextString();
        return Pattern.compile(patternString);
    }
}
