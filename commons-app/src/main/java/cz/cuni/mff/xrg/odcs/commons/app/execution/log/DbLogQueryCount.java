package cz.cuni.mff.xrg.odcs.commons.app.execution.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbQueryCount;

/**
 * Special query for logs.
 * 
 * @author Škoda Petr <skodapetr@gmail.com>
 */
class DbLogQueryCount extends DbQueryCount {

    final List<Object> filters;

    final List<String> fetchList;

    DbLogQueryCount(List<Object> filters, Set<String> fetchList) {
        super(null);
        this.filters = new ArrayList<>(filters);
        this.fetchList = new ArrayList<>(fetchList);
    }

    DbLogQueryCount(TypedQuery<Number> query) {
        super(query);
        this.filters = null;
        this.fetchList = null;
    }

}
