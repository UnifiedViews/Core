package cz.cuni.mff.xrg.odcs.commons.app.user;

/**
 * Represents entity owner by {@link User}.
 * 
 * @author Jan Vojt
 */
public interface OwnedEntity {

    /**
     * @return owner
     */
    public User getOwner();

}
