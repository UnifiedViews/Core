package cz.cuni.mff.xrg.odcs.commons.app.execution.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbQuery;

/**
 * Special query for logs.
 * 
 * @author Škoda Petr <skodapetr@gmail.com>
 */
class DbLogQuery extends DbQuery {

    final List<Object> filters;

    final List<String> fetchList;

    final String sortProperty;

    final Boolean sortAsc;

    Integer first;

    Integer count;

    DbLogQuery(List<Object> filters, Set<String> fetchList,
            String sortProperty, Boolean sortAsc) {
        super(null);
        this.filters = new ArrayList<>(filters);
        this.fetchList = new ArrayList<>(fetchList);
        this.sortProperty = sortProperty;
        this.sortAsc = sortAsc;
        this.first = null;
        this.count = null;
    }

    @Override
    public DbQuery limit(int first, int count) {
        this.first = first;
        this.count = count;
        return this;
    }

}
