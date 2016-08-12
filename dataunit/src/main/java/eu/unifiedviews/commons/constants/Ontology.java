package eu.unifiedviews.commons.constants;

/**
 * Contains definition of used predicates.
 *
 * @author Škoda Petr
 */
public class Ontology {

    private Ontology() {

    }

    public static final String BASE_URI = "http://unifiedviews.eu/";

    public static final String ONTOLOGY = "ontology/";

    public static final String RESOURCE = "resource/";

    public static final String INTERNAL = "internal/";

    /**
     * Ontology prefix for predicates in metadata unit.
     */
    public static final String ONTOLOGY_DATAUNIT = BASE_URI + ONTOLOGY + INTERNAL + "data/";

    public static final String PREDICATE_METADATA_CONTEXT_READ = ONTOLOGY_DATAUNIT + "read";

    public static final String PREDICATE_METADATA_CONTEXT_WRITE = ONTOLOGY_DATAUNIT + "write";

    public static final String PREDICATE_METADATA_ENTRY_COUNTER = ONTOLOGY_DATAUNIT + "counter";

    /**
     * Prefix for metadata graphs.
     */
    public static final String SUBJECT_METADATA_GRAPH = BASE_URI + RESOURCE + INTERNAL + "data/";

    /**
     * A static graph used to store informations about data units.
     */
    public static final String GRAPH_METADATA = BASE_URI + RESOURCE + INTERNAL + "data";

}
