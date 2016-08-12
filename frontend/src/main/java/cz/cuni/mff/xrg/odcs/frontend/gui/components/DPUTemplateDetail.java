package cz.cuni.mff.xrg.odcs.frontend.gui.components;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;

import cz.cuni.mff.xrg.odcs.commons.app.auth.ShareType;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.frontend.gui.AuthAwareUploadSucceededWrapper;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Component with details of {@link DPUTemplateRecord}. Provide functionality
 * for reloading of existing files.
 * TODO: Should be used in DPU view
 * 
 * @author Škoda Petr
 */
public class DPUTemplateDetail extends CustomComponent {

    private final OptionGroup groupVisibility;

    private final Label jarPath;

    private final RichTextArea jarDescription;

    private final FileUploadReceiver fileUploadReceiver;

    private final Upload reloadFile;

    private final UploadInfoWindow uploadInfoWindow;

    /**
     * True if the loaded file has wrong extension, ie. extension is not equal
     * to ".jar";
     */
    private boolean errorExtension = false;

    /**
     * True if the data are currently set. If true the {@link #valueChangeListener} should not fire.
     */
    private boolean contentSetPhase = false;

    /**
     * True if the dialog content is read only.
     */
    private boolean isReadOnly = false;

    /**
     * Used to report successful file upload.
     */
    private Upload.SucceededListener uploadSucceededListener = null;

    /**
     * Used to report change of insight property.
     */
    private ValueChangeListener valueChangeListener = null;

    public DPUTemplateDetail() {
        setWidth("100%");
        setHeight("100%");
        // create subcomponents
        GridLayout mainLayout = new GridLayout(2, 5);
        mainLayout.setMargin(true);

        // first label with fixed width
        {
            Label lbl = new Label(Messages.getString("DPUTemplateDetail.visibility"));
            lbl.setWidth("80px");
            mainLayout.addComponent(lbl, 0, 0);
        }

        groupVisibility = new OptionGroup();
        groupVisibility.addStyleName("horizontalgroup");
        groupVisibility.addItem(ShareType.PRIVATE);
        groupVisibility.setItemCaption(ShareType.PRIVATE, Messages.getString(ShareType.PRIVATE
                .name()));
        groupVisibility.addItem(ShareType.PUBLIC_RO);
        groupVisibility.setItemCaption(ShareType.PUBLIC_RO, Messages.getString(ShareType.PUBLIC_RO
                .name()));
        groupVisibility.addValueChangeListener(
                new ValueChangeListener() {

                    @Override
                    public void valueChange(ValueChangeEvent event) {
                        if (!contentSetPhase && !isReadOnly && valueChangeListener != null) {
                            valueChangeListener.valueChange(event);
                        }
                    }
                });

        mainLayout.addComponent(groupVisibility, 1, 0);

        mainLayout.addComponent(new Label(Messages.getString("DPUTemplateDetail.path.jar")), 0, 1);
        jarPath = new Label();
        mainLayout.addComponent(jarPath, 1, 1);

        fileUploadReceiver = new FileUploadReceiver();
        reloadFile = new Upload(null, fileUploadReceiver);
        uploadInfoWindow = new UploadInfoWindow(reloadFile);
        // init initRealoadFile
        initRealoadFile();
        mainLayout.addComponent(reloadFile, 1, 2);

        mainLayout.addComponent(new Label(Messages.getString("DPUTemplateDetail.description.jar")), 0, 3);
        jarDescription = new RichTextArea();
        jarDescription.setReadOnly(true);
        jarDescription.setWidth("100%");
        jarDescription.setHeight("100%");
        mainLayout.addComponent(reloadFile, 1, 3);

        // set expansions
        mainLayout.setColumnExpandRatio(0, 0.10f);
        mainLayout.setColumnExpandRatio(1, 0.90f);
        // expand only tha jar-description
        mainLayout.setRowExpandRatio(0, 0.01f);
        mainLayout.setRowExpandRatio(1, 0.01f);
        mainLayout.setRowExpandRatio(2, 0.01f);
        mainLayout.setRowExpandRatio(3, 1.00f);

        setCompositionRoot(mainLayout);
    }

