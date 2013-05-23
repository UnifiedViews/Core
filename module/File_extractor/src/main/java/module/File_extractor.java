package module;

import gui.ConfigDialog;

import com.vaadin.ui.CustomComponent;

import cz.cuni.xrg.intlib.commons.DpuType;
import cz.cuni.xrg.intlib.commons.configuration.Configuration;
import cz.cuni.xrg.intlib.commons.configuration.ConfigurationException;
import cz.cuni.xrg.intlib.commons.data.DataUnitType;
import cz.cuni.xrg.intlib.commons.data.rdf.RDFDataRepository;
import cz.cuni.xrg.intlib.commons.extractor.ExtractContext;
import cz.cuni.xrg.intlib.commons.extractor.ExtractException;
import cz.cuni.xrg.intlib.commons.message.MessageType;
import cz.cuni.xrg.intlib.commons.web.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Jiri Tomes
 * @author Petyr
 */
public class File_extractor implements GraphicalExtractor {

    /**
     * Configuration component.
     */
    private gui.ConfigDialog configDialog = null;
    
    /**
     * DPU configuration.
     */
    private Configuration config = null;

    /**
     * Logger class.
     */
    private Logger logger = LoggerFactory.getLogger(File_extractor.class);    
    
    public File_extractor() {
    }

    @Override
    public void saveConfigurationDefault(Configuration configuration) {
    	configuration.setValue(Config.OnlyThisText.name(), "");
    	configuration.setValue(Config.FileSuffix.name(), ".rdf");
    	configuration.setValue(Config.Path.name(), "");
    	configuration.setValue(Config.OnlyThisSuffix.name(), false);    	
    }    
    
    @Override
    public DpuType getType() {
        return DpuType.EXTRACTOR;
    }

    @Override
    public CustomComponent getConfigurationComponent(Configuration configuration) {
        // does dialog exist?
        if (this.configDialog == null) {
            // create it
            this.configDialog = new ConfigDialog();
            this.configDialog.setConfiguration(configuration);
        }
        return this.configDialog;
    }

	@Override
	public void loadConfiguration(Configuration configuration)
			throws ConfigurationException {
		// 
		logger.debug("Loading configuration..");
        if (this.configDialog == null) {
        } else {
            // get configuration from dialog
            this.configDialog.setConfiguration(configuration);
        }        
	}    
    
    @Override
    public void saveConfiguration(Configuration configuration) {
        this.config = configuration;
        if (this.configDialog == null) {
        } else {
            // also set configuration for dialog
            this.configDialog.getConfiguration(this.config);
        }
    }

    /**
     * Implementation of module functionality here.
     *
     */
    
    private String getPath() {
        String path = (String) config.getValue(Config.Path.name());
        logger.debug("Path: " + path);
        return path;
    }

    private String getFileSuffix() {
        String suffix = (String) config.getValue(Config.FileSuffix.name());
        logger.debug("FileSuffix: " + suffix);
        return suffix;
    }

    private boolean isOnlySuffixUsed() {
        boolean useSuffix = (Boolean) config.getValue(Config.OnlyThisSuffix.name());
        logger.debug("OnlyThisSuffix: " + useSuffix);
        return useSuffix;
    }

    @Override
    public void extract(ExtractContext context) throws ExtractException {
    	RDFDataRepository repository = null;
    	// create repository
    	repository = (RDFDataRepository)context.getDataUnitFactory().create(DataUnitType.RDF);
    	if (repository == null) {
    		throw new ExtractException("DataUnitFactory returned null.");
    	}
    	
    	context.addOutputDataUnit(repository);
    	
        final String baseURI = "";
        final String path = getPath();
        final String suffix = getFileSuffix();
        final boolean useOnlyThisSuffix = isOnlySuffixUsed();

        repository.extractRDFfromXMLFileToRepository(path, suffix, baseURI, useOnlyThisSuffix);
    }

}
