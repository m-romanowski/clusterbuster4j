package dev.marcinromanowski.clusterbuster4j.sql;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import dev.marcinromanowski.clusterbuster4j.fixture.SqlSnapshotSerializer;
import dev.marcinromanowski.clusterbuster4j.sql.extension.PostGISTemplateExtension;
import dev.marcinromanowski.clusterbuster4j.sql.spatial.BoundingBox;
import dev.marcinromanowski.clusterbuster4j.sql.spatial.LatLng;
import org.geolatte.geom.Geometry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SnapshotExtension.class)
class ClusterQueryTest {

    private static final BoundingBox BOUNDING_BOX = new BoundingBox(new LatLng(17.9, 47.7), new LatLng(26.36, 50.61));

    @SuppressWarnings("unused")
    private Expect expect;
    private SQLQueryFactory sqlQueryFactory;
    @SuppressWarnings("rawtypes")
    private SimplePath<Geometry> geometryExpr;

    @BeforeEach
    void setUp() {
        sqlQueryFactory = queryFactory();
        geometryExpr = Expressions.path(Geometry.class, "geom");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        19 | 20 | 15 | Min, max and current zoom should be normalized to valid values
        18 | 15 | 16 | Current zoom should be normalized to min zoom
        10 | 5  | 20 | All values are valid
        """
    )
    @SuppressWarnings("java:S2699") // Snapshot assertion
    void shouldAllowToCreateClusteringQuery(int zoom, int minZoom, int maxZoom, String reason) {
        // given
        final var query = baseQuery(geometryExpr);

        // when
        final var clusterQuery = ClusterQueryBuilder.builder(sqlQueryFactory)
            .withBaseQuery(query)
            .withBoundingBox(BOUNDING_BOX)
            .withZoom(zoom)
            .withMinZoom(minZoom)
            .withMaxZoom(maxZoom)
            .withGeometryExpression(geometryExpr)
            .build();

        // expect
        expect.scenario(reason)
            .serializer(SqlSnapshotSerializer.class)
            .toMatchSnapshot(clusterQuery.get().toString());
    }

    @Test
    void shouldReturnAnErrorWhenWeDoNotInitializeBuilderWithRequiredData() {
        // when
        final var builder = ClusterQueryBuilder.builder(sqlQueryFactory);

        // then
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void shouldReturnAnErrorWhenWeDoNotSpecifyGeometryExpression() {
        // given
        final var query = baseQuery(geometryExpr);
        final var builder = ClusterQueryBuilder.builder(sqlQueryFactory)
            .withBaseQuery(query)
            .withBoundingBox(BOUNDING_BOX)
            .withZoom(10)
            .withMinZoom(5)
            .withMaxZoom(20);

        // then
        final var error = assertThrows(IllegalArgumentException.class, builder::build);
        assertThat(error)
            .hasMessageContaining("Geometry expression (SRID 4326) must be not null");
    }

    @Test
    void shouldReturnAnErrorWhenWeDoNotSpecifyTheSameGeometryExpressionInBaseQuery() {
        // given
        final var geomExpr = Expressions.path(Geometry.class, "first");
        final var baseQueryGeomExpr = Expressions.path(Geometry.class, "second");
        final var query = baseQuery(baseQueryGeomExpr);
        final var builder = ClusterQueryBuilder.builder(sqlQueryFactory)
            .withBaseQuery(query)
            .withBoundingBox(BOUNDING_BOX)
            .withZoom(10)
            .withMinZoom(5)
            .withMaxZoom(20)
            .withGeometryExpression(geomExpr);

        // then
        final var error = assertThrows(IllegalArgumentException.class, builder::build);
        assertThat(error)
            .hasMessageContaining("The base query should contain the geometry expression");
    }

    @Test
    void shouldReturnAnErrorWhenBaseQueryIsNotProjectable() {
        // given
        final var query = new SQLQuery<>()
            .select(
                new SQLQuery<>()
                    .from(Expressions.path(Geometry.class, "sub_query"))
            )
            .from(Expressions.path(Geometry.class, "base"));
        final var builder = ClusterQueryBuilder.builder(sqlQueryFactory)
            .withBaseQuery(query)
            .withBoundingBox(BOUNDING_BOX)
            .withZoom(10)
            .withMinZoom(5)
            .withMaxZoom(20)
            .withGeometryExpression(geometryExpr);

        // then
        final var error = assertThrows(IllegalArgumentException.class, builder::build);
        assertThat(error)
            .hasMessageContaining("The base query should be a factory expression, such as JavaBean or Constructor projections");
    }

    private static SQLQueryFactory queryFactory() {
        return new SQLQueryFactory(new Configuration(new PostGISTemplateExtension()), () -> null);
    }

    @SuppressWarnings("rawtypes")
    private static SQLQuery<?> baseQuery(Expression<Geometry> geomExpr) {
        return new SQLQuery<>()
            .select(
                Expressions.path(String.class, "name"),
                Expressions.path(String.class, "address"),
                Expressions.path(String.class, "city"),
                Expressions.path(String.class, "postal_code"),
                geomExpr
            )
            .from(Expressions.path(String.class, "places"));
    }

}
