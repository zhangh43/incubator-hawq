set session role=usertest29;
CREATE EXTERNAL TABLE ext_t  (i int) LOCATION ( 'gpfdist://localhost:8081/*.csv' ) FORMAT 'CSV' ( DELIMITER ',' ); 

