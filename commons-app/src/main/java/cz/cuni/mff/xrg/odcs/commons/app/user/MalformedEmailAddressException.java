package cz.cuni.mff.xrg.odcs.commons.app.user;

import cz.cuni.mff.xrg.odcs.commons.app.i18n.Messages;

/**
 * @author Jan Vojt
 */
public class MalformedEmailAddressException extends RuntimeException {

    /**
     * Creates a new instance of <code>MalformedEmailAddressException</code> without detail message.
     */
    public MalformedEmailAddressException() {
    }

    /**
     * Constructs an instance of <code>MalformedEmailAddressException</code> with the specified detail
     * message.
     * 
     * @param email
     *            the detail message.
     */
    public MalformedEmailAddressException(String email) {
        super(Messages.getString("MalformedEmailAddressException.email.invalid", email));
    }
}
