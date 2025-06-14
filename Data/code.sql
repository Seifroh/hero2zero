LOAD DATA LOCAL INFILE 'D:/Projekte/hero2zero/Data/clean-co2.csv'
INTO TABLE countryemission
FIELDS TERMINATED BY ';'
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
  (country, country_code, year, co2_emissions);
  
show global variables like 'local_infile';
SET GLOBAL local_infile = 1;

