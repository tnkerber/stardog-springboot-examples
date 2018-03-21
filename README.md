# Stardog Spring Boot project

## Purpose

Show examples of ingesting different data format into Stardog
* N3
* Turtle
* OWL
* RDFXML
* R2RML (from a relational database)


## Prerequisites

Download and install Stardog at  at www.stardog.com.
* Once it's downloaded, unzip the archive to a destination directory
* Set the *STARDOG_HOME* environment variable to the directory above
* To start the Stardog server, `<STARDOG_HOME>/bin/stardog-admin server start`
* Verify that the server is runing and can be accessed in a web browser at  http://localhost:5820/


## Running the application

### Compile
```
mvn clean install
```
If you have issues with maven not finding stardog jar/pom files, remove your .m2/settings.xml

### Run
```
mvn spring-boot:run
```

### Integration Tests
Integration tests, which use Stardog embedded server, are disabled by default.  The following must be met before running the tests
* _STARDOG_HOME_ environment variable must be set
* Stardog server (to which _STARDOG_HOME_ points to) must not be running. Otherwise the embedded server cannot be started for the tests.

To run tests,
```
    mvn install -DskipTests=false
```


### Supported endpoints
The following endpoints load a hardcoded data file into a database. The database name can be overridden by providing a _dbName_ parameter.
* http://localhost:8080/load-n3 - create and load data from _data/sp2b_10k.n3_ into _n3-db_ database
* http://localhost:8080/load-turtle - create and load data from _data/starwars.ttl_ into _turtle-db_ database
* http://localhost:8080/load-owl - create and load data from _data/University0_0.owl_ and _data/lubmSchema.owl_ into _owl-db_ database
* http://localhost:8080/load-rdfxml - create and load data from _data/catalog.rdf_ into _rdfxml-db_ database
* http://localhost:8080/load-rdbms - create and load data from a relational database management system into _rdbms-db_ database
** The RDBMS configuration is stored in rdbms.properties file.  It's hardcoded to the schema 'postgres' in the local PostgreSQL instance
** Before running this endpoint, create relational test data by running 'rdbms.sql' script in the correct database instance
** *Note*: This feature is only supported with Enterprise or Developer license.
