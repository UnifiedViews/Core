package cz.cuni.xrg.intlib.frontend.gui.components;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.vaadin.annotations.AutoGenerated;
import com.vaadin.data.Container;
import com.vaadin.data.Validator;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.InlineDateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.Window;
import cz.cuni.xrg.intlib.commons.app.pipeline.Pipeline;
import cz.cuni.xrg.intlib.commons.app.scheduling.PeriodUnit;
import cz.cuni.xrg.intlib.commons.app.scheduling.Schedule;
import cz.cuni.xrg.intlib.commons.app.scheduling.ScheduleNotificationRecord;
import cz.cuni.xrg.intlib.commons.app.scheduling.ScheduleType;
import cz.cuni.xrg.intlib.frontend.auxiliaries.App;
import cz.cuni.xrg.intlib.frontend.auxiliaries.ContainerFactory;
import cz.cuni.xrg.intlib.frontend.gui.views.SimpleTreeFilter;

/**
 *  Dialog for the scheduling rule creation. Called from the {@link #Scheduler} 
 *  and {@link #PipelineEdit}. Designed for setting the description of when the pipeline
 *  should be executed.
 * 
 * @author Maria Kukhar
 *
 */
public class SchedulePipeline extends Window {

	private static final long serialVersionUID = 1L;
	@AutoGenerated
	private GridLayout coreLayout;
	@AutoGenerated
	private Label label;
	private GridLayout autoLayout;
	private GridLayout afterLayout;
	private Container container;
	private HorizontalLayout inervalEveryLayout;
	private VerticalLayout inervalLayout;
	private TextField pipeFilter;
	private TwinColSelect selectPipe;
	private TextField tfEvery;
	private ComboBox comboEvery;
	private HorizontalLayout  toleranceLayout;
	private TextField tfTolerance;
	private VerticalLayout strictlyTimedLayout;
	
	/**
	 * OptionGroup to set type of pipeline scheduling
	 */
	private OptionGroup scheduleType;
	private CheckBox justOnce;
	private CheckBox strictlyTimed;
	private OptionGroup intervalOption;

	private InlineDateField date;
	private Schedule schedule = null;
	private ObjectProperty<Integer> valueInt;
	private ObjectProperty<Integer> valueTol;

	private List<Pipeline> pipelines;
	private Set<Pipeline> afterPipelines = null;
	private Schedule selectSch = null;
	
	private VerticalLayout mainLayout;
	private TabSheet tabSheet;
	private EmailNotifications emailNotifications;
	private CheckBox notifyThis;
	private EmailComponent email;
	public GridLayout emailLayout;

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the visual
	 * editor.
	 */
	public SchedulePipeline() {
		this.setResizable(false);
		this.setModal(true);
		this.setCaption("Schedule a pipeline");

		buildMainLayout();
		this.setContent(mainLayout);
		setSizeUndefined();

	}

	/**
	 * The method calls from {@link #PipelineEdit} and sets the corresponding 
	 * value of Pipeline component to the dialog.
	 * 
	 * @param selectedPipeline. Pipeline that locate in the row of Pipeline List table 
	 * in which has been pressed the button Scheduler.
	 */
	public void setSelectePipeline(Pipeline selectedPipeline) {
		comboPipeline.setValue(selectedPipeline.getId());
	}

	private ComboBox comboPipeline = null;

