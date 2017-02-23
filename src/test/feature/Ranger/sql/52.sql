set session role=usertest52;
PREPARE fooplan (int) AS INSERT INTO a VALUES($1);

