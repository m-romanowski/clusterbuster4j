package dev.marcinromanowski.clusterbuster4j.sql;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.AbstractSQLQueryFactory;
import com.querydsl.sql.ProjectableSQLQuery;
import com.querydsl.sql.SQLCommonQuery;
import org.geolatte.geom.Geometry;

import static dev.marcinromanowski.clusterbuster4j.sql.Constants.CENTER_COL;
import static dev.marcinromanowski.clusterbuster4j.sql.Constants.CLUSTER_COL;
import static dev.marcinromanowski.clusterbuster4j.sql.Constants.CLUSTER_NO_COL;
import static dev.marcinromanowski.clusterbuster4j.sql.Constants.CLUSTER_SIZE_COL;
import static dev.marcinromanowski.clusterbuster4j.sql.Constants.PREV_CLUSTER_NO_COL;

/**
 * A window function that returns a cluster number for each input geometry, using the 2D Density-based spatial
 * clustering of applications with noise (<a href="https://en.wikipedia.org/wiki/DBSCAN">DBSCAN</a>) algorithm.
 */
class DBSCANCluster implements NamedQueryProvider {

    private final String name;
    private final ProjectableSQLQuery<?, ?> query;

    DBSCANCluster(
        AbstractSQLQueryFactory<? extends ProjectableSQLQuery<?, ?>> queryFactory,
        int zoom,
        double epsilon,
        Expression<?> baseQueryProjection,
        String baseQueryTableName,
        SQLCommonQuery<?> baseQuery
    ) {
        this.name = "cluster_%s".formatted(zoom);
        this.query = (ProjectableSQLQuery<?, ?>) baseQuery.with(
            Expressions.path(String.class, name),
            queryFactory.select(
                    Expressions.path(Geometry.class, CENTER_COL),
                    Expressions.path(Integer.class, CLUSTER_SIZE_COL),
                    Expressions.path(Integer.class, CLUSTER_NO_COL)
                        .as(PREV_CLUSTER_NO_COL),
                    Expressions.as(QueryExtensions.dbscan(Expressions.path(Geometry.class, CENTER_COL), epsilon, 1), CLUSTER_COL),
                    baseQueryProjection
                )
                .from(Expressions.path(String.class, baseQueryTableName))
        );
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
