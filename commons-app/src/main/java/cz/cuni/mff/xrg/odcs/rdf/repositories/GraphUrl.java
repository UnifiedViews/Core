package cz.cuni.mff.xrg.odcs.rdf.repositories;

/**
 * Help class for creating graph's url.
 * 
 * @author Petyr
 * @author Jiri Tomes
 */
public class GraphUrl {

    private static final String prefix = "http://unifiedviews.eu/resource/internal/dataunit/";

    private GraphUrl() {
    }

    /**
     * Translate the dataUnit id in to graph URL format.
     * 
     * @param dataUnitId
     *            string value of data unit ID.
     * @return string representation of graph URL format.
     */
    public static String translateDataUnitId(String dataUnitId) {
        return prefix + dataUnitId.replace('_', '/');
    }

    /**
     * Return defined graph prefix used for application graph names.
     * 
     * @return defined graph prefix used for application graph names.
     */
    public static String getGraphPrefix() {
        return prefix;
    }
}
