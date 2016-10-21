package cz.cuni.mff.xrg.odcs.commons.app.dao;

/**
 * Types of filters that can be used in {@link DataQueryBuilder}.
 * 
 * @author Petyr
 * @deprecated unused class, will be removed
 */
@Deprecated
public enum FilterType {
    LIKE("LIKE"),
    EQUAL("="),
    NOT("!="),
    GREATER(">"),
    LESS("<"),
    GREATER_OR_EQUAL(">="),
    LESS_OR_EQUAL("<=");

    private final String sql;

    FilterType(String sql) {
        this.sql = sql;
    }

    @Override
    public String toString() {
        return sql;
    }

}
