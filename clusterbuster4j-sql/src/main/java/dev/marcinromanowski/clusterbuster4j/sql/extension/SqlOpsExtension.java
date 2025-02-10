package dev.marcinromanowski.clusterbuster4j.sql.extension;

import com.querydsl.core.types.Operator;
import org.geolatte.geom.Geometry;

public enum SqlOpsExtension implements Operator {

    /**
     * Select first element from the aggregation.
     * <p><p>
     * Equal to SQL aggregation bellow:
     * <p>
     * <code>
     * CREATE OR REPLACE FUNCTION FIRST_AGG (anyelement, anyelement) RETURNS anyelement LANGUAGE SQL IMMUTABLE STRICT AS 'SELECT $1;';
     * </code>
     * <p>
     * <code>
     * CREATE OR REPLACE AGGREGATE FIRST (sfunc = FIRST_AGG, basetype = anyelement, stype = anyelement);
     * </code>
     */
    FIRST(Object.class),
    DBSCAN(Integer.class),
    MAKE_ENVELOPE(Geometry.class);

    private final Class<?> type;

    SqlOpsExtension(Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return this.type;
    }

}
