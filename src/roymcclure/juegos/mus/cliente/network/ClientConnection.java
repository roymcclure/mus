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

	private static java.net.Socket socket;
	private static ConnectionJobsQueue _connectionJobs;
	private static ControllerJobsQueue _controllerJobs;

	private ObjectInputStream objIn = null;
	
	private OutputStream out = null;
	private ObjectOutputStream objOut = null;
	private InputStream in = null;	
	private boolean connected = false;
	
	public ClientConnection(ConnectionJobsQueue connectionJobs, ControllerJobsQueue controllerJobs) {
		_connectionJobs = connectionJobs;
		_controllerJobs = controllerJobs;
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
				// if job defines that a reply is needed, wait for reply
				if (job.isReplyNeeded()) {
					sm = receive();
					sm.printContent();
					synchronized(_controllerJobs) {
						_controllerJobs.postControllerJob(new ServerMessageJob(sm));
						_controllerJobs.notify();
					}
					System.out.println("[ClientConnection] Posted job to controller Jobs\n");
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
		synchronized(_connectionJobs) {
			if (_connectionJobs.isEmpty()) {
				try {
					_connectionJobs.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			job = (ConnectionJob) _connectionJobs.getConnectionJob();
			System.out.println("[ClientConnection] Retrieved a Connection job from the queue.");
		}			
				
		return job;
	}

}
