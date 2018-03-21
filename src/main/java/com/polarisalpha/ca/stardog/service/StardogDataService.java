package com.polarisalpha.ca.stardog.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.openrdf.model.Model;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.complexible.common.rdf.query.resultio.TextTableQueryResultWriter;
import com.complexible.stardog.api.Connection;
import com.complexible.stardog.api.SelectQuery;
import com.complexible.stardog.api.UpdateQuery;
import com.complexible.stardog.api.admin.AdminConnection;
import com.complexible.stardog.virtual.api.VirtualGraph;
import com.complexible.stardog.virtual.api.admin.VirtualGraphAdminConnection;

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
     * Execute a SPARQL select query against a given database
     * @param dbName the database name
     * @param sparql the SPARQL query
     * @param outputStream the output stream to write the query result to
     * @throws Exception if error occurs
     */
    public void executeSelectQuery(String dbName, String sparql, ByteArrayOutputStream outputStream) throws Exception {
        try (final Connection conn = connectionService.getConnection(dbName)) {

            // execute the query
            final SelectQuery query = conn.select(sparql);
            final TupleQueryResult result = query.execute();

            // print out the result
            try {
                logger.debug("Query results from {} db", dbName);
                QueryResultIO.writeTuple(result, TextTableQueryResultWriter.FORMAT, outputStream);
            } finally {
                // always close your result sets, they hold resources which need to be released.
                result.close();
            }
        }
    }

    /**
     * Execute an update query against a given database
     * @param dbName the database name
     * @param sparql the SPARQL query
     * @throws Exception if error occurs
     */
    public void executeUpdateQuery(String dbName, String sparql) throws Exception {
        try (final Connection conn = connectionService.getConnection(dbName)) {
            // start transaction
            conn.begin();
            // execute query
            conn.update(sparql).execute();
            // commit
            conn.commit();
        }
    }

    /**
     * Remove a virtual graph if it exists
     * @param graphName the virtual graph name
     */
    public void removeVirtualGraph(String graphName) {
        try (final VirtualGraphAdminConnection vgAdminConn = connectionService.getVirtualGraphAdminConnection()) {
            // find out if virtual graph already exists
            final Optional<VirtualGraph> matchingGraph = vgAdminConn.getGraphs()
                    .stream()
                    .parallel()
                    .filter(vg -> vg.getName().equals(graphName))
                    .findAny();

            // remove the graph if it already exists
            if (matchingGraph.isPresent()) {
                vgAdminConn.removeGraph(graphName);
            }
        }
    }

    /**
     * Create a virtual graph
     * @param graphName the graph name to create
     * @param dbPropertyFile the JDBC connection configuration file path
     * @param mappingFile the R2RML mapping file path
     */
    public void createVirtualGraph(String graphName, String dbPropertyFile, String mappingFile) {
        // load the DB properties
        final Properties dbProperties = new Properties();
        try (final FileInputStream fileInputStream = new FileInputStream(dbPropertyFile)) {
            dbProperties.load(fileInputStream);
        } catch (IOException e) {
            final String errorMsg = String.format("Failed to load property file '%s'", dbPropertyFile);
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg);
        }

        // load the R2RML mapping
        Model model;
        try (final FileInputStream fileInputStream = new FileInputStream(mappingFile)) {
            final URL mappingFileUrl = new URL("file://" + mappingFile);
            model = Rio.parse(fileInputStream, mappingFileUrl.toString(), RDFFormat.TURTLE);
        } catch (IOException e) {
            final String errorMsg = String.format("Failed to load mapping file '%s'", mappingFile);
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg);
        }

        try (final VirtualGraphAdminConnection vgAdminConn = connectionService.getVirtualGraphAdminConnection()) {
            // remove virtual graph if it exists
            removeVirtualGraph(graphName);
            // create virtual graph
            vgAdminConn.addGraph(graphName, dbProperties, model);
        }
    }


}


