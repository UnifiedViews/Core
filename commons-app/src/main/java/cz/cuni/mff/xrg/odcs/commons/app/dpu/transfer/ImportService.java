package cz.cuni.mff.xrg.odcs.commons.app.dpu.transfer;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import cz.cuni.mff.xrg.odcs.commons.app.auth.AuthenticationContext;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.ImportException;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.xstream.JPAXStream;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;

/**
 * Service for importing dpu templates exported by {@link ExportService}
 * 
 * @author mvi
 */
public class ImportService {

    private static final Logger LOG = LoggerFactory.getLogger(ImportService.class);

    @Autowired(required = false)
    private AuthenticationContext authCtx;

    /**
     * @param lstFile
     * @return list of templates, null if lstFile doesn't exist
     * @throws ExportException
     */
    @SuppressWarnings("unchecked")
    public List<DPUTemplateRecord> importDPUs(File lstFile) throws ImportException {
        if (!lstFile.exists()) {
            LOG.warn("file: {} is not exist", lstFile.getName());
            return null;
        }
        checkAuth(getAuthCtx());

        final XStream xStream = JPAXStream.createForDPUTemplate(new DomDriver("UTF-8"));

        try {
            return (List<DPUTemplateRecord>) xStream.fromXML(lstFile);
        } catch (Throwable e) {
            LOG.error("Missing or wrong lst file");
            throw new ImportException(Messages.getString("ImportService.wrong.lst.file"), e);
        }
    }

    private void checkAuth(AuthenticationContext authCtx) throws ImportException {
        if (authCtx == null) {
            throw new ImportException(Messages.getString("ImportService.authenticationContext.fail"));
        }
        final User user = authCtx.getUser();
        if (user == null) {
            throw new ImportException(Messages.getString("ImportService.unknown.user"));
        }
    }

    public AuthenticationContext getAuthCtx() {
        return authCtx;
    }
}
