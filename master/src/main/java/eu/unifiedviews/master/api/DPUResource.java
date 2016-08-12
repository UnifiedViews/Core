package eu.unifiedviews.master.api;

import cz.cuni.mff.xrg.odcs.commons.app.auth.ShareType;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.facade.DPUFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ModuleFacade;
import cz.cuni.mff.xrg.odcs.commons.app.module.DPUCreateException;
import cz.cuni.mff.xrg.odcs.commons.app.module.DPUModuleManipulator;
import cz.cuni.mff.xrg.odcs.commons.app.module.DPUReplaceException;
import eu.unifiedviews.master.authentication.AuthenticationRequired;
import eu.unifiedviews.master.converter.ConvertUtils;
import eu.unifiedviews.master.i18n.Messages;
import eu.unifiedviews.master.model.ApiException;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Path("/import")
@AuthenticationRequired
public class DPUResource {

    @Autowired
    private DPUFacade dpuFacade;

    @Autowired
    private ModuleFacade moduleFacade;

    @Autowired
    private DPUModuleManipulator dpuManipulator;

    private static final Logger LOG = LoggerFactory.getLogger(DPUResource.class);

    @POST
    @Path("/dpu/jar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public String importJarDpu(@FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition contentDispositionHeader, @QueryParam("name") String dpuName, @QueryParam("description") String dpuDescription, @QueryParam("visibility") String visibility,
            @QueryParam("force") boolean force) {
        LOG.debug("Importing dpu file: {}, name: {}, description: {}, visibility: {}, force?: {}", contentDispositionHeader.getFileName(), dpuName, dpuDescription, visibility, force);
        // parse input steam to file, located in temporary directory
        File jarFile;
        try {
            jarFile = ConvertUtils.inputStreamToFile(inputStream, contentDispositionHeader.getFileName());
        } catch (IOException e) {
            LOG.error("Exception at reading input stream.", e);
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, Messages.getString("general.exception"), e.getMessage());
        }
        // if dpu name is empty, set it to null
        if (StringUtils.isEmpty(dpuName)) {
            dpuName = null;
        }
        ShareType shareType;
        if (StringUtils.isEmpty(visibility)) { // if visibility is null, or empty, use default value
            shareType = ShareType.PUBLIC_RO;
        } else { // else parse value
            try {
                shareType = ShareType.valueOf(visibility);
            } catch (IllegalArgumentException e) {
                LOG.error("Exception at parsing share type", e);
                throw new ApiException(Response.Status.BAD_REQUEST, Messages.getString("visibility.input.error"), "Failed to parse visibility value.");
            }
        }

        // check if DPU already exists
        String dpuDirName = getDirectoryName(jarFile.getName());
        if (dpuDirName == null) {
            LOG.error("Cannot parse directory name from JAR file: {}", jarFile.getName());
            throw new ApiException(Response.Status.BAD_REQUEST, Messages.getString("dpu.name.parse.error"), String.format("Cannot parse directory name from JAR file: %s", jarFile.getName()));
        }
        DPUTemplateRecord dpuTemplate = dpuFacade.getByDirectory(dpuDirName);

        if (dpuTemplate == null) { // it doesnt exists, create it
            //check if DPU of older version is on the disk
            File dpuDir = new File(moduleFacade.getDPUDirectory(), dpuDirName);
            if (dpuDir.exists()) {
                LOG.warn("Inconsistent state! DPU Record is not present in DB, but old files remain on disk. Directory: {}", dpuDir);
            }
            createDpu(dpuName, dpuDescription, shareType, jarFile);
        } else if (force == true) { // it does exists, check force flag and replace
            LOG.debug("DPU already exists! Force flag detected, replacing DPU.");
            if (StringUtils.isNotEmpty(dpuName)) {
                dpuTemplate.setName(dpuName);
            }
            if (StringUtils.isNotEmpty(dpuDescription)) {
                dpuTemplate.setDescription(dpuDescription);
            }
            if (StringUtils.isNotEmpty(visibility)) {
                dpuTemplate.setShareType(shareType);
            }
            replaceDpu(dpuTemplate, jarFile);
        } else {
            throw new ApiException(Response.Status.BAD_REQUEST, Messages.getString("dpu.already.exists"), "DPU with this name already exists.");
        }

        // now we can delete the file
        deleteTempFile(jarFile);
        LOG.debug("DPU {} successfully imported.", jarFile.getName());
        return "OK";
    }

    private void replaceDpu(DPUTemplateRecord dpuTemplate, File jarFile) {
        try {
            dpuManipulator.replace(dpuTemplate, jarFile);
        } catch (DPUReplaceException e) {
            LOG.error("Exception at replacing DPU", e);
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, Messages.getString("dpu.replace.failed"), e.getMessage());
        }
    }

    private void createDpu(String dpuName, String dpuDescription, ShareType shareType, File jarFile) {
        try {
            DPUTemplateRecord dpuTemplate = dpuManipulator.create(jarFile, dpuName);
            dpuTemplate.setDescription(dpuDescription);
            dpuTemplate.setShareType(shareType);
            dpuFacade.save(dpuTemplate);
        } catch (DPUCreateException e) {
            LOG.error("Exception at importing DPU", e);
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, Messages.getString("dpu.create.failed"), e.getMessage());
        }
    }

    /**
     * Delete temporary file created by webservice.
     * Method deletes file and its parent folder.
     *
     * @param file File to delete.
     */
    private void deleteTempFile(File file) {
        File parent = file.getParentFile();
        file.delete();
        parent.delete();
    }

    private static String getDirectoryName(String sourceFileName) {
        // the name must be in format: NAME-.*.jar
        final Pattern pattern = Pattern
                .compile("(.+)-(\\d(\\.\\d+)+).*\\.jar");
        final Matcher matcher = pattern.matcher(sourceFileName);
        if (matcher.matches()) {
            // 0 - original, 1 - name, 2 - version
            return matcher.group(1);
        } else {
            return null;
        }
    }
}