	/**
	 * The method calls from {@link #Scheduler} and sets the corresponding 
	 * values of specific scheduling rule parameters to the dialog.
	 * 
	 * @param selectedSchedule. Schedule that locate in the row of Schedule table 
	 * in which has been pressed the button Scheduler.
	 */
	public void setSelectedSchedule(Schedule selectedSchedule)
	{	
		//setting pipeline
		comboPipeline.setValue(selectedSchedule.getPipeline().getId());
		//setting scheduling rule type
		scheduleType.setValue(selectedSchedule.getType());
		//PERIODICALLY type
		if (selectedSchedule.getType().equals(ScheduleType.PERIODICALLY)) {
			//setting the date
			date.setValue(selectedSchedule.getFirstExecution());
			//setting just ones parameter
			if (selectedSchedule.isJustOnce()){
				justOnce.setValue(true);

			}
			//setting period of repeat
			else{
				if (((selectedSchedule.getPeriodUnit().equals(PeriodUnit.DAY))
						|| (selectedSchedule.getPeriodUnit()
								.equals(PeriodUnit.WEEK)) || (selectedSchedule
							.getPeriodUnit().equals(PeriodUnit.MONTH)))
						&& (selectedSchedule.getPeriod().equals(1))) {
					intervalOption.setValue(selectedSchedule.getPeriodUnit());
				}
				else{
					intervalOption.setValue("every");
					comboEvery.setValue(selectedSchedule.getPeriodUnit());
					valueInt.setValue(selectedSchedule.getPeriod());
				}
				
			}
			
			//TODO Petyr: set Strictly Timed and Tolerance to the dialog
/*			if (selectedSchedule.isStrictlyTimed()){
				strictlyTimed.setValue(true);
				valueTol.setValue(selectedSchedule.getTolerance());
			}
			else{
				strictlyTimed.setValue(false);
				
			}*/
			

		}
		//AFTER_PIPELINE type
		else{
			//setting after_pipeline list 
			Set<Pipeline> after = selectedSchedule.getAfterPipelines();
			List<String> afterNames = new ArrayList<String>();
			  for (Pipeline afteritem : after){
				  afterNames.add(afteritem.getName());
			  }
			
			
			selectPipe.setValue(afterNames);
			
		}
		
		if(selectedSchedule.getNotification()!=null)	
			notifyThis.setValue(true);

		emailNotifications.getScheduleNotificationRecord(selectedSchedule);
		email.getScheduleEmailNotification(selectedSchedule);
		
		selectSch = selectedSchedule;

	}

