# Stardog Spring Boot project

## Purpose

* Show examples of ingesting different data format into Stardog


## Prerequisites

Download and install Stardog at  at www.stardog.com.
* Once it's downloaded, unzip the archive to a destination directory
* Set the *STARDOG_HOME* environment variable to the directory above
* To start the Stardog server, `<STARDOG_HOME>/bin/stardog-admin server start`
* Verify that the server is runing and can be accessed in a web browser at  http://localhost:5820/


## Running the application

### Compile,
```
mvn clean install
```
If you have issues with maven not finding stardog jar/pom files, remove your .m2/settings.xml

### Run the Spring Boot application,
```
mvn spring-boot:run
```

### Supported endpoints
The following endpoints load a hardcoded data file into a database. The database name can be overridden by providing a _dbName_ parameter.
* http://localhost:8080/load-n3 - create and load data from _data/sp2b_10k.n3_ into _n3-db_ database
* http://localhost:8080/load-turtle - create and load data from _data/starwars.ttl_ into _turtle-db_ database
* http://localhost:8080/load-rdfxml1 - create and load data from _data/University0_0.owl_ and _data/lubmSchema.owl_ into _rdfxml1-db_ database
* http://localhost:8080/load-rdfxml2 - create and load data from _data/catalog.rdf_ into _rdfxml2-db_ database

