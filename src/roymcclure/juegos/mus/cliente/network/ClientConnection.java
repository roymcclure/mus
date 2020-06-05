package roymcclure.juegos.mus.cliente.network;

import java.io.IOException;
import java.net.Socket;

import roymcclure.juegos.mus.cliente.UI.ClientWindow;
import roymcclure.juegos.mus.common.logic.jobs.*;
import roymcclure.juegos.mus.common.network.*;
import static roymcclure.juegos.mus.common.logic.Language.ConnectionState.*;
import static roymcclure.juegos.mus.common.logic.Language.NodeState.*;
import static roymcclure.juegos.mus.common.logic.Language.PlayerActions.*;

public class ClientConnection implements Runnable {

	private static java.net.Socket _socket;
	private static ConnectionJobsQueue _connectionJobs;
	private static ControllerJobsQueue _controllerJobs;
	private final byte IRRELEVANT = 0; 

	private static boolean connected = false;

	private static Thread threadLectura;
	private static Thread threadEscritura;	

	public static void setConnectionJobsQueue(ConnectionJobsQueue queue) {
		_connectionJobs = queue;
	}

	public static void setControllerJobsQueue(ControllerJobsQueue queue) {
		_controllerJobs = queue;
	}

	public static boolean isConnected() {
		return connected;
	}

	public static void connect(String url, int port) throws IOException {
		_socket = new Socket(url, port);
		System.out.println("[ClientConnection] has connected!");
		connected = true;			
	}

	@Override
	public void run()  {
		if (connected) {
			// thread_id is important only to the server connection threads, so its not relevant here				
			
			System.out.print("Creating write connection thread for client...");
			ConnectionThread ctw = new ConnectionThread(WRITE, CLIENT, _connectionJobs, _controllerJobs,IRRELEVANT);
			ctw.setSocket(_socket);
			threadEscritura = new Thread(ctw);
			System.out.println("done.");
			
			System.out.print("Creating read connection thread for client...");
			ConnectionThread ctl = new ConnectionThread(READ, CLIENT, _connectionJobs, _controllerJobs,IRRELEVANT);
			System.out.println("object created. Setting socket...");
			ctl.setSocket(_socket);
			System.out.print("socket set. Creating thread...");
			threadLectura = new Thread(ctl);
			System.out.println("created.");
			System.out.println("done. Staring client write & read threads.");


			threadLectura.start();
			threadEscritura.start();
			//System.out.println("[ClientConnection] read and write threads have started.");
			
			synchronized(ClientWindow.semaphore) {
				ClientWindow.semaphore.notify();
			}
			
			try {
				threadLectura.join();
				threadEscritura.join();	
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

	}

	public static void stop() {
		connected = false;
		try {
			ClientMessage cm = new ClientMessage(CLOSE_CONNECTION, (byte) 0, "");
			ConnectionJob job = new ConnectionJob(cm);
			synchronized(_connectionJobs) {
				_connectionJobs.postConnectionJob(job);
				_connectionJobs.notify();
			}
			_socket.getInputStream().close();
			_socket.getOutputStream().close();
			_socket.close();
		} catch (IOException ex) {
			
		}
		threadLectura.interrupt();
		System.out.println("[CLIENTE] Interrumpido thread Lectura");
		threadEscritura.interrupt();
		System.out.println("[CLIENTE] Interrumpido thread Escritura");		
	}
	
	public static void abort() {
		threadEscritura.interrupt();
		threadLectura.interrupt();
	}


}
