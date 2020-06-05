package roymcclure.juegos.mus.common.logic.jobs;

import java.util.ArrayList;

public class ControllerJobsQueue {

	private ArrayList<Job> controllerJobs;

	
	public ControllerJobsQueue() {
		controllerJobs = new ArrayList<Job>();
	}
	
	public synchronized Job getControllerJob() {		
		Job j = controllerJobs.get(0);		
		return j;
	}
	
	public synchronized void deleteFirstJob() {
		controllerJobs.remove(0);		
	}

	
	public synchronized void postRequestJob(Job j) {
		//System.out.println("A JOB was posted in the controller jobs queue.");
		controllerJobs.add(j);
	}
	
	public boolean isEmpty() {
		return controllerJobs.isEmpty();
	}

	
}