    /**
     * Set properties for {@link #reloadFile}. Work with existing instance.
     */
    private void initRealoadFile() {
        reloadFile.setImmediate(true);
        reloadFile.setButtonCaption(Messages.getString("DPUTemplateDetail.replace"));
        reloadFile.addStyleName("horizontalgroup");
        reloadFile.setHeight("40px");
        reloadFile.addStartedListener(new Upload.StartedListener() {
            /**
             * Upload start presenter. If selected file has JAR extension then
             * an upload status window with upload progress bar will be shown.
             * If selected file has other extension, then upload will be
             * interrupted and error notification will be shown.
             */

            @Override
            public void uploadStarted(final Upload.StartedEvent event) {
                String filename = event.getFilename();
                String extension = filename.substring(
                        filename.lastIndexOf(".") + 1, filename.length());

                final String jar = "jar";
                // check for extension
                if (!jar.equals(extension)) {
                    reloadFile.interruptUpload();
                    errorExtension = true;
                    Notification.show(Messages.getString("DPUTemplateDetail.jar.error"),
                            Notification.Type.ERROR_MESSAGE);
                    return;
                }

                if (uploadInfoWindow.getParent() == null) {
                    UI.getCurrent().addWindow(uploadInfoWindow);
                }
                uploadInfoWindow.setClosable(false);
            }
        });

        reloadFile.addSucceededListener(new AuthAwareUploadSucceededWrapper(
                new Upload.SucceededListener() {

                    @Override
                    public void uploadSucceeded(Upload.SucceededEvent event) {
                        uploadInfoWindow.close();
                        if (!errorExtension && uploadSucceededListener != null) {
                            // call registered listener
                            uploadSucceededListener.uploadSucceeded(event);
                        }
                    }
                }));

        reloadFile.addFailedListener(new Upload.FailedListener() {

            @Override
            public void uploadFailed(Upload.FailedEvent event) {
                uploadInfoWindow.close();
                errorExtension = false;
                // show notification
                Notification.show(
                        Messages.getString("DPUTemplateDetail.uploading.failed", event.getFilename()),
                        Notification.Type.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Set listener that is called if the DPU file has been successfully
     * uploaded.
     * 
     * @param listener
     */
    public void setUploadSucceededListener(Upload.SucceededListener listener) {
        this.uploadSucceededListener = listener;
    }

    /**
     * Set listener that is called in case of change of any property.
     * 
     * @param valueChangeListener
     */
    public void setValueChangeListener(ValueChangeListener valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
    }

    /**
     * Set values in component from the given {@link DPUTemplateRecord}.
     * 
     * @param dpu
     * @param readOnly
     *            True if component should be read only.
     */
    public void loadFromTemplate(DPUTemplateRecord dpu, boolean readOnly) {
        this.isReadOnly = readOnly;
        contentSetPhase = true;

        reloadFile.setEnabled(!readOnly);
        // load data
        final String jarPathText = dpu.getJarPath();
        if (jarPathText.length() > 64) {
            jarPath.setValue("..." + jarPathText.substring(
                    jarPathText.length() - 61));
        } else {
            jarPath.setValue(jarPathText);
        }
        jarDescription.setValue(dpu.getJarDescription());

        final ShareType selecteDpuVisibility = dpu.getShareType();
        groupVisibility.setValue(selecteDpuVisibility);

        if (selecteDpuVisibility == ShareType.PUBLIC_RO) {
            groupVisibility.setValue(selecteDpuVisibility);
            groupVisibility.setEnabled(false);
        } else {
            groupVisibility.setValue(selecteDpuVisibility);
            // if we are not read only then enable
            groupVisibility.setEnabled(!readOnly);
        }

        contentSetPhase = false;
    }

    /**
     * Save the values from component into the given {@link DPUTemplateRecord}.
     * 
     * @param dpu
     */
    public void saveToTemplate(DPUTemplateRecord dpu) {
        if (isReadOnly) {
            // we are read only no changes to save
            return;
        }
        dpu.setShareType((ShareType) groupVisibility.getValue());
    }

}
