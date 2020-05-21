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
				//System.out.println("Got a job.");
				processJob(job);
			}
		}
	}

	private void processJob(Job job) {
		//System.out.println("Called processJob()");
		// game state is modified by clickReceived and message received
		if (job instanceof MessageJob) {
			//System.out.println("Controller: processing ServerMessageJob");
			MessageJob mj = (MessageJob) job;
			if (isValidRequest(mj.getClientMessage(),mj.getThreadId())) {
				updateGameStateWith(mj.getClientMessage(),mj.getThreadId());
				// include thread_id in should be broadcasted?
				// probably not
				if (shouldBeBroadcasted(mj.getClientMessage()))
					broadCastPlayerAction(mj.getClientMessage(), mj.getThreadId());				
			}

		} 
	}

	// TODO not all messages should not be broadcasted... but most should.
	private boolean shouldBeBroadcasted(ClientMessage clientMessage) {
		// do not broadcaste: REQUEST_GAME_STATE, REQUEST_SEAT
		boolean eligible = !(clientMessage.getAction() == REQUEST_GAME_STATE || clientMessage.getAction() == REQUEST_SEAT); 
		if (!eligible)
			return false;
		return true;
	}

	// checks preconditions for a valid request
	private boolean isValidRequest(ClientMessage clientMessage, byte threadId) {
		byte talking_seat_id = tableState.getJugador_debe_hablar();
		byte request_player_seat_id = tableState.getSeatOf(gameState.getPlayerID(threadId));
		boolean player_must_talk = talking_seat_id== request_player_seat_id;

		// requests only acceptable when its the player's turn
		switch(clientMessage.getAction()) {
		case PlayerActions.MUS:			
			return player_must_talk && tableState.getTipo_Lance()==GamePhase.MUS; 
		case PlayerActions.DESCARTAR:
			return tableState.getTipo_Lance() == DESCARTE; // everyone can talk at this point
		case PASS: // one can pass only in your turn
			// and can be done when others have envidado OR you should envidar but decide to pass instead
			if (player_must_talk) {
				switch(tableState.getTipo_Lance()) {
				case GamePhase.CHICA:
				case GamePhase.GRANDE:
				case GamePhase.PARES:
				case GamePhase.JUEGO:
					return true;
				}
				break;
			}
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
			//System.out.println("[SERVER CONTROLLER] Got a Controller job.");
		}			

		return job;
	}

	// if this method was called then preconditions were met
	// made sure in isValidRequest(...)
	public synchronized void updateGameStateWith(ClientMessage cm, byte thread_id) {
		// si cliente solicita información del mundo, realmente no hacemos gran cosa.
		String playerID = "";
		byte player_seat_id = 0;
		playerID = gameState.getPlayerID(thread_id);
		player_seat_id = tableState.getSeatOf(playerID);		
		switch (cm.getAction()) {

		case REQUEST_GAME_STATE:
			// player can pass their name here
			playerID = cm.getInfo();
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
					//System.out.println("[SERVER Controller] a player took a seat. Notifying...");
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
					// TODO: esto solo si no es un usuario que se había desconectado!!!!
					tableState.getBaraja().barajar();
					tableState.repartir();
					// because we could be coming from a disconnect
					if (tableState.getJugador_debe_hablar() == -1) {
						Random random = new Random();
						byte j = (byte)random.nextInt(MAX_CLIENTS);
						tableState.setJugador_debe_hablar(j);
						tableState.setMano_seat_id(j);
					}										
				}				
				broadCastGameState();	
			} else {
				// TODO: not very sure of this...
				sendGameState(thread_id);
			}
			break;

		case CLOSE_CONNECTION:
			//player wants to disconnect.
			System.out.println("Player from thread " + thread_id + " wants to disconnect.");

			// si el jugador estaba sentado, limpiamos su sitio
			playerID = gameState.getPlayerID(thread_id);
			gameState.setPlayerID("",thread_id);
			int seat_id = tableState.getSeatOf(playerID);
			if (seat_id >=0)
				tableState.clearSeat(seat_id);
			server.releaseThread(thread_id);
			//stop all threads to that client

			break;

		case PlayerActions.MUS:

			tableState.increaseTalkedPlayers();
			// if this is the last guy having mus, go to DESCARTANDO game phase
			if (tableState.allPlayersTalked()) {
				tableState.setTipo_Lance(DESCARTE);
				tableState.resetTalked();
				synchronized(key) {
					key.notify();
				}
			}
			// advance turn to next player, and inform all players
			// solo es mus corrido en la primera ronda
			tableState.advanceTalkingPlayer();
			if (tableState.getId_ronda()==0)
				tableState.setMano_seat_id(tableState.getJugador_debe_hablar());
			broadCastGameState();

			break;

		case PlayerActions.CORTO_MUS:
			tableState.setTipo_Lance(GRANDE);
			synchronized(key) {
				key.notify();
			}
			tableState.resetTalked();
			try {
				synchronized(key) {
					key.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			broadCastGameState();			

			break;

		case PlayerActions.DESCARTAR:
			// get which cards he wants to discard and keep it in state
			// if all players have chosen, deal cards
			PlayerState ps = tableState.getClient(player_seat_id);
			ps.setCommitedToDiscard(true);
			byte descartes = cm.getQuantity();
			ps.unmarkAll();
			for (byte i = 0; i < CARDS_PER_HAND; i++) {
				if (bitSet(i,descartes)) {					
					ps.markForReplacement(i);	
				}				
			}
			if (tableState.allPlayersCommitedToDiscard()) {
				tableState.setTipo_Lance(GamePhase.MUS);
				tableState.moveDiscarded();
				tableState.uncommitToDiscard();
				tableState.repartir();
				broadCastGameState();
			} else {
				sendGameState(thread_id);
			}
			break;

		case PlayerActions.ENVITE:
			
				// if its a new envite, add qty to piedras_envidadas_a_grande
				if (tableState.getJugadores_hablado_en_turno_actual()==0) {
					tableState.setPiedras_envidadas_ronda_actual(cm.getQuantity());
					tableState.setPiedras_en_ultimo_envite(cm.getQuantity());
				} else {
					// alguien está envidando más
					byte a_grande = tableState.getPiedras_envidadas_ronda_actual();
					byte anterior = tableState.getPiedras_acumuladas_en_apuesta();
					byte total = (byte)(anterior + tableState.getPiedras_en_ultimo_envite());
					tableState.setPiedras_acumuladas_en_apuesta(total);
					tableState.setPiedras_envidadas_ronda_actual((byte)(a_grande+ cm.getQuantity()));
					tableState.setPiedras_en_ultimo_envite(cm.getQuantity());
				}
				// this property allows us to properly assign the pot in case of bid rejection
				tableState.setUltimo_envidador(player_seat_id);				
				// next person that should talk is the mano of the other team
				byte mano_otro_equipo = tableState.getManoOtroEquipo(player_seat_id);
				tableState.setJugador_debe_hablar(mano_otro_equipo);
				tableState.increaseTalkedPlayers();
				broadCastGameState();
				break;
				
		case PlayerActions.ACCEPT:
			// if ordago
			// 		set_lance(final_de_ronda)
			// 		ver quien gana de todos
			// else
			byte piedras = tableState.getPiedras_envidadas_ronda_actual();
			switch(tableState.getTipo_Lance()) {
			case GRANDE:
				tableState.setPiedras_envidadas_a_grande(piedras);
				break;
			case CHICA:
				tableState.setPiedras_envidadas_a_chica(piedras);				
				break;
			case PARES:
				tableState.setPiedras_envidadas_a_pares(piedras);				
				break;
			case JUEGO:
				tableState.setPiedras_envidadas_a_juego(piedras);				
				break;
			}
			byte n = 0;
			tableState.setPiedras_acumuladas_en_apuesta(n);
			tableState.setPiedras_envidadas_ronda_actual(n);
			tableState.advanceLance();			
			tableState.setJugador_debe_hablar(tableState.getMano_seat_id());
			broadCastGameState();			
			break;

		case PlayerActions.PASS:
			
			if (tableState.isPostre(player_seat_id)) {
				if (tableState.getPiedras_envidadas_ronda_actual()>0) {
					//			give pot to the bidding team
					tableState.givePotTo(player_seat_id+1);
				} 
				tableState.advanceLance();
				tableState.setJugador_debe_hablar(tableState.getMano_seat_id());
				tableState.setPiedras_envidadas_ronda_actual((byte) 0);
			} else {
				if (tableState.getPiedras_envidadas_ronda_actual()>0) {
					tableState.setJugador_debe_hablar((byte)((player_seat_id + 2) % MAX_CLIENTS));
				}
				else {
					tableState.advanceTalkingPlayer();
				}
			}
			broadCastGameState();
			break;

		default:
			break;

		}
	}

	// TODO: in the controller? really?
	private boolean bitSet(byte bit_index, byte value) {
		// if and between bit_index 
		return (value & (byte)Math.pow(2, bit_index)) != 0;
	}

	//notify All Players
	public void broadCastGameState() {
		for (byte i = 0; i < MAX_CLIENTS; i++) {
			sendGameState(i);
		}
	}
	
	// sends a player the game state
	private void sendGameState(byte thread_id) {
		ServerMessage sm = ServerMessage.forgeStateMessage(gameState, tableState, gameState.getPlayerID(thread_id));
		postServerConnectionJob(sm, thread_id);
	}
	
	// broadcast content of cm to all players except the sender (the one in thread_id)
	public void broadCastPlayerAction(ClientMessage cm, byte thread_id) {
		for (byte i = 0; i < MAX_CLIENTS; i++) {
			// we dont tell a player what they have just done!!
			if(thread_id !=i)
				sendPlayerAction(cm,i, gameState.getPlayerID(thread_id));
		}		
	}

	private void sendPlayerAction(ClientMessage cm, byte thread_id, String playerID) {
		ServerMessage sm = ServerMessage.forgeBroadCastMessage(cm, playerID);
		postServerConnectionJob(sm, thread_id);
	}
	
	private void postServerConnectionJob(ServerMessage sm, byte thread_id) {
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
