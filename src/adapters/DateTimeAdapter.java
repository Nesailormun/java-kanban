package adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public void write(final JsonWriter jsonWriter, final LocalDateTime dateTime) throws IOException {
        if (dateTime == null) {
            jsonWriter.value("null");
            return;
        }
        jsonWriter.value(dateTime.format(DATE_TIME_FORMATTER));
    }

    @Override
    public LocalDateTime read(final JsonReader jsonReader) throws IOException {
        String dateTime = jsonReader.nextString();
        if (dateTime.equals("null")) {
            return null;
        }
        return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
    }
}
