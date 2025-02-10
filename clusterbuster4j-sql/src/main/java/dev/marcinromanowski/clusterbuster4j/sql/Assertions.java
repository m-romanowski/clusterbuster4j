package dev.marcinromanowski.clusterbuster4j.sql;

import static java.util.Objects.isNull;

class Assertions {

    private Assertions() {

    }

    static void assertNonNull(Object value, String propertyName) {
        if (isNull(value)) {
            throw new IllegalArgumentException("%s must be not null".formatted(propertyName));
        }
    }

    static void assertGreaterEqualTo(int expected, int actual, String propertyName) {
        if (expected > actual) {
            throw new IllegalArgumentException("%s must be greater than or equal to %s".formatted(propertyName, expected));
        }
    }

}
