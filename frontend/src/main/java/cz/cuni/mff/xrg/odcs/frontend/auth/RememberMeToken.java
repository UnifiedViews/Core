package cz.cuni.mff.xrg.odcs.frontend.auth;

import java.io.Serializable;
import java.util.Date;

/**
 * Token with a signed information about the user who authenticates with this
 * token.
 * TODO override writeObject and readObject to serialize effectively
 * 
 * @author Jan Vojt
 */
public class RememberMeToken implements Serializable {

    /**
     * Remember-me cookie separator.
     */
    public static final Character SEP = '#';

    private final String username;

    private final Date created;

    private final String hash;

    /**
     * Constructor.
     * 
     * @param username
     *            Username.
     * @param created
     *            Date of creation.
     * @param hash
     *            Password hash.
     */
    public RememberMeToken(String username, Date created, String hash) {
        this.username = username;
        this.created = created;
        this.hash = hash;
    }

    /**
     * Get username.
     * 
     * @return Username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get date created.
     * 
     * @return Date created.
     */
    public Date getCreated() {
        return created;
    }

    /**
     * Get password hash.
     * 
     * @return Password hash.
     */
    public String getHash() {
        return hash;
    }

}
