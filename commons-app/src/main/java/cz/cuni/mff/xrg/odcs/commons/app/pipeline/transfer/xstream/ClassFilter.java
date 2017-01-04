package cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.xstream;

import java.util.LinkedList;

/**
 * Filter members whose class contains given strings.
 * 
 * @author Škoda Petr
 */
public class ClassFilter implements MemberFilter {

    private final LinkedList<String> banList = new LinkedList<>();

    @Override
    public boolean shouldSerializeMember(Class definedIn, String fieldName) {
        final String className = definedIn.getCanonicalName();
        for (String str : banList) {
            if (className.contains(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add given name into class black list.
     * 
     * @param value
     */
    public void add(String value) {
        banList.add(value);
    }

}
