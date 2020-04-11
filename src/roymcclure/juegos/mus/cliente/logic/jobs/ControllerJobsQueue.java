package roymcclure.juegos.mus.cliente.logic.jobs;

import java.util.ArrayList;

public class ControllerJobsQueue {

	private ArrayList<Job> controllerJobs;

	
	public ControllerJobsQueue() {
		controllerJobs = new ArrayList<Job>();
	}
	
	public synchronized Job getControllerJob() {		
		Job j = controllerJobs.get(0);
		controllerJobs.remove(0);
		return j;
	}
	
	public synchronized void postControllerJob(Job j) {
		controllerJobs.add(j);
	}
	
	public boolean isEmpty() {
		return controllerJobs.isEmpty();
	}

	
}
