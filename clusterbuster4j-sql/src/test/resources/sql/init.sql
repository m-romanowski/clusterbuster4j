CREATE OR REPLACE FUNCTION FIRST_AGG (anyelement, anyelement)
    RETURNS anyelement
    LANGUAGE SQL IMMUTABLE STRICT AS 'SELECT $1;';

CREATE OR REPLACE AGGREGATE FIRST (sfunc = FIRST_AGG, basetype = anyelement, stype = anyelement);

CREATE TABLE places (
    id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    location geometry(Point, 4326),
    PRIMARY KEY (id)
);
