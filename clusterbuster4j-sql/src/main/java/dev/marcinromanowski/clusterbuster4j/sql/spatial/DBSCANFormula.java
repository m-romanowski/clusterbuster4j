package dev.marcinromanowski.clusterbuster4j.sql.spatial;

import java.util.function.BiFunction;

/**
 * Allows you to provide a custom formula for epsilon used
 * by <a href="https://pl.wikipedia.org/wiki/DBSCAN">DBSCAN</a> algorithm.
 */
@FunctionalInterface
public interface DBSCANFormula extends BiFunction<BoundingBox, Zoom, Epsilon> {

}
