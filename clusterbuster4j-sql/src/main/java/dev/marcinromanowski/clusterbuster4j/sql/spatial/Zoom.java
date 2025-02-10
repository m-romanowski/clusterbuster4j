package dev.marcinromanowski.clusterbuster4j.sql.spatial;

public record Zoom(int value) {

    public static Zoom of(int value) {
        return new Zoom(value);
    }

}
