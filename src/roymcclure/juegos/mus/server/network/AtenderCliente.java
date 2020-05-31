package roymcclure.juegos.mus.server.network;

import static roymcclure.juegos.mus.common.logic.Language.ConnectionState.*;
import static roymcclure.juegos.mus.common.logic.Language.NodeState.*;
import static roymcclure.juegos.mus.common.logic.Language.PlayerActions.CLOSE_CONNECTION;

import java.io.IOException;
import java.net.Socket;

import roymcclure.juegos.mus.common.logic.jobs.*;
import roymcclure.juegos.mus.common.network.*;
import roymcclure.juegos.mus.server.logic.SrvMus;

/**
 * 
 * @author Roy
 *
 *	span connection threads for a client
 *  
 */
public class AtenderCliente extends Thread {
	
	private Socket socket;
	private Thread readThread;
	private Thread writeThread;
	private ConnectionJobsQueue connectionJobsQueue;
	private ControllerJobsQueue controllerJobsQueue;
	
	byte thread_id=-1;
	private boolean connected = false;

	SrvMus server;

	
	public AtenderCliente(Socket s, byte thread_id, SrvMus server, ControllerJobsQueue controllerJobsQueue, ConnectionJobsQueue connectionJobsQueue) {
		this.socket = s;
		this.thread_id = thread_id;		
		this.connectionJobsQueue = connectionJobsQueue;
		this.controllerJobsQueue = controllerJobsQueue;
		
		ConnectionThread wct = new ConnectionThread(WRITE, SERVER, this.connectionJobsQueue, this.controllerJobsQueue, thread_id);
		//System.out.println("Server created write connection thread");		
		wct.setSocket(this.socket);
		//System.out.println("Server assigned socket to write connection thread");		
		writeThread = new Thread(wct);
		writeThread.setName("ConnectionThread-Write-Client"+thread_id);
		
		ConnectionThread rct = new ConnectionThread(READ, SERVER, this.connectionJobsQueue, this.controllerJobsQueue, thread_id);
		//System.out.println("Server created read connection thread");
		rct.setSocket(this.socket);
		//System.out.println("Server assigned socket to read connection thread");		
		readThread = new Thread(rct);
		readThread.setName("ConnectionThread-Read-Client"+thread_id);

		connected = true;
		this.server = server;
	}
	
	public void run() {
		
		readThread.start();
		writeThread.start();
		
		try {
			//System.out.println("[AtenderCliente] de thread_id: " + thread_id + " se queda esperando a que finalicen sus threads de lectura y escritura...");
			readThread.join();
			System.out.println("AtenderCliente: readThread joined.");
			writeThread.join();
			System.out.println("AtenderCliente: writeThread joined.");			
			//System.out.println("[AtenderCliente] de thread_id: " + thread_id + " threads de lectura y escritura: succesfully joined.");			
		} catch (InterruptedException ie) {
			server.log("Thread was interrupted while waiting for its read & write threads to join.");
			readThread.interrupt();
			writeThread.interrupt();			
		} finally {
			// tell server that user disconnected
			// and to release seat_id*/			
			synchronized(controllerJobsQueue) {
				ClientMessage cm = new ClientMessage(CLOSE_CONNECTION,(byte) 0,"");
				cm.setAction(CLOSE_CONNECTION);
				ConnectionJob job = new ConnectionJob(cm);
				job.setThreadId(thread_id);
				controllerJobsQueue.postRequestJob(new MessageJob(cm));
				controllerJobsQueue.notify();
			}
			System.out.println("AtenderCliente de thread " + thread_id + " terminó su ejecución.");
		}

	}

	public boolean isConnected() {
		return connected;
	}
	
	public void disconnect() {
		try {
			readThread.interrupt();
			writeThread.interrupt();
			socket.close();
			connected = false;			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
