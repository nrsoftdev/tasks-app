package nrsoft.tasks.app.sample;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import nrsoft.tasks.metadata.FileTaskCsvInMd;
import nrsoft.tasks.metadata.FileTaskCsvMd;
import nrsoft.tasks.metadata.FileTaskMd;
import nrsoft.tasks.metadata.IdleTaskMd;
import nrsoft.tasks.metadata.SortTaskMd;
import nrsoft.tasks.metadata.SqlTaskMd;
import nrsoft.tasks.metadata.SqlTaskSelectMd;
import nrsoft.tasks.metadata.TaskGroupExecutionType;
import nrsoft.tasks.metadata.TaskGroupMd;
import nrsoft.tasks.metadata.TaskMd;
import nrsoft.tasks.model.InitialProperty;
import nrsoft.tasks.model.JdbcConnector;
import nrsoft.tasks.model.TaskCollection;
import nrsoft.tasks.persistance.TasksDaoJPA;

public class DemoApp {
	
	public static nrsoft.tasks.model.TaskDefinition createComplexTaskDef(TasksDaoJPA processDAO, String userId) {
		
		
		JdbcConnector jdbcConnector = new JdbcConnector();
		jdbcConnector.setName("JDBC1");
		jdbcConnector.setDescription("Jdbc One");
		jdbcConnector.setDriver("org.h2.Driver");
		jdbcConnector.setUser("sa");
		jdbcConnector.setPassword("");
		jdbcConnector.setUrl("jdbc:h2:tcp://localhost/~/test");
		jdbcConnector.setCreationUser("ADMIN");
		
		processDAO.saveJdbcConnector(jdbcConnector);
		
		
		
		List<InitialProperty> initProp1 = new LinkedList<>();
		nrsoft.tasks.model.TaskDefinition sql1 = new nrsoft.tasks.model.TaskDefinition("Sql1", "nrsoft.tasks.sql.SqlTaskSelect", "select from test1");
		sql1.setConnectorName(jdbcConnector.getName());
		
		/*
		initProp1.add(new InitialProperty(sql1, SqlTaskMd.PROP_SQL_CONNECTION_URL, "jdbc:h2:tcp://localhost/~/test"));
		initProp1.add(new InitialProperty(sql1, SqlTaskMd.PROP_SQL_CONNECTION_USER, "sa"));
		initProp1.add(new InitialProperty(sql1, SqlTaskMd.PROP_SQL_CONNECTION_PASSWORD, ""));
		initProp1.add(new InitialProperty(sql1, SqlTaskMd.PROP_SQL_DRIVER, "org.h2.Driver"));
		*/
		initProp1.add(new InitialProperty(sql1, SqlTaskSelectMd.PROP_SQL_SELECT_QUERY, "SELECT * FROM TEST1"));
		sql1.setInitialProperties(initProp1);
		sql1.setCreationUser(userId);
		
		processDAO.saveTaskDefinition(sql1);
		
		
		List<InitialProperty> initProp2 = new LinkedList<>();
		nrsoft.tasks.model.TaskDefinition sql2 = new nrsoft.tasks.model.TaskDefinition("Sql2", "nrsoft.tasks.sql.SqlTaskSelect","select from test2");
		sql2.setConnectorName(jdbcConnector.getName());
		/*
		initProp2.add(new InitialProperty(sql2,SqlTaskMd.PROP_SQL_CONNECTION_URL, "jdbc:h2:tcp://localhost/~/test"));
		initProp2.add(new InitialProperty(sql2,SqlTaskMd.PROP_SQL_CONNECTION_USER, "sa"));
		initProp2.add(new InitialProperty(sql2,SqlTaskMd.PROP_SQL_CONNECTION_PASSWORD, ""));
		initProp2.add(new InitialProperty(sql2,SqlTaskMd.PROP_SQL_DRIVER, "org.h2.Driver"));
		*/
		initProp2.add(new InitialProperty(sql2,SqlTaskSelectMd.PROP_SQL_SELECT_QUERY, "SELECT * FROM TEST2"));
		sql2.setInitialProperties(initProp2);
		sql2.setCreationUser(userId);
		processDAO.saveTaskDefinition(sql1);
		
		List<InitialProperty> initProp3 = new LinkedList<>();
		nrsoft.tasks.model.TaskDefinition csv1 = new nrsoft.tasks.model.TaskDefinition("Csv1", "nrsoft.tasks.file.FileTaskCsvIn","Read from csv");
		initProp3.add(new InitialProperty(csv1,FileTaskMd.PROP_FILE_NAME, "c:\\TEMP\\java\\pippo.csv"));
		initProp3.add(new InitialProperty(csv1,FileTaskCsvMd.PROP_FILE_CSV_HEADER, "true"));
		initProp3.add(new InitialProperty(csv1,FileTaskCsvMd.PROP_FILE_CSV_SEP, ";"));
		initProp3.add(new InitialProperty(csv1,TaskMd.PROP_FIELDMAP,"ID1:F1,NAME1:F2"));
		initProp3.add(new InitialProperty(csv1,FileTaskCsvInMd.PROP_FILE_CSVIN_TYPES,"ID1:L"));
		csv1.setInitialProperties(initProp3);
		csv1.setCreationUser(userId);
		processDAO.saveTaskDefinition(csv1);
		
		List<InitialProperty> initProp4 = new LinkedList<>();
		nrsoft.tasks.model.TaskDefinition idl1 = new nrsoft.tasks.model.TaskDefinition("Idl1", "nrsoft.tasks.IdleTask", "Wait");
		initProp4.add(new InitialProperty(idl1,IdleTaskMd.PROP_IDLE_TIMEOUT, "100"));
		idl1.setInitialProperties(initProp4);
		idl1.setCreationUser(userId);
		processDAO.saveTaskDefinition(idl1);
		
		List<InitialProperty> initProp5 = new LinkedList<>();
		nrsoft.tasks.model.TaskDefinition sort = new nrsoft.tasks.model.TaskDefinition("Sort", "nrsoft.tasks.SortTask", "Sort");
		initProp5.add(new InitialProperty(sort,SortTaskMd.PROP_NAME_SORTFIELDS, "F1:A,F2:A"));
		sort.setCreationUser(userId);
		sort.setInitialProperties(initProp5);
		processDAO.saveTaskDefinition(sort);
		
		List<InitialProperty> initProp6 = new LinkedList<>();
		nrsoft.tasks.model.TaskDefinition csvOut = new nrsoft.tasks.model.TaskDefinition("CvsOut", "nrsoft.tasks.file.FileTaskCsvOut", "write to csv");
		initProp6.add(new InitialProperty(csvOut,FileTaskMd.PROP_FILE_NAME, "c:/temp/java/sample3.csv"));
		initProp6.add(new InitialProperty(csvOut,FileTaskCsvMd.PROP_FILE_CSV_HEADER, "true"));
		csvOut.setInitialProperties(initProp6);
		csvOut.setCreationUser(userId);
		processDAO.saveTaskDefinition(csvOut);
		
		//List<InitialProperty> initProp7 = new LinkedList<>();
		nrsoft.tasks.model.TaskDefinition taskSet1 = new nrsoft.tasks.model.TaskDefinition("ts1", "nrsoft.tasks.TaskGroup","ts1");
		taskSet1.setCreationUser(userId);
		TaskCollection taskCollection1 = new TaskCollection(taskSet1);
		taskCollection1.setMembersFromTaskList(Arrays.asList(new nrsoft.tasks.model.TaskDefinition[] {csv1, idl1}) );
		taskSet1.setTaskCollection(taskCollection1);
		processDAO.saveTaskDefinition(taskSet1);
		
		
		List<InitialProperty> initProp8 = new LinkedList<>();
		nrsoft.tasks.model.TaskDefinition taskSet2 = new nrsoft.tasks.model.TaskDefinition("ts2", "nrsoft.tasks.TaskGroup", "ts2");
		initProp8.add(new InitialProperty( taskSet2, TaskGroupMd.PROP_TASKGROUP_EXECUTION, TaskGroupExecutionType.PARALLEL.toString()));
		taskSet2.setCreationUser(userId);
		TaskCollection taskCollection2 = new TaskCollection(taskSet2);
		taskCollection2.setMembersFromTaskList(Arrays.asList(new nrsoft.tasks.model.TaskDefinition[] { sql1, sql2, taskSet1 }) );
		taskSet2.setTaskCollection(taskCollection2);
		processDAO.saveTaskDefinition(taskSet2);
		
		//List<InitialProperty> initProp9 = new LinkedList<>();
		nrsoft.tasks.model.TaskDefinition taskSet3 = new nrsoft.tasks.model.TaskDefinition("ts3", "nrsoft.tasks.TaskGroup", "ts3");
		taskSet3.setCreationUser(userId);
		TaskCollection taskCollection3 = new TaskCollection(taskSet3);
		taskCollection3.setMembersFromTaskList(Arrays.asList(new nrsoft.tasks.model.TaskDefinition[] { taskSet2, sort, csvOut }));
		taskSet3.setTaskCollection(taskCollection3);
		
		
		processDAO.saveTaskDefinition(taskSet3);
		
		return taskSet3;
		
	}


}
