package com.polarisalpha.ca.stardog.extractor;

import java.io.BufferedReader;
import java.io.Reader;
import org.openrdf.model.IRI;
import org.openrdf.model.Statement;
import com.complexible.common.openrdf.model.Models2;
import com.complexible.common.rdf.StatementSource;
import com.complexible.common.rdf.impl.MemoryStatementSource;
import com.complexible.common.rdf.model.Values;
import com.complexible.stardog.api.Connection;
import com.complexible.stardog.docs.extraction.tika.TextProvidingRDFExtractor;

public class WordAndLineCountExtractor extends TextProvidingRDFExtractor {
    public static final String WORD_COUNT = "tag:stardog:api:docs:wordCount";
    public static final String LINE_COUNT = "tag:stardog:api:docs:lineCount";

    /**
     * Compute the word and line count, create an RDF triple linking the word count to the document, return them as Models
     */
    @Override
    protected StatementSource extractFromText(Connection conn, IRI iri, Reader reader) throws Exception {
        int wordCount = 0;
        int lineCount = 0;

        try (final BufferedReader aBufferedReader = new BufferedReader(reader)) {
            String line;
            while ((line = aBufferedReader.readLine()) != null) {
                wordCount += line.split(" ").length;
                lineCount++;
            }
        }

        final Statement lineCountStmt = Values.statement(iri, Values.iri(String.format(LINE_COUNT)), Values.literal(lineCount));
        final Statement wordCountStmt = Values.statement(iri, Values.iri(String.format(WORD_COUNT)), Values.literal(wordCount));

        return MemoryStatementSource.of(Models2.newModel(wordCountStmt, lineCountStmt));
    }
}
