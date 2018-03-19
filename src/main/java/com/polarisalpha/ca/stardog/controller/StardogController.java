package com.polarisalpha.ca.stardog.controller;

import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.polarisalpha.ca.stardog.service.StardogService;

@RestController
public class StardogController {
    private static final Logger logger = LoggerFactory.getLogger(StardogController.class);

    @Autowired
    private StardogService stardogService;

    @RequestMapping("/load-n3")
    public void testN3() {
        loadDataset("n3",  RDFFormat.N3,"data/sp2b_10k.n3");
    }

    @RequestMapping("/load-turtle")
    public void testTurtle() {
        loadDataset("turtle", RDFFormat.TURTLE,"data/starwars.ttl");
    }

    @RequestMapping("/load-rdfxml1")
    public void testRDFXML1() {
        loadDataset("rdfxml1", RDFFormat.RDFXML,"data/University0_0.owl", "data/lubmSchema.owl");
    }

    @RequestMapping("/load-rdfxml2")
    public void testRDFXML2() {
        loadDataset("rdfxml2", RDFFormat.RDFXML,"data/catalog.rdf");
    }


    private void loadDataset(String db, RDFFormat format, String... fileNames) {
        try {
            stardogService.initDb(db);
            stardogService.loadDataset(db, format, fileNames);

            final String sparql = "SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 10";
            stardogService.executeQuery(db, sparql);

        } catch (Exception e) {
            logger.error("Exception found ingesting Stardog data", e);
            e.printStackTrace();
        }
    }
}
