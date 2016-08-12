package cz.cuni.mff.xrg.odcs.frontend.gui.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.validator.routines.EmailValidator;

import com.vaadin.data.Validator;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;

import cz.cuni.mff.xrg.odcs.commons.app.scheduling.Schedule;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.ScheduleNotificationRecord;
import cz.cuni.mff.xrg.odcs.commons.app.user.EmailAddress;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;
import cz.cuni.mff.xrg.odcs.commons.app.user.UserNotificationRecord;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Settings;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Builds E-mail notification component which consists of text fields for e-mail
 * and buttons for add and remove this text fields. Used in {@link Settings} and {@link EmailNotifications}
 * 
 * @author Maria Kukhar
 */
public class EmailComponent {

//	public EmailNotifications parentComponent; 
    private Button buttonEmailhRem;

    private Button buttonEmailAdd;

    private GridLayout gridLayoutEmail;

    /**
     * Text field with email.
     */
    public TextField textFieldEmail;

    /**
     * List of edited text fields.
     */
    public List<TextField> listedEditText = null;

    /**
     * Parent component.
     */
    public Settings parentComponentAccount;

    /**
     * List<String> that contains e-mails.
     */
    private List<String> griddata = initializeEmailData();

    /**
     * Initializes E-mail notification component.
     * 
     * @return Initialized E-mail notification component
     */
    public GridLayout initializeEmailList() {

        gridLayoutEmail = new GridLayout();
        gridLayoutEmail.setImmediate(false);
        gridLayoutEmail.setWidth("380px");
        gridLayoutEmail.setHeight("100%");
        gridLayoutEmail.setMargin(false);
        gridLayoutEmail.setColumns(2);
        gridLayoutEmail.setColumnExpandRatio(0, 0.95f);
        gridLayoutEmail.setColumnExpandRatio(1, 0.05f);

        refreshEmailData();
        return gridLayoutEmail;

    }

    /**
     * Save edited texts in the E-mail notification component
     */
    public void saveEditedTexts() {
        griddata = new LinkedList<>();
        for (TextField editText : listedEditText) {
            griddata.add(editText.getValue().trim());
        }

    }

