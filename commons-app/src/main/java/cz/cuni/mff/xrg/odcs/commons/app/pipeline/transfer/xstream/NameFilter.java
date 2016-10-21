package cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.xstream;

import java.util.LinkedList;

/**
 * Filter members whose name contains given string.
 * 
 * @author Škoda Petr
 */
class NameFilter implements MemberFilter {

    private final LinkedList<String> banList = new LinkedList<>();

    @Override
    public boolean shouldSerializeMember(Class definedIn, String fieldName) {
        for (String str : banList) {
            if (fieldName.contains(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add given value into the name black list.
     * 
     * @param value
     */
    public void add(String value) {
        banList.add(value);
    }

}
