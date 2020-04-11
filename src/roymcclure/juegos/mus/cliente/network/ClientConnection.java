package roymcclure.juegos.mus.cliente.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import roymcclure.juegos.mus.cliente.logic.jobs.*;
import roymcclure.juegos.mus.common.network.*;

public class ClientConnection extends Thread {

	private java.net.Socket socket;
	private ConnectionJobsQueue connectionJobs;
	private ControllerJobsQueue controllerJobs;

	private ObjectInputStream objIn = null;
	
	private OutputStream out = null;
	private ObjectOutputStream objOut = null;
	private InputStream in = null;	
	private boolean connected = false;
	// is player ID really an attribute of the connection? feels off but so does in clientGameState

	
	// there has to be some link between client window and connection
	// connection receives the data
	public ClientConnection(ConnectionJobsQueue connectionJobs, ControllerJobsQueue controllerJobs) {
		this.connectionJobs = connectionJobs;
		this.controllerJobs = controllerJobs;
	}

	public int connect(String url, int port) {
		int error = 0; // OK
		try {
			socket = new Socket(url, port);
			connected = true;			
		} catch (UnknownHostException e) {
			// server doesnt respond
			error = 1;
//			e.printStackTrace();
		} catch (ConnectException ce) {
			// ce.printStackTrace();
			error = 2;
		} catch (IOException e) {
			// error in data transference
			error = 3;
			// e.printStackTrace();
		}
		return error;
	}

	@Override
	public void run() {
		ServerMessage sm;
		ConnectionJob job;
		while (connected) {

			try {				
				// fetch job from messageQueue
				job = getJob();								
				// forge petition from job
				send(job.getClientMessage());
				System.out.println("ClientConnection: sent ClientMessage from Job, action:" + job.getClientMessage().getAction());
				// if job defines that a reply is needed, wait for reply
				if (job.isReplyNeeded()) {
					System.out.print("ClientConnection: awaiting reply from server...");					
					sm = receive();
					
					System.out.println("reply received.");
					System.out.println("Reply:");
					sm.printContent();
					synchronized(controllerJobs) {
						controllerJobs.postControllerJob(new ServerMessageJob(sm));
						controllerJobs.notify();
					}
					System.out.println("Posted job to controller Jobs");
				}
				
			}  catch (BrokenConnectionException e) {
				e.printStackTrace();
				connected = false;
			} catch (IOException e) {

				e.printStackTrace();
			} 

		}
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("WTF HERE2?");
		}
	}

	public void send(ClientMessage cm) throws IOException {
		if (out==null && objOut == null) {
			out = socket.getOutputStream();
			objOut = new ObjectOutputStream(out);
		}
		objOut.writeObject(cm);
	}

	public ServerMessage receive() throws BrokenConnectionException {
		ServerMessage sm;

		try {
			if (in==null)
				in = socket.getInputStream();
			//if (in.read() == -1)
			//	throw new BrokenConnectionException();
			if (objIn == null)
				objIn = new ObjectInputStream(in);
			sm = (ServerMessage) objIn.readObject();
			return sm;
		} catch (IOException e) {
			System.out.println("Error: IOException!!");
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			System.out.println("Error: ClassNotFoundException!!");
			e.printStackTrace();			
			return null;
		} catch (ClassCastException e) {
			System.out.println("Error: ClassCastException!!");
			e.printStackTrace();
			return null;
		}
	}
	
	private ConnectionJob getJob() {
		ConnectionJob job;
		synchronized(connectionJobs) {
			if (connectionJobs.isEmpty()) {
				try {
					connectionJobs.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			job = (ConnectionJob) connectionJobs.getConnectionJob();
			System.out.println("ClientConnection: retrieved a Connection job from the queue.");
		}			
				
		return job;
	}

}
