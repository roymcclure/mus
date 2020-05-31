package roymcclure.juegos.mus.common.network;

import java.io.Serializable;

import roymcclure.juegos.mus.common.logic.GameState;
import roymcclure.juegos.mus.common.logic.TableState;
import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.*;
import static roymcclure.juegos.mus.common.logic.Language.ServerGameState.*;

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
	private boolean broadCast; // it is a broadcast msg
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
		if (gameState != null) { 
			switch(gameState.getServerGameState()) {
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
			System.out.println("Sentados:");
			for (byte i=0; i<MAX_CLIENTS;i++)	{
				System.out.println("Sitio " + i + ": " + tableState.getClient(i).getID());				
			}
		} else {
			System.out.println("El mensaje no contiene info del estado de la partida.");
		}		
		
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

	private void setClientMessage(ClientMessage cm, String playerID) {
		this.clientMessage = new ClientMessage(cm.getAction(), cm.getQuantity(),playerID);
		
	}

	public ClientMessage getBroadCastMessage() {
		return clientMessage;
	}
	
}
