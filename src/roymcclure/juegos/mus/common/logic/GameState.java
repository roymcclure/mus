package roymcclure.juegos.mus.common.logic;

import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.*;
import static roymcclure.juegos.mus.common.logic.Language.ServerGameState.*;

import java.io.Serializable;



public class GameState implements Serializable {

	/**
	 * DATA THAT DOESNT CHANGE ALONG THE GAME
	 */
	private static final long serialVersionUID = 5539051360919540144L;
	// domain specific data
	private byte piedras_juego;
	private byte juegos_vaca;
	private byte vacas_partida;
	
	
	//  The thread_id used to index player ids is only relevant to the server.
	private String[] player_ids;
	
	// this is the SERVER game state.
	private byte gameState;
	
	public GameState() {
		player_ids = new String[MAX_CLIENTS];
		this.gameState = WAITING_ALL_PLAYERS_TO_CONNECT;
	}

	public GameState(byte piedras_juego, byte juegos_vaca, byte vacas_partida, String[] player_ids, byte gameState) {
		this.piedras_juego = piedras_juego;
		this.juegos_vaca = juegos_vaca;
		this.vacas_partida = vacas_partida;
		this.player_ids = player_ids;
		this.gameState = gameState;
	}

	public byte getPiedras_juego() {
		return piedras_juego;
	}

	public void setPiedras_juego(byte piedras_juego) {
		this.piedras_juego = piedras_juego;
	}

	public byte getJuegos_vaca() {
		return juegos_vaca;
	}

	public void setJuegos_vaca(byte juegos_vaca) {
		this.juegos_vaca = juegos_vaca;
	}

	public byte getVacas_partida() {
		return vacas_partida;
	}

	public void setVacas_partida(byte vacas_partida) {
		this.vacas_partida = vacas_partida;
	}

	public byte getGameState() {
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
		return new GameState(piedras_juego, juegos_vaca, vacas_partida, player_ids, gameState);
	}
	
}
