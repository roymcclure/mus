package roymcclure.juegos.mus.cliente.logic;


import roymcclure.juegos.mus.common.network.ServerMessage;
import roymcclure.juegos.mus.common.logic.GameState;
import roymcclure.juegos.mus.common.logic.PlayerState;
import roymcclure.juegos.mus.common.logic.TableState;

import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.*;

public class ClientGameState {

	// data relevant only to the client
	private static String playerID;
	private static int mouseOverCard; // to inform the handler to render a frane over it
	private static boolean[] selectedCard; 	// to inform the server which cards we want to discard
											// also to know which ones we should draw a frame over
											// even when mouse leaves
	// data shared with the rest of nodes
	private static TableState tableState;
	private static GameState serverGameState;	

	public ClientGameState() {
		mouseOverCard = -1;
		selectedCard = new boolean[CARDS_PER_HAND];		
		for (int i = 0; i < CARDS_PER_HAND; i++) {
			selectedCard[i] = false;
		}
	}
	
	public static GameState getGameState() {
		return serverGameState;
	}

	public static TableState table() {
		return tableState;
	}
	
	public static void updateWith(ServerMessage sm) {
		tableState=sm.getTableState();
		System.out.println("[ClientGameState] printing table content");
		tableState.printContent();
		serverGameState=sm.getGameState();
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

	public static void setMouseOverCard(int i) {
		mouseOverCard = i;
	}
	
	public static int getMouseOverCard() {
		return mouseOverCard;
	}
	
	public static boolean getSelectedCard(int card_index) {
		return selectedCard[card_index];
	}

	public static void setSelectedCard(int card_index, boolean isSelected) {
		assert(card_index >=0 && card_index < CARDS_PER_HAND);
		selectedCard[card_index] = isSelected;
	}
	
	public static byte getSelectedCardsAsByte() {
		// return a mask of bits
		byte retorno = 0;
		for (int i=0;i<CARDS_PER_HAND;i++) {
			if (selectedCard[i])
				retorno ^= (int)(Math.pow(2, i));
		}
		return retorno;
	}
	
	public static byte my_seat_id() {
		if (tableState != null)
			return table().getSeatOf(getPlayerID());
		return -1;
	}
	
	public static PlayerState me() {
		return table().getClient(my_seat_id());
	}

}
