package roymcclure.juegos.mus.server.logic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;

import javax.swing.JTextArea;

import roymcclure.juegos.mus.common.logic.GameState;
import roymcclure.juegos.mus.common.logic.Language;
import roymcclure.juegos.mus.common.logic.Language.ServerGameState;

import static roymcclure.juegos.mus.common.logic.Language.ServerGameState.*;
import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.CARDS_PER_HAND;
import static roymcclure.juegos.mus.common.logic.Language.GamePhase.*;
import roymcclure.juegos.mus.common.logic.TableState;
import roymcclure.juegos.mus.common.logic.cards.Carta;
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
	private Object semaphore;
	public static boolean disconnection = false;

	private ArrayList<TableState> history;

	private boolean historyAdded = false;

	public SrvMus (ServerWindow serverWindow) {
		this.serverWindow = serverWindow;
		gameState = new GameState();
		tableState = new TableState();
		controllerJobsQueue = new ControllerJobsQueue();
		connectionJobs = new ConnectionJobsQueue[MAX_CLIENTS];
		for (int i = 0; i < MAX_CLIENTS; i++) {
			connectionJobs[i] = new ConnectionJobsQueue();
		}
		semaphore = new Object();
		resetThreads();		
		this.serverController = new ServerController(controllerJobsQueue, connectionJobs, gameState, tableState, this, semaphore, hilos);	
		Thread controllerThread = new Thread(serverController);
		controllerThread.setName("Thread-ServerController");
		controllerThread.start();


		history = new ArrayList<TableState>();

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
				System.out.println("[SRVMUS] entering while");
				switch(gameState.getServerGameState()) {

				case ServerGameState.WAITING_ALL_PLAYERS_TO_CONNECT:
					//System.out.println("Baraja ANTES de waitAllConnected()..");
					//tableState.getBaraja().print();
					this.waitAllConnected();
					//System.out.println("Baraja DESPUES de waitAllConnected()..");
					//tableState.getBaraja().print();
					break;

				case ServerGameState.WAITING_ALL_PLAYERS_TO_SEAT: {
					this.waitingAllSeated();
					// System.out.println("Baraja despues de waitAllConnected()..");
					//tableState.getBaraja().print();					
				}
				break;

				case ServerGameState.DEALING:
					this.dealing();
					break;

				case ServerGameState.PLAYING: {
					this.playing();
					System.out.println("[SRVMUS] exited playing()");
				}
				break;
				case ServerGameState.END_OF_ROUND: {
					this.endOfRound();
				}

				break;
				case ServerGameState.GAME_FINISHED:{
					this.gameFinished();
				}
				break;
				}			

			}

		} catch (SocketException e) {

		} catch (IOException e) {
			e.printStackTrace();
			log("[SRVMUS] IOEXCEPTION");			
		} catch (IllegalThreadStateException i) {
			log("[SRVMUS] ILLEGALTHREADSTATEEXCEPTION");
			try {
				this.halt();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
		} catch (Exception e) {
			// error barajando cartas
			log("[SRVMUS] EXCEPTION");
		}

	}

	private void waitAllConnected() throws IOException {
		while (clientsConnected() < MAX_CLIENTS) {
			//	Por cada cliente se lanza un hilo que le atender�
			byte thread_id = getFreeThreadID(); 
			synchronized(socket) {
				hilos[thread_id] =
						new AtenderCliente(socket.accept(), thread_id, this, controllerJobsQueue, connectionJobs[thread_id]);
			}
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
				synchronized(semaphore) {
					semaphore.wait();
				}
				log("Me informa el controller que alguien se ha sentado!");
				if (disconnection) {
					gameState.setGameState(WAITING_ALL_PLAYERS_TO_CONNECT);
					break;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (!disconnection) {
			gameState.setGameState(Language.ServerGameState.DEALING);			
		} else {
			disconnection = false;
		}
	}

	private void dealing() {
		while(tableState.getGamePhase()!=GRANDE) {
			try {
				log("WAITING FOR END OF DEALING");
				synchronized(semaphore) {
					semaphore.wait();
				}
				if (disconnection) {
					gameState.setGameState(WAITING_ALL_PLAYERS_TO_CONNECT);
					break;
				}
				log("[controller]: ronda descartes finalizada");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//System.out.println("DESPUES DE REPARTIR; LAS CARTAS SON LAS SIGUIENTES:");
		//tableState.printContent();
		if (!disconnection) {
			gameState.setGameState(ServerGameState.PLAYING);
		} else {
			disconnection = false;
		}
		log("Finished dealing cards. Informing clients.");
		// if player in turn == -1 means beginning of game, assign one randomly	

	}	

	private void playing() {
		synchronized(semaphore) {
			semaphore.notify();
		}
		// me bloqueo esperando a que controller me diga
		// que el juego ha terminado
		byte ronda = tableState.getId_ronda();
		try {

			log("WAITING FOR ROUND [" + tableState.getId_ronda() + "] TO FINISH, OR MUS");
			synchronized(semaphore) {
				semaphore.wait();
			}
			if (disconnection) {
				gameState.setGameState(WAITING_ALL_PLAYERS_TO_CONNECT);
				disconnection = false;
			} else {
				log("Me informa el controller para que compruebe si acabo la ronda o si nos damos otra mano de mus.");
				if (tableState.getId_ronda()==ronda) {
					System.out.println("[SRVMUS] ronda id es " +tableState.getId_ronda() + ", pasando a DEALING");
					gameState.setGameState(DEALING);
				}
				else {
					System.out.println("[SRVMUS] ronda id es " +tableState.getId_ronda() + ", pasando a END_Of_ROUND");				
					gameState.setGameState(ServerGameState.END_OF_ROUND);
				}

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}



	}

	private void endOfRound() {
		//make a copy of the tablestate add it to history
		System.out.println("[SRVMUS] endOfRound(): adding history...");
		if (!historyAdded) {
			log("END OF ROUND. Next round: " + tableState.getId_ronda());
			history.add(new TableState(tableState));
			historyAdded  = true;
		}
		// wait for the controller to tell me all clients said lets go to next round
		try {

			while(!gameState.allReadyForNextRound()) {
				synchronized(semaphore) {
					semaphore.wait();
				}				
			}
			if (tableState.getVacas_norte_sur()==tableState.getVacas_por_partida() || tableState.getVacas_oeste_este()==tableState.getVacas_por_partida()) {
				gameState.setGameState(ServerGameState.GAME_FINISHED);
			} else {
				gameState.setGameState(ServerGameState.DEALING);				
			}
			synchronized (semaphore) {
				semaphore.notify();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

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
				defineCommand(tokens);				
			}
			if (tokens[0].toLowerCase().equals("status")) {
				statusCommand(tokens);
			}
			if (tokens[0].toLowerCase().equals("loadstate")) {
				loadStateCommand();
			}			
		} catch(ArrayIndexOutOfBoundsException e) {
			log("INVALID COMMAND");
		}
	}

	private void loadStateCommand() {

		try {
			BufferedReader bfr = new BufferedReader(new FileReader("state.txt"));
			String line = "";
			while ((line = bfr.readLine())!=null) {
				System.out.println(line);
				int eq_indx = line.indexOf('=')+1;
				if (line.contains("cartas_jugador")) {
					line = line.substring(eq_indx).trim();					
					Carta[] cartas = new Carta[CARDS_PER_HAND];
					String[] ids = line.split(",");
					for (int i=0; i< CARDS_PER_HAND; i++) {	
						cartas[i] = new Carta(Byte.parseByte(ids[i+1]));
					}
					tableState.getClient(Integer.parseInt(ids[0])).setCartas(cartas );
					//GameState.base_vacas_partida = Byte.parseByte(line);							
				}
				if (line.contains("gamephase")) {
					line = line.substring(eq_indx).trim();
					tableState.setGamePhase(Byte.parseByte(line));							
				}
				if (line.contains("juego")) {
					line = line.substring(eq_indx).trim();
					String[] juego = line.split(",");
					for (int i = 0; i< juego.length; i++) {
						if (juego[i].equals("0")) {
							//tableState.
						}
					}

				}
				if (line.contains("pares")) {
					//line = line.substring(eq_indx).trim();
					//tableState.setGamePhase(Byte.parseByte(line));							
				}
				if (line.contains("en_paso")) {
					//line = line.substring(eq_indx).trim();
					//tableState.setGamePhase(Byte.parseByte(line));							
				}				

			}
			serverController.broadCastGameState();
			bfr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void defineCommand(String[] tokens) {
		if (tokens.length == 1) {
			log("stones_to_game [" + tableState.getPiedras_por_juego() + "]");
			log("games_to_cow: " + tableState.getJuegos_por_vaca());
			log("cows_to_match: " + tableState.getVacas_por_partida());			
		}
		if (tokens[1].toLowerCase().equals("port")) {
			if (running) {
				this.port =Integer.parseInt(tokens[2]); 
				log("New port defined: " + this.port);
			} else log("ERROR: cannot change port while running.");
		}
	}

	private void statusCommand(String[] tokens) {
		if (tokens.length == 1) {
			log("========SERVER=STATUS:\n");
			log("SERVER: " + (running ? "ONLINE" :  "OFFLINE"));
			log("Clients connected: " + clientsConnected());
			for (int i = 0; i < MAX_CLIENTS; i++) {
				log("ID in thread " + i + ":"+gameState.getPlayerID(i));
			}
			String[] serverGameState = {"WAITING_ALL_PLAYERS_TO_CONNECT","WAITING_ALL_PLAYERS_TO_SEAT","DEALING","PLAYING","END OF ROUND", "GAME FINISHED"};
			log("state:" + serverGameState[gameState.getServerGameState()]);
			log("Piedras por juego:"+tableState.getPiedras_por_juego());
			log("Juegos por vaca" + tableState.getJuegos_por_vaca());
			log("Vacas para partida:" + tableState.getVacas_por_partida());
			for (int i = 0; i < MAX_CLIENTS; i++) {
				if (gameState.isReadyForNextRound((byte) i)) {
					log("Player with seat_id " + i + " ready for next round.");
				} else {
					log("Player with seat_id " + i + " NOT ready for next round.");					
				}
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


			// round state
			log("Jugador que es mano:" + tableState.getMano_seat_id());
			log("Jugador que debe hablar:" + tableState.getJugador_debe_hablar());			
			String[] lances = {"MUS","DESCARTES","GRANDE","CHICA","PARES","JUEGO","FIN DE RONDA"};
			log("Lance actual:" + lances[tableState.getGamePhase()]);
			log("Piedras en el bote:" + tableState.getPiedras_acumuladas_en_apuesta());
			log("Piedras envidadas en ronda actual:" + tableState.getPiedras_envidadas_ronda_actual());
			log("Piedras envidadas a grande:" + tableState.getPiedras_envidadas_a_grande());
			log("Piedras envidadas a chica:" + tableState.getPiedras_envidadas_a_chica());
			log("Piedras envidadas a pares:" + tableState.getPiedras_envidadas_a_pares());
			log("Piedras envidadas a juego:" + tableState.getPiedras_envidadas_a_juego());
			log("HAy ordago en juego? " + (tableState.isOrdago_lanzado()? "SI":"NO"));
			// general state
			log("Piedras pareja norte/sur:" + tableState.getPiedras_norte_sur());
			log("Piedras pareja oeste/este:" + tableState.getPiedras_oeste_este());
			log("Juegos norte/sur:" + tableState.getJuegos_norte_sur());
			log("Juegos oeste/este:" + tableState.getJuegos_oeste_este());
			log("Vacas norte/sur:" + tableState.getVacas_norte_sur());
			log("Vacas oeste/este:" + tableState.getVacas_oeste_este());

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
