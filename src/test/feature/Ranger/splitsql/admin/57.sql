begin; DECLARE mycursor CURSOR FOR SELECT * FROM a; FETCH FORWARD 2 FROM mycursor; commit;

