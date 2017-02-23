set session role=usertest65;
BEGIN; INSERT INTO a VALUES (3); SAVEPOINT my_savepoint; INSERT INTO a VALUES (4); RELEASE SAVEPOINT my_savepoint; COMMIT;

