set session role=usertest31;
CREATE WRITABLE EXTERNAL TABLE ext_t2 (LIKE ext_t) LOCATION ('gpfdist://localhost:8081/t.out') FORMAT 'TEXT' ( DELIMITER '|' NULL ' ');

