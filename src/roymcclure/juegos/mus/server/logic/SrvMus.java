package roymcclure.juegos.mus.server.logic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.JTextArea;

import roymcclure.juegos.mus.common.logic.GameState;
import roymcclure.juegos.mus.common.logic.Language;
import static roymcclure.juegos.mus.common.logic.Language.ServerGameState.*;
import roymcclure.juegos.mus.common.logic.TableState;
import roymcclure.juegos.mus.common.logic.jobs.*;
import roymcclure.juegos.mus.server.UI.ServerWindow;
import roymcclure.juegos.mus.server.network.AtenderCliente;

/***
 * 
 * @author roy
 *
 * oversees the global server state
 * 
 *
 */
public class SrvMus extends Thread {

	private ServerWindow serverWindow;

	private ServerSocket socket;
	private int port = 5678;
	private static AtenderCliente[] hilos;
	public static int MAX_CLIENTS = 4;
	private boolean running = false;

	private byte serverState = WAITING_ALL_PLAYERS_TO_SEAT;
	private GameState gameState;
	private TableState tableState;
	private ControllerJobsQueue controllerJobsQueue;
	private ConnectionJobsQueue[] connectionJobs;	
	private ServerController serverController;
	
	public SrvMus (ServerWindow serverWindow) {
		this.serverWindow = serverWindow;
		gameState = new GameState();
		tableState = new TableState();
		controllerJobsQueue = new ControllerJobsQueue();
		connectionJobs = new ConnectionJobsQueue[MAX_CLIENTS];
		for (int i = 0; i < MAX_CLIENTS; i++) {
			connectionJobs[i] = new ConnectionJobsQueue();
		}
		this.serverController = new ServerController(controllerJobsQueue, connectionJobs, gameState, tableState, this);	
		Thread controllerThread = new Thread(serverController);
		controllerThread.setName("Thread-Controller");
		controllerThread.start();
		initThreads();
	}

	private static void initThreads() {
		hilos = new AtenderCliente[MAX_CLIENTS];
		for (int i=0; i<MAX_CLIENTS;i++) {
			hilos[i] = null;
		}
	}

	// How many threads are running
	private static int clientsConnected() {
		int n= 0;
		for (int i=0; i<MAX_CLIENTS;i++) {
			if (hilos[i] != null)
				if (hilos[i].isConnected()) {
					n++;
				}
		}		
		return n;
	}

	public void onStateModified() {
		// tell all threads so they can update their clients
		for (int i = 0; i < MAX_CLIENTS; i++) {
			if (hilos[i] != null) {
				hilos[i].notifyStateChange();
			}
		}
	}

