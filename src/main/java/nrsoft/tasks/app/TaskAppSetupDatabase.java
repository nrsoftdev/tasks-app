package nrsoft.tasks.app;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;


import org.h2.tools.Server;

import nrsoft.tasks.model.Role;
import nrsoft.tasks.model.TaskDefinition;
import nrsoft.tasks.model.TextConnector;
import nrsoft.tasks.model.User;
import nrsoft.tasks.persistance.TasksDaoJPA;

public class TaskAppSetupDatabase {
	

	
	public static void main(String[] args) {
		
		TasksDaoJPA processDAO = new TasksDaoJPA.Builder()
				.setPersitanceUnitName("processDefinitionDev")
				.setTransactionManual(true)
				.build();
		
		Role role = new Role();
		role.setDescription("ADMIN");
		role.setAdmin(true);
		processDAO.saveRole(role);
		
		User user = new User();
		user.setUserId("ADMIN");
		user.setName("Admin");
		user.setPassword("password");
		user.setCreationUser("ADMIN");
		user.getRoles().add(role);
		processDAO.saveUser(user);
		
		/*
		TextConnector textConnector = new TextConnector();
		textConnector.setCreationUser("ADMIN");
		textConnector.setDescription("Prova");
		textConnector.setName("prova1");
		textConnector.setFilename("c:/temp/pippo.txt");
		processDAO.saveTextConnector(textConnector);
		*/
				
		
	}
	
	

	
	

}
