package roymcclure.juegos.mus.common.logic.jobs;

import java.util.ArrayList;

public class ConnectionJobsQueue {

	
	private ArrayList<Job> connectionJobs;
	
	public ConnectionJobsQueue() {
		connectionJobs = new ArrayList<Job>();
	}
	
	public synchronized Job getConnectionJob() {		
		assert(connectionJobs.size() > 0);
		Job j = connectionJobs.get(0);
		connectionJobs.remove(0);
		return j;
	}
	
	public synchronized void postConnectionJob(Job j) {
		System.out.println("A CONNECTION JOB WAS POSTED IN THE JOBS QUEUE");
		connectionJobs.add(j);		
	}
	
	public synchronized boolean isEmpty() {
		return connectionJobs.isEmpty();
	}
	
}