	public void run() {
		try {
			running = true;
			serverWindow.log("Resetting client array...");			
			serverWindow.log("reset.\n");
			serverWindow.log("Starting server in port " + port + "...\n");			
			socket = new ServerSocket(port);
			serverWindow.log("started.\n");			

			while (running) {
				serverWindow.log("Waiting for all players to be seated...\n");

				switch(serverState) {

				case WAITING_ALL_PLAYERS_TO_SEAT: {
					this.waitingAllPlayers();
				}
				break;

				case PLAYING: {
					this.playing();					
				}
				break;
				case END_OF_ROUND: {
					this.endOfRound();
				}

				break;
				case GAME_FINISHED:{
					this.gameFinished();
				}
				break;
				}			

			}

		} catch (SocketException e) {

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalThreadStateException i) {
			try {
				this.halt();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			// error barajando cartas
		}

	}

	private void waitingAllPlayers() throws IOException {
			while (clientsConnected() < MAX_CLIENTS) {

				//	Por cada cliente se lanza un hilo que le atenderá
				serverWindow.log("---Waiting for client connection...\n");
				byte thread_id = getFreeThreadID(); 

				hilos[thread_id] =
						new AtenderCliente(socket.accept(), gameState, tableState, thread_id, this, controllerJobsQueue, connectionJobs[thread_id]);						


				serverWindow.log("Client connected.\n");
				hilos[thread_id].start();

			}
			serverWindow.log("all seated. Starting game:\n");
			serverWindow.log("Starting game.\n");
			// actualizamos el estado de juego a playing
			gameState.setGameState(Language.ServerGameState.PLAYING);
	}

	private void playing() throws NotImplementedException {
		throw new NotImplementedException();		
	}

	private void endOfRound() throws NotImplementedException {
		if (running)
			throw new NotImplementedException();
	}

	private void gameFinished() throws NotImplementedException {
		if (running)
			throw new NotImplementedException();		
	}	

	public void releaseThread(byte thread_id) {
		// System.out.println("releaseThread called on thread id:"+thread_id);
		hilos[thread_id] = null;
	}

	private byte getFreeThreadID() {
		for (byte i=0; i<MAX_CLIENTS;i++) {
			if (hilos[i]==null) {
				return i;
			} else if(!hilos[i].isConnected()){
				return i;
			}
		}
		return -1;
	}

	public void halt() throws IOException {
		// enviar mensaje a todos los clientes que el servidor se cierra
		// serverWindow.log("HALT CALLED ON SERVER");
		running = false;
		for (int i = 0; i<MAX_CLIENTS;i++ ) {
			try {
				// ac.sendCloseMsg();
				if (hilos[i]!=null) {
					serverWindow.log("Attempting to kill client " + i + "\n");
					hilos[i].interrupt();
					hilos[i].disconnect();
					hilos[i].join();
					hilos[i] = null;

				}
				socket.close();				
			} catch (InterruptedException e) {

				e.printStackTrace();
			} catch (Exception e) {

			}
		}
		//	cerrar el socket
		this.interrupt();


	}

	// accepts commands via server window
	public void runCommand(String cmd, JTextArea txtLog) {
		String[] tokens = cmd.split(" ");
		try {
			if (tokens[0].toLowerCase().equals("define")) {
				defineCommand(tokens, txtLog);				
			}
			if (tokens[0].toLowerCase().equals("status")) {
				statusCommand(tokens, txtLog);
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			txtLog.append("INVALID COMMAND\n");
		}
	}

	private void defineCommand(String[] tokens, JTextArea txtLog) {
		if (tokens.length == 1) {
			txtLog.append("stones_to_game [" + gameState.getPiedras_juego() + "]\n");
			txtLog.append("games_to_cow: " + gameState.getJuegos_vaca() + "\n");
			txtLog.append("cows_to_match: " + gameState.getVacas_partida() + "\n");			
		}
		if (tokens[1].toLowerCase().equals("port")) {
			if (running) {
				this.port =Integer.parseInt(tokens[2]); 
				txtLog.append("New port defined: " + this.port + "\n");
			} else txtLog.append("ERROR: cannot change port while running.\n");
		}
	}

	private void statusCommand(String[] tokens, JTextArea txtLog) {
		if (tokens.length == 1) {
			txtLog.append("========SERVER=STATUS:\n");
			txtLog.append("SERVER: " + (running ? "ONLINE" :  "OFFLINE") + "\n");
			txtLog.append("Clients connected: " + clientsConnected() + "\n");
			for (int i = 0; i < MAX_CLIENTS; i++) {
				txtLog.append("ID in thread " + i + ":"+gameState.getPlayerID(i)+"\n");
			}
		} else 	if (tokens[1].toLowerCase().contentEquals("client")) {
			// show state for that client
			try {
				txtLog.append("Player ID [" + tableState.getClient(Integer.parseInt(tokens[2])).getID() +"]\n");
			} catch (Exception e) {
				txtLog.append("Usage: status client [thread_id]\n");
			}
		} else 	if (tokens[1].toLowerCase().contentEquals("table")) {
			txtLog.append("========TABLE=STATUS:\n");			
			for (int i = 0; i < MAX_CLIENTS; i++) {
				txtLog.append( "[seat_id "+i+"]" + "Player ID [" + tableState.getClient(i).getID() +"]\n");				
			}
		}
	}	

	public void log(String text) {
		serverWindow.log(text + "\n");
	}
	
	private class NotImplementedException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;}

}
