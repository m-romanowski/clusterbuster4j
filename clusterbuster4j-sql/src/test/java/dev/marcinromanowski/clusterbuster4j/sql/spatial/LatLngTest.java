package dev.marcinromanowski.clusterbuster4j.sql.spatial;

import dev.marcinromanowski.clusterbuster4j.fixture.TextFixture;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LatLngTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        -100 |   20 | latitude < -90
         100 |  120 | latitude > 90
          50 | -200 | longitude < -180
          50 |  200 | longitude > 180
        """
    )
    void shouldReturnAnErrorForInvalidLongitudeLatitude(double lat, double lng, String ignored) {
        // expect
        assertThrows(IllegalArgumentException.class, () -> new LatLng(lat, lng));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        48.1372   | 11.5756   | 52.5186   | 13.4083  | 504.22
        50.064651 | 19.944981 | 52.229675 | 21.01223 | 251.98
        53.010272 | 18.604809 | 54.370685 | 18.61298 | 151.27
        """
    )
    void shouldAllowToCalculateDistanceBetweenTwoPoints(
        double aLat,
        double aLng,
        double bLat,
        double bLng,
        double expectedDistance
    ) {
        // given
        final var aPoint = new LatLng(aLat, aLng);
        final var bPoint = new LatLng(bLat, bLng);

        // expect
        assertThat(normalize(aPoint.computeDistanceKm(bPoint)))
            .isEqualByComparingTo(normalize(expectedDistance));
    }

    private static BigDecimal normalize(double value) {
        return TextFixture.normalize(value, 2);
    }

}
