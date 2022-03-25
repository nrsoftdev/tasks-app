package nrsoft.tasks.app.sample;

import java.io.IOException;

import nrsoft.tasks.model.ChildObject;
import nrsoft.tasks.model.ParentObject;
import nrsoft.tasks.persistance.TasksDaoJPA;

public class TaskAppCmd2 {
	
	

	

	/*
	 * --user=ADMIN PROCESS IMPORT Proc1 Descr1 step1 C:\TEMP\test1.xml
	 * --user=ADMIN PROCESS IMPORT Proc2 Descr2 step2 C:\TEMP\test2.xml
	 * --user=ADMIN PROCESS EXPORT 4 C:\TEMP\test2out.xml
	 * --user=ADMIN PROCESS RUN 4
	 */
	public static void main(String[] args) {
		
		// NO SURE IF THIS STILL WORKS
		TasksDaoJPA processDAO = new TasksDaoJPA.Builder().build();
		
		
		try {
			
			ParentObject parent1 = new ParentObject();
			//parent1.setId("prova1");
			
			ChildObject child11 = new ChildObject();
			child11.setParent(parent1);
			ChildObject.Pk pk11 = new ChildObject.Pk();
			pk11.setName("prop1");
			child11.setPk(pk11);
			parent1.getAttrs().add(child11);
			
			ChildObject child12 = new ChildObject();
			child12.setParent(parent1);
			ChildObject.Pk pk12 = new ChildObject.Pk();
			pk12.setName("prop2");
			child12.setPk(pk12);
			parent1.getAttrs().add(child12);
			
			ParentObject parent2 = new ParentObject();
			//parent2.setId("prova2");

			
			processDAO.getEntityManager().getTransaction().begin();
			processDAO.getEntityManager().persist(parent1);
			processDAO.getEntityManager().persist(parent2);
			processDAO.getEntityManager().getTransaction().commit();
	
			

		} catch (Exception e) {
			if(processDAO.getEntityManager().getTransaction().isActive())
				processDAO.getEntityManager().getTransaction().rollback();
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


}

