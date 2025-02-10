# clusterbuster4j

A collection of libraries and tools that allow Java-based applications to optimally cluster (divide into chunks)
data using various methods and algorithms.

## Getting started

The tools are designed for use on a Java-based server connected to a SQL database that supports spatial
operations, such as `PostgreSQL` with the `PostGIS` extension installed. Each module should contain
a README file, so please look there if you want to get more details and examples. The library has been divided
into modules to customize the implementation.

## Modules
* [clusterbuster4j-sql](./clusterbuster4j-sql) - Basic clustering algorithm for SQL databases
* [clusterbuster4j-spring](./clusterbuster4j-spring) - Toolkit for [Spring](https://spring.io/projects/spring-framework)

## Built with
* [Spring](https://spring.io/projects/spring-framework)
* [Querydsl](http://querydsl.com/)
