package cz.cuni.mff.xrg.odcs.frontend.gui.dialogs.details;

import java.text.DateFormat;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.Window;

import cz.cuni.mff.xrg.odcs.commons.app.execution.message.MessageRecord;
import cz.cuni.mff.xrg.odcs.commons.app.i18n.LocaleHolder;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Shows detail of selected event record.
 * 
 * @author Bogo
 */
public class RecordDetail extends Window {

    RichTextArea fullMessageContent;

    private Label pipelineExecutionContent;

    private Label timeContent;

    private Label instanceContent;

    private Label shortMessageContent;

    private String formattedDate;

    /**
     * Constructor with Record to show.
     * 
     * @param record
     *            Record which detail should be showed.
     */
    public RecordDetail(MessageRecord record) {
        this.setCaption(Messages.getString("RecordDetail.record.detail"));
        GridLayout mainLayout = new GridLayout(2, 7);
        mainLayout.setImmediate(true);
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.setSizeFull();

        Label pipelineExecutionLabel = new Label(Messages.getString("RecordDetail.excecution"));
        pipelineExecutionLabel.setWidth(120, Unit.PIXELS);
        mainLayout.addComponent(pipelineExecutionLabel, 0, 0);
        pipelineExecutionContent = new Label(String.format("%d", record.getExecution().getId()));
        pipelineExecutionContent.setWidth(100, Unit.PIXELS);
        mainLayout.addComponent(pipelineExecutionContent, 1, 0);

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, LocaleHolder.getLocale());
        formattedDate = df.format(record.getTime());

        Label timeLabel = new Label(Messages.getString("RecordDetail.time"));
        mainLayout.addComponent(timeLabel, 0, 1);
        timeContent = new Label(formattedDate);
        mainLayout.addComponent(timeContent, 1, 1);

        Label instanceLabel = new Label(Messages.getString("RecordDetail.type"));
        mainLayout.addComponent(instanceLabel, 0, 2);
        instanceContent = new Label(record.getType().toString());
        mainLayout.addComponent(instanceContent, 1, 2);

        Label shortMessageLabel = new Label(Messages.getString("RecordDetail.short.message"));
        mainLayout.addComponent(shortMessageLabel, 0, 3);
        shortMessageContent = new Label(record.getShortMessage());
        mainLayout.addComponent(shortMessageContent, 1, 3);

        Label messageLabel = new Label(Messages.getString("RecordDetail.message"));
        mainLayout.addComponent(messageLabel, 0, 4);

        fullMessageContent = new RichTextArea();
        fullMessageContent.setValue(record.getFullMessage().replaceAll("\\n", "</br>"));
        fullMessageContent.setReadOnly(true);
        fullMessageContent.setSizeFull();
        mainLayout.addComponent(fullMessageContent, 0, 5, 1, 5);
        mainLayout.setRowExpandRatio(5, 1.0f);

        mainLayout.setColumnExpandRatio(1, 1.0f);
        mainLayout.setRowExpandRatio(5, 1.0f);

        Button closeButton = new Button(Messages.getString("RecordDetail.close"), new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });
        mainLayout.addComponent(closeButton, 1, 6);
        mainLayout.setComponentAlignment(closeButton, Alignment.MIDDLE_RIGHT);

        this.setContent(mainLayout);

    }

    /**
     * Load new record detail to existing detail window.
     * 
     * @param record
     *            Record to load.
     */
    public void loadMessage(MessageRecord record) {
        pipelineExecutionContent.setValue(String.format("%d", record.getExecution().getId()));
        timeContent.setValue(formattedDate);
        instanceContent.setValue(record.getType().toString());
        shortMessageContent.setValue(record.getShortMessage());
        fullMessageContent.setReadOnly(false);
        fullMessageContent.setValue(record.getFullMessage());
        fullMessageContent.setReadOnly(true);
    }

    /**
     * Resizes content due to resize of whole dialog.
     * 
     * @param height
     *            New height of whole dialog.
     * @param unit
     *            {@link Unit} of height.
     */
    public void setContentHeight(float height, Unit unit) {
        fullMessageContent.setHeight(height - 250, unit);
    }
}
