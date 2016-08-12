package cz.cuni.mff.xrg.odcs.rdf.help;

import java.util.HashMap;
import java.util.Map;

/**
 * Generates unique file name for each pipeline run.
 * 
 * @author Jiri Tomes
 */
public class UniqueNameGenerator {

    private static Map<String, Integer> map = new HashMap<>();

    /**
     * For given string name of file create unique file name depends on last
     * calling of this method.
     * Example of using: name="my.file.rdf"
     * Firt calling returns: "my.file-1.rdf"
     * Second calling returns:""my.file-2.rdf"
     * etc...
     * 
     * @param name
     *            Basic string name of file you should be unique.
     * @return Unique file name depends on last calling this method with same
     *         string basic name.
     */
    public static String getNextName(String name) {
        int value = 1;

        if (!map.containsKey(name)) {
            map.put(name, value);
        } else {
            value = map.remove(name);
            value++;
            map.put(name, value);

        }

        int lastBot = name.lastIndexOf(".");

        if (lastBot == -1) {
            return name + "-" + String.valueOf(value);
        } else {

            String first = name.substring(0, lastBot);
            String second = name.substring(lastBot + 1, name.length());

            return first + "-" + String.valueOf(value) + "." + second;
        }
    }
}