    /**
     * Builds E-mail notification component which consists of textfields for
     * e-mail and buttons for add and remove this textfields. Used in // * {@link #initializeEmailList} and also in adding and removing fields for
     * component refresh
     */
    public void refreshEmailData() {
        gridLayoutEmail.removeAllComponents();
        int row = 0;
        listedEditText = new ArrayList<>();
        if (griddata.size() < 1) {
            griddata.add("");
        }
        gridLayoutEmail.setRows(griddata.size() + 1);
        for (String item : griddata) {
            textFieldEmail = new TextField();
            textFieldEmail.setImmediate(true);
            listedEditText.add(textFieldEmail);

            //text field for the graph
            textFieldEmail.setWidth("100%");
            textFieldEmail.setData(row);
            textFieldEmail.setValue(item.trim());
            textFieldEmail.addTextChangeListener(new TextChangeListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void textChange(TextChangeEvent event) {
                    if (parentComponentAccount != null && parentComponentAccount.buttonMyAccountBar != null)
                        parentComponentAccount.buttonMyAccountBar.setEnabled(true);

                    saveEditedTexts();
                }
            });
            textFieldEmail.setInputPrompt(Messages.getString("EmailComponent.email.prompt"));

            textFieldEmail.addValidator(new Validator() {
                private static final long serialVersionUID = 1L;

                @Override
                public void validate(Object value) throws InvalidValueException {

                    if (value.getClass() == String.class
                            && !((String) value).isEmpty()) {
                        String inputEmail = (String) value;
                        if (!EmailValidator.getInstance().isValid(inputEmail)) {
                            throw new InvalidValueException(Messages.getString("EmailComponent.email.wrong"));
                        }

                        int count = 0;
                        for (TextField emailField : listedEditText) {
                            if (emailField.getValue().equals(inputEmail))
                                count++;
                            if (count > 1) {
                                throw new InvalidValueException(Messages.getString("EmailComponent.email.duplicate"));
                            }

                        }
                    } else {
                        throw new InvalidValueException(Messages.getString("EmailComponent.email.not.filled"));
                    }
                }
            });

            //remove button
            buttonEmailhRem = new Button();
            if (griddata.size() > 1)
                buttonEmailhRem.setEnabled(true);
            else
                buttonEmailhRem.setEnabled(false);
            buttonEmailhRem.setWidth("55px");
            buttonEmailhRem.setCaption("-");
            buttonEmailhRem.setData(row);
            buttonEmailhRem.addClickListener(new Button.ClickListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    if (parentComponentAccount != null && parentComponentAccount.buttonMyAccountBar != null)
                        parentComponentAccount.buttonMyAccountBar.setEnabled(true);
                    saveEditedTexts();
                    Button senderButton = event.getButton();
                    Integer row = (Integer) senderButton.getData();
                    removeDataEmailData(row);
                    refreshEmailData();
                }
            });
            gridLayoutEmail.addComponent(textFieldEmail, 0, row);
            gridLayoutEmail.addComponent(buttonEmailhRem, 1, row);
            gridLayoutEmail.setComponentAlignment(buttonEmailhRem,
                    Alignment.TOP_RIGHT);
            row++;
        }
        //add button
        buttonEmailAdd = new Button();
        buttonEmailAdd.setCaption("+");
        buttonEmailAdd.setImmediate(true);
        buttonEmailAdd.setWidth("55px");
        buttonEmailAdd.setHeight("-1px");
        buttonEmailAdd.addClickListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                saveEditedTexts();
                addDataToEmailData(" ");
                refreshEmailData();
            }
        });
        gridLayoutEmail.addComponent(buttonEmailAdd, 0, row);

    }

    /**
     * Set notification to display.
     * 
     * @param notofication
     */
    public void setUserEmailNotification(UserNotificationRecord notofication) {
        Set<EmailAddress> emails = stringsToEmails(griddata);
        notofication.setEmails(emails);
    }

    /**
     * Set notification for schedules.
     * 
     * @param notofication
     * @param schedule
     */
    public void setScheduleEmailNotification(ScheduleNotificationRecord notofication, Schedule schedule) {
        Set<EmailAddress> emails = stringsToEmails(griddata);
        notofication.setEmails(emails);
    }

    /**
     * Get notifications for user.
     * 
     * @param user
     */
    public void getUserEmailNotification(User user) {

        UserNotificationRecord notification = user.getNotification();

        if (notification != null) {
            griddata = emailsToStrings(notification.getEmails());
            refreshEmailData();
        } else {
            griddata = Arrays.asList(user.getEmail().toString());
            refreshEmailData();
        }
    }

    /**
     * Get notification for schedules.
     * 
     * @param schedule
     */
    public void getScheduleEmailNotification(Schedule schedule) {

        ScheduleNotificationRecord notification = schedule.getNotification();

        if (notification != null) {
            griddata = emailsToStrings(notification.getEmails());
            refreshEmailData();
        } else {
            getUserEmailNotification(schedule.getOwner());
        }

    }

    /**
     * Initializes data of the E-mail notification component
     */
    private static List<String> initializeEmailData() {
        List<String> result = new LinkedList<>();
        result.add("");

        return result;
    }

    /**
     * Add new data to E-mail notification component.
     * 
     * @param newData
     *            . String that will be added
     */
    private void addDataToEmailData(String newData) {
        griddata.add(newData.trim());
    }

    /**
     * Remove data from E-mail notification component. Only if component contain
     * more then 1 row.
     * 
     * @param row
     *            Data that will be removed.
     */
    private void removeDataEmailData(Integer row) {
        int index = row;
        if (griddata.size() > 1) {
            griddata.remove(index);
        }

    }

    /**
     * Converts {@link EmailAddress}es into {@code String}s.
     * 
     * @param emails
     * @return sorted emails as list of strings
     */
    private static List<String> emailsToStrings(Set<EmailAddress> emails) {
        List emailStr = new ArrayList<>(emails.size());
        for (EmailAddress mail : emails) {
            emailStr.add(mail.toString());
        }

        return emailStr;
    }

    /**
     * Converts {@code String}s of emails to {@link EmailAddress}es.
     * 
     * @param emails
     * @return sorted emails as list of strings
     */
    private static Set<EmailAddress> stringsToEmails(List<String> emails) {
        Set<EmailAddress> emailAddrs = new LinkedHashSet<>();
        for (String mail : emails) {
            if (!mail.equals("")) {
                EmailAddress e = new EmailAddress(mail);
                emailAddrs.add(e);
            }
        }
        return emailAddrs;
    }
}
