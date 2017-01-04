package cz.cuni.mff.xrg.odcs.commons.app.dpu;

/**
 * Determine type of DPU.
 * 
 * @author Petyr
 */
public enum DPUType {
    /**
     * Represents an extractor.
     */
    EXTRACTOR,
    /**
     * Represents a transformer.
     */
    TRANSFORMER,
    /**
     * Represents a loader.
     */
    LOADER,
    /**
     * Represents a quality verifier.
     */
    QUALITY
}
