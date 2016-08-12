package cz.cuni.mff.xrg.odcs.commons.app.data.handlers;

import java.util.LinkedList;
import java.util.List;

/**
 * Error handler for {@link cz.cuni.mff.xrg.odcs.commons.app.data.EdgeCompiler},
 * that store all the invalid mappings.
 * 
 * @author Petyr
 */
public class StoreInvalidMappings extends LogAndIgnore {

    private final List<String> invalidMapping = new LinkedList<>();

    @Override
    public void invalidMapping(String item) {
        super.invalidMapping(item);
        // add to the list
        invalidMapping.add(item);
    }

    /**
     * @return List of examined invalid mappings.
     */
    public List<String> getInvalidMapping() {
        return invalidMapping;
    }

}
