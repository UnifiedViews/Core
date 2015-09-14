/**
 * This file is part of UnifiedViews.
 *
 * UnifiedViews is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UnifiedViews is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UnifiedViews.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.unifiedviews.master.api;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

import cz.cuni.mff.xrg.odcs.commons.app.constants.LenghtLimits;
import eu.unifiedviews.master.i18n.Messages;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PipelineDTO createPipeline(PipelineDTO pipelineDTO) {
        // validate pipeline name length
        if(pipelineDTO.getName().length() > LenghtLimits.PIPELINE_NAME) {
            throw new ApiException(Response.Status.BAD_REQUEST,  Messages.getString("pipeline.name.length.exceeded"), String.format("Pipeline length cannot exceed 1024 characters! Actual is %d", pipelineDTO.getName().length()));
        }
        Pipeline pipeline = null;
        try {
            // check if pipeline with the same name already exists
            boolean alreadyExists = pipelineFacade.hasPipelineWithName(pipelineDTO.getName(), null);
            if(alreadyExists) {
                throw new ApiException(Response.Status.CONFLICT, Messages.getString("pipeline.name.duplicate", pipelineDTO.getName()), String.format("Pipeline with name '%s' already exists. Pipeline cannot be created!", pipelineDTO.getName()));
            }

            // try to get user
            User user = userFacade.getUserByExtId(pipelineDTO.getUserExternalId());
            if (user == null) {
                throw new ApiException(Response.Status.NOT_FOUND, Messages.getString("pipeline.user.id.not.found"), String.format("User '%s' could not be found! Pipeline could not be created.", pipelineDTO.getUserExternalId()));
            }

            pipeline = pipelineFacade.createPipeline();
            final UserActor actor = this.userFacade.getUserActorByExternalId(pipelineDTO.getUserActorExternalId());
            pipeline.setUser(user);
            if (actor != null) {
                pipeline.setActor(actor);
            }
            pipeline = PipelineDTOConverter.convertFromDTO(pipelineDTO, pipeline);

            this.pipelineFacade.save(pipeline);
        } catch (ApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, Messages.getString("pipeline.create.general.error"), e.getMessage());
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
                pipelines = new ArrayList<>();
            }

            if (pipelines == null) {
                pipelines = new ArrayList<>();
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
        try{
            pipelines = this.pipelineFacade.getAllVisiblePipelines(userExternalId);
            
            if (pipelines == null) {
                pipelines = new ArrayList<>();
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
            // try to get user
            User user = userFacade.getUserByExtId(pipelineDTO.getUserExternalId());
            if (user == null) {
                throw new ApiException(Response.Status.NOT_FOUND, Messages.getString("pipeline.user.id.not.found"), String.format("User '%s' could not be found! Pipeline could not be created.", pipelineDTO.getUserExternalId()));
            }

            // check if pipeline with the same name already exists
            boolean alreadyExists = pipelineFacade.hasPipelineWithName(pipelineDTO.getName(), null);
            if(alreadyExists) {
                throw new ApiException(Response.Status.CONFLICT, Messages.getString("pipeline.name.duplicate", pipelineDTO.getName()), String.format("Pipeline with name '%s' already exists. Pipeline cannot be created!", pipelineDTO.getName()));
            }

            final UserActor actor = this.userFacade.getUserActorByExternalId(pipelineDTO.getUserActorExternalId());
            pipelineCopy = this.pipelineFacade.copyPipeline(pipeline);
            pipelineCopy.setUser(user);
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
    public PipelineDTO importPipeline(@FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition contentDispositionHeader, @FormDataParam("importUserData") boolean importUserData, @FormDataParam("importSchedule") boolean importSchedule) {
        // parse input steam to file, located in temporary directory
        File pipelineFile;
        Pipeline importedPipeline;
        try {
            pipelineFile = ConvertUtils.inputStreamToFile(inputStream, contentDispositionHeader.getFileName());
            importedPipeline = importService.importPipeline(pipelineFile, importUserData, importSchedule);
        } catch (IOException | ImportException e) {
            LOG.error("Exception at importing pipeline", e);
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, Messages.getString("pipeline.import.general.error"), e.getMessage());
        }
        return PipelineDTOConverter.convert(importedPipeline);
    }
}
