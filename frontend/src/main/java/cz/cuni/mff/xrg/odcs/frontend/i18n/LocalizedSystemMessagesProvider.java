package cz.cuni.mff.xrg.odcs.frontend.i18n;

import com.vaadin.server.CustomizedSystemMessages;
import com.vaadin.server.SystemMessages;
import com.vaadin.server.SystemMessagesInfo;
import com.vaadin.server.SystemMessagesProvider;

public class LocalizedSystemMessagesProvider implements SystemMessagesProvider {

    private static final long serialVersionUID = -4891268300002329674L;

    private CustomizedSystemMessages systemMessages;

    public LocalizedSystemMessagesProvider() {
        this.systemMessages = new CustomizedSystemMessages();
        initLocalizedSystemMessages(this.systemMessages);
    }

    @Override
    public SystemMessages getSystemMessages(SystemMessagesInfo systemMessagesInfo) {
        return this.systemMessages;
    }

    private static void initLocalizedSystemMessages(CustomizedSystemMessages systemMessages) {
        systemMessages.setAuthenticationErrorCaption(Messages.getString("SystemMessages.authenticationErrorCaption"));
        systemMessages.setAuthenticationErrorMessage(Messages.getString("SystemMessages.authenticationErrorMessage"));

        systemMessages.setCommunicationErrorCaption(Messages.getString("SystemMessages.communicationErrorCaption"));
        systemMessages.setCommunicationErrorMessage(Messages.getString("SystemMessages.communicationErrorMessage"));

        systemMessages.setCookiesDisabledCaption(Messages.getString("SystemMessages.cookiesDisabledCaption"));
        systemMessages.setCookiesDisabledMessage(Messages.getString("SystemMessages.cookiesDisabledMessage"));

        systemMessages.setSessionExpiredCaption(Messages.getString("SystemMessages.sessionExpiredCaption"));
        systemMessages.setSessionExpiredMessage(Messages.getString("SystemMessages.sessionExpiredMessage"));

        systemMessages.setInternalErrorCaption(Messages.getString("SystemMessages.internalErrorCaption"));
        systemMessages.setInternalErrorMessage(Messages.getString("SystemMessages.internalErrorMessage"));

        systemMessages.setOutOfSyncCaption(Messages.getString("SystemMessages.outOfSyncCaption"));
        systemMessages.setOutOfSyncMessage(Messages.getString("SystemMessages.outOfSyncMessage"));
    }

}
