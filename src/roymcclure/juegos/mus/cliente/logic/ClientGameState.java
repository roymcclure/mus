package roymcclure.juegos.mus.cliente.logic;


import roymcclure.juegos.mus.common.network.ServerMessage;
import roymcclure.juegos.mus.common.logic.GameState;
import roymcclure.juegos.mus.common.logic.TableState;




public class ClientGameState {

	private static String playerID;
	// this index indicates where I am seated
	// according to 0 = north, 1 = east, etc...

	private static TableState tableState;
	private static GameState gameState;
	


	public ClientGameState() {}
	
	public static void updateWith(ServerMessage sm) {
		// table state
		tableState=sm.getTableState();
		System.out.println("[ClientGameState] printing table content");
		tableState.printContent();
		gameState=sm.getGameState();
		
		
		
	}
	
	public static String getPlayerName() {
		return playerID.substring(0, playerID.indexOf(':'));
	}
	
	public static void setPlayerID(String id) {
		playerID = id;
	}

	public static String getPlayerID() {
		return playerID;
	}	
	
	public static TableState table() {
		return tableState;
	}
	
	public static GameState game() {
		return gameState;
	}
	

}
