package dev.marcinromanowski.clusterbuster4j.sql;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.AbstractSQLQueryFactory;
import com.querydsl.sql.ProjectableSQLQuery;
import org.geolatte.geom.Geometry;

import java.util.stream.Stream;

import static dev.marcinromanowski.clusterbuster4j.sql.Constants.CENTER_COL;
import static dev.marcinromanowski.clusterbuster4j.sql.Constants.CLUSTER_SIZE_COL;
import static dev.marcinromanowski.clusterbuster4j.sql.Constants.LAT_COL;
import static dev.marcinromanowski.clusterbuster4j.sql.Constants.LNG_COL;

/**
 * The output query, which is the last step of the clustering function. The base aggregation
 * will be returned only if it is possible to specify a cluster with EXACTLY one
 * geometry. Otherwise, only the number of geometries forming the cluster (sum
 * of geometries in the cluster) will be returned.
 */
class OutputClusterQuery implements QueryProvider {

    private final ProjectableSQLQuery<?, ?> query;

    OutputClusterQuery(
        AbstractSQLQueryFactory<? extends ProjectableSQLQuery<?, ?>> queryFactory,
        FactoryExpression<?> baseQueryProjection
    ) {
        this.query = queryFactory.select(
            Stream.concat(
                    Stream.of(
                        Expressions.path(Integer.class, CLUSTER_SIZE_COL),
                        ExpressionUtils.as(QueryExtensions.x(Expressions.path(Geometry.class, CENTER_COL)), LNG_COL),
                        ExpressionUtils.as(QueryExtensions.y(Expressions.path(Geometry.class, CENTER_COL)), LAT_COL)
                    ),
                    baseQueryProjection.getArgs()
                        .stream()
                        .map(expr -> new CaseBuilder()
                            .when(Expressions.booleanTemplate("%s = {0}".formatted(CLUSTER_SIZE_COL), 1))
                            .then(expr)
                            .otherwise(Expressions.nullExpression())
                        )
                )
                .toList()
                .toArray(new Expression[]{})
        );
    }

    @Override
    public ProjectableSQLQuery<?, ?> query() {
        return query;
    }

}
