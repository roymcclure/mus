package roymcclure.juegos.mus.server.logic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

import javax.swing.JTextArea;

import roymcclure.juegos.mus.common.logic.Language;
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

	// socker for the server
	private ServerSocket socket;
	private int port = 5678;
	private boolean running = false;
	
	private JTextArea txtAreaLog;
	
	public static int MAX_CLIENTS = 4;
	// almacenar los hilos de cada cliente
	private static AtenderCliente[] hilos;


	
	private ServerGameState gameState;

	public SrvMus (JTextArea txtLog) {
		txtAreaLog = txtLog;
		gameState = new ServerGameState();
		initThreads();
	}
	
	public void runCommand(String cmd, JTextArea txtLog) {
		String[] tokens = cmd.split(" ");
		try {
			if (tokens[0].toLowerCase().equals("define")) {
				if (tokens[1].toLowerCase().equals("port")) {
					if (running) {
						this.port =Integer.parseInt(tokens[2]); 
						txtLog.append("New port defined: " + this.port + "\n");
					} else txtLog.append("ERROR: cannot change port while running.\n"); 
				}
			}
			if (tokens[0].toLowerCase().equals("status")) {
				if (tokens.length == 1) {
					txtLog.append("================STATUS:\n");
					txtLog.append("SERVER: " + (running ? "ONLINE" :  "OFFLINE") + "\n");
					txtLog.append("Clients connected: " + clientsConnected() + "\n");
					txtLog.append("State of game: " + gameState.getGameState() + "\n");
					txtLog.append("Round nr: " + gameState.getId_ronda() + "\n");
					txtLog.append("Round type: " + gameState.getTipo_ronda() + "\n");
					txtLog.append("================");					
				} else 	if (tokens[1].toLowerCase().contentEquals("client")) {

				}
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			txtLog.append("INVALID COMMAND\n");
		}
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
				n++;
		}		
		return n;
	}

	// returns -1 if all connected
	private static int getFreeSeatID() {
		for (int i=0; i<MAX_CLIENTS;i++) {
			if (hilos[i] == null)
				return i;
		}
		return -1;
	}

	public void run() {
		try {
			running = true;
			txtAreaLog.append("Resetting client array...");			
			txtAreaLog.append("reset.\n");
			txtAreaLog.append("Starting server in port " + port + "...\n");			
			socket = new ServerSocket(port);
			txtAreaLog.append("started.\n");			

			while (running) {
				txtAreaLog.append("Waiting for all players to be seated...\n");		
				
				// esperando que entren todos los jugadores
				while (clientsConnected() < MAX_CLIENTS) {

					//	Por cada cliente se lanza un hilo que le atenderá
					txtAreaLog.append("---Waiting for client connection...\n");
					int thread_id = getFreeThreadID(); 
					hilos[thread_id] =
							new AtenderCliente(socket.accept(), gameState, getFreeThreadID(), this);
					txtAreaLog.append("Client connected.\n");
					hilos[thread_id].start();			
					
					// tras la conexión, cada hilocliente le pregunta al servidor el estado de la partida.

				}
				txtAreaLog.append("all seated.\n");
				txtAreaLog.append("Starting game.\n");
				// actualizamos el estado de juego a playing
				gameState.setGameState(Language.ServerGameState.PLAYING);
				// TODO: esto no tengo clar a
				// notificarmos a cada hilo para que se despierte.
				// wakeClients();
				while (gameState.getGameState() != Language.ServerGameState.GAME_FINISHED) {
					Thread.sleep(1000);
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
	
	/*
	private void wakeClients() {
		for (int i = 0; i<MAX_CLIENTS;i++) {
			hilos[i].notify();
		}
		
	}
	*/

	
	public void releaseThread(byte seat_ID) {
		hilos[seat_ID] = null;
	}

	private byte getFreeThreadID() {
		for (byte i=0; i<MAX_CLIENTS;i++) {
			if (hilos[i]==null) {
				return i;
			}
		}
		return -1;
	}

	public void halt() throws IOException {
		// enviar mensaje a todos los clientes que el servidor se cierra
		running = false;
		for (int i = 0; i<MAX_CLIENTS;i++ ) {
			try {
				// ac.sendCloseMsg();
				if (hilos[i]!=null) {
					txtAreaLog.append("Attempting to kill client " + i + "\n");
					hilos[i].interrupt();
					hilos[i].disconnect();
					hilos[i].join();

				}
				socket.close();				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				
			}
		}
		//	cerrar el socket
		 this.interrupt();


	}

}
