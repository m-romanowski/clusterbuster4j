package dev.marcinromanowski.clusterbuster4j.sql;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.spatial.GeometryExpression;
import com.querydsl.spatial.GeometryExpressions;
import com.querydsl.spatial.SpatialOps;
import dev.marcinromanowski.clusterbuster4j.sql.extension.SqlOpsExtension;
import org.geolatte.geom.Geometry;

import java.util.List;

class QueryExtensions {

    private QueryExtensions() {

    }

    @SuppressWarnings("rawtypes")
    static Expression<Object> first(Path path) {
        return ExpressionUtils.as(ExpressionUtils.operation(Object.class, SqlOpsExtension.FIRST, path), path.toString());
    }

    @SuppressWarnings("rawtypes")
    static List<Expression<Object>> first(List<Path> paths) {
        return paths.stream()
            .map(QueryExtensions::first)
            .toList();
    }

    @SuppressWarnings("rawtypes")
    static <T extends NumberExpression<? extends Number>> GeometryExpression<Geometry> makeEnvelope(
        T xMin,
        T yMin,
        T xMax,
        T yMax,
        T srid
    ) {
        return GeometryExpressions.geometryOperation(SqlOpsExtension.MAKE_ENVELOPE, xMin, yMin, xMax, yMax, srid);
    }

    @SuppressWarnings("rawtypes")
    static GeometryExpression<Geometry> makeEnvelope(
        double xMin,
        double yMin,
        double xMax,
        double yMax,
        int srid
    ) {
        return makeEnvelope(
            Expressions.asNumber(xMin),
            Expressions.asNumber(yMin),
            Expressions.asNumber(xMax),
            Expressions.asNumber(yMax),
            Expressions.asNumber(srid)
        );
    }

    @SuppressWarnings("rawtypes")
    static <T extends Expression<Geometry>> Expression<Integer> dbscan(
        T geometry,
        double epsilon,
        int minPoints
    ) {
        return ExpressionUtils.operation(
            Integer.class,
            SqlOpsExtension.DBSCAN,
            geometry,
            Expressions.asNumber(epsilon),
            Expressions.asNumber(minPoints)
        );
    }

    @SuppressWarnings("rawtypes")
    static <T extends Expression<Geometry>> Expression<Geometry> centroid(T geometry) {
        return GeometryExpressions.geometryOperation(SpatialOps.CENTROID, geometry);
    }

    @SuppressWarnings("rawtypes")
    static <T extends Expression<Geometry>> Expression<Geometry> collect(T geometry) {
        return GeometryExpressions.geometryOperation(SpatialOps.COLLECT, geometry);
    }

    @SuppressWarnings("rawtypes")
    static <T extends Expression<Geometry>> Expression<Number> x(T geometry) {
        return ExpressionUtils.operation(Number.class, SpatialOps.X, geometry);
    }

    @SuppressWarnings("rawtypes")
    static <T extends Expression<Geometry>> Expression<Number> y(T geometry) {
        return ExpressionUtils.operation(Number.class, SpatialOps.Y, geometry);
    }

}
