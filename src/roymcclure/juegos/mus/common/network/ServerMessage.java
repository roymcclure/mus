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
	public static ServerMessage forgeDataPacket(GameState gs, TableState tableState, String playerID) {
		ServerMessage sm = new ServerMessage();
		sm.setGameState(gs.clone());
		sm.setTableState(tableState.clone(playerID));
		return sm;
	}
	
	public void printContent() {
		System.out.println("Estado de juego:");
		System.out.println("----------------");
		if (gameState != null) { 
			switch(gameState.getGameState()) {
			case WAITING_ALL_PLAYERS_TO_SEAT:
				System.out.println("STATE SERVER: ESPERANDO A QUE TODOS SE SIENTEN");
				break;
			case PLAYING:
				System.out.println("STATE SERVER: JUGANDO");
				break;
			case GAME_FINISHED:
				System.out.println("STATE SERVER: JUEGo TERMINADO");
				break;
			case END_OF_ROUND:
				System.out.println("STATE SERVER: FIN DE LA RONDA");
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
	
	
}
