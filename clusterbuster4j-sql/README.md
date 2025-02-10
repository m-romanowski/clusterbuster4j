# clusterbuster4j-sql

Allows you to create a query that groups geometries and returns clusters (groups) of geometries that are close
to each other. The algorithm primarily uses the bounding box and the current zoom. Queries use
CTE ([Common Table Expressions](https://www.postgresql.org/docs/current/queries-with.html)), so make sure your
SQL database supports this query format. A window function returns a cluster number for each input
geometry, using the 2D Density-based spatial clustering of applications with
noise ([DBSCAN](https://en.wikipedia.org/wiki/DBSCAN)) algorithm.

## Getting started

The library was prepared to be easy to configure without additional properties. Look into the examples, if you
want to get more information.
```xml
<dependency>
    <groupId>dev.marcinromanowski</groupId>
    <artifactId>clusterbuster4j-sql</artifactId>
    <version>${clusterbuster4j.version}</version>
</dependency>
```

The clustering mechanism uses non-standard SQL aggregation functions, so you need to create the required
functions before using it.
```sql
CREATE OR REPLACE FUNCTION FIRST_AGG (anyelement, anyelement)
    RETURNS anyelement
    LANGUAGE SQL IMMUTABLE STRICT AS 'SELECT $1;';

CREATE OR REPLACE AGGREGATE FIRST (sfunc = FIRST_AGG, basetype = anyelement, stype = anyelement);
```

Alternatively, look to `src/main/resources/sql` directory if you would like to copy the prepared schemas.

## Usage example

```java
final int zoom = 12;
final double swLat = 52.94136;
final double swLng = 18.35364;
final double neLat = 54.10656;
final double neLng = 18.78838;
final BoundingBox bbox = new BoundingBox(new LatLng(neLat, neLng), new LatLng(swLat, swLng));

final Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/test", "postgres", "postgres");
final SQLQueryFactory factory = new SQLQueryFactory(new Configuration(new PostGISTemplateExtension()), () -> connection);
final SimplePath<Geometry> locationExpr = Expressions.path(Geometry.class, "location"); // SRID 4326 (longitude, latitude)
final SQLQuery<Tuple> baseQuery = new SQLQuery<>()
    .select(
        Expressions.path(String.class, "name"),
        locationExpr
    )
    .from(Expressions.path(String.class, "places"));
final ClusterQuery<Tuple> clusterQuery = ClusterQueryBuilder.builder(factory)
    .withBaseQuery(baseQuery)
    .withZoom(zoom)
    .withMinZoom(6)
    .withMaxZoom(20)
    .withBoundingBox(bbox)
    .withGeometryExpression(locationExpr)
    .build();
final List<Tuple> queryResult = clusterQuery.get()
    .fetch();
```

The query, in addition to the basic information specified in the base query, which the user passes to the
factory, also returns such information as the location of the cluster - SRID 4326 (longitude and latitude) and
the size of the cluster. The data specified in the base query is returned only if the cluster has exactly one
geometry (size = 1). Otherwise, the cluster contains only location (longitude, latitude) and size information.
