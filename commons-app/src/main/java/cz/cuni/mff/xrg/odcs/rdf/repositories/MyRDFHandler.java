package cz.cuni.mff.xrg.odcs.rdf.repositories;

import java.io.OutputStream;
import java.io.Writer;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.n3.N3Writer;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.openrdf.rio.trig.TriGWriter;
import org.openrdf.rio.trix.TriXWriter;
import org.openrdf.rio.turtle.TurtleWriter;

import cz.cuni.mff.xrg.odcs.rdf.enums.RDFFormatType;

/**
 * Class for better writing RDF data to file using RDFHandler.
 * 
 * @author Jiri Tomes
 */
public class MyRDFHandler implements RDFHandler {

    private RDFHandler handler;

    private RDFFormatType formatType;

    /**
     * Create new instance of {@link MyRDFHandler} for given writer and out RDF
     * data format.
     * 
     * @param writer
     *            Instance of writer where RDF data will be written.
     * @param formatType
     *            Specifies output RDF data format.
     */
    public MyRDFHandler(Writer writer, RDFFormatType formatType) {
        this.formatType = formatType;
        setHandler(writer);
    }

    /**
     * Create new instance of {@link MyRDFHandler} for given ouput stream and
     * out RDF data format.
     * 
     * @param os
     *            OutputStream where RDF data will be added.
     * @param formatType
     *            Specifies output RDF data format.
     */
    public MyRDFHandler(OutputStream os, RDFFormatType formatType) {
        this.formatType = formatType;
        setHandler(os);
    }

    /**
     * Return instance of {@link RDFHandler} depends on {@link RDFFormatType} used in constructor.
     * 
     * @return instance of {@link RDFHandler} depends on {@link RDFFormatType} used in constructor.
     */
    public RDFHandler getRDFHandler() {
        return handler;
    }

    /**
     * Set {@link #handler} as concrete implementation of {@link RDFWriter} depends on given {@link RDFFormatType} defined in constructor.
     * 
     * @param os
     *            instance of outputStream for you set hander.
     */
    private void setHandler(OutputStream os) {

        switch (formatType) {

            case N3:
                handler = new N3Writer(os);
                break;
            case RDFXML:
                handler = new RDFXMLPrettyWriter(os);
                break;
            case TRIG:
                handler = new TriGWriter(os);
                break;
            case TTL:
                handler = new TurtleWriter(os);
                break;
            case NT:
                handler = new NTriplesWriter(os);
                break;
            case TRIX:
                handler = new TriXWriter(os);
                break;
            default:
                handler = new RDFXMLWriter(os);
                break;
        }

    }

    /**
     * Set {@link #handler} as concrete implementation of {@link RDFWriter} depends on given {@link RDFFormatType} defined in constructor.
     * 
     * @param writer
     *            instance of writer for you set hander.
     */
    private void setHandler(Writer writer) {

        switch (formatType) {

            case N3:
                handler = new N3Writer(writer);
                break;
            case RDFXML:
                handler = new RDFXMLPrettyWriter(writer);
                break;
            case TRIG:
                handler = new TriGWriter(writer);
                break;
            case TTL:
                handler = new TurtleWriter(writer);
                break;
            case NT:
                handler = new NTriplesWriter(writer);
                break;
            case TRIX:
                handler = new TriXWriter(writer);
                break;
            default:
                handler = new RDFXMLWriter(writer);
                break;

        }

    }

    /**
     * Call same named method {@link RDFHandler#startRDF()} on instance of {@link #handler}.
     * 
     * @throws RDFHandlerException
     *             if handler find out problem during execution
     *             this method.
     */
    @Override
    public void startRDF() throws RDFHandlerException {
        handler.startRDF();
    }

    /**
     * Call same named method {@link RDFHandler#endRDF()} on instance of {@link #handler}.
     * 
     * @throws RDFHandlerException
     *             if handler find out problem during execution
     *             this method.
     */
    @Override
    public void endRDF() throws RDFHandlerException {
        handler.endRDF();
    }

    /**
     * Call same named method {@link RDFHandler#handleNamespace(java.lang.String, java.lang.String)
	 * } on instance of {@link #handler}.
     * 
     * @param prefix
     *            String value of prefix of defined namespace
     * @param uri
     *            String value of namespace
     * @throws RDFHandlerException
     *             if handler find out problem during execution
     *             this method.
     */
    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
        handler.handleNamespace(prefix, uri);
    }

    /**
     * Call same named method {@link RDFHandler#handleStatement(org.openrdf.model.Statement)
	 * } on instance of {@link #handler}.
     * 
     * @param st
     *            Statement that will be added to repository.
     * @throws RDFHandlerException
     *             if handler find out problem during execution
     *             this method.
     */
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        handler.handleStatement(st);
    }

    /**
     * Call same named method {@link RDFHandler#handleComment(java.lang.String)
	 * } on instance of {@link #handler}.
     * 
     * @param comment
     *            String value of comment.
     * @throws RDFHandlerException
     *             if handler find out problem during execution
     *             this method.
     */
    @Override
    public void handleComment(String comment) throws RDFHandlerException {
        handler.handleComment(comment);
    }
}
