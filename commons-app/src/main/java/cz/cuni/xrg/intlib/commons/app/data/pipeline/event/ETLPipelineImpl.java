package cz.cuni.xrg.intlib.commons.app.data.pipeline.event;

import cz.cuni.xrg.intlib.commons.app.data.pipeline.event.wraper.*;
import cz.cuni.xrg.intlib.commons.app.data.pipeline.event.extract.*;
import cz.cuni.xrg.intlib.commons.app.data.pipeline.event.transform.*;
import cz.cuni.xrg.intlib.commons.app.data.pipeline.event.load.*;

import cz.cuni.xrg.intlib.commons.app.data.pipeline.event.extract.Extractor;
import cz.cuni.xrg.intlib.commons.app.data.pipeline.event.load.Loader;
import cz.cuni.xrg.intlib.commons.app.data.pipeline.event.transform.Transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.util.RDFInserter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

/**
 * Represents a fixed workflow composed of one or several {@link Extractor}s,
 * {@link Transformer}s and {@link Loader}s executed in a fixed order. <br/>
 * Processing will always take place in the following order: <ol> <li>Execute
 * all {@link Extractor}s in the order of the {@link List}</li> <ul> <li>If an
 * Extractor throws an error publish an {@link ExtractFailedEvent} - otherwise
 * publish an {@link ExtractCompletedEvent}</li> <li>If an Extractor requests
 * cancellation of the pipeline through {@link ProcessingContext#cancelPipeline}
 * publish a {@link PipelineAbortedEvent} and exit</li> </ul> <li>Execute all
 * {@link Transformer}s in the order of the {@link List}</li> <ul> <li>If a
 * Transformer throws an error publish an {@link TransformFailedEvent} -
 * otherwise publish an {@link TransformCompletedEvent}</li>
 * <li>If a Transformer requests cancellation of the pipeline through
 * {@link ProcessingContext#cancelPipeline} publish a
 * {@link PipelineAbortedEvent} and exit</li> </ul> <li>Execute all
 * {@link Loader}s in the order of the {@link List}</li> <ul> <li>If a Loader
 * throws an error publish an {@link LoadFailedEvent} - otherwise publish an
 * {@link LoadCompletedEvent}</li>
 * <li>If a Loader requests cancellation of the pipeline through
 * {@link ProcessingContext#cancelPipeline} publish a
 * {@link PipelineAbortedEvent} and exit</li> </ul> <li>Publish a
 * {@link PipelineCompletedEvent}
 * </ol> <br/> A Spring {@link ApplicationEventPublisher} is required for
 * propagation of important events occurring thoughout the pipeline.<br/> Also,
 * a {@link Repository} instance capable of storing named graphs is essential
 * for this pipeline to work. All extracted RDF data will be stored in a
 * dedicated graph in the repository and accessed / manipulated by the
 * {@link Transformer}s before it is exported by the {@link Loader}s.
 *
 * @see Extractor
 * @see Transformer
 * @see Loader
 * @author Alex Kreiser (akreiser@gmail.com)
 */
public class ETLPipelineImpl implements ETLPipeline, ApplicationEventPublisherAware {

    protected List<Extractor> extractors = new ArrayList<Extractor>();
    protected List<Transformer> transformers = new ArrayList<Transformer>();
    protected List<Loader> loaders = new ArrayList<Loader>();
   
    protected static final Logger logger = Logger.getLogger(ETLPipelineImpl.class);
    protected boolean cancelAllowed = true;
    
    protected String ID;
    protected ApplicationEventPublisher eventPublisher;
    protected Repository repository;

    /**
     * Constructor
     *
     * @param ID The identifier of this pipeline, will be used as named graph
     * where all the RDF data of this pipeline will be cached
     * @param eventPublisher Publisher for {@link ETLEvent}s
     * @param repository Repository functioning as RDF cache
     */
    public ETLPipelineImpl(String ID, ApplicationEventPublisher eventPublisher, Repository repository) {
        this.ID = ID;
        this.eventPublisher = eventPublisher;
        this.repository = repository;
    }

