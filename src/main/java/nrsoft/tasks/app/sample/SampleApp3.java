package nrsoft.tasks.app.sample;

import nrsoft.tasks.IdleTask;
import nrsoft.tasks.InitialProperties;
import nrsoft.tasks.ProcessData;
import nrsoft.tasks.SortTask;
import nrsoft.tasks.Task;
import nrsoft.tasks.TaskResult;
import nrsoft.tasks.TaskGroup;
import nrsoft.tasks.file.FileTask;
import nrsoft.tasks.file.FileTaskCsv;
import nrsoft.tasks.file.FileTaskCsvIn;
import nrsoft.tasks.file.FileTaskCsvOut;
import nrsoft.tasks.metadata.TaskGroupExecutionType;
import nrsoft.tasks.sql.SqlTask;
import nrsoft.tasks.sql.SqlTaskSelect;
import nrsoft.tasks.metadata.*;

public class SampleApp3 {
	
	/*

					*
					|
		+-----------+------------+
		|           |            |
	 +------+    +------+    +------+
	 | Sql1 |    | Sql2 |    | Csv1 |
	 +------+    +------+    +------+
        |           |            |
        |           |            |
        |           |        +------+
        |           |        | Idl1 |
        |           |        +------+
        |           |            |
		+-----------+------------+
		            |
		       +-------+
		       | Sort  |
		       +-------+
		            |
		       +--------+
		       | CsvOut |
		       +--------+
		            |
		            O                                       
	 */

	public static void main(String[] args) {
		
		InitialProperties prop1 = new InitialProperties();
		prop1.addProperty(SqlTaskMd.PROP_SQL_CONNECTION_URL, "jdbc:h2:tcp://localhost/~/test");
		prop1.addProperty(SqlTaskMd.PROP_SQL_CONNECTION_USER, "sa");
		prop1.addProperty(SqlTaskMd.PROP_SQL_CONNECTION_PASSWORD, "");
		prop1.addProperty(SqlTaskMd.PROP_SQL_DRIVER, "org.h2.Driver");
		prop1.addProperty(SqlTaskSelectMd.PROP_SQL_SELECT_QUERY, "SELECT * FROM TEST1");
		Task sql1 = new SqlTaskSelect("Sql1", prop1);
		
		InitialProperties prop2 = new InitialProperties();
		prop2.addProperty(SqlTaskMd.PROP_SQL_CONNECTION_URL, "jdbc:h2:tcp://localhost/~/test");
		prop2.addProperty(SqlTaskMd.PROP_SQL_CONNECTION_USER, "sa");
		prop2.addProperty(SqlTaskMd.PROP_SQL_CONNECTION_PASSWORD, "");
		prop2.addProperty(SqlTaskMd.PROP_SQL_DRIVER, "org.h2.Driver");
		prop2.addProperty(SqlTaskSelectMd.PROP_SQL_SELECT_QUERY, "SELECT * FROM TEST2");
		Task sql2 = new SqlTaskSelect("Sql2", prop2);
		
		InitialProperties prop3 = new InitialProperties();
		prop3.addProperty(FileTaskMd.PROP_FILE_NAME, "c:\\TEMP\\pippo.csv");
		prop3.addProperty(FileTaskCsvMd.PROP_FILE_CSV_HEADER, "true");
		prop3.addProperty(FileTaskCsvMd.PROP_FILE_CSV_SEP, ";");
		prop3.addProperty(TaskMd.PROP_FIELDMAP,"ID1:F1,NAME1:F2");
		prop3.addProperty(FileTaskCsvInMd.PROP_FILE_CSVIN_TYPES,"ID1:L");
		
		
		Task csv1 = new FileTaskCsvIn("Csv1", prop3);
		
		InitialProperties prop4 = new InitialProperties();
		prop4.addProperty(IdleTaskMd.PROP_IDLE_TIMEOUT, "100");
		Task idl1 = new IdleTask("Idl1", prop4);
		
		InitialProperties prop5 = new InitialProperties();
		prop5.addProperty(SortTaskMd.PROP_NAME_SORTFIELDS, "F1:D,F2:D");
		Task sort = new SortTask("Sort", prop5);
		
		InitialProperties prop6 = new InitialProperties();
		prop6.addProperty(FileTaskMd.PROP_FILE_NAME, "c:/temp/sample3.csv");
		prop6.addProperty(FileTaskCsvMd.PROP_FILE_CSV_HEADER, "true");
		Task csvOut = new FileTaskCsvOut("CvsOut", prop6);
		
		InitialProperties prop7 = new InitialProperties();
		TaskGroup taskSet1 = new TaskGroup("ts1", prop7);
		
		taskSet1.addTask(csv1);
		taskSet1.addTask(idl1);
		
		InitialProperties prop8 = new InitialProperties();
		prop8.addProperty(TaskGroupMd.PROP_TASKGROUP_EXECUTION, TaskGroupExecutionType.PARALLEL.toString());
		TaskGroup taskSet2 = new TaskGroup("ts2", prop8);
		
		taskSet2.addTask(sql1);
		taskSet2.addTask(sql2);
		taskSet2.addTask(taskSet1);
		
		
		InitialProperties prop9 = new InitialProperties();
		TaskGroup taskSet3 = new TaskGroup("ts3", prop9);
		
		taskSet3.addTask(taskSet2);
		taskSet3.addTask(sort);
		taskSet3.addTask(csvOut);
		
		ProcessData processData = new ProcessData(); 
		TaskResult result = taskSet3.execute(processData);
	}

}
