package dev.marcinromanowski.clusterbuster4j.sql;

import com.querydsl.sql.ProjectableSQLQuery;

@FunctionalInterface
interface QueryProvider {
    ProjectableSQLQuery<?, ?> query();
}
