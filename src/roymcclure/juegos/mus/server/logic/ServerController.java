package roymcclure.juegos.mus.server.logic;

import static roymcclure.juegos.mus.common.logic.Language.PlayerActions.*;
import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.*;
import static roymcclure.juegos.mus.common.logic.Language.GamePhase.*;
import static roymcclure.juegos.mus.common.logic.Language.ServerGameState.*;

import java.util.Random;

import roymcclure.juegos.mus.common.logic.*;
import roymcclure.juegos.mus.common.logic.Language.GamePhase;
import roymcclure.juegos.mus.common.logic.Language.PlayerActions;
import roymcclure.juegos.mus.common.logic.jobs.*;
import roymcclure.juegos.mus.common.network.ClientMessage;
import roymcclure.juegos.mus.common.network.ServerMessage;

/*
 * This class performs (and controls) modifications to the model
 * from the client perspective.
 * Continuously fetches jobs from the ControllerJobsQueue,
 * verifies if they are applicable, applies them or not, 
 * then posts a job to indicate the result of the operation.
 * For instance, if a client receives a message trying to
 * perform an illegal operation (illegally forged packet)
 * it will reply to the client with a OPERATION_DENIED
 * 
 */

public class ServerController implements Runnable{

	ControllerJobsQueue _controllerJobsQueue;
	private boolean _running= false;
	private GameState gameState;
	private TableState tableState;
	private ConnectionJobsQueue[] connectionJobs;
	private Object key;
	private SrvMus server;

	public ServerController(ControllerJobsQueue controllerJobsQueue, ConnectionJobsQueue[] connectionJobs, GameState gameState, TableState tableState, SrvMus server, Object key) {
		this.gameState = gameState;
		this.tableState = tableState;
		_controllerJobsQueue = controllerJobsQueue;
		this.connectionJobs = connectionJobs;
		this.key = key;
		this.server = server;
	}

	@Override
	public void run() {
		Job job;
		_running = true;
		while(_running) {
			//System.out.print("[Controller] Awaiting for a job to process....\n");
			synchronized(_controllerJobsQueue) {
				job = getJob();
				System.out.println("Got a job.");
				processJob(job);

			}		

		}

	}


	private void processJob(Job job) {
		System.out.println("Called processJob()");
		// game state is modified by clickReceived and message received
		if (job instanceof MessageJob) {
			System.out.println("Controller: processing ServerMessageJob");
			MessageJob mj = (MessageJob) job;
			if (isValidRequest(mj.getClientMessage(),mj.getThreadId()))
				updateGameStateWith(mj.getClientMessage(),mj.getThreadId());
		} 
	}









	// checks preconditions for a valid request
	private boolean isValidRequest(ClientMessage clientMessage, byte threadId) {
		byte talking_seat_id = tableState.getJugador_debe_hablar();
		byte request_player_seat_id = tableState.getSeatOf(gameState.getPlayerID(threadId));
		boolean player_must_talk = talking_seat_id== request_player_seat_id;

			// requests only acceptable when its the player's turn
			switch(clientMessage.getAction()) {
			case PlayerActions.MUS:			
				if (player_must_talk && tableState.getTipo_Lance()==GamePhase.MUS) {
					return true;
				}
				break;
			case PASS:
				if (player_must_talk && tableState.getTipo_Lance()==GamePhase.MUS) {
					return true;
				} else {
					// i can pass when 
				}
				break;
			case ENVITE:
				if (player_must_talk)
					if (tableState.getTipo_Lance()==GRANDE || tableState.getTipo_Lance()==CHICA || tableState.getTipo_Lance()==PARES || tableState.getTipo_Lance()==JUEGO) {
						return true;
					}				
				break;
			case ACCEPT:
				if (player_must_talk && tableState.getPiedras_envidadas_ronda_actual()>0)
					return true;				
				break;
			case ORDAGO:
				// TODO
				break;
			case CORTO_MUS:
				if (player_must_talk && tableState.getTipo_Lance()==GamePhase.MUS) {
					return true;
				}				
				break;
			case HANDSHAKE:
			case REQUEST_GAME_STATE: // a player can always request the game state
			case REQUEST_SEAT: // a player can always request a seat.
			case CLOSE_CONNECTION: // a player can always close the connection.
				return true;
			default:
				return false;				

			}

		return false;
	}