	/**
	 * Builds main layout contains pipeline, the type of pipeline scheduling, 
	 * layouts with components for setting each of the type.
	 * 
	 * @return mainLayout GridLayout with all dialog components
	 */
	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);

		tabSheet = new TabSheet();
		tabSheet.setImmediate(true);
		
		tabSheet.addSelectedTabChangeListener(new SelectedTabChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				
				if(event.getTabSheet().getSelectedTab().equals(coreLayout)){
					
					tabSheet.setWidth(490, Unit.PIXELS);
				}
				else{
					tabSheet.setWidth(440, Unit.PIXELS);
				}
				
			}
		});
		
		coreLayout = new GridLayout(1, 3);
		coreLayout.setImmediate(false);
		coreLayout.setSpacing(true);
		coreLayout.setMargin(true);

		container = App.getApp().getBean(ContainerFactory.class).createPipelines();


		pipelines = App.getApp().getPipelines().getAllPipelines();

		HorizontalLayout layoutPipeline = new HorizontalLayout();
		layoutPipeline.setSpacing(true);
		layoutPipeline.setMargin(false);
		
		//Pipeline component
		comboPipeline = new ComboBox();
		comboPipeline.setImmediate(true);
		comboPipeline.setContainerDataSource(container);
		comboPipeline.setNullSelectionAllowed(false);
		comboPipeline.setItemCaptionPropertyId("name");
		//setting mandatory for the pipeline component
		comboPipeline.addValidator(new Validator() {

			private static final long serialVersionUID = 1L;

			@Override
			public void validate(Object value) throws InvalidValueException {
				if (value != null) {
					return;
				}
				throw new InvalidValueException("Pipeline must be filled!");
			}
		});

		comboPipeline.addValueChangeListener(new ValueChangeListener() {

			/**
			 * If scheduling type is AFTER_PIPELINE, then refreshing list of pipelines
			 * in the after_pipeline component. It should not contain selected for
			 * scheduling pipeline.
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if (scheduleType.getValue().equals(ScheduleType.AFTER_PIPELINE)) {
					coreLayout.removeComponent(0, 2);
					afterLayout = buildAfterLayout();
					coreLayout.addComponent(afterLayout, 0, 2);
				}
			}
		});

		layoutPipeline.addComponent(new Label("Pipeline "));
		layoutPipeline.addComponent(comboPipeline);
		layoutPipeline.addComponent(new Label(" was selected for scheduling."));

		coreLayout.addComponent(layoutPipeline, 0, 0);
		
		//Schedule type component. Two types: PERIODICALLY and AFTER_PIPELINE
		scheduleType = new OptionGroup();
		scheduleType.setImmediate(true);
		scheduleType.addItem(ScheduleType.PERIODICALLY);
		scheduleType.addItem(ScheduleType.AFTER_PIPELINE);
		scheduleType.setValue(ScheduleType.PERIODICALLY);
		scheduleType
				.setItemCaption(ScheduleType.PERIODICALLY,
						"Schedule the pipeline to run automatically in fixed interval.");
		scheduleType.setItemCaption(ScheduleType.AFTER_PIPELINE,
				"Schedule the pipeline to run after selected pipelines finish.");
		scheduleType.addValueChangeListener(new ValueChangeListener() {

			/**
			 * For each type will be shown corresponding layout with components 
			 * for scheduling rule settings.
			 */
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				
				if (event.getProperty().getValue() == ScheduleType.AFTER_PIPELINE) {

					coreLayout.removeComponent(0, 2);
					afterLayout = buildAfterLayout();
					coreLayout.addComponent(afterLayout, 0, 2);

				} else {
					coreLayout.removeComponent(0, 2);
					coreLayout.addComponent(autoLayout, 0, 2);
				}

			}
		});

		coreLayout.addComponent(scheduleType, 0, 1);
		autoLayout = buildAutoLayout();
		coreLayout.addComponent(autoLayout, 0, 2);
		
		//Layout with buttons Save and Cancel
		HorizontalLayout buttonBar = new HorizontalLayout();
		buttonBar.setMargin(true);
		
		//Save button
		Button createRule = new Button();
		createRule.setCaption("Save");
		createRule.setWidth("90px");
		createRule.setImmediate(true);
		createRule.addClickListener(new ClickListener() {

			/**
			 * Checks validation of mandatory fields. Gets settings of scheduling rule 
			 * and store record to the Database.
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				
				email.saveEditedTexts();   
				
				//validation
				//pipeline should be filled
				if (!comboPipeline.isValid()) {
					Notification.show("Failed to create scheduler rule.",
							"Mandatory fields should be filled",
							Notification.Type.ERROR_MESSAGE);
					return;
				}
				
				
				//Interval of PERIODICALLY type should be positive number
				if ((scheduleType.getValue().equals(ScheduleType.PERIODICALLY))
						&& (!tfEvery.isValid())) {
					Notification.show("Failed to create scheduler rule.",
							"Interval value error",
							Notification.Type.ERROR_MESSAGE);
					return;
				}
				
				//Interval of PERIODICALLY type should be positive number
				if ((scheduleType.getValue().equals(ScheduleType.PERIODICALLY))
						&& (!tfTolerance.isValid())) {
					Notification.show("Failed to create scheduler rule.",
							"Tolerance value error",
							Notification.Type.ERROR_MESSAGE);
					return;
				}
				//selected pipeline in the AFTER_PIPELINE case should be filled.
				if ((scheduleType.getValue()
						.equals(ScheduleType.AFTER_PIPELINE))
						&& (!selectPipe.isValid())) {
					Notification.show("Failed to create scheduler rule.",
							"Mandatory fields should be filled",
							Notification.Type.ERROR_MESSAGE);
					return;
				}

				
				//checking if the dialog was open from the Scheduler table
				//if no, create new scheduling rule
				if (selectSch== null){
					schedule = new Schedule();
				}
				else{
					schedule = selectSch;
					selectSch = null;
				}
				
				Object pipeID = comboPipeline.getValue();
				
				//setting scheduler parameters
				//setting pipeline
				for (Pipeline item : pipelines) {
					if (pipeID.equals(item.getId())) {
						schedule.setPipeline(item);
						break;
					}

				}
				//setting type
				schedule.setType((ScheduleType) scheduleType.getValue());

				// Periodically Schedule type selected. Setting parameters.
				if (scheduleType.getValue().equals(ScheduleType.PERIODICALLY)) {
					schedule.setFirstExecution(date.getValue());
					schedule.setJustOnce(justOnce.getValue());
					if (justOnce.getValue().equals(false)) {

						if ((intervalOption.getValue().equals(PeriodUnit.DAY))
								|| (intervalOption.getValue()
										.equals(PeriodUnit.WEEK))
								|| (intervalOption.getValue()
										.equals(PeriodUnit.MONTH))) {

							schedule.setPeriodUnit((PeriodUnit) intervalOption
									.getValue());
							schedule.setPeriod(1);

						} else {
							schedule.setPeriodUnit((PeriodUnit) comboEvery
									.getValue());
							schedule.setPeriod(valueInt.getValue());
						}
					}
					//TODO Petyr: get Strictly Timed and Tolerance from dialog
/*					schedule.setStrictlyTimed(strictlyTimed.getValue());
					if (strictlyTimed.getValue().equals(true)) 
						schedule.setTolerance(valueTol.getValue());*/
					
				
										
				}
				// After pipeline Schedule type selected Setting parameters.
				else {
					Set<Object> selectedPipelines = (Set) selectPipe.getValue();
					Iterator<Object> it = selectedPipelines.iterator();
					afterPipelines = new HashSet<Pipeline>();
					while (it.hasNext()) {
						Object selectPipe = it.next();
						for (Pipeline item : pipelines) {
							if (item.getName().equals(selectPipe)) {
								afterPipelines.add(item);
							}
						}

					}

					schedule.setAfterPipelines(afterPipelines);
					schedule.setJustOnce(true);
					schedule.setFirstExecution(null);
					schedule.setPeriodUnit(null);
					schedule.setPeriod(null);
				}
				schedule.setEnabled(true);
				
				if(notifyThis.getValue().equals(true)){
					
					if(emailLayout.isEnabled()){
					String errorText="";
					String wrongFormat="";
					boolean notEmpty=false;
					int errorNumber=0;
					int fieldNumber =0;
					for (TextField emailField : email.listedEditText){
						if(!emailField.getValue().trim().isEmpty()){
							notEmpty=true;
							break;
							}
						}
						
						if(notEmpty){
							for (TextField emailField : email.listedEditText){
								fieldNumber++;
								try {
									emailField.validate();
				
								} catch (Validator.InvalidValueException e) {
										
									if (e.getMessage().equals("wrong е-mail format")){
										if(fieldNumber==1)
											wrongFormat="\""+emailField.getValue()+ "\"";
										else
											wrongFormat=wrongFormat+ ", " + "\"" + emailField.getValue() + "\"";
										errorNumber++;
									}
								}
							}
							if(errorNumber==1)
								errorText ="Email "+ wrongFormat + " has wrong format.";
							if(errorNumber>1)
								errorText ="Emails "+  wrongFormat + ", have wrong format.";
						}
						else
							errorText ="At least one mail has to be filled, so that the notification can be send.";
						
						if(!errorText.equals("")){
							Notification.show("Failed to save settings, reason:",errorText, Notification.Type.ERROR_MESSAGE);
							return;
						}
					}
					ScheduleNotificationRecord notification = schedule.getNotification();
					if(notification!=null){
						
						emailNotifications.setScheduleNotificationRecord(notification,schedule);
						email.setScheduleEmailNotification(notification,schedule);
						schedule.setNotification(notification);
					}
					else{
						
						ScheduleNotificationRecord scheduleNotificationRecord = new ScheduleNotificationRecord();
						emailNotifications.setScheduleNotificationRecord(scheduleNotificationRecord,schedule);
						email.setScheduleEmailNotification(scheduleNotificationRecord,schedule);
						schedule.setNotification(scheduleNotificationRecord);

					}
				}
				else{
					if(schedule.getNotification()!=null){

						App.getApp().getSchedules().deleteNotification(schedule.getNotification());
						
					}
				}
				// store scheduling rule record to DB
				App.getApp().getSchedules().save(schedule);
				
				Notification.show(String.format("Pipeline %s scheduled successfuly!", schedule.getPipeline().getName()), Notification.Type.HUMANIZED_MESSAGE);
			
				close();

			}
		});
		
		buttonBar.addComponent(createRule);
		
		Button cancelButton = new Button("Cancel", new Button.ClickListener() {

			/**
			 * Closes Scheduling pipeline window
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(Button.ClickEvent event) {
			close();

			}
		});
		cancelButton.setWidth("90px");
		buttonBar.addComponent(cancelButton);
		
		VerticalLayout notificationsLayout = buildnotificationsLayout();
 
		tabSheet.addTab(coreLayout, "Core", null);
		tabSheet.addTab(notificationsLayout, "Notifications", null);
			
		mainLayout.addComponent(tabSheet);
		mainLayout.addComponent(buttonBar);
//		mainLayout.setComponentAlignment(buttonBar, Alignment.MIDDLE_RIGHT);

		return mainLayout;
	}
	
	private VerticalLayout buildnotificationsLayout() {
		
		
		VerticalLayout notificationsLayout = new VerticalLayout();
        notificationsLayout.setMargin(true);
        notificationsLayout.setSpacing(true);
        notificationsLayout.setImmediate(true);
        
        emailNotifications = new EmailNotifications();
        emailNotifications.parentComponentSh=this; 
		notificationsLayout = emailNotifications.buildEmailNotificationsLayout();
		
        email = new EmailComponent();
        emailLayout = new GridLayout();
        emailLayout = email.initializeEmailList();
        notificationsLayout.addComponent(emailLayout);
        email.getUserEmailNotification(App.getApp().getAuthCtx().getUser());

        notifyThis = new CheckBox();
        notifyThis.setImmediate(true);
        notifyThis.setCaption("Overwrite default settings for all scheduled events with the following:");
        notificationsLayout.addComponent(notifyThis,0);
        emailNotifications.setDisableComponents();
        emailLayout.setEnabled(false);
        notifyThis.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				
		        if(event.getProperty().getValue().equals(false)){
		        	emailNotifications.setDisableComponents();

		        }
		        else
		        	emailNotifications.setEnableComponents();

			}
		});
        

        


    
        return notificationsLayout;

	}

	/**
	 * Building layout for the AFTER_PIPELINE scheduling type. Calls from 
	 * {@link #buildMainLayout} in case of {@link #scheduleType} is setting as
	 * {@link ScheduleType#AFTER_PIPELINE}
	 * 
	 * @return afterLayout GridLayout with components that designed 
	 * for setting schedule the pipeline to run after selected pipelines finish.
	 */
	private GridLayout buildAfterLayout() {

		afterLayout = new GridLayout(2, 1);
		afterLayout.setImmediate(false);
		afterLayout.setHeight("400px");
		afterLayout.setSpacing(true);
		afterLayout.setColumnExpandRatio(0, 0.2f);
		afterLayout.setColumnExpandRatio(1, 0.8f);
		afterLayout.setStyleName("scheduling");

		afterLayout.addComponent(new Label("Select pipeline:"), 0, 0);

		VerticalLayout selectPipelineLayout = new VerticalLayout();
		selectPipelineLayout.setSpacing(true);

		pipeFilter = new TextField();
		pipeFilter.setImmediate(false);
		pipeFilter.setInputPrompt("type to filter pipelines");
		pipeFilter.setWidth("140px");
		pipeFilter.setTextChangeEventMode(TextChangeEventMode.LAZY);
		pipeFilter.addTextChangeListener(new FieldEvents.TextChangeListener() {
			/**
			 * Filtering by pipeline name in the selectPipe component.
			 */
			private static final long serialVersionUID = 1L;
			SimpleTreeFilter filter = null;

			@Override
			public void textChange(FieldEvents.TextChangeEvent event) {
				selectPipe.getItemCaptionPropertyId();
				Container.Filterable f = (Container.Filterable) selectPipe
						.getContainerDataSource();

				// Remove old filter
				if (filter != null) {
					f.removeContainerFilter(filter);
				}

				// Set new filter
				filter = new SimpleTreeFilter(event.getText(), true, false);
				f.addContainerFilter(filter);

			}
		});

		selectPipelineLayout.addComponent(pipeFilter);
		
		//Component for pipelines select
		selectPipe = new TwinColSelect();
		//getting all pipelines to the left column
		for (Pipeline item : pipelines) {
			if (item.getId() != comboPipeline.getValue()) {
				selectPipe.addItem(item.getName());
			}
		}

		selectPipe.setNullSelectionAllowed(true);
		selectPipe.setMultiSelect(true);
		selectPipe.setImmediate(true);
		selectPipe.setWidth("320px");
		selectPipe.setHeight("300px");
		selectPipe.setLeftColumnCaption("Available pipelines");
		selectPipe.setRightColumnCaption("Selected pipelines");
		//selectPipe is mandatory component 
		selectPipe.addValidator(new Validator() {

			private static final long serialVersionUID = 1L;

			@Override
			public void validate(Object value) throws InvalidValueException {

				if (!value.toString().equals("[]")) {
					return;
				}
				throw new InvalidValueException(
						"Selected pipeline must be filled!");

			}
		});

		selectPipelineLayout.addComponent(selectPipe);
		afterLayout.addComponent(selectPipelineLayout, 1, 0);

		return afterLayout;

	}
	
	/**
	 * Building layout for the PERIODICALLY scheduling type. Calls from 
	 * {@link #buildMainLayout} in case of {@link #scheduleType} is setting as
	 * {@link ScheduleType#PERIODICALLY}
	 * 
	 * @return autoLayout GridLayout with components that designed 
	 * for setting schedule the pipeline to run automatically in fixed interval.
	 */
	@SuppressWarnings({ "static-access", "deprecation" })
	private GridLayout buildAutoLayout() {

		autoLayout = new GridLayout(2, 4);
		autoLayout.setImmediate(false);
		autoLayout.setSpacing(true);
		autoLayout.setHeight("450px");
		autoLayout.setColumnExpandRatio(0, 0.6f);
		autoLayout.setColumnExpandRatio(1, 0.4f);
		autoLayout.setStyleName("scheduling");
		
		//Date component
		autoLayout.addComponent(new Label("Date and time of first execution:"),
				0, 0);

		date = new InlineDateField();
		date.setValue(new java.util.Date());
		date.setResolution(date.RESOLUTION_SEC);
		autoLayout.addComponent(date, 1, 0);
		
		//Just ones component. Used if the pipeline will be run only ones
		justOnce = new CheckBox();
		justOnce.setCaption("Just once");
		justOnce.setValue(false);
		justOnce.setImmediate(true);
		justOnce.addValueChangeListener(new ValueChangeListener() {

			/**
			 * If justOnce is selected then the OptionGroup inervalLayout is disabled.
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if (event.getProperty().getValue().equals(true)) {
					inervalLayout.setEnabled(false);
					if (!tfEvery.isValid()) {
						tfEvery.setValue("1");
					}
				} else {
					inervalLayout.setEnabled(true);
				}

			}
		});

		autoLayout.addComponent(justOnce, 1, 1);
		autoLayout.addComponent(new Label("Interval:"), 0, 2);
		
		//OptionGroup with an interval 
		inervalLayout = new VerticalLayout();
		intervalOption = new OptionGroup();
		intervalOption.setImmediate(true);
		intervalOption.addItem(PeriodUnit.DAY);
		intervalOption.setItemCaption(PeriodUnit.DAY, "every day");
		intervalOption.addItem(PeriodUnit.WEEK);
		intervalOption.setItemCaption(PeriodUnit.WEEK, "every week");
		intervalOption.addItem(PeriodUnit.MONTH);
		intervalOption.setItemCaption(PeriodUnit.MONTH, "every month");
		intervalOption.addItem("every");
		intervalOption.setValue(PeriodUnit.DAY);
		intervalOption.addValueChangeListener(new ValueChangeListener() {

			/**
			 * If selected "every" then will be enable component for setting nonstandard
			 * interval.
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {

				if (event.getProperty().getValue().toString().equals("every")) {

					comboEvery.setEnabled(true);
					tfEvery.setEnabled(true);

				} else {

					comboEvery.setEnabled(false);
					if (!tfEvery.isValid()) {
						tfEvery.setValue("1");
					}
					tfEvery.setEnabled(false);
				}

			}
		});
		inervalLayout.addComponent(intervalOption);
		
		//layout for the component for setting nonstandard interval.
		//contains text field for setting numbers and combobox with period values.
		inervalEveryLayout = new HorizontalLayout();
		inervalEveryLayout.setSpacing(true);
		inervalEveryLayout.setMargin(true);

		valueInt = new ObjectProperty<Integer>(1);
		tfEvery = new TextField(valueInt);
		tfEvery.setConverter(Integer.class);
		tfEvery.setWidth("50px");
		tfEvery.setImmediate(true);
		tfEvery.setEnabled(false);
		tfEvery.addValidator(new Validator() {

			private static final long serialVersionUID = 1L;

			@Override
			public void validate(Object val) throws InvalidValueException {
				if (((Integer) val != null) && ((Integer) val > 0)) {
					return;
				}
				throw new InvalidValueException("Value must be positive");

			}
		});

		inervalEveryLayout.addComponent(tfEvery);

		comboEvery = new ComboBox();
		comboEvery.setNullSelectionAllowed(false);
		comboEvery.setImmediate(true);
		comboEvery.addItem(PeriodUnit.MINUTE);
		comboEvery.setItemCaption(PeriodUnit.MINUTE, "Minutes");
		comboEvery.addItem(PeriodUnit.HOUR);
		comboEvery.setItemCaption(PeriodUnit.HOUR, "Hours");
		comboEvery.addItem(PeriodUnit.DAY);
		comboEvery.setItemCaption(PeriodUnit.DAY, "Days");
		comboEvery.addItem(PeriodUnit.MONTH);
		comboEvery.setItemCaption(PeriodUnit.MONTH, "Months");
		comboEvery.setValue(PeriodUnit.DAY);
		comboEvery.setEnabled(false);
		comboEvery.setTextInputAllowed(false);

		inervalEveryLayout.addComponent(comboEvery);
		inervalLayout.addComponent(inervalEveryLayout);
		autoLayout.addComponent(inervalLayout, 1, 2);
		
		//strictly timed component
		
		strictlyTimedLayout = new VerticalLayout();
		strictlyTimedLayout.setSpacing(true);
		
		strictlyTimed = new CheckBox();
		strictlyTimed.setCaption("Strictly Timed");
		strictlyTimed.setValue(false);
		strictlyTimed.setImmediate(true);
		strictlyTimed.addValueChangeListener(new ValueChangeListener() {

			/**
			 * If strictlytimed isn't selected then the tolerance cpmponent is disabled.
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if (event.getProperty().getValue().equals(false)) {
					toleranceLayout.setEnabled(false);
					
				} else {
					
					toleranceLayout.setEnabled(true);

				}

			}
		});

		strictlyTimedLayout.addComponent(strictlyTimed);

		toleranceLayout = new HorizontalLayout();
		toleranceLayout.setSpacing(true);
		toleranceLayout.setEnabled(false);
		toleranceLayout.addComponent(new Label("Tolerance: "));
		
		
		valueTol = new ObjectProperty<Integer>(1);
		tfTolerance = new TextField(valueTol);
		tfTolerance.setConverter(Integer.class);
		tfTolerance.setWidth("50px");
		tfTolerance.setImmediate(true);
		tfTolerance.addValidator(new Validator() {

			private static final long serialVersionUID = 1L;

			@Override
			public void validate(Object val) throws InvalidValueException {
				if (((Integer) val != null) && ((Integer) val > 0)) {
					return;
				}
				throw new InvalidValueException("Value must be positive");

			}
		});

		toleranceLayout.addComponent(tfTolerance);
		
		toleranceLayout.addComponent(new Label("minutes"));

		strictlyTimedLayout.addComponent(toleranceLayout);
		autoLayout.addComponent(strictlyTimedLayout, 1, 3);
	

		return autoLayout;

	}

}
