package roymcclure.juegos.mus.cliente.logic;

import roymcclure.juegos.mus.common.logic.Language;
import roymcclure.juegos.mus.common.network.ServerMessage;

public class ClientGameState {

	// this object is populated upon initial connection.
	// a packet is sent with all the game info.
	private int stonesToRound; // cuantas piedras hacen falta para ganar una vaca?
	private int roundsToCow; // cuantos juegos para ganar una vaca?
	private int cowsToGame; // cuantas vacas hacen falta para ganar la partida?
	
	private byte[][] cards;
	private String[] player_ids;
	
	private int gameState;
	
	private String playerName;

	
	public ClientGameState() {
		player_ids = new String[Language.GameDefinitions.MAX_CLIENTS];
		for (byte i = 0; i<Language.GameDefinitions.MAX_CLIENTS; i++) {
			player_ids[i] = "empty";
		}
		gameState=Language.ClientGameState.DISCONNECTED;
	}

	public int getGameState() {
		return gameState;
	}
	
	public void setGameState(int gameState) {
		this.gameState = gameState;
	}
	
	private byte id_north, id_east, id_south, id_west;
	
	public String getPlayerIDbySeatID(byte seat_id) {
		return player_ids[seat_id];
	}
	
	public void setPlayerID_by_Seat(byte seat_id, String player_id) {
		player_ids[seat_id] = player_id;
	}
	
	public void updateWith(ServerMessage sm) {
		// ids
		for (byte i = 0; i<Language.GameDefinitions.MAX_CLIENTS; i++) {
			this.setPlayerID_by_Seat(i, sm.getTableState().getPlayerId_By_Seat(i));
		}
		// cards
		for (byte player = 0; player<Language.GameDefinitions.MAX_CLIENTS; player++) {
			
			for (byte card = 0; card < Language.GameDefinitions.CARDS_PER_HAND; card++) {
								
			}
			
		}
		// stones for each player
		// games for each couple
		// cows for each couple
		// stones to game
		// games to cow
		// 
	}
	
	public String getPlayerName() {
		return playerName;
	}
	
	public void setPlayerName(String name) {
		playerName = name;
	}
	
	public void printState() {
		for (int i=0; i < Language.GameDefinitions.MAX_CLIENTS; i++) {
			System.out.println("En pos " + i + ": " + player_ids[i] + "\n");
		}
	}
	

}
