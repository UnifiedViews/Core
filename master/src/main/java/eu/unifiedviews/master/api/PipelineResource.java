package eu.unifiedviews.master.api;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.facade.PipelineFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.UserFacade;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.ImportException;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.ImportService;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;
import cz.cuni.mff.xrg.odcs.commons.app.user.UserActor;
import eu.unifiedviews.master.authentication.AuthenticationRequired;
import eu.unifiedviews.master.converter.ConvertUtils;
import eu.unifiedviews.master.converter.PipelineDTOConverter;
import eu.unifiedviews.master.model.ApiException;
import eu.unifiedviews.master.model.PipelineDTO;

@Component
@Path("/pipelines")
@AuthenticationRequired
public class PipelineResource {

    private static final Logger LOG = LoggerFactory.getLogger(PipelineResource.class);

    @Autowired
    private PipelineFacade pipelineFacade;

    @Autowired
    private ImportService importService;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private AppConfig appConfig;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PipelineDTO createPipeline(PipelineDTO pipelineDTO) {
        // try to get user
        User user = userFacade.getUserByExtId(pipelineDTO.getUserExternalId());
        if (user == null) {
            throw new ApiException(Response.Status.NOT_FOUND, String.format("User '%s' could not be found! Pipeline could not be created.", pipelineDTO.getUserExternalId()));
        }

        Pipeline pipeline = null;
        try {
            pipeline = pipelineFacade.createPipeline();
            if (pipeline == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("Pipeline could not be created."));
            }
            final UserActor actor = this.userFacade.getUserActorByExternalId(pipelineDTO.getUserActorExternalId());
            pipeline.setUser(user);
            if (actor != null) {
                pipeline.setActor(actor);
            }
            pipeline = PipelineDTOConverter.convertFromDTO(pipelineDTO, pipeline);
            this.pipelineFacade.save(pipeline);
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException exception) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
        return PipelineDTOConverter.convert(pipeline);
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public List<PipelineDTO> getPipelines(@QueryParam("userExternalId") String userExternalId) {
        List<Pipeline> pipelines = null;
        try {
            if (isNotEmpty(userExternalId)) {
                pipelines = this.pipelineFacade.getAllPipelines(userExternalId);
            } else {
                pipelines = this.pipelineFacade.getAllPipelines();
            }

            if (pipelines == null) {
                throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, "PipelineFacade returned null!");
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException exception) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, exception.getMessage());
        }

        return PipelineDTOConverter.convert(pipelines);
    }

    @GET
    @Path("/{pipelineid}")
    @Produces({ MediaType.APPLICATION_JSON })
    public PipelineDTO getPipeline(@PathParam("pipelineid") String id) {
        Pipeline pipeline = null;
        if (StringUtils.isBlank(id) || !StringUtils.isNumeric(id)) {
            throw new ApiException(Response.Status.NOT_FOUND, String.format("ID=%s is not valid pipeline ID", id));
        }
        try {
            pipeline = pipelineFacade.getPipeline(Long.parseLong(id));
            if (pipeline == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("Pipeline with id=%s doesn't exist!", id));
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException exception) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, exception.getMessage());
        }

        return PipelineDTOConverter.convert(pipeline);
    }

    @POST
    @Path("/{pipelineid}/clones")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PipelineDTO clonePipeline(@PathParam("pipelineid") String id, PipelineDTO pipelineDTO) {
        Pipeline pipeline = null;
        Pipeline pipelineCopy = null;
        if (StringUtils.isBlank(id) || !StringUtils.isNumeric(id)) {
            throw new ApiException(Response.Status.NOT_FOUND, String.format("ID=%s is not valid pipeline ID", id));
        }
        try {
            // try to get pipeline
            pipeline = pipelineFacade.getPipeline(Long.parseLong(id));
            if (pipeline == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("Pipeline with id=%s doesn't exist!", id));
            }
            // try to get user
            User user = userFacade.getUserByExtId(pipelineDTO.getUserExternalId());
            if (user == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("User '%s' could not be found! Pipeline could not be created.", pipelineDTO.getUserExternalId()));
            }

            final UserActor actor = this.userFacade.getUserActorByExternalId(pipelineDTO.getUserActorExternalId());
            pipelineCopy = this.pipelineFacade.copyPipeline(pipeline);
            pipelineCopy.setUser(user);
            pipelineCopy.setUser(user);
            if (actor != null) {
                pipelineCopy.setActor(actor);
            }
            pipelineCopy = PipelineDTOConverter.convertFromDTO(pipelineDTO, pipelineCopy);
            this.pipelineFacade.save(pipelineCopy);
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException exception) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
        return PipelineDTOConverter.convert(pipelineCopy);
    }

    @POST
    @Path("/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public PipelineDTO importPipeline(@FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition contentDispositionHeader, @FormDataParam("importUserData") boolean importUserData, @FormDataParam("importSchedule") boolean importSchedule) {
        // parse input steam to file, located in temporary directory
        File pipelineFile;
        Pipeline importedPipeline;
        try {
            pipelineFile = ConvertUtils.inputStreamToFile(inputStream, contentDispositionHeader.getFileName());
            importedPipeline = importService.importPipeline(pipelineFile, importUserData, importSchedule);
        } catch (IOException e) {
            LOG.error("Exception at reading input stream", e);
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (ImportException e) {
            LOG.error("Exception at importing pipeline", e);
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        return PipelineDTOConverter.convert(importedPipeline);
    }
}
