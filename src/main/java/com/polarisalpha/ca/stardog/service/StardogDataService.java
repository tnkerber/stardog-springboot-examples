package com.polarisalpha.ca.stardog.service;

import java.io.FileInputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.complexible.common.rdf.query.resultio.TextTableQueryResultWriter;
import com.complexible.stardog.api.Connection;
import com.complexible.stardog.api.SelectQuery;
import com.complexible.stardog.api.admin.AdminConnection;

@Service
public class StardogDataService {
    private static final Logger logger = LoggerFactory.getLogger(StardogDataService.class);

    @Autowired
    private StardogConnectionService connectionService;

    /**
     * Create a Stardog database
     * @param dbName database name
     */
    public void createDb(String dbName) {
        // create an admin connection to the Stardog embedded or remote server
        try (final AdminConnection aAdminConnection = connectionService.getAdminConnection()) {

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
        try (final Connection conn = connectionService.getConnection(dbName)) {
            // All changes to a database *must* be performed within a transaction.
            conn.begin();

            // `IO` will automatically close the stream once the data has been read.
            for (String fileName: fileNames) {
                logger.debug("Loading dataset from file '{}' into '{}' db" + fileName, dbName);
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
     * @param outputStream the output stream to write the query result to
     * @throws Exception if error occurs
     */
    public void executeQuery(String dbName, String sparql, ByteArrayOutputStream outputStream) throws Exception {
        try (final Connection conn = connectionService.getConnection(dbName)) {

            // execute the query
            final SelectQuery aQuery = conn.select(sparql);
            final TupleQueryResult result = aQuery.execute();

            try {
                logger.debug("Query results from {} db", dbName);
                QueryResultIO.writeTuple(result, TextTableQueryResultWriter.FORMAT, outputStream);
            } finally {
                // always close your result sets, they hold resources which need to be released.
                result.close();
            }
        }
    }


}


