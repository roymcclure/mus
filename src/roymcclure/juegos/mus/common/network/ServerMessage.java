package roymcclure.juegos.mus.common.network;

import java.io.Serializable;

import roymcclure.juegos.mus.common.logic.GameState;
import roymcclure.juegos.mus.common.logic.TableState;

/*
 * Containts connection-related data, and possibly
 * game state data.
 *  
 */

public class ServerMessage implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4450746104884476694L;

	private TableState tableState;
	private GameState gameState;
	private boolean broadCast = false; // it is a broadcast msg
	// TODO: only for broadcast purposes. NOT ELEGANT. the right way? create a BroadCastMessage class.
	@SuppressWarnings("unused")
	private ClientMessage clientMessage; 
	
	public GameState getGameState() {
		return gameState;
	}

	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}

	private byte reply;

	public ServerMessage() {}

	public TableState getTableState() {
		return tableState;
	}


	public void setTableState(TableState tableState) {
		this.tableState = tableState;
	}	
	
	public byte getReply() {
		return reply;
	}

	public void setReply(byte reply) {
		this.reply = reply;
	}

	
	// why does a message sent to the player require a player id?
	public static ServerMessage forgeStateMessage(GameState gs, TableState tableState, String playerID) {
		ServerMessage sm = new ServerMessage();
		sm.setGameState(gs.clone());
		sm.setTableState(tableState.clone(playerID));
		return sm;
	}
	
	public void printContent() {
		System.out.println("Estado de juego:");
		System.out.println("----------------");
		if (gameState != null) 
			gameState.printContent();
		System.out.println("Estado de la mesa:");
		if (tableState != null)
			tableState.printContent();		
	}

	public boolean isBroadCastMsg() {
		return broadCast;
	}	
	
	public void setBroadCastMsg(boolean b) {
		broadCast = b;
	}

	public static ServerMessage forgeBroadCastMessage(ClientMessage cm, String playerID) {
		ServerMessage sm = new ServerMessage();
		sm.setClientMessage(cm, playerID);
		sm.setBroadCastMsg(true);
		return sm;
	}

	// TODO: for starters i pass the original sender in info[]
	// so if info field neeeded to be broadcasted as well we have a problem.
	// and secondly, 
	private void setClientMessage(ClientMessage cm, String playerID) {
		this.clientMessage = new ClientMessage(cm.getAction(), cm.getQuantity(),playerID);
		
	}

	public ClientMessage getBroadCastMessage() {
		return clientMessage;
	}
	
}
