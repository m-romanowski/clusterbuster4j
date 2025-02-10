package dev.marcinromanowski.clusterbuster4j.sql.spatial;

public record Epsilon(double value) {

    public static Epsilon of(double value) {
        return new Epsilon(value);
    }

}