    /**
     * Empty constructor
     */
    public ETLPipelineImpl() {
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void setId(String ID) {
        this.ID = ID;
    }

    public Repository getRepository() {
        return repository;
    }

    @Override
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    /**
     * Returns the event publisher instance.
     *
     * @return
     */
    public ApplicationEventPublisher getEventPublisher() {
        return eventPublisher;
    }

    public List<Extractor> getExtractors() {
        return extractors;
    }

    public void setExtractors(List<Extractor> extractors) {
        this.extractors = extractors;
    }

    @Override
    public List<Loader> getLoaders() {
        return loaders;
    }

    @Override
    public void setLoaders(List<Loader> loaders) {
        this.loaders = loaders;
    }

    @Override
    public List<Transformer> getTransformers() {
        return transformers;
    }

    @Override
    public void setTransformers(List<Transformer> transformers) {
        this.transformers = transformers;
    }

    /**
     * Returns if single components are allowed to cancel the entire pipeline
     * using {@link ProcessingContext#cancelPipeline(java.lang.String)}.
     *
     * @return
     */
    public boolean isCancelAllowed() {
        return cancelAllowed;
    }

    /**
     * Sets if single components are allowed to cancel the entire pipeline using
     * {@link ProcessingContext#cancelPipeline(java.lang.String)}.
     *
     * @param cancelAllowed
     */
    public void setCancelAllowed(boolean cancelAllowed) {
        this.cancelAllowed = cancelAllowed;
    }

    @Override
    public void run() {
        long pipelineStart = System.currentTimeMillis();
        String runId = UUID.randomUUID().toString();
        final URI pipelineId = new URIImpl(ID);
        try {
            final Map<String, Object> customData = new HashMap<String, Object>();
            try {
                eventPublisher.publishEvent(new PipelineStartedEvent(this, runId, this));
                
                RepositoryConnection con = repository.getConnection();
                con.setAutoCommit(false);
                
                RDFInserter inserter = new RDFInserter(con);
                inserter.enforceContext(pipelineId);
                
                NoStartEndWrapper wrapper = new NoStartEndWrapper(inserter);
                
                try {
                    for (Extractor extractor : extractors) {
                        if (extractor instanceof Disable && ((Disable) extractor).isDisabled()) {
                            continue;
                        }
                        ExtractContext context = new ExtractContext(runId, customData);
                        context.setPipeline(this);
                       
                        TripleCountingWrapper tripleCounter = new TripleCountingWrapper(wrapper);
                        try {
                            long start = System.currentTimeMillis();
                            extractor.extract(tripleCounter, context);
                            con.commit();
                            
                            context.setDuration(System.currentTimeMillis() - start);
                            context.setTriplesExtracted(tripleCounter.getTriples());
                            eventPublisher.publishEvent(new ExtractCompletedEvent(extractor, context, this));
                        } catch (ExtractException ex) {
                            con.rollback();
                            eventPublisher.publishEvent(new ExtractFailedEvent(ex, extractor, context, this));
                        }
                        if (cancelAllowed && context.isCancelPipeline()) {
                            eventPublisher.publishEvent(new PipelineAbortedEvent(context.getCancelMessage(), this, runId, extractor));
                            return;
                        }
                    }
                    inserter.endRDF();
                } finally {
                    con.commit();
                    con.close();
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }

            for (Transformer transformer : transformers) {
                if (transformer instanceof Disable && ((Disable) transformer).isDisabled()) {
                    continue;
                }
                TransformContext context = new TransformContext(runId, customData);
                context.setPipeline(this);
                try {
                    long start = System.currentTimeMillis();
                    transformer.transform(repository, pipelineId, context);
                    context.setDuration(System.currentTimeMillis() - start);
                    eventPublisher.publishEvent(new TransformCompletedEvent(transformer, context, this));
                } catch (TransformException ex) {
                    eventPublisher.publishEvent(new TransformFailedEvent(ex, transformer, context, this));
                }
                if (cancelAllowed && context.isCancelPipeline()) {
                    eventPublisher.publishEvent(new PipelineAbortedEvent(context.getCancelMessage(), this, runId, transformer));
                    return;
                }
            }

            for (Loader loader : loaders) {
                if (loader instanceof Disable && ((Disable) loader).isDisabled()) {
                    continue;
                }
                LoadContext context = new LoadContext(runId, customData);
                context.setPipeline(this);
                try {
                    long start = System.currentTimeMillis();
                    loader.load(repository, pipelineId, context);
                    context.setDuration(System.currentTimeMillis() - start);
                    eventPublisher.publishEvent(new LoadCompletedEvent(loader, context, this));
                } catch (LoadException ex) {
                    eventPublisher.publishEvent(new LoadFailedEvent(ex, loader, context, this));
                }
                if (cancelAllowed && context.isCancelPipeline()) {
                    eventPublisher.publishEvent(new PipelineAbortedEvent(context.getCancelMessage(), this, runId, loader));
                    return;
                }
            }
        } finally {
            RepositoryConnection con = null;
            try {
                con = repository.getConnection();
                con.clear(pipelineId);
                con.commit();
            } catch (Exception ex) {
                logger.fatal("Unable to clean graph [" + ID + "]", ex);
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (RepositoryException ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }
                eventPublisher.publishEvent(new PipelineCompletedEvent((System.currentTimeMillis() - pipelineStart), this, runId, this));
            }
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
}