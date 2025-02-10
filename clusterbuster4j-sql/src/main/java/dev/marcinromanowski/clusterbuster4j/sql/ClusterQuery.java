package dev.marcinromanowski.clusterbuster4j.sql;

import com.querydsl.core.Tuple;
import com.querydsl.sql.ProjectableSQLQuery;

import java.util.function.Supplier;

@FunctionalInterface
public interface ClusterQuery<T extends Tuple> extends Supplier<ProjectableSQLQuery<T, ?>> {

}
