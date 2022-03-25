package nrsoft.tasks.app.sample;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nrsoft.tasks.InitialProperties;
import nrsoft.tasks.ProcessData;
import nrsoft.tasks.Task;
import nrsoft.tasks.TaskResult;
import nrsoft.tasks.TaskGroup;
import nrsoft.tasks.file.FileTaskCsvIn;
import nrsoft.tasks.metadata.*;
import nrsoft.tasks.sql.SqlTaskDML;
import nrsoft.tasks.runtime.Process;

public class SampleApp {

	public static void main(String[] args) {
		
		
	
		InitialProperties initProp1 = new InitialProperties();
		
		initProp1.addProperty(FileTaskCsvMd.PROP_FILE_CSV_HEADER, "true");
		initProp1.addProperty(FileTaskCsvMd.PROP_FILE_CSV_SEP, ",");
		initProp1.addProperty(FileTaskMd.PROP_FILE_NAME, "c:/temp/input.csv");
		
		
		Task loadFromCsv = new FileTaskCsvIn("csvLoader", initProp1 );
		

		
		InitialProperties initProp2 = new InitialProperties();
		
		initProp2.addProperty(SqlTaskMd.PROP_SQL_CONNECTION_URL, "jdbc:h2:tcp://localhost/~/test");
		initProp2.addProperty(SqlTaskMd.PROP_SQL_CONNECTION_USER, "sa");
		initProp2.addProperty(SqlTaskMd.PROP_SQL_CONNECTION_PASSWORD, "");
		initProp2.addProperty(SqlTaskMd.PROP_SQL_DRIVER, "org.h2.Driver");
		
		//initProp2.addProperty(SqlTaskDML.PROP_SQL_DML_TABLE, "COUNTER");
		initProp2.addProperty(SqlTaskDMLMd.PROP_SQL_DML_TABLE, "@TableName");
		initProp2.addProperty(SqlTaskDMLMd.PROP_SQL_DML_OPERATION, "INSERT");
		
		initProp2.addProperty(SqlTaskDMLMd.PROP_SQL_DML_JDBC_CATALOGNAME, "TEST");
		initProp2.addProperty(SqlTaskDMLMd.PROP_SQL_DML_JDBC_SCHEMANAME, "PUBLIC");
		
		
		Task insertInDB = new SqlTaskDML("dbWriter", initProp2 );
		
		Map<String, Object> newVariables = new HashMap<>();
		newVariables.put("TableName", "COUNTER");
		insertInDB.setNewVariables(newVariables );
		

		InitialProperties taskSetProp = new InitialProperties();
		TaskGroup taskSet = new TaskGroup("Tasks1", taskSetProp );
		
		List<Task> tasks = new LinkedList<>();
		
		taskSet.setTasks(tasks);
		
		
		tasks.add(loadFromCsv);
		tasks.add(insertInDB);

		
		Process process = Process.create("ADMIN", taskSet);
		
		process.run();
		
		TaskResult result = process.getResult();
		
		System.out.println( String.format("Errore: %b, %s", result.isError(), result.getMessage()));
		

	}

}
