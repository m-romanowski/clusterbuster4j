package dev.marcinromanowski.clusterbuster4j.sql.spatial;

import dev.marcinromanowski.clusterbuster4j.fixture.TextFixture;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BoundingBoxTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        52.452557 |   16.649952 | 53.745361 |   21.571827 | 53.09896 |   19.11089
        51.203818 |   -2.071180 | 54.326485 |   -1.477919 | 52.76515 |   -1.77455
        33.692495 | -120.496674 | 36.106953 | -116.365814 | 34.89972 | -118.43124
        """
    )
    void shouldAllowToGetCenterOfBBox(
        double aLat,
        double aLng,
        double bLat,
        double bLng,
        double bboxLat,
        double bboxLng
    ) {
        // given
        final var ne = new LatLng(aLat, aLng);
        final var sw = new LatLng(bLat, bLng);
        final var bbox = new BoundingBox(ne, sw);

        // expect
        final var center = bbox.center();
        assertThat(normalize(center.lat()))
            .isEqualByComparingTo(normalize(bboxLat));
        assertThat(normalize(center.lng()))
            .isEqualByComparingTo(normalize(bboxLng));
    }

    private static BigDecimal normalize(double value) {
        return TextFixture.normalize(value, 5);
    }

}
