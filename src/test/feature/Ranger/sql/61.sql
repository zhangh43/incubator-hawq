set session role=usertest61;
CREATE FUNCTION getfoo() RETURNS SETOF mytype AS $$ SELECT i,i FROM a $$ LANGUAGE SQL;

