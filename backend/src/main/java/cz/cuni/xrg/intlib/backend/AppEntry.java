package cz.cuni.xrg.intlib.backend;

import cz.cuni.xrg.intlib.commons.app.conf.AppConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cz.cuni.xrg.intlib.backend.communication.Server;
import cz.cuni.xrg.intlib.backend.execution.EngineEvent;
import cz.cuni.xrg.intlib.backend.execution.EngineEventType;
import cz.cuni.xrg.intlib.commons.app.communication.CommunicationException;
import cz.cuni.xrg.intlib.commons.app.conf.ConfigProperty;
import cz.cuni.xrg.intlib.commons.app.module.ModuleFacade;

/**
 * Backend entry point.
 * 
 * @author Petyr
 *
 */
public class AppEntry {

	/**
	 * Path to the spring configuration file.
	 */
	private final static String SPRING_CONFIG_FILE = "backend-context.xml";
	
	/**
	 * Logger class.
	 */
	private static Logger LOG = LoggerFactory.getLogger(AppEntry.class);

	/**
	 * Path to the configuration file.
	 */
	private String configFileLocation = null;
	
	/**
	 * Spring context.
	 */
	private AbstractApplicationContext context = null;
	
	/**
	 * Application configuration.
	 */
	private AppConfig appConfig = null;
	
	/**
	 * Module facade.
	 */
	private ModuleFacade modeleFacade = null;
			
	/**
	 * Server for network communication.
	 */
	private Server server = null;
	
	/**
	 * Separate thread for network server.
	 */
	private Thread serverThread = null;
	
	/**
	 * Heartbeat class instance.
	 */
	private Heartbeat heartbeat = null;	
	
	/**
	 * Thread for heartbeat.
	 */
	private Thread heartbeatThread = null;
	
	/**
	 * Parse program arguments.
	 * @param args
	 */
	private void parseArgs(String[] args) {
		// define args
		Options options = new Options();
		options.addOption("c", "config", true, "path to the configuration file");
		// parse args
		CommandLineParser parser = new org.apache.commons.cli.BasicParser();
		try {
			CommandLine cmd = parser.parse( options, args);
			// read args ..
			configFileLocation = cmd.getOptionValue("config");
		} catch (ParseException e) {			
			LOG.error("Unexpected exception:" + e.getMessage());
		}
		
		// override default configuration path if it has been provided
		if (configFileLocation != null) {
			AppConfig.confPath = configFileLocation;
		}		
	}
	
	/**
	 * All initialization goes here.
	 */
	private void init() {
		// load spring
		context = new ClassPathXmlApplicationContext(SPRING_CONFIG_FILE);
		context.registerShutdownHook();
		// load configuration
		appConfig = (AppConfig)context.getBean("configuration");
		// engine is setup automatically 
		// set module facade
		LOG.info("Configuring dynamic module worker ...");
		modeleFacade = (ModuleFacade)context.getBean("moduleFacade");
		modeleFacade.start();		
	}
	
	/**
	 * Initialise and start network TCP/IP server.
	 */
	private void initNetworkServer() {
		// set TCP/IP server
		LOG.info("Starting TCP/IP server ...");
		server = (Server)context.getBean("server");
		try {
			server.init();
		} catch (CommunicationException e1) {
			LOG.error("Fatal error: Can't start server");
			context.close();
			return;
		}
		// start server in another thread
		serverThread = new Thread(server);
		serverThread.start();		
	}
	
	/**
	 * Get and start Heartbeat in other thread.
	 */
	private void initHeartbeat() {
		// start heartbeat
		heartbeat = (Heartbeat)context.getBean("heartbeat");
		heartbeatThread = new Thread(heartbeat);
		heartbeatThread.start();
		LOG.info("Heartbeat is running ... ");		
	}
	
	/**
	 * Main execution method.
	 * @param args
	 */
	private void run(String[] args) {
		// parse args
		parseArgs(args);
		// initialise
		init();
		
		// publish event for engine about start of the execution
		context.publishEvent(new EngineEvent(EngineEventType.StartUp, AppEntry.class));
				
		// start server
		initNetworkServer();
		// start heartbeat
		initHeartbeat();
		
		// print some information ..
		LOG.info("DPURecord directory:" + appConfig.getString(ConfigProperty.MODULE_PATH));
		LOG.info("Listening on port:" + appConfig.getInteger(ConfigProperty.BACKEND_PORT));
		LOG.info("Running ...");
		
		// infinite loop
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);		
		while (true) {
			try {
				in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}			
		}		
	}
	
	public static void main(String[] args) {
		AppEntry app = new AppEntry();
		app.run(args);				
	}
	
}