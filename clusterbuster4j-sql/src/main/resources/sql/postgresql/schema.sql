CREATE OR REPLACE FUNCTION FIRST_AGG (anyelement, anyelement)
    RETURNS anyelement
    LANGUAGE SQL IMMUTABLE STRICT AS 'SELECT $1;';

CREATE OR REPLACE AGGREGATE FIRST (sfunc = FIRST_AGG, basetype = anyelement, stype = anyelement);
