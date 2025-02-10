package dev.marcinromanowski.clusterbuster4j.fixture;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TextFixture {

    private TextFixture() {

    }

    public static BigDecimal normalize(double value, int scale) {
        return BigDecimal.valueOf(value)
            .setScale(scale, RoundingMode.HALF_EVEN);
    }

}
