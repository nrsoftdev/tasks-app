package nrsoft.tasks.app;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.springframework.schema.beans.Beans;

import it.nrsoft.nrlib.argparser.ArgParser;
import it.nrsoft.nrlib.argparser.InvalidSwitchException;
import it.nrsoft.nrlib.argparser.Switch;
import it.nrsoft.nrlib.argparser.SwitchDefType;
import it.nrsoft.nrlib.tuples.Pair;
import it.nrsoft.nrlib.util.Util;
import nrsoft.connectors.io.TextConnector;
import nrsoft.connectors.jdbc.JdbcConnector;
import nrsoft.tasks.TaskResult;
import nrsoft.tasks.ValueTypes;
import nrsoft.tasks.app.sample.DemoApp;
import nrsoft.tasks.logger.LoggerProvider;
import nrsoft.tasks.logger.LoggersProvider;
import nrsoft.tasks.metadata.Metadata;
import nrsoft.tasks.metadata.MetadataDefinition;
import nrsoft.tasks.metadata.PropertyDefinition;
import nrsoft.tasks.model.InitialProperty;
import nrsoft.tasks.model.ProcessDefinition;
import nrsoft.tasks.model.ProcessDefinitionVariable;
import nrsoft.tasks.model.TaskDefinition;
import nrsoft.tasks.model.User;
import nrsoft.tasks.persistance.TasksDaoJPA;
import nrsoft.tasks.runtime.Process;
import nrsoft.tasks.runtime.ProcessObserver;
import nrsoft.tasks.runtime.ProcessObserverPersistance;
import nrsoft.tasks.runtime.Processes;
import nrsoft.tasks.spring.BeanCreator;
import nrsoft.tasks.spring.XmlSpringConfigurationBuilder;

public class TaskAppCmd {
	
	
	private static final String CMD_DELETE = "DELETE";
	private static final String CMD_CREATE = "CREATE";
	private static final String CMD_CHANGE = "CHANGE";
	private static final String CMD_IMPORT = "IMPORT";
	private static final String CMD_EXPORT = "EXPORT";
	private static final String CMD_RUN = "RUN";
	private static final String CMD_LIST = "LIST";
	private static final String CMD_GENERATE = "GENERATE";
	
	private static final String OBJ_JDBCCONN = "JDBCCONN";
	private static final String OBJ_TEXTCONN = "TEXTCONN";
	private static final String OBJ_TASK = "TASK";
	private static final String OBJ_PROCESS = "PROCESS";
	private static final String OBJ_DEMO = "DEMO";
	private static int MAX_COL_LEN = 20;
	
	
	private static String[] OBJECTS = new String[] {OBJ_PROCESS, OBJ_TASK, OBJ_TEXTCONN, OBJ_JDBCCONN, OBJ_DEMO};
	private static String[] COMMANDS = new String[] {CMD_LIST, CMD_RUN, CMD_EXPORT, CMD_IMPORT, CMD_CREATE, CMD_CHANGE, CMD_DELETE};
	
	
	// private static String[] COMMANDS_PARAMS = new String[] {"", "id", "id filename", "id filename"};
	private static String userId;
	
	static TasksDaoJPA processDAO = null;

