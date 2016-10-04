package cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.xstream;

/**
 * Filter used in {@link JPAXStream}.
 * 
 * @author Škoda Petr
 */
interface MemberFilter {

    public boolean shouldSerializeMember(Class definedIn, String fieldName);

}
