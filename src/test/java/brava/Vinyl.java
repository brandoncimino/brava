package brava;

import com.google.common.collect.MoreCollectors;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.HashMap;

public record Vinyl(String artist, String title, int year) {
    public static RecordComponent ARTIST = getComponent(Vinyl.class, "artist");
    public static RecordComponent TITLE = getComponent(Vinyl.class, "title");
    public static RecordComponent YEAR = getComponent(Vinyl.class, "year");

    private static RecordComponent getComponent(Class<? extends Record> recordType, String name) {
        return Arrays.stream(recordType.getRecordComponents())
              .filter(it -> it.getName().equals(name))
              .collect(MoreCollectors.onlyElement());
    }

    public HashMap<RecordComponent, ?> toMap() {
        var map = new HashMap<RecordComponent, Object>();
        map.put(ARTIST, this.artist);
        map.put(TITLE, this.title);
        map.put(YEAR, this.year);
        return map;
    }
}
