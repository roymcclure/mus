package roymcclure.juegos.mus.server.logic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Random;

import javax.swing.JTextArea;

import roymcclure.juegos.mus.common.logic.GameState;
import roymcclure.juegos.mus.common.logic.Language;
import static roymcclure.juegos.mus.common.logic.Language.ServerGameState.*;
import static roymcclure.juegos.mus.common.logic.Language.GamePhase.*;
import roymcclure.juegos.mus.common.logic.TableState;
import roymcclure.juegos.mus.common.logic.jobs.*;
import roymcclure.juegos.mus.server.UI.ServerWindow;
import roymcclure.juegos.mus.server.network.AtenderCliente;

/***
 * 
 * @author roy
 *
 * This class performs (and controls) modifications to the model
 * from the server perspective.
 * manages the global server state
 * state machine.
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

	private GameState gameState;
	private TableState tableState;
	private ControllerJobsQueue controllerJobsQueue;
	private ConnectionJobsQueue[] connectionJobs;	
	private ServerController serverController;
	private Object key;
	
	public SrvMus (ServerWindow serverWindow) {
		this.serverWindow = serverWindow;
		gameState = new GameState();
		tableState = new TableState();
		controllerJobsQueue = new ControllerJobsQueue();
		connectionJobs = new ConnectionJobsQueue[MAX_CLIENTS];
		for (int i = 0; i < MAX_CLIENTS; i++) {
			connectionJobs[i] = new ConnectionJobsQueue();
		}
		key = new Object();
		this.serverController = new ServerController(controllerJobsQueue, connectionJobs, gameState, tableState, this, key);	
		Thread controllerThread = new Thread(serverController);
		controllerThread.setName("Thread-ServerController");
		controllerThread.start();
		resetThreads();

	}


	
	private static void resetThreads() {
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

	public void run() {
		try {
			running = true;
			log("Resetting client array...");			
			log("reset.");
			log("Starting server in port " + port + "...");			
			socket = new ServerSocket(port);
			log("started.\n");			

			while (running) {
				switch(gameState.getGameState()) {

				case WAITING_ALL_PLAYERS_TO_CONNECT:
					//System.out.println("Baraja ANTES de waitAllConnected()..");
					//tableState.getBaraja().print();
					this.waitAllConnected();
					//System.out.println("Baraja DESPUES de waitAllConnected()..");
					//tableState.getBaraja().print();
					break;
				
				case WAITING_ALL_PLAYERS_TO_SEAT: {
					this.waitingAllSeated();
					// System.out.println("Baraja despues de waitAllConnected()..");
					//tableState.getBaraja().print();					
				}
				break;

				case DEALING:
 					this.dealing();
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

	private void waitAllConnected() throws IOException {
		while (clientsConnected() < MAX_CLIENTS) {
			//	Por cada cliente se lanza un hilo que le atenderá
			byte thread_id = getFreeThreadID(); 
			hilos[thread_id] =
					new AtenderCliente(socket.accept(), thread_id, this, controllerJobsQueue, connectionJobs[thread_id]);						
			log("Client connected.\n");
			hilos[thread_id].start();

		}
		log("All connected. Changing state to WAITING_ALL_PLAYERS_TO_SEAT.");
		// actualizamos el estado de juego a playing
		gameState.setGameState(Language.ServerGameState.WAITING_ALL_PLAYERS_TO_SEAT);

		
	}

	private void waitingAllSeated() throws IOException {
		while(!tableState.allSeated()) {
			try {
				log("WAITING FOR A PLAYER TO BE SEATED");
				synchronized(key) {
					key.wait();
				}
				log("Me informa el controller que alguien se ha sentado!");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		gameState.setGameState(Language.ServerGameState.DEALING);		
	}

	private void dealing() {
		while(tableState.getTipo_Lance()!=GRANDE) {
			try {
				log("WAITING FOR THE CONTROLLER TO TELL ME THAT DEALING WAS FINISHED");
				synchronized(key) {
					key.wait();
				}
				log("[controller]: ronda descartes finalizada");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//System.out.println("DESPUES DE REPARTIR; LAS CARTAS SON LAS SIGUIENTES:");
		//tableState.printContent();
		gameState.setGameState(PLAYING);
		log("Finished dealing cards. Informing clients.");
		// if player in turn == -1 means beginning of game, assign one randomly				
	}	
	
	private void playing() {

		// me bloqueo esperando a que controller me diga
		// que el juego ha terminado
		byte ronda = tableState.getId_ronda();
		try {

			log("WAITING FOR ROUND [" + tableState.getId_ronda() + "] TO FINISH, OR MUS");
			synchronized(key) {
				key.wait();
			}
			log("Me informa el controller para que compruebe si acabo la ronda o si nos damos otra mano de mus.");
			if (tableState.getId_ronda()==ronda) {
				gameState.setGameState(DEALING);
			}
			else {
				gameState.setGameState(END_OF_ROUND);
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		
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
		if (hilos[thread_id] != null) {
			hilos[thread_id].disconnect();
			hilos[thread_id] = null;
		}
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
		// log("HALT CALLED ON SERVER");
		running = false;
		for (int i = 0; i<MAX_CLIENTS;i++ ) {
			try {
				// ac.sendCloseMsg();
				if (hilos[i]!=null) {
					log("Attempting to kill client " + i + "\n");
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
			log("INVALID COMMAND\n");
		}
	}

	private void defineCommand(String[] tokens, JTextArea txtLog) {
		if (tokens.length == 1) {
			log("stones_to_game [" + gameState.getPiedras_juego() + "]");
			log("games_to_cow: " + gameState.getJuegos_vaca());
			log("cows_to_match: " + gameState.getVacas_partida());			
		}
		if (tokens[1].toLowerCase().equals("port")) {
			if (running) {
				this.port =Integer.parseInt(tokens[2]); 
				log("New port defined: " + this.port);
			} else log("ERROR: cannot change port while running.");
		}
	}

	private void statusCommand(String[] tokens, JTextArea txtLog) {
		if (tokens.length == 1) {
			log("========SERVER=STATUS:\n");
			log("SERVER: " + (running ? "ONLINE" :  "OFFLINE"));
			log("Clients connected: " + clientsConnected());
			for (int i = 0; i < MAX_CLIENTS; i++) {
				log("ID in thread " + i + ":"+gameState.getPlayerID(i));
			}
		} else 	if (tokens[1].toLowerCase().contentEquals("client")) {
			// show state for that client
			try {
				log("Player ID [" + tableState.getClient(Integer.parseInt(tokens[2])).getID() +"]");
			} catch (Exception e) {
				log("Usage: status client [thread_id]");
			}
		} else 	if (tokens[1].toLowerCase().contentEquals("table")) {
			log("========TABLE=STATUS:\n");			
			for (int i = 0; i < MAX_CLIENTS; i++) {
				log( "[seat_id "+i+"]" + "Player ID [" + tableState.getClient(i).getID() +"]");				
			}
		}
	}	

	public void log(String text) {
		serverWindow.log(text);
	}
	
	private class NotImplementedException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;}

}
