package nrsoft.tasks.app.sample;

import java.util.LinkedList;
import nrsoft.tasks.metadata.*;
import java.util.List;
import nrsoft.tasks.InitialProperties;
import nrsoft.tasks.ProcessData;
import nrsoft.tasks.SortTask;
import nrsoft.tasks.Task;
import nrsoft.tasks.TaskResult;
import nrsoft.tasks.TaskGroup;
import nrsoft.tasks.file.FileTask;
import nrsoft.tasks.file.FileTaskCsv;
import nrsoft.tasks.file.FileTaskCsvOut;
import nrsoft.tasks.runtime.Process;
import nrsoft.tasks.sql.SqlTask;
import nrsoft.tasks.sql.SqlTaskSelect;

public class SampleApp2 {

	public static void main(String[] args) {
		
		
		InitialProperties taskSetProp = new InitialProperties();
		TaskGroup taskSet = new TaskGroup("taskSet1", taskSetProp);
		
		InitialProperties initProp = new InitialProperties();
		
		initProp.addProperty(SqlTaskMd.PROP_SQL_CONNECTION_URL, "jdbc:sqlserver://100GW070\\SVI;databaseName=M3FDBSVI");
		initProp.addProperty(SqlTaskMd.PROP_SQL_CONNECTION_USER, "MDBADM");
		initProp.addProperty(SqlTaskMd.PROP_SQL_CONNECTION_PASSWORD, "M3passw0rd");
		initProp.addProperty(SqlTaskMd.PROP_SQL_DRIVER, "com.microsoft.sqlserver.jdbc.SQLServerDriver");
		initProp.addProperty(SqlTaskSelectMd.PROP_SQL_SELECT_QUERY, "SELECT * FROM MVXJDTA.OCUSMA");
		
		Task selectOCUSMA = new SqlTaskSelect("dbReader", initProp );
		
		
		
		InitialProperties initProp2 = new InitialProperties();
		initProp2.addProperty(SortTaskMd.PROP_NAME_SORTFIELDS, "OKCUA1,OKCUA2,OKCUA3,OKCUA4");
		Task sort = new SortTask("Sort", initProp2  );

		

		
		
		
		InitialProperties initProp3 = new InitialProperties();
		initProp3.addProperty(FileTaskMd.PROP_FILE_NAME, "c:/temp/ocusma.csv");
		initProp3.addProperty(FileTaskCsvMd.PROP_FILE_CSV_HEADER, "true");
		initProp3.addProperty(FileTaskCsvMd.PROP_FILE_CSV_SEP, ";");
		Task csvOut = new FileTaskCsvOut("OutCsv", initProp3  );
		
		
		
		
		List<Task> taskSetList = new LinkedList<>();
		taskSetList.add(selectOCUSMA);
		taskSetList.add(sort);
		taskSetList.add(csvOut);
		taskSet.setTasks(taskSetList);

		
		Process process = Process.create("ADMIN", taskSet);
		process.run();
		
		
		TaskResult result = process.getResult();
		
		System.out.println( String.format("Errore: %b, %s", result.isError(), result.getMessage()));
		

	}

}
