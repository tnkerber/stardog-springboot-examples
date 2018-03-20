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
import com.polarisalpha.ca.stardog.service.StardogService;

@RestController
public class StardogController {
    private static final Logger logger = LoggerFactory.getLogger(StardogController.class);

    @Autowired
    private StardogService stardogService;

    @RequestMapping(value = "/load-n3", produces = "text/plain")
    public String testN3(@RequestParam(value = "dbName", defaultValue = "n3-db") final String dbName) {
        return loadDataset(dbName,  RDFFormat.N3,"data/sp2b_10k.n3");
    }

    @RequestMapping(value = "/load-turtle", produces = "text/plain")
    public String testTurtle(@RequestParam(value = "dbName", defaultValue = "turtle-db") final String dbName) {
        return loadDataset(dbName, RDFFormat.TURTLE,"data/starwars.ttl");
    }

    @RequestMapping(value = "/load-rdfxml1", produces = "text/plain")
    public String testRDFXML1(@RequestParam(value = "dbName", defaultValue = "rdfxml1-db") final String dbName) {
        return loadDataset(dbName, RDFFormat.RDFXML,"data/University0_0.owl", "data/lubmSchema.owl");
    }

    @RequestMapping(value = "/load-rdfxml2", produces = "text/plain")
    public String testRDFXML2(@RequestParam(value = "dbName", defaultValue = "rdfxml2-db") final String dbName) {
        return loadDataset(dbName, RDFFormat.RDFXML,"data/catalog.rdf");
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

        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
            stardogService.initDb(dbName);
            outputStream.write(String.format("Successfully created database '%s'.\n\n", dbName).getBytes());

            stardogService.loadDataset(dbName, format, fileNames);
            outputStream.write(String.format("Loaded file '%s' to database '%s'\n\n",
                    StringUtils.join(fileNames, ","), dbName).getBytes());

            final String sparql = "SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 10";
            stardogService.executeQuery(dbName, sparql, outputStream);

            result = outputStream.toString();

        } catch (Exception e) {
            result = "Exception found loading dataset into Stardog: " + e.getMessage();
            logger.error(result, e);
        }

        return result;
    }
}
