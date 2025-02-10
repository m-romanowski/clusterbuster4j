package dev.marcinromanowski.clusterbuster4j.sql;

import com.querydsl.core.Tuple;
import com.querydsl.sql.ProjectableSQLQuery;

record SqlClusterQuery<T extends Tuple>(ProjectableSQLQuery<T, ?> get) implements ClusterQuery<T> {

}
