package eu.unifiedviews.master.api;

import cz.cuni.mff.xrg.odcs.commons.app.constants.LenghtLimits;
import cz.cuni.mff.xrg.odcs.commons.app.facade.PipelineFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.UserFacade;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.ImportException;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.ImportService;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;
import cz.cuni.mff.xrg.odcs.commons.app.user.UserActor;
import eu.unifiedviews.master.authentication.AuthenticationRequired;
import eu.unifiedviews.master.authentication.BasicAuthenticationFilter;
import eu.unifiedviews.master.converter.ConvertUtils;
import eu.unifiedviews.master.converter.PipelineDTOConverter;
import eu.unifiedviews.master.i18n.Messages;
import eu.unifiedviews.master.model.ApiException;
import eu.unifiedviews.master.model.PipelineDTO;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

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
    private UserHelper userHelper;

//    @Autowired
//    private BasicAuthenticationFilter authFilter;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PipelineDTO createPipeline(PipelineDTO pipelineDTO) {
        // validate pipeline name length
        if (pipelineDTO.getName().length() > LenghtLimits.PIPELINE_NAME) {
            throw new ApiException(Response.Status.BAD_REQUEST, Messages.getString("pipeline.name.length.exceeded"), String.format("Pipeline length cannot exceed 1024 characters! Actual is %d", pipelineDTO.getName().length()));
        }
        Pipeline pipeline = null;

        // check if pipeline with the same name already exists
        boolean alreadyExists = pipelineFacade.hasPipelineWithName(pipelineDTO.getName(), null);
        if (alreadyExists) {
            throw new ApiException(Response.Status.CONFLICT, Messages.getString("pipeline.name.duplicate", pipelineDTO.getName()), String.format("Pipeline with name '%s' already exists. Pipeline cannot be created!", pipelineDTO.getName()));
        }

        String username = userHelper.getUser(pipelineDTO.getUserExternalId());
        if (username == null) {
            throw new ApiException(Response.Status.NOT_FOUND, Messages.getString("pipeline.user.id.not.found"), String.format("We cannot fetch username from the request!"));
        }
        User user = userFacade.getUserByExtId(username);
        if (user == null) {
            throw new ApiException(Response.Status.NOT_FOUND, Messages.getString("pipeline.user.id.not.found"), String.format("User '%s' could not be found! Pipeline could not be created.", pipelineDTO.getUserExternalId()));
        }

        pipeline = pipelineFacade.createPipeline();
        pipeline.setUser(user);

        final UserActor actor = this.userFacade.getUserActorByExternalId(pipelineDTO.getUserActorExternalId());
        if (actor != null) {
            pipeline.setActor(actor);
        }
        pipeline = PipelineDTOConverter.convertFromDTO(pipelineDTO, pipeline);

        try {
            this.pipelineFacade.save(pipeline);
        } catch (ApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, Messages.getString("pipeline.create.general.error"), e.getMessage());
        }
        return PipelineDTOConverter.convert(pipeline);
    }

    /**
     * Returns pipelines for the user specified as a query parameter. If user is not specified, it uses the user from the basic authentication header
     * @param userExternalId
     * @return Pipelines for the user specified as a query parameter
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public List<PipelineDTO> getPipelines(@QueryParam("userExternalId") String userExternalId) {
        List<Pipeline> pipelines = null;

        try {
            String user = userHelper.getUser(userExternalId);
            if (user != null) {
                pipelines = this.pipelineFacade.getAllPipelines(user);
            }
        } catch (ApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, Messages.getString("pipeline.get.general.error"), e.getMessage());
        }

        return PipelineDTOConverter.convert(pipelines);
    }

    @GET
    @Path("/visible")
    @Produces({ MediaType.APPLICATION_JSON })
    public List<PipelineDTO> getVisiblePipelines(@QueryParam("userExternalId") String userExternalId) {
        List<Pipeline> pipelines = null;
        try {
            String user = userHelper.getUser(userExternalId);
            if (user != null) {
                pipelines = this.pipelineFacade.getAllVisiblePipelines(user);
            }
        } catch (ApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, Messages.getString("pipeline.get.general.error"), e.getMessage());
        }
        return PipelineDTOConverter.convert(pipelines);
    }


    @GET
    @Path("/{pipelineid}")
    @Produces({ MediaType.APPLICATION_JSON })
    public PipelineDTO getPipeline(@PathParam("pipelineid") String id) {
        Pipeline pipeline = null;
        if (StringUtils.isBlank(id) || !StringUtils.isNumeric(id)) {
            throw new ApiException(Response.Status.BAD_REQUEST, Messages.getString("pipeline.id.invalid", id), String.format("ID=%s is not valid pipeline ID", id));
        }
        try {
            pipeline = pipelineFacade.getPipeline(Long.parseLong(id));
            if (pipeline == null) {
                throw new ApiException(Response.Status.NOT_FOUND, Messages.getString("pipeline.id.not.found", id), String.format("Pipeline with id=%s doesn't exist!", id));
            }
        } catch (ApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, Messages.getString("pipeline.get.general.error"), e.getMessage());
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
            throw new ApiException(Response.Status.BAD_REQUEST, Messages.getString("pipeline.id.invalid", id), String.format("ID=%s is not valid pipeline ID", id));
        }

        // validate pipeline name length
        if (pipelineDTO.getName().length() > LenghtLimits.PIPELINE_NAME) {
            throw new ApiException(Response.Status.BAD_REQUEST, Messages.getString("pipeline.name.length.exceeded"), String.format("Pipeline length cannot exceed 1024 characters! Actual is %d", pipelineDTO.getName().length()));
        }

        try {
            // try to get pipeline
            pipeline = pipelineFacade.getPipeline(Long.parseLong(id));
            if (pipeline == null) {
                throw new ApiException(Response.Status.NOT_FOUND, Messages.getString("pipeline.id.not.found", id), String.format("Pipeline with id=%s doesn't exist!", id));
            }
            // get user
            String username = userHelper.getUser(pipelineDTO.getUserExternalId());
            if (username == null) {
                throw new ApiException(Response.Status.NOT_FOUND, Messages.getString("pipeline.user.id.not.found"), String.format("We cannot fetch username from the request!"));
            }
            User user = userFacade.getUserByExtId(username);
            if (user == null) {
                throw new ApiException(Response.Status.NOT_FOUND, Messages.getString("pipeline.user.id.not.found"), String.format("User '%s' could not be found! Pipeline could not be created.", pipelineDTO.getUserExternalId()));
            }

            // check if pipeline with the same name already exists
            boolean alreadyExists = pipelineFacade.hasPipelineWithName(pipelineDTO.getName(), null);
            if (alreadyExists) {
                throw new ApiException(Response.Status.CONFLICT, Messages.getString("pipeline.name.duplicate", pipelineDTO.getName()), String.format("Pipeline with name '%s' already exists. Pipeline cannot be created!", pipelineDTO.getName()));
            }

            pipelineCopy = this.pipelineFacade.copyPipeline(pipeline);
            pipelineCopy.setUser(user);

            final UserActor actor = this.userFacade.getUserActorByExternalId(pipelineDTO.getUserActorExternalId());
            if (actor != null) {
                pipelineCopy.setActor(actor);
            }

            pipelineCopy = PipelineDTOConverter.convertFromDTO(pipelineDTO, pipelineCopy);
            this.pipelineFacade.save(pipelineCopy);
        } catch (ApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, Messages.getString("pipeline.create.general.error"), e.getMessage());
        }
        return PipelineDTOConverter.convert(pipelineCopy);
    }

    @POST
    @Path("/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public PipelineDTO importPipeline(@FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition contentDispositionHeader, @FormDataParam("userExternalId") String username, @FormDataParam("importUserData") @DefaultValue("false") boolean importUserData, @FormDataParam("importSchedule") @DefaultValue("false") boolean importSchedule) {
        // parse input steam to file, located in temporary directory
        File pipelineFile;
        Pipeline importedPipeline;
        try {
            pipelineFile = ConvertUtils.inputStreamToFile(inputStream, contentDispositionHeader.getFileName());

            String usernameCreator = userHelper.getUser(username);
            if (usernameCreator == null || usernameCreator.isEmpty()) {
                throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, Messages.getString("pipeline.import.general.error"), "Please specify name of the user who is the pipeline creator");
            }
            User user = userFacade.getUserByUsername(usernameCreator);
            if (user == null) {
                throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, Messages.getString("pipeline.import.general.error"), "User with the given username does not exist");
            }
            importedPipeline = importService.importPipelineApi(pipelineFile, user, importUserData, importSchedule);
        } catch (IOException | ImportException e) {
            LOG.error("Exception at importing pipeline", e);
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, Messages.getString("pipeline.import.general.error"), e.getMessage());
        }
        return PipelineDTOConverter.convert(importedPipeline);
    }

}
