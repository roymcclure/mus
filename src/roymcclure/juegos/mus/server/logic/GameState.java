package roymcclure.juegos.mus.server.logic;

import roymcclure.juegos.mus.common.logic.Language;
import roymcclure.juegos.mus.common.network.ClientMessage;


// Contains data representing the state of a game in a particular moment in time.

public class GameState {
	
	
	private byte estado_partida = 0; // 0 esperando que se llenen los sitios, 
	private byte player_in_turn = 0;
	
	public byte getGameState() {
		return estado_partida;
	}

	public byte getPlayer_in_turn() {
		return player_in_turn;
	}

	public void setPlayer_in_turn(byte player_in_turn) {
		this.player_in_turn = player_in_turn;
	}

	public void setGameState(byte s) {
		this.estado_partida = s;
	}
	
	public byte getExpectedAction(int id) {
		switch(estado_partida) {
			case Language.GameState.WAITING_ALL_PLAYERS_TO_SEAT:
				return waitingAllPlayers(id);
			case Language.GameState.PLAYING:
				return playing(id);
			case Language.GameState.END_OF_ROUND:
				return endOfRound(id);
			case Language.GameState.GAME_FINISHED:
				return gameFinished(id);
		}
		return -1;
	}
	
	public synchronized void updateGameStateWith(ClientMessage cm) {
		
	}
	
	// in this state we check if the player is the first to arrive
	// if he is, 
	private byte waitingAllPlayers(int player_id) {
		return 0;
	}
	
	private byte playing(int player_id) {
		return 0;
	}
	
	private byte endOfRound(int player_id) {
		return 0;
	}
	
	private byte gameFinished(int player_id) {
		return 0;
	}
	
	
	
}
