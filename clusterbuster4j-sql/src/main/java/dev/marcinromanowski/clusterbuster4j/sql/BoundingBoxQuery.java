package dev.marcinromanowski.clusterbuster4j.sql;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.spatial.GeometryExpressions;
import com.querydsl.sql.AbstractSQLQueryFactory;
import com.querydsl.sql.ProjectableSQLQuery;
import dev.marcinromanowski.clusterbuster4j.sql.spatial.BoundingBox;
import dev.marcinromanowski.clusterbuster4j.sql.spatial.LatLng;
import org.geolatte.geom.Geometry;

import static dev.marcinromanowski.clusterbuster4j.sql.Constants.CENTER_COL;
import static dev.marcinromanowski.clusterbuster4j.sql.Constants.CLUSTER_NO_COL;
import static dev.marcinromanowski.clusterbuster4j.sql.Constants.CLUSTER_SIZE_COL;

/**
 * Returns all geometries that fall (intersects) within the specified bounding
 * box - the area bounded by NE (North-East) and SW (South-West) coordinates.
 */
class BoundingBoxQuery implements NamedQueryProvider {

    private static final String QUERY_NAME = "bbox_query";

    private final ProjectableSQLQuery<?, ?> query;

    @SuppressWarnings("rawtypes")
    BoundingBoxQuery(
        AbstractSQLQueryFactory<? extends ProjectableSQLQuery<?, ?>> queryFactory,
        ProjectableSQLQuery<?, ?> baseQuery,
        Expression<Geometry> geomExpr,
        BoundingBox bbox
    ) {
        final LatLng bboxNe = bbox.ne();
        final LatLng bboxSw = bbox.sw();
        this.query = queryFactory.select(
                Expressions.as(geomExpr, CENTER_COL),
                Expressions.asNumber(1)
                    .as(CLUSTER_SIZE_COL),
                Expressions.asNumber(0)
                    .as(CLUSTER_NO_COL),
                baseQuery.getMetadata()
                    .getProjection()
            )
            .from(Expressions.as(baseQuery, "base_query"))
            .where(
                GeometryExpressions.asGeometry(geomExpr)
                    // bbox = left, bottom, right, top
                    // bbox = min lng, min lat, max lng, max lat
                    .intersects(QueryExtensions.makeEnvelope(bboxSw.lng(), bboxSw.lat(), bboxNe.lng(), bboxNe.lat(), 4326))
            );
    }

    @Override
    public String name() {
        return QUERY_NAME;
    }

    @Override
    public ProjectableSQLQuery<?, ?> query() {
        return query;
    }

}
