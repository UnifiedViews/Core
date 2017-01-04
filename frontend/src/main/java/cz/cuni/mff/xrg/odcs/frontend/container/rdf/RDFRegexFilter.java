/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.mff.xrg.odcs.frontend.container.rdf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;

/**
 * Filter for regex matching of RDF data.
 * 
 * @author Bogo
 */
public class RDFRegexFilter implements Filter {

    private String columnName;

    private String regex;

    /**
     * Constructor.
     * 
     * @param columnName
     *            Column to filter.
     * @param regex
     *            Regex to use.
     */
    public RDFRegexFilter(String columnName, String regex) {
        this.columnName = columnName;
        this.regex = regex;
    }

    @Override
    public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {
        String value = item.getItemProperty(columnName).getValue().toString();
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(value);
        return matcher.find();
    }

    @Override
    public boolean appliesToProperty(Object propertyId) {
        return true;
    }

    /**
     * Get filtered column.
     * 
     * @return filtered column name
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * Get regex.
     * 
     * @return regex
     */
    public String getRegex() {
        return regex;
    }

}
