package nrsoft.tasks.app.sample;

import nrsoft.tasks.ProcessData;
import nrsoft.tasks.Task;
import nrsoft.tasks.TaskResult;
import nrsoft.tasks.runtime.TaskProviderSpringClasspath;

public class ProcessRunnerSpringCmd {
	
	public static void main(String[] args) {
		
		String springFile = args[0];
		String taskName = args[1];
		
		TaskProviderSpringClasspath provider = new TaskProviderSpringClasspath(springFile, taskName);
		
		Task task = provider.load();
		
		ProcessData dataIn = null;
		TaskResult result = task.execute(dataIn );
		
		if(result.isError())
			System.out.println(result.getMessage());
		
		provider.afterExecute();
	}

}