	private Job getJob() {
		Job job;
		synchronized(_controllerJobsQueue) {
			if (_controllerJobsQueue.isEmpty()) {
				try {
					_controllerJobsQueue.wait();
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}
			job = _controllerJobsQueue.getControllerJob();
			System.out.println("[SERVER CONTROLLER] Got a Controller job.");
		}			

		return job;
	}


	public synchronized void updateGameStateWith(ClientMessage cm, byte thread_id) {
		// si cliente solicita información del mundo, realmente no hacemos gran cosa.

		switch (cm.getAction()) {

		case REQUEST_GAME_STATE:
			// player can pass their name here
			String playerID = cm.getInfo();
			synchronized(gameState) {
				gameState.setPlayerID(playerID, thread_id);
			}
			System.out.println("SERVER: player " + playerID.toString() + " connected.");
			sendGameState(thread_id);

			break;

		case REQUEST_SEAT:
			// we try to seat the player in the requested seat
			// we assume the player knows the game state so it doesnt really need
			// a refresh at this point
			System.out.println("PLAYER " + gameState.getPlayerID(thread_id) + " requested the SEAT " + cm.getQuantity());
			byte requested_seat = cm.getQuantity();
			if (tableState.takeAseat(requested_seat, gameState.getPlayerID(thread_id))) {
				try {
					System.out.println("[SERVER Controller] a player took a seat. Notifying...");
					synchronized(key) {
						key.notify();
					}
					// this is made in order to simply tell the server that a player took a seat, in case
					// it is waiting 
					
				} catch (IllegalMonitorStateException e) {
					// esta excepcion se puede lanzar si notificamos antes de llamar a key.wait()
					// en srvmus. por ejemplo si estamos en WAITING_ALL_SEATED
				}
				if (tableState.allSeated()) {
					tableState.getBaraja().barajar();
					tableState.repartir();
					if (tableState.getJugador_debe_hablar() == -1) {
						Random random = new Random();
						tableState.setJugador_debe_hablar((byte)random.nextInt(MAX_CLIENTS));
					}										
				}				
				updateAllPlayers();	
			} else {
				// TODO: not very sure of this...
				sendGameState(thread_id);
			}
			break;

		case CLOSE_CONNECTION:
			//player wants to disconnect.
			System.out.println("Player from thread " + thread_id + " wants to disconnect.");

			// si el jugador estaba sentado, limpiamos su sitio
			String player_id = gameState.getPlayerID(thread_id);
			gameState.setPlayerID("",thread_id);
			int seat_id = tableState.getSeatOf(player_id);
			if (seat_id >=0)
				tableState.clearSeat(seat_id);
			server.releaseThread(thread_id);
			//stop all threads to that client

			break;

		case PlayerActions.MUS:
			// allow if player requesting MUS is the one that has to talk, and we are in the MUS turn
			byte talking_seat_id = tableState.getJugador_debe_hablar();
			byte request_player_seat_id = tableState.getSeatOf(gameState.getPlayerID(thread_id));
			if (tableState.getTipo_Lance()==GamePhase.MUS && talking_seat_id== request_player_seat_id) {
				tableState.setJugadores_hablado_en_turno_actual((byte)(tableState.getJugadores_hablado_en_turno_actual()+1));
				// if this is the last guy having mus, go to DESCARTANDO game phase
				if (tableState.getJugadores_hablado_en_turno_actual()==MAX_CLIENTS) {
					gameState.setGameState(DESCARTE);
					synchronized(key) {
						key.notify();
					}
				}
				// advance turn to next player, and inform all players
				tableState.advanceTurn();
				updateAllPlayers();
			}
			break;

		default:
			break;

		}
	}

	//notify All Players
	public void updateAllPlayers() {
		for (byte i = 0; i < MAX_CLIENTS; i++) {
			sendGameState(i);
		}
	}

	// sends a player the game state
	private void sendGameState(byte thread_id) {
		ServerMessage sm = ServerMessage.forgeDataPacket(gameState, tableState, gameState.getPlayerID(thread_id));
		ConnectionJob job = new ConnectionJob(sm);
		job.setThreadId(thread_id);
		//post to the queue of jobs for that thread. so connectionJob should contain
		//the thread_id I guess??
		synchronized(connectionJobs[thread_id]) {
			connectionJobs[thread_id].postConnectionJob(job);
			connectionJobs[thread_id].notify();
		}
	}



}
