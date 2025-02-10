package dev.marcinromanowski.clusterbuster4j.sql;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import dev.marcinromanowski.clusterbuster4j.base.BaseIntegrationTest;
import dev.marcinromanowski.clusterbuster4j.sql.extension.PostGISTemplateExtension;
import dev.marcinromanowski.clusterbuster4j.sql.spatial.BoundingBox;
import dev.marcinromanowski.clusterbuster4j.sql.spatial.LatLng;
import org.assertj.core.api.Assertions;
import org.geolatte.geom.Geometry;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class ClusterQueryAcceptanceIntegrationTest extends BaseIntegrationTest {

    @Test
    @SuppressWarnings("rawtypes")
    void acceptanceTest() throws SQLException {
        // given
        int zoom = 12;
        double swLat = 52.94136;
        double swLng = 18.35364;
        double neLat = 54.10656;
        double neLng = 18.78838;

        final Connection connection = getJdbcConnection();
        final SQLQueryFactory queryFactory = sqlQueryFactory(connection);
        final SimplePath<Geometry> locationExpr = Expressions.path(Geometry.class, "location");
        final SQLQuery<Tuple> baseQuery = new SQLQuery<>()
            .select(
                Expressions.path(String.class, "name"),
                locationExpr
            )
            .from(Expressions.path(String.class, "places"));

        // when
        BoundingBox bbox = new BoundingBox(new LatLng(neLat, neLng), new LatLng(swLat, swLng));
        ClusterQueryBuilder clusterQueryBuilder = ClusterQueryBuilder.builder(queryFactory)
            .withBaseQuery(baseQuery)
            .withZoom(zoom)
            .withMinZoom(6)
            .withMaxZoom(20)
            .withBoundingBox(bbox)
            .withGeometryExpression(locationExpr);
        ClusterQuery<Tuple> clusterQuery = clusterQueryBuilder
            .build();

        // then
        assertThat(clusterQuery.get().fetchCount())
            .isZero();

        // when
        createLocation(connection, "First location", 18.65329, 53.00274);
        createLocation(connection, "Second location", 18.37992, 53.02172);

        // then
        List<Tuple> queryResult = clusterQuery.get()
            .fetch();
        assertThat(queryResult)
            .hasSize(2);

        final Tuple firstLocation = findExactlyOnce(queryResult, tuple -> tuple.size() > 3 && "First location".equals(tuple.get(3, String.class)));
        assertThat(firstLocation)
            .matches(tuple -> Integer.valueOf(1).equals(tuple.get(0, Integer.class))
                && Double.valueOf(18.65329).equals(tuple.get(1, Double.class))
                && Double.valueOf(53.00274).equals(tuple.get(2, Double.class))
            );

        final Tuple secondLocation = findExactlyOnce(queryResult, tuple -> tuple.size() > 3 && "Second location".equals(tuple.get(3, String.class)));
        assertThat(secondLocation)
            .matches(tuple -> Integer.valueOf(1).equals(tuple.get(0, Integer.class))
                && Double.valueOf(18.37992).equals(tuple.get(1, Double.class))
                && Double.valueOf(53.02172).equals(tuple.get(2, Double.class))
            );

        // when
        zoom = 6;
        swLat = 57.43215;
        swLng = 30.53739;
        neLat = 46.69343;
        neLng = 9.11405;
        bbox = new BoundingBox(new LatLng(neLat, neLng), new LatLng(swLat, swLng));
        clusterQueryBuilder = clusterQueryBuilder.withZoom(zoom)
            .withBoundingBox(bbox);
        clusterQuery = clusterQueryBuilder.build();

        // then
        queryResult = clusterQuery.get()
            .fetch();

        assertThat(queryResult)
            .hasSize(1)
            .matches(tuples -> tuples.getFirst().size() == 5
                && Integer.valueOf(2).equals(tuples.getFirst().get(0, Integer.class))
                && Double.valueOf(18.516605).equals(tuples.getFirst().get(1, Double.class))
                && Double.valueOf(53.01223).equals(tuples.getFirst().get(2, Double.class))
                && tuples.getFirst().get(3, Object.class) == null
                && tuples.getFirst().get(4, Object.class) == null
            );

        // when
        createLocation(connection, "Third location", 18.65329, 53.00274);

        // and
        zoom = 12;
        swLat = 54.10656;
        swLng = 18.78838;
        neLat = 52.94136;
        neLng = 18.35364;
        bbox = new BoundingBox(new LatLng(neLat, neLng), new LatLng(swLat, swLng));
        clusterQueryBuilder = clusterQueryBuilder.withZoom(zoom)
            .withBoundingBox(bbox);
        clusterQuery = clusterQueryBuilder.build();

        // then
        queryResult = clusterQuery.get()
            .fetch();
        assertThat(queryResult)
            .hasSize(2);

        final Tuple firstCluster = findExactlyOnce(queryResult, tuple -> Integer.valueOf(2).equals(tuple.get(0, Integer.class)));
        assertThat(firstCluster)
            .matches(tuple -> tuple.size() == 5
                && Double.valueOf(18.65329).equals(tuple.get(1, Double.class))
                && Double.valueOf(53.00274).equals(tuple.get(2, Double.class))
                && tuple.get(3, Object.class) == null
                && tuple.get(4, Object.class) == null
            );

        final Tuple secondCluster = findExactlyOnce(queryResult, tuple -> Integer.valueOf(1).equals(tuple.get(0, Integer.class)));
        assertThat(secondCluster)
            .matches(tuple -> tuple.size() == 5
                && Integer.valueOf(1).equals(tuple.get(0, Integer.class))
                && Double.valueOf(18.37992).equals(tuple.get(1, Double.class))
                && Double.valueOf(53.02172).equals(tuple.get(2, Double.class))
                && "Second location".equals(tuple.get(3, String.class))
            );
    }

    private static Tuple findExactlyOnce(List<Tuple> queryResult, Predicate<Tuple> predicate) {
        final List<Tuple> results = queryResult.stream()
            .filter(predicate)
            .toList();
        if (results.size() != 1) {
            throw new IllegalStateException("Unable to find exactly one element");
        }
        return results.getFirst();
    }

    private SQLQueryFactory sqlQueryFactory(Connection connection) {
        return new SQLQueryFactory(new Configuration(new PostGISTemplateExtension()), () -> connection);
    }

    @SuppressWarnings("SqlNoDataSourceInspection")
    private void createLocation(Connection connection, String name, double lng, double lat) {
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO places (id, name, location) VALUES (?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326))");
            preparedStatement.setObject(1, UUID.randomUUID());
            preparedStatement.setString(2, name);
            preparedStatement.setDouble(3, lng);
            preparedStatement.setDouble(4, lat);
            final int result = preparedStatement.executeUpdate();
            Assertions.assertThat(result)
                .isEqualTo(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
