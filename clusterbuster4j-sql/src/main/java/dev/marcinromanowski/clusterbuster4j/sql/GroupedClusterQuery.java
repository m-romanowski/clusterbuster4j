package dev.marcinromanowski.clusterbuster4j.sql;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.AbstractSQLQueryFactory;
import com.querydsl.sql.ProjectableSQLQuery;
import com.querydsl.sql.SQLCommonQuery;
import org.geolatte.geom.Geometry;

import java.util.List;
import java.util.stream.Stream;

import static dev.marcinromanowski.clusterbuster4j.sql.Constants.CENTER_COL;
import static dev.marcinromanowski.clusterbuster4j.sql.Constants.CLUSTER_COL;
import static dev.marcinromanowski.clusterbuster4j.sql.Constants.CLUSTER_NO_COL;
import static dev.marcinromanowski.clusterbuster4j.sql.Constants.CLUSTER_SIZE_COL;

/**
 * Groups clusters computed by {@link DBSCANCluster} function with the same number. Each group
 * computes a new point (longitude, latitude - SRID 4326) from calculated cluster - the geometric
 * center of mass of a geometry.
 */
class GroupedClusterQuery implements NamedQueryProvider {

    private final String name;
    private final ProjectableSQLQuery<?, ?> query;

    GroupedClusterQuery(
        AbstractSQLQueryFactory<? extends ProjectableSQLQuery<?, ?>> queryFactory,
        int zoom,
        FactoryExpression<?> baseQueryProjection,
        String baseQueryTableName,
        SQLCommonQuery<?> baseQuery
    ) {
        final var groupedClusterQuery = queryFactory.select(
            Stream.concat(
                    Stream.of(
                        ExpressionUtils.as(QueryExtensions.centroid(QueryExtensions.collect(Expressions.path(Geometry.class, CENTER_COL))), CENTER_COL),
                        Expressions.asNumber(Expressions.path(Integer.class, CLUSTER_SIZE_COL)).sum().as(CLUSTER_SIZE_COL),
                        Expressions.path(Integer.class, CLUSTER_COL).as(CLUSTER_NO_COL)
                    ),
                    QueryExtensions.first(pathsOf(baseQueryProjection))
                        .stream()
                )
                .toList()
                .toArray(new Expression[]{})
            )
            .from(Expressions.path(String.class, baseQueryTableName))
            .groupBy(Expressions.path(Integer.class, CLUSTER_COL));
        this.name = "grouped_cluster_%s".formatted(zoom);
        this.query = (ProjectableSQLQuery<?, ?>) baseQuery.with(Expressions.path(String.class, name), groupedClusterQuery);
    }

    @SuppressWarnings("rawtypes")
    private static List<Path> pathsOf(FactoryExpression<?> baseQueryProjection) {
        return baseQueryProjection.getArgs()
            .stream()
            .filter(Path.class::isInstance)
            .map(Path.class::cast)
            .toList();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public ProjectableSQLQuery<?, ?> query() {
        return query;
    }

}
