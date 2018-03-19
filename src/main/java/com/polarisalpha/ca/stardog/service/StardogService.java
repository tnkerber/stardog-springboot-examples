package com.polarisalpha.ca.stardog.service;

import java.io.FileInputStream;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.complexible.common.rdf.query.resultio.TextTableQueryResultWriter;
import com.complexible.stardog.api.Connection;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.api.SelectQuery;
import com.complexible.stardog.api.admin.AdminConnection;
import com.complexible.stardog.api.admin.AdminConnectionConfiguration;

@Service
public class StardogService {
    private static final Logger logger = LoggerFactory.getLogger(StardogService.class);

    @Value("${stardog.user}")
    private String user;
    @Value("${stardog.password}")
    private String passwd;

    /**
     * Initialize Stardog database
     * @param dbName database name
     */
    public void initDb(String dbName) {
        // create a connection to the DBMS
        try (final AdminConnection aAdminConnection = AdminConnectionConfiguration.toEmbeddedServer()
                .credentials(user, passwd)
                .connect()) {

            // drop re-create db if needed
            if (aAdminConnection.list().contains(dbName)) {
                aAdminConnection.drop(dbName);
            }

            // Create a database with default settings
            aAdminConnection.newDatabase(dbName).create();
        }
    }

    /**
     * Load data from a file into Stardog db
     * @param dbName the Stardog database name
     * @param format the file format
     * @param fileNames the file names
     * @throws Exception if error occurs
     */
    public void loadDataset(String dbName, RDFFormat format, String... fileNames) throws Exception {
        // open a connection to stardog DB
        try (final Connection conn = ConnectionConfiguration
                .to(dbName)
                .credentials(user, passwd)
                .connect()) {
            // All changes to a database *must* be performed within a transaction.
            conn.begin();

            // `IO` will automatically close the stream once the data has been read.
            for (String fileName: fileNames) {
                logger.debug("Loading dataset from {} into {} db" + fileName, dbName);
                conn.add().io()
                        .format(format)
                        .stream(new FileInputStream(fileName));
            }

            // commit the transaction.
            conn.commit();
        }
    }

    /**
     * Execute a SPARQL query against a given database
     * @param dbName the database name
     * @param sparql the SPARQL query
     * @throws Exception if error occurs
     */
    public void executeQuery(String dbName, String sparql) throws Exception {
        try (Connection conn = ConnectionConfiguration
                .to(dbName)
                .credentials(user, passwd)
                .connect()) {
            final SelectQuery aQuery = conn.select(sparql);

            // execute the query
            final TupleQueryResult result = aQuery.execute();
            try {
                logger.debug("Query results from {} db", dbName);
                QueryResultIO.writeTuple(result, TextTableQueryResultWriter.FORMAT, System.out);
            } finally {
                // always close your result sets, they hold resources which need to be released.
                result.close();
            }
        }
    }


}


