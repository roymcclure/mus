package roymcclure.juegos.mus.cliente.logic.jobs;

import java.util.ArrayList;

public class ConnectionJobsQueue {

	
	private ArrayList<Job> connectionJobs;
	
	public ConnectionJobsQueue() {
		connectionJobs = new ArrayList<Job>();
	}
	
	public synchronized Job getConnectionJob() {		
		Job j = connectionJobs.get(0);
		connectionJobs.remove(0);
		return j;
	}
	
	public synchronized void postConnectionJob(Job j) {
		connectionJobs.add(j);
	}
	
	public synchronized boolean isEmpty() {
		return connectionJobs.isEmpty();
	}
	
}
