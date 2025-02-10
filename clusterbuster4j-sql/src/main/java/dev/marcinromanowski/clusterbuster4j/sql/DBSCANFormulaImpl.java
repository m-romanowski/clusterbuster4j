package dev.marcinromanowski.clusterbuster4j.sql;

import dev.marcinromanowski.clusterbuster4j.sql.spatial.BoundingBox;
import dev.marcinromanowski.clusterbuster4j.sql.spatial.DBSCANFormula;
import dev.marcinromanowski.clusterbuster4j.sql.spatial.Epsilon;
import dev.marcinromanowski.clusterbuster4j.sql.spatial.Zoom;

class DBSCANFormulaImpl implements DBSCANFormula {

    @Override
    public Epsilon apply(BoundingBox bbox, Zoom zoom) {
        final double zoomToKm = bbox.center().computeDistanceKm(bbox.ne());
        final double epsilon = zoomToKm / Math.pow(2, zoom.value());
        return Epsilon.of(epsilon);
    }

}
