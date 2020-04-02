package roymcclure.juegos.mus.server.logic;

import roymcclure.juegos.mus.common.logic.Language;
import roymcclure.juegos.mus.common.network.ClientMessage;

// this is part of business logic

public class MessageValidation {

	// is the client message well constructed?
	public static boolean isClientMessageValid(ClientMessage cm) {
		if (cm.getAction() < Language.PlayerActions.PASS || cm.getAction() > Language.PlayerActions.REQUEST_SEAT ) {
			return false;
		}
		if (cm.getAction() == Language.PlayerActions.REQUEST_SEAT) {
			if (cm.getQuantity() <0 || cm.getQuantity() > SrvMus.MAX_CLIENTS - 1) {
				return false;
			}
		}
		if (cm.getAction()== Language.PlayerActions.ENVITE) {
			if (cm.getQuantity()<2 || cm.getQuantity() > Language.GameDefinitions.STONES_TO_ROUND) {
				return false;
			}
		}
		return true;
	}
	
	// does the message make sense within the context?
	public static boolean isClientMessageConsistent(int action, int qty, GameState gs, int client_id) {
		// pass only makes sense if round_id is 0
		if (action == Language.PlayerActions.PASS && gs.getGameState() != 0) {
			return false;
		}
		if (action == Language.PlayerActions.ENVITE) {
			// solo aceptable cuando:
			// - estamos en ronda de juego
			if (gs.getGameState()!=Language.GameState.PLAYING) {
				return false;
			}
			// - me toca hablar
			// de dónde viene el ID del que envía el mensaje?
			// de su hiloCliente correspondiente
			if (gs.getPlayer_in_turn()!=client_id) {
				return false;
			}

		}
		return true;
	}
	
	
}
/*

if (isClientMessageValid(clientMessage) && isClientMessageConsistent()) {
	updateGameState(clientMessage);
}*/