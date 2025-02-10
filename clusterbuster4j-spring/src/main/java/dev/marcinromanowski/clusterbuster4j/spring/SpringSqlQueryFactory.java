package dev.marcinromanowski.clusterbuster4j.spring;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.spring.SpringConnectionProvider;

import javax.sql.DataSource;

public class SpringSqlQueryFactory extends SQLQueryFactory {

    public SpringSqlQueryFactory(Configuration configuration, DataSource dataSource) {
        super(configuration, new SpringConnectionProvider(dataSource));
    }

}
