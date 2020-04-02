package roymcclure.juegos.mus.server.logic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

import javax.swing.JTextArea;

import roymcclure.juegos.mus.common.logic.Language;
import roymcclure.juegos.mus.server.network.AtenderCliente;


public class SrvMus extends Thread {

	// socker for the server
	ServerSocket s;
	int port;
	private boolean running = false;
	
	private JTextArea txtAreaLog;
	
	public static int MAX_CLIENTS = 4;
	// almacenar los hilos de cada cliente
	private static AtenderCliente[] hilos;

	private GameState gameState;
	
	public SrvMus (JTextArea txtLog) {
		txtAreaLog = txtLog;
		gameState = new GameState();
	}
	
	
	private static void initThreads() {
		hilos = new AtenderCliente[MAX_CLIENTS];
		for (int i=0; i<MAX_CLIENTS;i++) {
			hilos[i] = null;
		}
	}

	// How many threads are running
	private static int nPlayers() {
		int n= 0;
		for (int i=0; i<MAX_CLIENTS;i++) {
			if (hilos[i] != null)
				n++;
		}		
		return n;
	}

	// returns -1 if all connected
	private static int freePosition() {
		for (int i=0; i<MAX_CLIENTS;i++) {
			if (hilos[i] == null)
				return i;
		}
		return -1;
	}

	public void run() {
		try {
			running = true;
			initThreads();
			txtAreaLog.append("Reset Thread array.\n");
			s = new ServerSocket(port);
			
			// todo: exception tratando de atarse a un puerto en uso




			//	Se esperan clientes hasta que se hayan conectado cuatro
			//	Si en algún momento de la partida se hubiera desconectado un cliente
			//	habríamos vuelto a este punto y después de haber cuatro clientes conectados
			//	volveríamos al estado en el que se encontrara la partida
			//  por tanto cada hilo

			while (running) {
				
				// esperando que entren todos los jugadores
				while (nPlayers() < 4) {

					//	Por cada cliente se lanza un hilo que le atenderá
					txtAreaLog.append("Waiting for client connection...");
					AtenderCliente hiloCliente = new AtenderCliente(s.accept(), gameState, getAvailableClientId());
					txtAreaLog.append("Client connected.\n");
					hilos[freePosition()] = hiloCliente;			
					hiloCliente.start();
					// tras la conexión, cada hilocliente le pregunta al servidor el estado de la partida.

				}
				// actualizamos el estado de juego a playing
				gameState.setGameState(Language.GameState.PLAYING);
				// notificarmos a cada hilo para que se despierte.
				while (gameState.getGameState() != Language.GameState.GAME_FINISHED) {
					Thread.sleep(1000);
				}
				
			}

			
			
			txtAreaLog.append("All clients connected. Starting game...\n");
			
			
			
			// Deck is instantiated and cards shuffled on game start.
			// d.iniciarPartida();
			// We must keep in mind that clients do not need to know what is on the deck and what is not.
			// they just need to know their own cards
			
			
			
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
	
	private int getAvailableClientId() {
		for (int i=0; i<MAX_CLIENTS;i++) {
			if (hilos[i]==null) {
				return i;
			}
		}
		return -1;
	}


	public void start(String port_) throws NumberFormatException, IOException {
		
		port = Integer.parseInt(port_);
				
	}

	public void halt() throws IOException {
		// enviar mensaje a todos los clientes que el servidor se cierra
		running = false;
		for (int i = 0; i<MAX_CLIENTS;i++ ) {
			try {
				// ac.sendCloseMsg();
				if (hilos[i]!=null) {
					hilos[i].interrupt();
					hilos[i].disconnect();
					hilos[i].join();

				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//	cerrar el socket
			this.interrupt();
		System.out.println("interrupt llamado");
		s.close();
		System.out.println("Socket cerrado");
	}

}
