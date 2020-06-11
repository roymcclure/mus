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
import roymcclure.juegos.mus.server.network.AtenderCliente;

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
	AtenderCliente[] hilos;

	private boolean[] acks_ronda_no_jugada;
	

	public ServerController(ControllerJobsQueue controllerJobsQueue, ConnectionJobsQueue[] connectionJobs, GameState gameState, TableState tableState, SrvMus server, Object key, AtenderCliente[] hilos) {
		this.gameState = gameState;
		this.tableState = tableState;
		this.hilos = hilos;
		_controllerJobsQueue = controllerJobsQueue;
		this.connectionJobs = connectionJobs;
		this.key = key;
		this.server = server;
		acks_ronda_no_jugada=new boolean[MAX_CLIENTS];
	}

	@Override
	public void run() {
		Job job;
		_running = true;
		while(_running) {
			//System.out.print("[Controller] Awaiting for a job to process....\n");
			job = getJob();
			System.out.println("[SERVER CONTROLLER] Received a job. Processing...");
			processJob(job);
		}
	}

	/**
	 * 
	 * JOB RELATED FUNCTIONS
	 * 
	 */

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
			_controllerJobsQueue.deleteFirstJob();
			 System.out.println("[SERVER CONTROLLER] Got a Controller job.");
		}			

		return job;
	}

	private void processJob(Job job) {
		System.out.println("[CONTROLLER] processJob() starting");
		// game state is modified by clickReceived and message received
		if (job instanceof MessageJob) {
			System.out.println("[CONTROLLER] job is a MessageJob");
			MessageJob mj = (MessageJob) job;
			if (isValidRequest(mj.getClientMessage(),mj.getThreadId())) {
				System.out.println("[CONTROLLER] job contains a valid request.");				
				if (shouldBeBroadcasted(mj.getClientMessage())) {
					System.out.print("[CONTROLLER] job is being broadcasted...");					
					broadCastPlayerAction(mj.getClientMessage(), mj.getThreadId());
					System.out.println("done.");
				}
				System.out.println("[CONTROLLER] updating game state...");
				updateGameStateWith(mj.getClientMessage(),mj.getThreadId());
				// include thread_id in should be broadcasted?
				// probably not the need for a broadcast is determined
				// by the nature of the message not so much by who sends it				
			}
		} 
	}


	// checks preconditions for a valid request
	private boolean isValidRequest(ClientMessage clientMessage, byte threadId) {
		byte talking_seat_id = tableState.getJugador_debe_hablar();
		byte request_player_seat_id = tableState.getSeatOf(gameState.getPlayerID(threadId));
		boolean player_must_talk = (talking_seat_id== request_player_seat_id);

		// requests only acceptable when its the player's turn
		switch(clientMessage.getAction()) {

		case PlayerActions.MUS:			
			return player_must_talk && tableState.getGamePhase()==GamePhase.MUS;

		case PlayerActions.DESCARTAR:
			return tableState.getGamePhase() == DESCARTE; // everyone can talk at this point

		case ORDAGO:
		case PASS: // one can pass only in your turn
			// and can be done when others have envidado OR you should envidar but decide to pass instead
			if (player_must_talk) {
				switch(tableState.getGamePhase()) {
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
				if (tableState.getGamePhase()==GRANDE || tableState.getGamePhase()==CHICA || tableState.getGamePhase()==PARES || tableState.getGamePhase()==JUEGO) {
					return true;
				}				
			break;
		case ACCEPT:			
			if (player_must_talk && (tableState.isOrdago_lanzado() || tableState.getPiedras_envidadas_ronda_actual()>0))
				return true;				
			break;

		case CORTO_MUS:
			if (player_must_talk && tableState.getGamePhase()==GamePhase.MUS) {
				return true;
			}				
			break;
		case NO_SE_JUEGA_RONDA:
			if (tableState.getGamePhase()==JUEGO || tableState.getGamePhase() == PARES)
				return true;
			break;
		case Language.ConnectionState.ALIVE:
		case REQUEST_GAME_STATE: // a player can always request the game state
		case REQUEST_SEAT: // a player can always request a seat.
		case CLOSE_CONNECTION: // a player can always close the connection.
			return true;
		case SIG_RONDA:
			if (tableState.getGamePhase() == FIN_RONDA) {
				return true;
			}
			break;
		default:
			return false;				

		}

		return false;
	}


	// TODO not all messages should not be broadcasted... but most should.
	private boolean shouldBeBroadcasted(ClientMessage clientMessage) {
		// do not broadcaste: REQUEST_GAME_STATE, REQUEST_SEAT
		boolean eligible = true;
		if (clientMessage.getAction() == REQUEST_GAME_STATE)
			eligible = false;
		if (clientMessage.getAction() == REQUEST_SEAT)
			eligible = false;
		if (clientMessage.getAction() == NO_SE_JUEGA_RONDA)
			eligible = false;
		if (clientMessage.getAction() == SIG_RONDA)
			eligible = false;
		if (clientMessage.getAction() == Language.ConnectionState.ALIVE)
			eligible = false;
		if (!eligible)
			return false;
		return true;
	}

	// broadcast content of cm to all players except the sender (the one in thread_id)
	public void broadCastPlayerAction(ClientMessage cm, byte thread_id) {
		for (byte i = 0; i < MAX_CLIENTS; i++) {
			// we dont tell a player what they have just done!!
			if(thread_id !=i)
				sendPlayerAction(cm,i, gameState.getPlayerID(thread_id));
		}		
	}

	// send what a player (playerID) did to another player (thread_id)
	private void sendPlayerAction(ClientMessage cm, byte thread_id, String playerID) {
		ServerMessage sm = ServerMessage.forgeBroadCastMessage(cm, playerID);
		postServerConnectionJob(sm, thread_id);
	}


	public synchronized void updateGameStateWith(ClientMessage cm, byte thread_id) {
		// si cliente solicita información del mundo, realmente no hacemos gran cosa.
		System.out.println("[CONTROLLER] updateGameStateWith()");
		String playerID = "";
		byte player_seat_id = 0;
		playerID = gameState.getPlayerID(thread_id);
		player_seat_id = tableState.getSeatOf(playerID);		
		switch (cm.getAction()) {

		case PlayerActions.REQUEST_GAME_STATE:
			updateOnRequestGameState(cm.getInfo(),thread_id);
			break;

		case REQUEST_SEAT:
			updateOnRequestSeat(cm.getQuantity(), thread_id);			
			break;

		case CLOSE_CONNECTION:
			//player wants to disconnect.
			System.out.println("[SERVER CONTROLLER] Player from thread " + thread_id + " wants to disconnect.");

			// si el jugador estaba sentado, limpiamos su sitio
			playerID = gameState.getPlayerID(thread_id);
			gameState.setPlayerID("",thread_id);
			int seat_id = tableState.getSeatOf(playerID);
			if (seat_id >=0)
				tableState.clearSeat(seat_id);
			server.releaseThread(thread_id);
			//stop all threads to that client
			SrvMus.disconnection = true;
			synchronized(key) {
				key.notify();
			}
			break;

		case PlayerActions.MUS:

			updateOnMus();
			break;

		case Language.ConnectionState.ALIVE:
			// signal its AtenderCliente to set nReplies to 0
			System.out.println("reseting nReplies in thread " + thread_id);
			hilos[thread_id].resetNotReplied();
			break;
			
		case PlayerActions.CORTO_MUS:
			// we are in DESCARTE state
			tableState.setGamePhase(GRANDE);
			tableState.resetTalked();
			if (tableState.getId_ronda() !=0 ) {
				tableState.setJugador_debe_hablar(tableState.getMano_seat_id());
			}
			synchronized(key) {
				key.notify();
			}
			try {
				synchronized(key) {
					key.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;

		case PlayerActions.DESCARTAR:
			updateStateWithDescartar(player_seat_id, cm.getQuantity(), thread_id);
			break;

		case PlayerActions.ENVITE:

			updateStateWithEnvite(player_seat_id, cm.getQuantity());
			break;

		case PlayerActions.ACCEPT:

			updateStateWithAccept();
			break;

			// this message is used so that when its not possible to play pares or juego,
			// we wait for all players to communicate that
		case PlayerActions.NO_SE_JUEGA_RONDA:
			updateOnSkippingRound(thread_id);
			break;

		case PlayerActions.ORDAGO:
			updateStateWithOrdago();
			break;

		case PlayerActions.PASS:

			updateStateWithPass();
			break;
		case PlayerActions.SIG_RONDA:
			updateStateWithSigRonda(thread_id);
			break;

		default:
			break;

		}
		// cada vez que me llega un mensaje de un cliente, si estoy en fin de ronda aviso a srvmus
		// para qué? para que él compruebe y si todos los jugadores ya quieren seguir, srvmus avanza.
		if (tableState.getGamePhase()==FIN_RONDA) {
			// we notify the server so that it can advance to END_OF_ROUND			
			synchronized(key) {
				key.notify();
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// then he waits there until all four sig_ronda are received
		}
		// tell other players what happened
		if (cm.getAction()!=REQUEST_GAME_STATE && cm.getAction() != DESCARTAR && cm.getAction()!=SIG_RONDA && cm.getAction()!=Language.ConnectionState.ALIVE) {
			System.out.println("[CONTROLLER] BROADCASTING GAME STATE");
			broadCastGameState();
		}
	}

	private void updateStateWithSigRonda(byte thread_id) {
        // 
		String playerID = gameState.getPlayerID(thread_id);
		byte seat_id = tableState.getSeatOf(playerID);
		gameState.setReadyForNextRound(seat_id, true);
		if (gameState.allReadyForNextRound()) {

			synchronized(key) {
				try {
					// tell server all clients want to move on
					key.notify();
					// wait for server to tell me it switched states					
					while(gameState.getServerGameState()==END_OF_ROUND) {
						key.wait();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			tableState.resetForNewround();	
			tableState.postRoundCheck();	
			gameState.postRoundCheck();
			broadCastGameState();
		} else {
			sendGameState(thread_id);
		}		
	}

	/***
	 * 
	 * UPDATE STATE FUNCTIONS
	 * 
	 * 
	 */


	private boolean allAcks() {
		byte acks = 0;
		for (int i = 0; i < MAX_CLIENTS; i++) {
			if(acks_ronda_no_jugada[i])
				acks++;
		}
		return acks == MAX_CLIENTS;
	}
	
	private void clearAcks() {
		for (int i = 0; i < MAX_CLIENTS; i++) {
			acks_ronda_no_jugada[i] = false;
		}		
	}
	
	private void updateOnSkippingRound(byte thread_id) {
		System.out.println("LLEGO UN MSG NO SE JUEGA RONDA");
		acks_ronda_no_jugada[thread_id] = true;
		if (allAcks()) {
			System.out.println("LLEGARON LOS 4, PASAMOS A SIGUIENTE LANCE");				
			clearAcks();
			tableState.pasarASiguienteLance();
		}

	}

	private void updateOnMus() {

		// cuando se recibe un mus
		// 	si todo el mundo ya habló
		//		pasamos a ronda descartes
		//		


		tableState.increaseTalkedPlayers();
		// if this is the last guy having mus, go to DESCARTANDO game phase
		if (tableState.allPlayersTalked()) {
			tableState.pasarASiguienteLance();
			synchronized(key) {
				key.notify();
			}
		}
		// solo es mus corrido en la primera ronda
		tableState.advanceTalkingPlayer();
		if (tableState.getId_ronda()==0) {						
			tableState.setMano_seat_id(tableState.getJugador_debe_hablar());
		}

	}

	private void updateOnRequestSeat(byte requested_seat, byte thread_id) {
		// we try to seat the player in the requested seat
		// we assume the player knows the game state so it doesnt really need
		// a refresh at this point
		System.out.println("PLAYER " + gameState.getPlayerID(thread_id) + " requested the SEAT " + requested_seat);
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
			System.out.println("[CONTROLLER] sendGameState() : else in updateOnRequestSeat()");
			sendGameState(thread_id);
		}		

	}

	private void updateOnRequestGameState(String playerID, byte thread_id) {
		// player can pass their name here
		synchronized(gameState) {
			gameState.setPlayerID(playerID, thread_id);
		}
		server.log("SERVER: player " + playerID.toString() + " connected.");
		sendGameState(thread_id);		
	}

	private void updateStateWithAccept() {
		if (tableState.isOrdago_lanzado()) {
			tableState.setGamePhase(FIN_RONDA);
		} else {
			byte piedras = tableState.getPiedras_envidadas_ronda_actual();
			tableState.setPiedras_envidadas(tableState.getGamePhase(), piedras);
			tableState.pasarASiguienteLance();
		}		
	}

	private void updateStateWithDescartar(byte player_seat_id, byte descartes, byte thread_id) {
		// get which cards he wants to discard and keep it in state
		// if all players have chosen, deal cards
		PlayerState ps = tableState.getClient(player_seat_id);
		ps.setCommitedToDiscard(true);
		ps.unmarkAll();
		for (byte i = 0; i < CARDS_PER_HAND; i++) {
			if (ByteMessage.isBitSet(i,descartes)) {					
				ps.markForReplacement(i);	
			}				
		}
		if (tableState.allPlayersCommitedToDiscard()) {
			tableState.setGamePhase(GamePhase.MUS);
			tableState.moveDiscarded();
			tableState.uncommitToDiscard();
			tableState.repartir();
			broadCastGameState();
		} else {
			sendGameState(thread_id);
		}

	}

	private void updateStateWithOrdago() {
		tableState.setEnPaso(tableState.getGamePhase(), false);
		tableState.setOrdago_lanzado(true);
		// this must be done. if the ordago is rejected, we need to know
		// the winner of the pot
		tableState.setUltimo_envidador(tableState.getJugador_debe_hablar());
		tableState.assignNextTalker();

	}



	private void updateStateWithEnvite(byte player_seat_id, byte qtyEnvidada) {		
		//TODO: chapucilla.... por?
		tableState.setEnPaso((byte) (tableState.getGamePhase()), false);
		// if its a new envite, add qty to piedras_envidadas_a_grande
		if (tableState.getJugadores_hablado_en_turno_actual()==0) {
			tableState.setPiedras_envidadas_ronda_actual(qtyEnvidada);
			tableState.setPiedras_en_ultimo_envite(qtyEnvidada);
		} else {
			// alguien está envidando más

			// increase total bet
			byte inc = tableState.getPiedras_envidadas_ronda_actual();
			tableState.setPiedras_envidadas_ronda_actual((byte)(inc + qtyEnvidada));

			// update pot
			byte anterior = tableState.getPiedras_acumuladas_en_apuesta();
			byte total = (byte)(anterior + tableState.getPiedras_en_ultimo_envite());
			tableState.setPiedras_acumuladas_en_apuesta(total);

			tableState.setPiedras_en_ultimo_envite(qtyEnvidada);
		}

		// this property allows us to properly assign the pot in case of bid rejection
		tableState.setUltimo_envidador(player_seat_id);
		tableState.assignNextTalker();	
		tableState.increaseTalkedPlayers();

	}

	// world state is updated with the pass message from the player
	private void updateStateWithPass() {

		byte player_seat_id = tableState.getJugador_debe_hablar();
		byte piedrasEnvidadas = tableState.getPiedras_envidadas_ronda_actual(); 
		// current player pasa. entonces 
		switch(tableState.getGamePhase()) {
		case GRANDE:
		case CHICA:
			if (tableState.isOrdago_lanzado()) {
				if (tableState.isPostreEnSuEquipo(player_seat_id)) {
					tableState.givePotTo(player_seat_id+1);
					tableState.pasarASiguienteLance();					
				} else {
					tableState.setJugador_debe_hablar(tableState.opuesto(player_seat_id));
				}
			} else if (piedrasEnvidadas >0 ) {
				// si estamos a pares o juego y yo era el único que podía hablar,
				// damos pot y pasamos a siguiente lance
				if (tableState.isPostreEnSuEquipo(player_seat_id)) {
					tableState.givePotTo(player_seat_id+1);
					tableState.pasarASiguienteLance();
				} else {
					tableState.setJugador_debe_hablar(tableState.opuesto(player_seat_id));
				}
			} else {
				if (tableState.isPostre(player_seat_id)) {
					tableState.pasarASiguienteLance();
				} else {
					tableState.setJugador_debe_hablar(TableState.nextTableSeatId(player_seat_id));
				}			
			}
			break;
		case PARES:
			if (tableState.isOrdago_lanzado()) {
				boolean miParejaTienePares = tableState.tienePares(tableState.opuesto(player_seat_id));
				if (tableState.isPostreEnSuEquipo(player_seat_id) || !miParejaTienePares) {
					tableState.givePotTo(player_seat_id+1);
					tableState.pasarASiguienteLance();
				} else {
					tableState.setJugador_debe_hablar(tableState.opuesto(player_seat_id));
				}
			}else if (piedrasEnvidadas >0 ) {
				// si estamos a pares o juego y yo era el único que podía hablar,
				// damos pot y pasamos a siguiente lance
				boolean miParejaTienePares = tableState.tienePares(tableState.opuesto(player_seat_id));
				if (tableState.isPostreEnSuEquipo(player_seat_id) || !miParejaTienePares) {
					tableState.givePotTo(player_seat_id+1);
					tableState.pasarASiguienteLance();
				} else {
					tableState.setJugador_debe_hablar(tableState.opuesto(player_seat_id));
				}
			} else {
				// yo paso y no hay ningún envite
				// si soy el último jugador con pares pasamos al siguiente lance
				// de lo contrario que hable el siguiente que tenga pares
				if (tableState.isUltimoConPares(player_seat_id)) {
					tableState.pasarASiguienteLance();
				} else {
					tableState.setJugador_debe_hablar(tableState.siguienteConPares(player_seat_id));
				}
			}			
			break;
		case JUEGO:
			if (!tableState.seJuegaAlPunto()) {
				if (piedrasEnvidadas >0 ) {
					// si estamos a pares o juego y yo era el único que podía hablar,
					// damos pot y pasamos a siguiente lance
					boolean miParejaTieneJuego = tableState.tieneJuego(tableState.opuesto(player_seat_id));
					if (tableState.isPostreEnSuEquipo(player_seat_id) || !miParejaTieneJuego) {
						tableState.givePotTo(player_seat_id+1);
						tableState.pasarASiguienteLance();
					} else {
						tableState.setJugador_debe_hablar(tableState.opuesto(player_seat_id));
					}
				} else {
					// yo paso y no hay ningún envite
					// si soy el último jugador con juego pasamos al siguiente lance
					// de lo contrario que hable el siguiente que tenga pares
					if (tableState.isUltimoConJuego(player_seat_id)) {
						tableState.pasarASiguienteLance();					
					} else {
						tableState.setJugador_debe_hablar(tableState.siguienteConJuego(player_seat_id));
					}		
				}				
			} else {
				if (piedrasEnvidadas >0 ) {
					if (tableState.isPostreEnSuEquipo(player_seat_id)) {
						tableState.givePotTo(player_seat_id+1);
						tableState.pasarASiguienteLance();
					} else {
						tableState.setJugador_debe_hablar(tableState.opuesto(player_seat_id));
					}
				} else {
					if (tableState.isPostreEnSuEquipo(player_seat_id)) {
						tableState.givePotTo(player_seat_id+1); 
						tableState.pasarASiguienteLance();						
					} else {
						tableState.setJugador_debe_hablar(TableState.nextTableSeatId(player_seat_id));						
					}
				}
			}

			break;
		default:
			break;
		}
	}


	//notify All Players
	public void broadCastGameState() {
		for (byte i = 0; i < MAX_CLIENTS; i++) {
			System.out.println("[CONTROLLER] sending gamestate for " + i);
			sendGameState(i);
		}
	}

	// sends a player the game state
	private void sendGameState(byte thread_id) {
		ServerMessage sm = ServerMessage.forgeStateMessage(gameState, tableState, gameState.getPlayerID(thread_id));
		System.out.println("[SERVER CONTROLLER] Forged a message for the client.");
		//sm.printContent();
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

	/*
	private void updateScore() {
		if (tableState.vacaAlcanzada(gameState.getJuegos_vaca())) {
			tableState.asignarVaca(gameState.getJuegos_vaca());
			if (tableState.partidaAlcanzada(gameState.getVacas_partida())) {
				tableState.asignarPartida(gameState.getVacas_partida());
			}
		}		
	} */	

}
