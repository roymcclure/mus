package roymcclure.juegos.mus.common.logic;

import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.*;
import static roymcclure.juegos.mus.common.logic.Language.ServerGameState.*;

import java.io.Serializable;

/***
 * 
 * @author roy
 *
 * Reflects players state in terms of non-game related data.
 *
 */

public class GameState implements Serializable {

	/**
	 * DATA THAT DOESNT CHANGE ALONG THE GAME
	 */
	private static final long serialVersionUID = 5539051360919540144L;
	
	//  This here is indexed by thread_id. Players can therefore know the thread_id of the other players, but 
	//  it really isn't relevant info to them, unless (maybe) they have evil stuff in mind.
	private String[] player_ids;
	private boolean[] ready_for_next_round;
	
	// this is the SERVER game state.
	private byte gameState;
	
	public GameState() {
		player_ids = new String[MAX_CLIENTS];
		ready_for_next_round = new boolean[MAX_CLIENTS];
		for (int i = 0; i < MAX_CLIENTS; i++) {
			ready_for_next_round[i] = false;
		}
		this.gameState = WAITING_ALL_PLAYERS_TO_CONNECT;
	}

	public GameState(String[] player_ids, boolean[] ready_for_next_round, byte gameState) {
		this.player_ids = player_ids;
		this.ready_for_next_round = new boolean[MAX_CLIENTS];
		for (int i = 0; i < MAX_CLIENTS; i++) {
			this.ready_for_next_round[i] = ready_for_next_round[i];
		}
		this.gameState = gameState;		
	}

	public void setReadyForNextRound(byte idx_player, boolean value) {
		this.ready_for_next_round[idx_player] = value;
	}
	
	public boolean isReadyForNextRound(byte idx_player) {
		return ready_for_next_round[idx_player];
	}

	public byte getServerGameState() {
		return gameState;
	}

	public void setGameState(byte gameState) {
		this.gameState = gameState;
	}	
	
	public String getPlayerID(int thread_id) {
		return player_ids[thread_id];
	}
	
	public void setPlayerID(String player_ID, int thread_id) {
		player_ids[thread_id] = player_ID;
	}
	
	public GameState clone() {
		return new GameState(player_ids, ready_for_next_round, gameState);
	}

	public void printContent() {
		switch(getServerGameState()) {
		case WAITING_ALL_PLAYERS_TO_CONNECT:
			System.out.println("SERVER STATE: ESPERANDO A QUE TODOS SE CONECTEN");				
			break;
		case WAITING_ALL_PLAYERS_TO_SEAT:
			System.out.println("SERVER STATE: ESPERANDO A QUE TODOS SE SIENTEN");
			break;
		case PLAYING:
			System.out.println("SERVER STATE: JUGANDO");
			break;
		case GAME_FINISHED:
			System.out.println("SERVER STATE: JUEGo TERMINADO");
			break;
		case END_OF_ROUND:
			System.out.println("SERVER STATE: FIN DE LA RONDA");
			break;
		}		
	}

	public boolean allReadyForNextRound() {
		int count = 0;
		for (int i = 0; i < MAX_CLIENTS; i++) {
			if (isReadyForNextRound((byte) i))
				count++;
		}
		return count == MAX_CLIENTS;
	}

	public void postRoundCheck() {
		for (int i = 0; i < MAX_CLIENTS; i++) {
			setReadyForNextRound((byte) i, false);
		}
		
	}
	
}
