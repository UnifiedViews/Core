package cz.cuni.mff.xrg.odcs.commons.app.auth;

import java.util.EnumSet;

/**
 * Types of Entity shares, which define how is authorization decided
 * for {@link cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject} implementing {@link SharedEntity}.
 * 
 * @see SharedEntity
 * @see AuthAwarePermissionEvaluator
 * @see cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAuthorizator
 * @author Jan Vojt
 */
public enum ShareType {

    /**
     * Entity is private, no other user except admin and owner can see it.
     */
    PRIVATE,

    /**
     * Entity is publicly viewable, but only admin and owner can modify it.
     */
    PUBLIC_RO,

    /**
     * Entity is public, anyone can make changes to it. Only admin and owner
     * can delete it.
     */
    PUBLIC_RW;

    /**
     * Set of states in which entity is publicly viewable.
     */
    public static final EnumSet<ShareType> PUBLIC = EnumSet.of(
            PUBLIC_RO,
            PUBLIC_RW
            );
}
