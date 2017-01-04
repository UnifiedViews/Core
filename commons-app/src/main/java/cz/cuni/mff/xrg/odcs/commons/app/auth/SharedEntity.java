package cz.cuni.mff.xrg.odcs.commons.app.auth;

/**
 * Entities implementing <code>SharedEntity</code> have special permission rules
 * applied to them when deciding authorization.
 * 
 * @see AuthAwarePermissionEvaluator
 * @see cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAuthorizator
 * @author Jan Vojt
 */
public interface SharedEntity {

    /**
     * Getter for shared type configured on entity. Share type specifies
     * authorization rules.
     * 
     * @return share type
     */
    public ShareType getShareType();

}
