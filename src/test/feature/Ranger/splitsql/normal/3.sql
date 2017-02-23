set session role=usertest3;
CREATE OR REPLACE FUNCTION f4() RETURNS TEXT AS $$ plpy.execute("select * from a") $$ LANGUAGE plpythonu VOLATILE;

