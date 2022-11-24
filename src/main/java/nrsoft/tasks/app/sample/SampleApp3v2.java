package nrsoft.tasks.app.sample;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.springframework.schema.beans.Beans;

import nrsoft.tasks.ProcessData;
import nrsoft.tasks.Task;
import nrsoft.tasks.TaskResult;
import nrsoft.tasks.def.TaskDefinition;
import nrsoft.tasks.metadata.TaskGroupExecutionType;
import nrsoft.tasks.runtime.TaskProviderSpringClasspath;
import nrsoft.tasks.spring.BeanCreator;
import nrsoft.tasks.spring.XmlSpringConfigurationBuilder;
import nrsoft.tasks.sql.SqlTask;
import nrsoft.tasks.sql.SqlTaskSelect;
import nrsoft.tasks.metadata.*;

public class SampleApp3v2 {
	
	/*

					*
					|
		+-----------+------------+
		|           |            |
	 +------+    +------+    +------+
	 | Sql1 |    | Sql2 |    | Csv1 | --+
	 +------+    +------+    +------+   |
        |           |            |	   ts1
        |           |            |      | 
        |           |        +------+   |
        |           |        | Idl1 | --+ 
        |           |        +------+
        |           |            |
        |<--------[ts2]--------->|
		+-----------#------------+
		            |
		        +--------+
		        |  Sort  |
		        +--------+
		            |
		        +--------+
		        | CsvOut |
		        +--------+
		            |
		            O
		      # = merge datasets
		      ts3 = ts2, Sort, CsvOut
	 */

	public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException, JAXBException, jakarta.xml.bind.JAXBException {
		
		java.util.Map<String,String> prop1 = new HashMap<String,String>();
		prop1.put(SqlTaskMd.PROP_SQL_CONNECTION_URL, "jdbc:h2:tcp://localhost/~/test");
		prop1.put(SqlTaskMd.PROP_SQL_CONNECTION_USER, "sa");
		prop1.put(SqlTaskMd.PROP_SQL_CONNECTION_PASSWORD, "");
		prop1.put(SqlTaskMd.PROP_SQL_DRIVER, "org.h2.Driver");
		prop1.put(SqlTaskSelectMd.PROP_SQL_SELECT_QUERY, "SELECT * FROM TEST1");
		TaskDefinition sql1 = new TaskDefinition("Sql1", "nrsoft.tasks.sql.SqlTaskSelect", prop1, null);
		
		
		java.util.Map<String,String> prop2 = new HashMap<String,String>();
		prop2.put(SqlTaskMd.PROP_SQL_CONNECTION_URL, "jdbc:h2:tcp://localhost/~/test");
		prop2.put(SqlTaskMd.PROP_SQL_CONNECTION_USER, "sa");
		prop2.put(SqlTaskMd.PROP_SQL_CONNECTION_PASSWORD, "");
		prop2.put(SqlTaskMd.PROP_SQL_DRIVER, "org.h2.Driver");
		prop2.put(SqlTaskSelectMd.PROP_SQL_SELECT_QUERY, "SELECT * FROM TEST2");
		TaskDefinition sql2 = new TaskDefinition("Sql2", "nrsoft.tasks.sql.SqlTaskSelect", prop2, null);
		
		java.util.Map<String,String> prop3 = new HashMap<String,String>();
		prop3.put(FileTaskMd.PROP_FILE_NAME, "c:/TEMP/java/pippo.csv");
		prop3.put(FileTaskCsvMd.PROP_FILE_CSV_HEADER, "true");
		prop3.put(FileTaskCsvMd.PROP_FILE_CSV_SEP, ";");
		prop3.put(TaskMd.PROP_FIELDMAP,"ID1:F1,NAME1:F2");
		prop3.put(FileTaskCsvInMd.PROP_FILE_CSVIN_TYPES,"ID1:L");
		TaskDefinition csv1 = new TaskDefinition("Csv1", "nrsoft.tasks.file.FileTaskCsvIn", prop3, null);
		
		java.util.Map<String,String> prop4 = new HashMap<String,String>();
		prop4.put(IdleTaskMd.PROP_IDLE_TIMEOUT, "100");
		TaskDefinition idl1 = new TaskDefinition("Idl1", "nrsoft.tasks.IdleTask", prop4, null);
		
		java.util.Map<String,String> prop5 = new HashMap<String,String>();
		prop5.put(SortTaskMd.PROP_NAME_SORTFIELDS, "F1:A,F2:A");
		TaskDefinition sort = new TaskDefinition("Sort", "nrsoft.tasks.SortTask", prop5, null);
		
		java.util.Map<String,String> prop6 = new HashMap<String,String>();
		prop6.put(FileTaskMd.PROP_FILE_NAME, "c:/temp/sample3.csv");
		prop6.put(FileTaskCsvMd.PROP_FILE_CSV_HEADER, "true");
		TaskDefinition csvOut = new TaskDefinition("CvsOut", "nrsoft.tasks.file.FileTaskCsvOut", prop6, null);
		
		java.util.Map<String,String> prop7 = new HashMap<String,String>();
		LinkedList<TaskDefinition> child1 = arraysAsList(new TaskDefinition[] { csv1,idl1  });
		TaskDefinition taskSet1 = new TaskDefinition("ts1", "nrsoft.tasks.TaskGroup", prop7, child1);
		
		
		java.util.Map<String,String> prop8 = new HashMap<String,String>();
		prop8.put(TaskGroupMd.PROP_TASKGROUP_EXECUTION, TaskGroupExecutionType.PARALLEL.toString());
		LinkedList<TaskDefinition> child2 = arraysAsList(new TaskDefinition[] { sql1, sql2, taskSet1 });
		TaskDefinition taskSet2 = new TaskDefinition("ts2", "nrsoft.tasks.TaskGroup", prop8, child2);
		
		
		java.util.Map<String,String> prop9 = new HashMap<String,String>();
		LinkedList<TaskDefinition> child3 = arraysAsList(new TaskDefinition[] { taskSet2, sort, csvOut });
		TaskDefinition taskSet3 = new TaskDefinition("ts3", "nrsoft.tasks.TaskGroup", prop9, child3);
		
		Beans beans = new Beans();
		
		BeanCreator creator = new BeanCreator();

		for(Object bean: creator.createBeans(taskSet3))
			beans.getImportOrAliasOrBean().add(bean);

		FileWriter writer = new FileWriter("c:/temp/java/taskmud.xml");
		writer.write( XmlSpringConfigurationBuilder.buildXml(beans) );
		writer.close();
		
		
		TaskProviderSpringClasspath provider = new TaskProviderSpringClasspath("file:///c:/temp/java/taskmud.xml", "ts3");
		
		Task task = provider.load();
		
		ProcessData dataIn = new ProcessData();
		TaskResult result = task.execute(dataIn);
		
		if(result.isError())
			System.err.println(result.getMessage());
		else
			System.out.println("Processo eseguito senza errori");
		
		provider.afterExecute();
	}
	
	private static LinkedList<TaskDefinition> arraysAsList(TaskDefinition[] taskDefinitions) {

		LinkedList<TaskDefinition> list = new LinkedList<>();
		for(TaskDefinition td: taskDefinitions) {
			list.add(td);
		}
		return list;
	}
	

}
