package roymcclure.juegos.mus.common.network;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;


import static roymcclure.juegos.mus.common.logic.Language.ConnectionState.*;
import static roymcclure.juegos.mus.common.logic.Language.NodeState.*;


import roymcclure.juegos.mus.common.logic.jobs.*;

/***
 * This class is responsible of receiving (or sending) a message.
 * It can be server side, or client side.
 * Upon reception, it will post a ControllerJob to the queue.
 * For sending, it will wait on the ConnectionJobs queue in the
 * classical producer-consumer pattern.
 * @author roy
 *
 */
public class ConnectionThread implements Runnable   {

	private boolean running = false;
	private byte operation;
	private byte side;

	private ObjectOutputStream objOut = null;
	private OutputStream out = null;	
	private ObjectInputStream objIn = null;
	private InputStream in = null;	
	private ConnectionJobsQueue _connectionJobs;
	private ControllerJobsQueue _controllerJobs;
	private byte thread_id; // identifies the client thread associated with this Connection thread.
	
	public ConnectionThread(byte operation, byte side, ConnectionJobsQueue connectionJobsQueue, ControllerJobsQueue controllerJobsQueue, byte thread_id) {
		this.operation = operation;
		this.side = side;
		this.thread_id = thread_id;
		_connectionJobs = connectionJobsQueue;
		_controllerJobs = controllerJobsQueue;
	}

	private void identify() {
		System.out.print("ConnectionThread" + (this.side == SERVER? "[SERVER]" : "[CLIENT]") + (this.operation == READ ? "READ" : "WRITE"));
	}
	
	@Override
	public void run() {

		running = true;
		// System.out.println("Connection thread [" + (this.operation == READ? "READ": "WRITE") + "]: running");		
		while(running) {

			try {
				switch(this.operation) {
				case READ:
					read();
					break;
				case WRITE:
					write();
					break;
					default:
						throw new Exception("Undefined mode for ConnectionThread");
				}
			
			}catch (Exception e) {
				identify();
				System.out.println(" --> EXCEPTION CAUGHT");
				if (e instanceof EOFException || e instanceof SocketException) {
					// exception en el thread de lectura del servidor.
					// the write thread is still reading though.
					// we need to notify the connectionJobsQueue so the write thread can finish as well
					synchronized(_connectionJobs) {
						_connectionJobs.notify();
					}
				}
				e.printStackTrace();
				
				running = false;				
			}
		}

		System.out.println("Exiting");	

	}
	
	private void write() throws IOException {
		//System.out.println("[ConnectionThread:Write] blocking on getJob()");
		ConnectionJob job;
		// fetch job from messageQueue
		job = getJob();
		//System.out.println("[ConnectionThread:Write] got a job, sending...)");		
		// forge petition from job
		if (job!=null)
			if (this.side == SERVER) {			
				identify();
				System.out.print("SENDING SERVER MESSAGE...");
				send(job.getServerMessage());
				out.flush();
				System.out.println("Message sent.");				
			}
			else if (this.side == CLIENT) {
				send(job.getClientMessage());
				out.flush();			
			}
		
	}

	private void read() throws IOException, ClassNotFoundException{
		if (this.side == SERVER) {
			// read client message
			ClientMessage cm;
			//System.out.println("SERVER CONNECTION READ THREAD: waiting for message...");
			cm = receiveFromClient();
			//System.out.println("MESSAGE RECEIVED. POSTING TO SERVER CONTROLLER JOBS....");
			System.out.println(cm);
			synchronized(_controllerJobs) {
				MessageJob job = new MessageJob(cm);
				job.setThreadId(this.thread_id);
				_controllerJobs.postRequestJob(job);
				_controllerJobs.notify();
			}
			
		}
		else if (this.side == CLIENT) {
			// read server message
			ServerMessage sm;
			//System.out.println("CLIENT CONNECTION READ THREAD: waiting for message...");			
			sm = receiveFromServer();
			identify();
			System.out.println("[CONNECTION THREAD] MESSAGE RECEIVED. POSTING TO CLIENT CONTROLLER JOBS....");			
			//sm.printContent();
			synchronized(_controllerJobs) {
				
				_controllerJobs.postRequestJob(new MessageJob(sm));
				identify();
				System.out.print("[CONNECTION THREAD] message posted. notifying for the client controller...");
				_controllerJobs.notify();
				System.out.println("notified.");
			}
			
		}
		//System.out.println("Posted job to controller Jobs\n");


		
	}

	public void setSocket(Socket socket) {
		//assert(socket!=null);		
		try {
			switch(this.operation) {
			case WRITE:
				System.out.println("setting socket for write");
				out = socket.getOutputStream();
				objOut = new ObjectOutputStream(out);				
				break;
			case READ:
				System.out.print("setting socket for read..");				
				in = socket.getInputStream();
				System.out.println("Got input stream.");
				objIn = new ObjectInputStream(in);
				System.out.println("created ObjectInputStream");
				break;
			}			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void stop() {
		running = false;
	}
	
	public ClientMessage receiveFromClient() throws IOException, ClassNotFoundException {
		ClientMessage cm = (ClientMessage) objIn.readObject();
		return cm;
	}
	
	public ServerMessage receiveFromServer() throws IOException, ClassNotFoundException {
		ServerMessage sm = (ServerMessage) objIn.readObject();
		return sm;
	}
	
	public void send(Object message) throws IOException {
		// TODO: transform message into a bit word, write those bytes to the server
		objOut.writeObject(message);
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
			// we might have been notified because the read thread threw an exception
			// upon an ungraceful client disconnection.
			// so the write thread (the one running this method) bypasses the block
			// by notifying the _connectionJobs 
			// if the connectionJobsQueue is empty it means we were notified
			// by the read thread
			if (_connectionJobs.quantity()==0) {
				running = false;
				job = null;
			}
			else {
				job = (ConnectionJob) _connectionJobs.getConnectionJob();
			}
			//System.out.println("[ConnectionThread:Write] Retrieved a Connection job from the queue.");
		}			

		// cuando llega un mensaje, podemos notificar al controller
		// es básicamente lo que hacemos ahora con getJob()->processJob()->messageReceived() | clickReceived()
		
		return job;
	}

	public class OperationNotSupportedException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3274684805971075626L;}
		
	
	
}
