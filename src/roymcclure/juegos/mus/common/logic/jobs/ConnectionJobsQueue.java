package roymcclure.juegos.mus.common.logic.jobs;

import java.util.ArrayList;

public class ConnectionJobsQueue {

	
	private ArrayList<Job> connectionJobs;
	
	public ConnectionJobsQueue() {
		connectionJobs = new ArrayList<Job>();
	}

	public synchronized Job getConnectionJob() {		
		try {
			Job j = connectionJobs.get(0);
			connectionJobs.remove(0);
			return j;
		} catch (IndexOutOfBoundsException e) {
			System.out.println("Se intento obtener un elemento inexistente.");
		}
		return null;
	}

	public synchronized void postConnectionJob(Job j) {
		// System.out.println("A CONNECTION JOB WAS POSTED IN THE JOBS QUEUE");
		connectionJobs.add(j);		
	}

	public synchronized boolean isEmpty() {
		return connectionJobs.isEmpty();
	}

	public int quantity() {
		return connectionJobs.size();
	}
	
}
