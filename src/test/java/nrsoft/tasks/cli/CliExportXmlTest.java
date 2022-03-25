package nrsoft.tasks.cli;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;
import org.xml.sax.InputSource;

import nrsoft.tasks.def.TaskDefinition;
import nrsoft.tasks.metadata.FileTaskCsvInMd;
import nrsoft.tasks.metadata.FileTaskCsvMd;
import nrsoft.tasks.metadata.FileTaskMd;
import nrsoft.tasks.metadata.IdleTaskMd;
import nrsoft.tasks.metadata.SortTaskMd;
import nrsoft.tasks.metadata.SqlTaskMd;
import nrsoft.tasks.metadata.SqlTaskSelectMd;
import nrsoft.tasks.metadata.TaskMd;
import nrsoft.tasks.metadata.TaskGroupExecutionType;
import nrsoft.tasks.metadata.TaskGroupMd;

public class CliExportXmlTest {
	
	
	@Test
	public void buildXml() throws JAXBException, IOException {
		JAXBContext jaxbContext     = JAXBContext.newInstance( TaskDefinition.class );
		Marshaller jaxbMarshaller   = jaxbContext.createMarshaller();
		
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		/*
		jaxbMarshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,
				"http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd " + 
				"http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd");
		*/
		
		TaskDefinition def = createComplexTaskDef(); // createSimpleTaskDef();
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		jaxbMarshaller.marshal( def, os );
		
		String xml = os.toString(StandardCharsets.UTF_8.displayName());
		os.close();
		
		FileWriter fw = new FileWriter("c:/temp/java/tasfdef.xml");
		fw.write(xml);
		fw.close();
		
		FileReader fr = new FileReader("c:/temp/java/tasfdef.xml");
		xml = "";
		try (BufferedReader br = new BufferedReader(fr)) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       xml += line;
		    }
		}
		fr.close();
		
		
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		
		InputSource source = new InputSource(new StringReader(xml));
		
		TaskDefinition def2 = (TaskDefinition)		unmarshaller.unmarshal(source);
		
		System.out.println(def2.getClassName());
		

	}

	private TaskDefinition createSimpleTaskDef() {
		TaskDefinition def = new TaskDefinition();
		def.setName("prova");
		def.setClassName("prova2");
		
		Map<String, String> properties = new HashMap<>();
		properties.put("key1", "value1");
		properties.put("key2", "value2");
		def.setProperties(properties );
		
		TaskDefinition child = new TaskDefinition();
		child.setName("child1");
		
		def.getChildren().add(child);
		return def;
	}
	
/**
	            *
	            |
    +-----------+------------+
    |           |            |
+------+    +------+      +------+
| Sql1 |    | Sql2 |      | Csv1 |--+
+------+    +------+      +------+  |
    |           |            |	   ts1
    |           |            |      | 
    |           |         +------+  |
    |           |         | Idl1 |--+ 
    |           |         +------+
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

	private TaskDefinition createComplexTaskDef() {
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
		prop3.put(FileTaskMd.PROP_FILE_NAME, "c:\\TEMP\\pippo.csv");
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
		LinkedList<TaskDefinition> child1 = ArraysAsList(new TaskDefinition[] { csv1,idl1  });
		TaskDefinition taskSet1 = new TaskDefinition("ts1", "nrsoft.tasks.TaskGroup", prop7, child1);
		
		
		java.util.Map<String,String> prop8 = new HashMap<String,String>();
		prop8.put(TaskGroupMd.PROP_TASKGROUP_EXECUTION, TaskGroupExecutionType.PARALLEL.toString());
		LinkedList<TaskDefinition> child2 = ArraysAsList(new TaskDefinition[] { sql1, sql2, taskSet1 });
		TaskDefinition taskSet2 = new TaskDefinition("ts2", "nrsoft.tasks.TaskGroup", prop8, child2);
		
		java.util.Map<String,String> prop9 = new HashMap<String,String>();
		LinkedList<TaskDefinition> child3 = ArraysAsList(new TaskDefinition[] { taskSet2, sort, csvOut });
		TaskDefinition taskSet3 = new TaskDefinition("ts3", "nrsoft.tasks.TaskGroup", prop9, child3);
		
		return taskSet3;
		
	}

	private LinkedList<TaskDefinition> ArraysAsList(TaskDefinition[] taskDefinitions) {

		LinkedList<TaskDefinition> list = new LinkedList<>();
		for(TaskDefinition td: taskDefinitions) {
			list.add(td);
		}
		return list;
	}


}