	/*
	 * --user=ADMIN PROCESS IMPORT Proc1 Descr1 step1 C:\TEMP\test1.xml
	 * --user=ADMIN PROCESS IMPORT Proc2 Descr2 step2 C:\TEMP\test2.xml
	 * --user=ADMIN PROCESS EXPORT 4 C:\TEMP\test2out.xml
	 * --user=ADMIN PROCESS RUN 4
	 */
	public static void main(String[] args) {
		
		
		processDAO = new TasksDaoJPA.Builder()
				.setTransactionManual(true)
				.build();
		

		ArgParser argParser = new ArgParser();
		
		
		argParser.addSwitchChar('-');
		argParser.addSwitchChar("--");
		
		argParser.addSwitchDef(new String[] {"u", "user"}, SwitchDefType.stValued ,"User");
		argParser.addSwitchDef(new String[] {"p", "password"} , SwitchDefType.stValued,"Password");
		
		argParser.setMinNumArgs(2);
		
		if(args.length==0)
		{
			printUsage(argParser);
			System.exit(1);
		}
		
		
		
		try {
			argParser.parse(args);
		} catch (InvalidSwitchException | IllegalArgumentException e) {
			
			printUsage(argParser);
			System.exit(1);
		}
		
		Switch switchUserId = argParser.getSwitches().get("u");
		if(switchUserId==null) {
			System.out.println("Missing user id");
			System.exit(1);
		}
		userId = switchUserId.getValues().get(0);
		
		User user = processDAO.getUserById(userId);
		final boolean isAdmin = user.getRoles().stream().anyMatch((role) -> role.isAdmin());
		
		
		String object = argParser.getArguments().get(0);
		
		String command = argParser.getArguments().get(1);
		List<String> params = argParser.getArguments().subList(2, argParser.getArguments().size());
		
		Processes.bootstrap(processDAO);
		
		try {
			execute(object, command, params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				processDAO.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		


	}

	private static void printUsage(ArgParser argParser) {
		System.out.println("TaskAppCmd <switches> OBJECT COMMAND [PARAMS]");
		
		System.out.println("\r\nObjects:");
		for(int i=0;i<OBJECTS.length;i++) {
			System.out.println(OBJECTS[i]);
		}
		
		
		System.out.println("\r\nCommands:");
		for(int i=0;i<COMMANDS.length;i++) {
			System.out.println(COMMANDS[i] + " params");
		}
		System.out.println("Switches:");
		System.out.println(argParser.usage());
	}

	private static void execute(String object, String command, List<String> params) {
		
		switch(object) {
		case OBJ_PROCESS:
			executeCommandOnProcess(command, params);
			break;
		case OBJ_TASK:
			executeCommandOnTask(command, params);
			break;			
		case OBJ_TEXTCONN:
			executeCommandOnTextConn(command, params);
			break;
		case OBJ_JDBCCONN:
			executeCommandOnJdbcConn(command, params);
			break;
		case OBJ_DEMO:
			executeCommandOnDemo(command, params);
			break;
			
			
		}

		
		
	}

	private static void executeCommandOnDemo(String command, List<String> params) {
		switch(command) {
		case CMD_CREATE:
			executeDEMO_CREATE(System.in, System.out, params);
			break;		
		}

		
	}

	private static void executeDEMO_CREATE(InputStream in, PrintStream out, List<String> params) {

		TaskDefinition taskDef = DemoApp.createComplexTaskDef(processDAO, userId);
		
		ProcessDefinition procDef = new ProcessDefinition("Demo1", taskDef.getName(), "Demo Process", taskDef);
		procDef.setCreationUser(userId);

		
		ProcessDefinitionVariable variable = new ProcessDefinitionVariable();
		variable.setProcessDefinition(procDef);
		variable.setName("var1");
		variable.setValue("pippo");
		variable.setType(ValueTypes.JAVA_LANG_STRING);
		procDef.getVariables().add(variable );
		
		processDAO.saveProcessDefinition(procDef);
		
		
		executePROCESS_GENERATE(in, out, Arrays.asList(new String[] { String.valueOf(procDef.getProcessId()), "0" }));
		//executePROCESS_RUN(out, Arrays.asList(new String[] { String.valueOf(procDef.getProcessId()), "0" }));

		
	}
	
	

	private static TreeSet<TaskDefinition> arraysAsSet(TaskDefinition[] taskDefinitions) {

		TreeSet<TaskDefinition> set = new TreeSet<>();
		set.addAll(Arrays.asList(taskDefinitions));
		return set;
	}

	private static void executeCommandOnTask(String command, List<String> params) {
		switch(command) {
		case CMD_CREATE:
			executeTASK_CREATE(System.in,System.out);
			break;
		case CMD_CHANGE:
			executeTASK_CHANGE(System.in,System.out, params);
			break;
		case CMD_LIST:
			executeTASK_LIST(System.out);
			break;
			
		}
		
		
	}

	private static void executeTASK_CREATE(InputStream in, PrintStream out) {

		int i=1;
		out.println("Select task class:");
		for(MetadataDefinition mdf : Metadata.metadataDefinitions) {
			out.println(String.format("%2d - %s (%s)", i++, mdf.getName(), mdf.getClassName()));
		}
		out.print("> ");
		
		Scanner input = new Scanner(in);
		if(input.hasNextLine()) {
			int selection = Integer.valueOf(input.nextLine());
			if(selection<Metadata.metadataDefinitions.size()) {
				
				String className = Metadata.metadataDefinitions.get(selection-1).getClassName();
				
				List<InitialProperty> initialProperties = new LinkedList<InitialProperty>();
				
				String name= getStringValue("Enter name:","name", out, input);
				
				String description= getStringValue("Enter description:","description", out, input);
				
				String connectorName= getStringValue("Enter Connector Name:","connectorName", out, input, true);
				
				
				String parentId= getStringValue("Enter Parent Id (0 for nothing):","Parent Id", out, input);
				
				long parentTaskId=0;
				try {
					parentTaskId = Long.valueOf(parentId);
				} catch(NumberFormatException e) {
					
				}
				TaskDefinition parentTask = null;
				if(parentTaskId>0) {
					parentTask = processDAO.getTaskDefinitionById(parentTaskId);
				}
				
				TaskDefinition taskDefinition = new TaskDefinition();
				taskDefinition.setClassName(className);
				taskDefinition.setName(name);
				taskDefinition.setDescription(description);
				taskDefinition.setCreationUser(userId);
				taskDefinition.setConnectorName(connectorName);
				//if(parentTask!=null) taskDefinition.setParent(parentTask);

				
				
				Map<String, PropertyDefinition> properties = Metadata.propertyDefinitions.get(className);
				for(Entry<String, PropertyDefinition> entry: properties.entrySet()) {
					
					
					
					String propName = entry.getKey();
					PropertyDefinition propDef = entry.getValue();
					
					if(connectorName.length()>0 && propDef.isConnector())
						continue;
					

					boolean valueOk = false;
					String value = "";

					do {
						
						prompt(propDef, out, input);
						
						if(input.hasNextLine()) {
							
							
							value = input.nextLine().trim();
							
							Pair<Boolean, String> retVal = validate(value,propDef);
							
							if(retVal.getMember1()) {
								valueOk = true;
								value = retVal.getMember2();
							} else if(!valueOk) {
								out.println("ERROR: must provide valid value for property " + propName);
							}
						}
					} while(!valueOk);
						

					
					InitialProperty initialProperty = new InitialProperty(
							taskDefinition
							, entry.getValue().getName()
							, value);
					initialProperty.setCreationUser(userId);
					initialProperties.add(initialProperty);
					
					
				}
				
				taskDefinition.setInitialProperties(initialProperties);
				
				processDAO.saveTaskDefinition(taskDefinition);
				
				out.println("Create task with id:" + taskDefinition.getTaskId());
			}
		}
		input.close();
		
	}
	
	private static void executeTASK_CHANGE(InputStream in, PrintStream out, List<String> params) {

		int i=1;
		
		long taskId=0;
		TaskDefinition taskDefinition = null;
		
		if(params.size()>=1) {
			try {
				taskId = Long.valueOf(params.get(0));
			} catch(NumberFormatException e) {}
		}
		
		Scanner input = new Scanner(in);
		
		if(taskId==0) {
			out.println("Input Task ID:");
			out.print("> ");

			if(input.hasNextLine()) {
				taskId = Long.valueOf(input.nextLine());
			}
		}
		
		taskDefinition = processDAO.getTaskDefinitionById(taskId);
		
		if(taskDefinition!=null) {
			
			String name= getStringValue(String.format("Enter new name (%s), blank leaves unchanged :", taskDefinition.getName())
					,"name", out, input, true);
			
			String description= getStringValue(String.format("Enter new description (%s), blank leaves unchanged :", taskDefinition.getDescription())
					,"description", out, input, true);
			
			String connectorName= getStringValue(String.format("Enter new Connector Id (%s), blank leaves unchanged:", taskDefinition.getConnectorName())
					,"connectorId", out, input, true);
			
			/*
			String parentId= getStringValue(String.format("Enter Parent Id (%d), 0 for remove, blank leave unchanged:", taskDefinition.getName())
					,"Parent Id", out, input);
			*/
			boolean changed = false;
			if(name.length()>0) { taskDefinition.setName(name); changed = true; }
			if(description.length()>0) { taskDefinition.setDescription(description); changed = true; }
			if(connectorName.length()>0) { taskDefinition.setConnectorName(connectorName); changed = true; }
			
			
			if(changed) {
				taskDefinition.setChangeUser(userId);
			
				//if(parentTask!=null) taskDefinition.setParent(parentTask);
			
				processDAO.changeTaskDefinition(taskDefinition);
			
				out.println("Changed task with id:" + taskDefinition.getTaskId());				
			} else {
				out.println("Nothing to change for task with id:" + taskDefinition.getTaskId());
			}
		
		

		} else {
			out.println("No task def found");
		}
		
		
		input.close();
		
	}
	

	private static Pair<Boolean,String> validate(String inputValue, PropertyDefinition propDef) {
		
		boolean isValid = false;
		String value="";
		switch(propDef.getPropertyType()) {
		case CHOICE:
			int index=Integer.MIN_VALUE;
			try {
				index = Integer.valueOf(inputValue);
				if(index==0)
					value=propDef.getDefaultValue();
				else if(index<=propDef.getChoiceValues().length) {
					value=propDef.getChoiceValues()[index-1];
				}
			} catch(NumberFormatException e) {
				for(int i=0;i<propDef.getChoiceValues().length;i++) {
					if(propDef.getChoiceValues()[i].equals(inputValue)) {
						value=inputValue;
						break;
					}
				}
			}
			break;
		default:
			value = inputValue;
			if(value.length()==0) {
				if(propDef.getDefaultValue().length()>0)
					value = propDef.getDefaultValue();
			}
			break;
		}
		
		if(value.length()==0) {
			isValid =  propDef.isOptional();
		} else {
			switch(propDef.getPropertyType()) {
			case CHOICE:
				for(int i=0;i<propDef.getChoiceValues().length;i++) {
					if(propDef.getChoiceValues()[i].equals(value)) {
						isValid = true;
						break;
					}
				}
				break;
			default:
				isValid = true;
				break;
			}
		}
		
		
		return new Pair<Boolean,String>(isValid, value);
	}

	private static void prompt(PropertyDefinition propDef,PrintStream out, Scanner input) {
		
		out.println( String.format("Enter value for property %s%s (%s%s): "
				, propDef.getName()
				, propDef.isOptional()?"":"*"
				, propDef.getDescription()
				, propDef.getDefaultValue().length()>0? ", default " + propDef.getDefaultValue()  :""
				));


		switch(propDef.getPropertyType()) {
		case CHOICE:
			int i=1;
			for(String choice : propDef.getChoiceValues()) {
				out.println( String.format("% 2d: %s", i++, choice) );
			}
			break;
		default:
			break;
		}
		out.print("> ");
		
		
	}
	
	private static String getStringValue(String caption, String valueName, PrintStream out, Scanner input) {
		return getStringValue(caption, valueName, out, input, false);
	}

	private static String getStringValue(String caption, String valueName, PrintStream out, Scanner input, boolean optional) {
		String value = "";
		boolean defaulted = false;
		do {
			out.println(caption);
			out.print("> ");
		
			if(input.hasNextLine()) {
				value = input.nextLine().trim();
			}
			if(value.length()==0) {
				if(!optional)
					out.println("ERROR: must provide value for name");
				else
					defaulted = true;
			}
		} while(value.length()==0 && !defaulted);
		
		return value;
	}


	private static long getLongValue(String caption, String name, PrintStream out, Scanner input, boolean optional) {
		return getLongValue(caption, name, out, input, optional, 0);
	}

	private static long getLongValue(String caption, String name, PrintStream out, Scanner input, boolean optional, long defaultValue) {
		long value = defaultValue;
		boolean defaulted = false;
		do {
			out.println(caption);
			out.print("> ");
		
			String s="";
			if(input.hasNextLine()) {
				s = input.nextLine().trim();
			}
			if(s.length()==0) {
				if(!optional)
					out.println("ERROR: must provide value for " + name);
				else
					defaulted = true;
			} else {
				try {
					value = Long.valueOf(s);
				} catch(NumberFormatException e) {}
			}
		} while(value==0 || !defaulted);
		
		return value;
	}

	private static void executeCommandOnTextConn(String command, List<String> params) {
		switch(command) {
		case CMD_CREATE:
			executeTEXTCONN_CREATE(System.out);
			break;
		case CMD_LIST:
			executeTEXTCONN_LIST(System.out);
			break;
			
		}
		
	}
	

	private static void executeCommandOnJdbcConn(String command, List<String> params) {
		switch(command) {
		case CMD_CREATE:
			executeJDBCCONN_CREATE(System.out);
			break;
		case CMD_LIST:
			executeJDBCCONN_LIST(System.out);
			break;
			
		}
		
		
	}
	
	private static void executeTEXTCONN_LIST(PrintStream out) {
		
		out.println("ID--------|FILENAME------------|DESCRIPTION---------|USER------|CREATION");
		for(nrsoft.tasks.model.TextConnector connector: processDAO.getTextConnectorList()) {
			
			String line = String.format("%10d|%-20s|%-20s|%-10s|%s", 
					connector.getConnId()
					, connector.getFilename().substring(0, Util.min(20,connector.getFilename().length()))
					, connector.getDescription().substring(0, Util.min(20,connector.getDescription().length()))
					, connector.getCreationUser(), connector.getCreationTime().toString());
			
			out.println(line);
			
		}
		
		
	}


	private static void executeJDBCCONN_LIST(PrintStream out) {
		
		
		//  80 RULER 00000000011111111112222222222333333333344444444445555555555666666666677777777778
		//           12345678901234567890123456789012345678901234567890123456789012345678901234567890
		out.println("ID--------|NAME----------------|URL-----------------|DESCRIPTION---------|USER------|CREATION");
		for(nrsoft.tasks.model.JdbcConnector connector: processDAO.getJdbcConnectorList()) {
			
			String line = String.format("%10d|%-20s|%-20s|%-20s|%-10s|%s", 
					connector.getConnId()
					, safeSubs(connector.getName(),20)
					, safeSubs(connector.getUrl(),20)
					, safeSubs(connector.getDescription(),20)
					, connector.getCreationUser()
					, connector.getCreationTime().toString());
			
			out.println(line);
			
		}

		
	}


	private static void executeTASK_LIST(PrintStream out) {
		
		//  80 RULER 00000000011111111112222222222333333333344444444445555555555666666666677777777778
		//           12345678901234567890123456789012345678901234567890123456789012345678901234567890
		out.println("ID--------|CLASSNAME---------------------|NAME----------------|DESCRIPTION---------|CONNECTOR-|USER------|CREATION");
		for(nrsoft.tasks.model.TaskDefinition taskDef: processDAO.getTaskDefinitionList(0,0)) {
			
			String line = String.format("%10d|%-30s|%-20s|%-20s|%-10d|%-10s|%s", 
					taskDef.getTaskId()
					, safeSubs(smartClassName(taskDef.getClassName(),30),30)
					, safeSubs(taskDef.getName(),20)
					, safeSubs(taskDef.getDescription(),20)
					, safeSubs(taskDef.getConnectorName(),10)
					, taskDef.getCreationUser()
					, taskDef.getCreationTime().toString());
			
			out.println(line);
			
		}
		
	}

	private static String smartClassName(String className, int maxLen) {

		String smartClassName=className;
		if(className!=null && className.length()>0) {
			String[] parts = className.split("\\.");
			
			smartClassName = parts[parts.length-1];
			if(smartClassName.length()>maxLen) {
				String prefix = "";
				for(int i=0;i<parts.length-1;i++) {
					prefix += parts[i].substring(0, 1) + ".";
				}
				smartClassName = prefix + smartClassName;
			} else {
				int i=parts.length-2;
				while(i>=0) {
					if(parts[i].length() + 1 + smartClassName.length() > maxLen)
						smartClassName = parts[i].substring(0,1) + "." + smartClassName;
					else
						smartClassName = parts[i] + "." + smartClassName;
					i--;
				}
				
			}
		}
		
		return smartClassName;
	}

	private static String safeSubs(String value, int len) {
		if(value==null) return null;
		if(value.length()==0) return "";
		
		int l = Util.min(len,value.length());
		
		return value.substring(0, l);
	}

	private static void executeTEXTCONN_CREATE(PrintStream out) {

		Map<String,String> properties = readTextConnectorProperties(out, System.in);
		
		nrsoft.tasks.model.TextConnector model = new nrsoft.tasks.model.TextConnector();
		model.setName(properties.get( TextConnector.PROP_NAME  ));
		model.setDescription(properties.get( TextConnector.PROP_DESCRIPTION  ));
		model.setFilename(properties.get( TextConnector.PROP_FILENAME));
		model.setCreationUser(userId);
		
		processDAO.saveTextConnector(model);
		
	}
	
	private static void executeJDBCCONN_CREATE(PrintStream out) {

		Map<String,String> properties = readJdbcConnectorProperties(out, System.in);
		
		nrsoft.tasks.model.JdbcConnector model = new nrsoft.tasks.model.JdbcConnector();
		
		model.setName(properties.get( JdbcConnector.PROP_NAME  ));
		model.setDescription(properties.get( JdbcConnector.PROP_DESCRIPTION  ));
		model.setUrl(properties.get(JdbcConnector.PROP_URL));
		model.setDriver(properties.get(JdbcConnector.PROP_DRIVER));
		model.setUser(properties.get(JdbcConnector.PROP_USER));
		model.setPassword(properties.get(JdbcConnector.PROP_PASSWORD));
		model.setCreationUser(userId);

		
		processDAO.saveJdbcConnector(model);
		
	}
	
	
	private static Map<String, String> readFromInput(PrintStream out, InputStream in, String[][] propertiesDef) {
		Scanner input = new Scanner(in);
		
		Map<String, String> map = new TreeMap<>();
		
		for( String[] prop: propertiesDef ) {
			
			out.println(prop[0] + " - " + prop[1] + ":");
			if(input.hasNextLine()) {
				 String s =input.nextLine();
				 map.put(prop[0], s.trim());
			}
			
			
			
			
		}
		
		input.close();
		
		return map;

	}

	private static Map<String, String> readTextConnectorProperties(PrintStream out, InputStream in) {
		
		return readFromInput(out, in, TextConnector.propertiesDef);

	}
	
	private static Map<String, String> readJdbcConnectorProperties(PrintStream out, InputStream in) {
		
		
		return readFromInput(out, in, JdbcConnector.propertiesDef);	
	}
	

	private static void executeCommandOnProcess(String command, List<String> params) {
		switch(command) {
		case CMD_CREATE:
			executePROCESS_CREATE(System.in, System.out);
			break;		
			
		case CMD_GENERATE:
			executePROCESS_GENERATE(System.in, System.out, params);
			break;
		case CMD_LIST:
			executePROCESS_LIST(System.out);
			break;
		case CMD_IMPORT:
			executePROCESS_IMPORT(System.out, params);
			break;
		case CMD_EXPORT:
			executePROCESS_EXPORT(System.out, params);
			break;
			
		case CMD_RUN:
			executePROCESS_RUN(System.out, params);
			break;
			
			
		}
	}

	private static void executePROCESS_GENERATE(InputStream in, PrintStream out,List<String> params) {
		
		if(params.size()<2) {
			out.println("Missing parameters for command PROCESS GENERATE <id> <version>");
			System.exit(1);
		}
		
		long procId = Long.valueOf(params.get(0));
		long version = Long.valueOf(params.get(1));

		
		
		Beans beans = new Beans();
		BeanCreator creator = new BeanCreator();

		ProcessDefinition processDefinition = processDAO.getProcessDefinitionById(procId, version);
		for(Object bean: creator.createBeans(processDefinition.getTaskDefinition()))
			beans.getImportOrAliasOrBean().add(bean);


		try {
			String xml =  XmlSpringConfigurationBuilder.buildXml(beans);
			processDefinition.setGeneratedCode(xml);
			processDefinition.setGenerationUser(userId);
			processDefinition.setGenerationTime(OffsetDateTime.now());
			
			processDAO.saveProcessDefinition(processDefinition);
			
			out.println("Process generated / id=" + processDefinition.getProcessId());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (jakarta.xml.bind.JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	private static void executePROCESS_CREATE(InputStream in, PrintStream out) {

		ProcessDefinition processDefinition = new ProcessDefinition();
				
		Scanner input = new Scanner(in);
		String value= getStringValue("Enter name:", "name", out, input );
		
		processDefinition.setName(value);
		
		value= getStringValue("Enter Start Bean Name:", "Start Bean Name", out, input );
		
		processDefinition.setStartBeanName(value);
		
		
		value= getStringValue("Enter description:", "description", out, input );
		
		processDefinition.setDescription(value);
		
		long taskId = getLongValue("Enter Task Id:", "task", out, input, false );
		
		
		TaskDefinition taskDef = processDAO.getTaskDefinitionById(taskId);
		processDefinition.setTaskDefinition(taskDef);
		
		processDefinition.setCreationUser(userId);
		
		processDAO.saveProcessDefinition(processDefinition);
		
		out.println("Create process with id:" + processDefinition.getProcessId());
		
		
		input.close();
		
	}
	
	
	private static void executePROCESS_RUN(PrintStream out, List<String> params) {
		// TODO Auto-generated method stub
		
		
		long id = Long.valueOf(params.get(0));
		long version = Long.valueOf(params.get(1));
		
		ProcessDefinition processDef = processDAO.getProcessDefinitionById(id, version);
		
		ProcessObserverPersistance processObserver = new ProcessObserverPersistance(processDef, processDAO.getEntityManager());
		nrsoft.tasks.runtime.Process process = new Process(); 
		nrsoft.tasks.runtime.Process.setup(
				process,
				"ADMIN", processDef
				, Arrays.asList(new ProcessObserver[] {  processObserver } ));	
		
		nrsoft.tasks.model.Process processMdl = processDAO.createProcess(process.getUUID(), "ADMIN", processDef);
		processObserver.setProcessModel(processMdl);
		
		LoggersProvider.buildLogger(process.getUUID());
		process.setLoggerProvider(new LoggerProvider(process.getUUID()));
		
			
		TaskResult result = null;
		try {
			process.run();
		
			result = process.getResult();
		} catch(Exception e) {
			out.println("Process run exception: " + e.getMessage());
		}
		if(result!=null) {
			if(result.isError())
				out.println(result.getMessage());
			else
				out.println("Succesfully execution.");
		}	

	}

	private static void executePROCESS_IMPORT(PrintStream out, List<String> params) {
		
		if(params.size()<4) {
			out.println("Missing parameters for command IMPORT");
			System.exit(1);
		}
		
		String name = params.get(0);
		String description = params.get(1);
		String beanName = params.get(2);
		String filename = params.get(3);
		
		
		StringBuilder sb = new StringBuilder();
		
		try (Stream<String> stream = Files.lines(Paths.get(filename))) {
	        stream.forEach((line) -> sb.append(line));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		
		ProcessDefinition processDef = new ProcessDefinition();
		processDef.setName(name);
		processDef.setCreationUser(userId);
		processDef.setStartBeanName(beanName);
		processDef.setDescription(description);
		processDef.setGeneratedCode(sb.toString());
		processDAO.saveProcessDefinition(processDef);
		
		
		
	}
	
	private static void executePROCESS_EXPORT(PrintStream out, List<String> params) {
		
		if(params.size()<2) {
			out.println("Missing parameters fro command EXPORT");
			System.exit(1);
		}
		
		long id = Long.valueOf(params.get(0));
		long version = Long.valueOf(params.get(1));
		String filename = params.get(2);
		
		
		
		ProcessDefinition process = processDAO.getProcessDefinitionById(id, version);
		
		FileWriter writer;
		try {
			writer = new FileWriter(filename);
			writer.write(process.getGeneratedCode());
			writer.close();			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	
	
	

	private static void executePROCESS_LIST(PrintStream out) {

		out.println("ID--------|VERSION---|DESCRIPTION---------|USER------|CREATION");
		for(ProcessDefinition proc: processDAO.getProcessDefinitionList()) {
			
			String line = String.format("%10d|%10d|%-20s|%-10s|%s", 
					proc.getProcessId()
					, proc.getVersion()
					, proc.getDescription().substring(0, Util.min(20,proc.getDescription().length()))
					, proc.getCreationUser(), proc.getCreationTime().toString());
			
			out.println(line);
			
		}
		
	}

}

