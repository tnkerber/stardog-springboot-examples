package com.polarisalpha.ca.stardog.controller;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.polarisalpha.ca.stardog.service.StardogDataService;

@RestController
public class StardogController {
    private static final Logger logger = LoggerFactory.getLogger(StardogController.class);

    @Autowired
    private StardogDataService dataService;

    @RequestMapping(value = "/load-n3", produces = "text/plain")
    public String loadN3File(@RequestParam(value = "dbName", defaultValue = "n3-db") final String dbName) {
        return loadDataset(dbName,  RDFFormat.N3,"data/sp2b_10k.n3");
    }

    @RequestMapping(value = "/load-turtle", produces = "text/plain")
    public String loadTurtleFile(@RequestParam(value = "dbName", defaultValue = "turtle-db") final String dbName) {
        return loadDataset(dbName, RDFFormat.TURTLE,"data/starwars.ttl");
    }

    @RequestMapping(value = "/load-owl", produces = "text/plain")
    public String loadOwlFiles(@RequestParam(value = "dbName", defaultValue = "owl-db") final String dbName) {
        return loadDataset(dbName, RDFFormat.RDFXML,"data/University0_0.owl", "data/lubmSchema.owl");
    }

    @RequestMapping(value = "/load-rdfxml", produces = "text/plain")
    public String loadRdfXmlFile(@RequestParam(value = "dbName", defaultValue = "rdfxml-db") final String dbName) {
        return loadDataset(dbName, RDFFormat.RDFXML,"data/catalog.rdf");
    }

    @RequestMapping(value = "/load-rdbms", produces = "text/plain")
    public String loadFromRdbms(@RequestParam(value = "dbName", defaultValue = "rdbms-db") final String dbName) {
        String result;

        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // create the virtual graph
            final String virtualGraph = dbName + "-vg";
            dataService.createVirtualGraph(virtualGraph, "data/rdbms.properties", "data/rdbms.ttl");
            outputStream.write(String.format("Successfully added virtual graph '%s'.\n\n", virtualGraph).getBytes());

            // create the db
            dataService.createDb(dbName);
            outputStream.write(String.format("Successfully created database '%s'.\n\n", dbName).getBytes());

            // materialize the virtual graph
            final String namedGraph = dbName + "-graph";
            String sparql = String.format("COPY <virtual://%s> to <%s>", virtualGraph, namedGraph);
            dataService.executeUpdateQuery(dbName, sparql);
            outputStream.write(String.format("Materialized virtual graph to named graph '%s'.\n\n", namedGraph).getBytes());

            // remove the virtual graph
            dataService.removeVirtualGraph(virtualGraph);
            outputStream.write(String.format("Removed virtual graph '%s'.\n\n", virtualGraph).getBytes());

            // query against the named graph
            sparql = String.format("select ?s ?p ?o { graph <%s> { ?s ?p ?o } } LIMIT 10", namedGraph);
            dataService.executeSelectQuery(dbName, sparql, outputStream);

            result = outputStream.toString();
        } catch (Exception e) {
            result = "Exception found creating virtual graph from R2RML mapping: " + e.getMessage();
            logger.error(result, e);
        }

        return result;
    }

    @RequestMapping(value = "/load-doc", produces = "text/plain")
    public String loadUnstructuredData(@RequestParam(value = "dbName", defaultValue = "doc-db") final String dbName) {
        String result;

        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            dataService.createDb(dbName);
            outputStream.write(String.format("Successfully created database '%s'.\n\n", dbName).getBytes());

            final String datasetFile = "data/person_movie.ttl";
            dataService.loadDataset(dbName, RDFFormat.TURTLE, datasetFile);
            outputStream.write(String.format("Loaded dataset from '%s' to database '%s'\n\n", datasetFile, dbName).getBytes());

            final String docName = "article.txt";
            final String docPath = "data/" + docName;
            dataService.loadDocs(dbName, docPath);
            outputStream.write(String.format("Loaded document '%s' to database '%s'\n\n", docPath, dbName).getBytes());

            outputStream.write(String.format("The following people are mentioned in the loaded document '%s'\n", docPath).getBytes());
            final String sparql = String.format("select ?mention ?entity where {"
                    + " graph <tag:stardog:api:docs:%s:%s> {"
                    + "   ?s rdfs:label ?mention ."
                    + "   ?s <http://purl.org/dc/terms/references> ?entity ."
                    + " } }",
                    dbName, docName);
            dataService.executeSelectQuery(dbName, sparql, outputStream);


            result = outputStream.toString();
        } catch (Exception e) {
            result = "Exception found loading dataset into Stardog: " + e.getMessage();
            logger.error(result, e);
        }

        return result;
    }


    /**
     * Common method to load a dataset from a file to a stardog database
     * @param dbName the database name
     * @param format the RDFFormat
     * @param fileNames the file name(s)
     * @return string result
     */
    private String loadDataset(String dbName, RDFFormat format, String... fileNames) {
        String result;

        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            dataService.createDb(dbName);
            outputStream.write(String.format("Successfully created database '%s'.\n\n", dbName).getBytes());

            dataService.loadDataset(dbName, format, fileNames);
            outputStream.write(String.format("Loaded file '%s' to database '%s'\n\n",
                    StringUtils.join(fileNames, ","), dbName).getBytes());

            final String sparql = "SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 10";
            dataService.executeSelectQuery(dbName, sparql, outputStream);

            result = outputStream.toString();

        } catch (Exception e) {
            result = "Exception found loading dataset into Stardog: " + e.getMessage();
            logger.error(result, e);
        }

        return result;
    }
}
