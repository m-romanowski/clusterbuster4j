package dev.marcinromanowski.clusterbuster4j.sql;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.AbstractSQLQueryFactory;
import com.querydsl.sql.ProjectableSQLQuery;
import com.querydsl.sql.SQLCommonQuery;
import com.querydsl.sql.SQLQuery;
import dev.marcinromanowski.clusterbuster4j.sql.spatial.BoundingBox;
import dev.marcinromanowski.clusterbuster4j.sql.spatial.DBSCANFormula;
import dev.marcinromanowski.clusterbuster4j.sql.spatial.Zoom;
import org.geolatte.geom.Geometry;

import static dev.marcinromanowski.clusterbuster4j.sql.Assertions.assertGreaterEqualTo;
import static dev.marcinromanowski.clusterbuster4j.sql.Assertions.assertNonNull;

/**
 * Allows you to create a query that groups geometries and returns
 * clusters (groups) of geometries that are close to each other.
 * The algorithm primarily uses the bounding box and the current
 * zoom. Queries use CTE (<a href="https://www.postgresql.org/docs/current/queries-with.html">Common Table Expressions</a>), so
 * make sure your SQL database supports this query format.
 */
public class ClusterQueryBuilder {

    private final AbstractSQLQueryFactory<? extends ProjectableSQLQuery<?, ?>> queryFactory;

    private int zoom;
    private int minZoom;
    private int maxZoom;
    private BoundingBox bbox;
    private ProjectableSQLQuery<?, ?> baseQuery;
    @SuppressWarnings("rawtypes")
    private Expression<Geometry> geometryExpression;
    private DBSCANFormula dbScanFormula = new DBSCANFormulaImpl();

    private ClusterQueryBuilder(AbstractSQLQueryFactory<? extends ProjectableSQLQuery<?, ?>> factory) {
        this.queryFactory = factory;
    }

    public static ClusterQueryBuilder builder(AbstractSQLQueryFactory<? extends ProjectableSQLQuery<?, ?>> factory) {
        return new ClusterQueryBuilder(factory);
    }

    public ClusterQueryBuilder withZoom(int zoom) {
        this.zoom = zoom;
        return this;
    }

    public ClusterQueryBuilder withMinZoom(int minZoom) {
        assertGreaterEqualTo(0, minZoom, "minZoom");
        this.minZoom = minZoom;
        return this;
    }

    public ClusterQueryBuilder withMaxZoom(int maxZoom) {
        assertGreaterEqualTo(1, maxZoom, "maxZoom");
        this.maxZoom = maxZoom;
        return this;
    }

    public ClusterQueryBuilder withBoundingBox(BoundingBox bbox) {
        this.bbox = bbox;
        return this;
    }

    public ClusterQueryBuilder withBaseQuery(SQLQuery<?> baseQuery) {
        this.baseQuery = baseQuery;
        return this;
    }

    @SuppressWarnings("rawtypes")
    public ClusterQueryBuilder withGeometryExpression(Expression<Geometry> geometryExpression) {
        this.geometryExpression = geometryExpression;
        return this;
    }

    public ClusterQueryBuilder withDBSCANFormula(DBSCANFormula formula) {
        this.dbScanFormula = formula;
        return this;
    }

    public <T extends Tuple> ClusterQuery<T> build() {
        this.zoom = Math.min(zoom, maxZoom);
        this.minZoom = Math.min(Math.max(zoom, Math.max(0, minZoom)), maxZoom);

        assertNonNull(bbox, "Bounding box");
        assertNonNull(bbox.sw(), "SW coordinates");
        assertNonNull(bbox.ne(), "NE coordinates");
        assertNonNull(geometryExpression, "Geometry expression (SRID 4326)");
        assertNonNull(baseQuery, "Cluster base query");
        assertNonNull(dbScanFormula, "DBSCAN epsilon formula");

        return new SqlClusterQuery<T>(buildQuery());
    }

    @SuppressWarnings("unchecked")
    private <T extends Tuple> ProjectableSQLQuery<T, ?> buildQuery() {
        final Expression<?> projection = baseQuery.getMetadata()
            .getProjection();

        if (!(projection instanceof FactoryExpression<?> exprFactory)) {
            throw new IllegalArgumentException("The base query should be a factory expression, such as JavaBean or Constructor projections");
        }

        if (!(exprFactory.getArgs().contains(geometryExpression))) {
            throw new IllegalArgumentException("The base query should contain the geometry expression");
        }

        final BoundingBoxQuery bboxQuery = new BoundingBoxQuery(queryFactory, baseQuery, geometryExpression, bbox);
        final OutputClusterQuery out = new OutputClusterQuery(queryFactory, exprFactory);

        String queryName = bboxQuery.name();
        SQLCommonQuery<?> queryChain = out.query().with(Expressions.path(String.class, queryName), bboxQuery.query());
        for (int i = maxZoom; i >= zoom; i--) {
            final DBSCANCluster dbScanCluster = createDbScanClusterQueryFor(i, bbox, exprFactory, queryName, queryChain);
            final GroupedClusterQuery groupedCluster = createGroupedClusterQueryFor(i, exprFactory, dbScanCluster.name(), dbScanCluster.query());
            queryChain = groupedCluster.query();
            queryName = groupedCluster.name();
        }

        return (ProjectableSQLQuery<T, ?>) queryChain.from(Expressions.path(String.class, queryName));
    }

    private DBSCANCluster createDbScanClusterQueryFor(
        int zoom,
        BoundingBox bbox,
        FactoryExpression<?> projection,
        String baseQueryTableName,
        SQLCommonQuery<?> baseQuery
    ) {
        return new DBSCANCluster(
            queryFactory,
            zoom,
            dbScanFormula.apply(bbox, Zoom.of(zoom))
                .value(),
            projection,
            baseQueryTableName,
            baseQuery
        );
    }

    private GroupedClusterQuery createGroupedClusterQueryFor(
        int zoom,
        FactoryExpression<?> projection,
        String baseQueryTableName,
        SQLCommonQuery<?> baseQuery
    ) {
        return new GroupedClusterQuery(
            queryFactory,
            zoom,
            projection,
            baseQueryTableName,
            baseQuery
        );
    }

}
